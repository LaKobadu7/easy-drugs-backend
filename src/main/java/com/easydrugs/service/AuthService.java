package com.easydrugs.service;

import com.easydrugs.dto.request.LoginRequest;
import com.easydrugs.dto.request.RegisterRequest;
import com.easydrugs.dto.response.AuthResponse;
import com.easydrugs.entity.Patient;
import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.Role;
import com.easydrugs.exception.ResourceNotFoundException;
import com.easydrugs.exception.UnauthorizedException;
import com.easydrugs.repository.PatientRepository;
import com.easydrugs.repository.UtilisateurRepository;
import com.easydrugs.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification — EASY-DRUGS.
 * Gère l'inscription (register) et la connexion (login) pour les 3 rôles.
 * Retourne un token JWT à chaque authentification réussie.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // ── Inscription ─────────────────────────────────────────────

    /**
     * Inscrit un nouvel utilisateur (Patient uniquement via l'app mobile).
     * Les comptes Pharmacie et Admin sont créés par l'administrateur.
     */
    @Transactional
    public AuthResponse inscrire(RegisterRequest request) {

        // Vérification unicité téléphone
        if (utilisateurRepository.existsByTelephone(request.telephone())) {
            throw new IllegalArgumentException(
                "Ce numéro de téléphone est déjà utilisé : " + request.telephone()
            );
        }

        // Vérification unicité email (si fourni)
        if (request.email() != null
                && !request.email().isBlank()
                && utilisateurRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "Cet email est déjà utilisé : " + request.email()
            );
        }

        // Création compte utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
            .nom(request.nom())
            .prenom(request.prenom())
            .email(request.email())
            .telephone(request.telephone())
            .motDePasse(passwordEncoder.encode(request.motDePasse()))
            .role(Role.PATIENT)       // Seul rôle auto-inscriptible
            .actif(true)
            .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        // Création profil patient associé
        Patient patient = Patient.builder()
            .utilisateur(utilisateur)
            .adresse(request.adresse())
            .build();

        patientRepository.save(patient);

        log.info("Nouveau patient inscrit : {} ({})", utilisateur.getNomComplet(), utilisateur.getTelephone());

        // Génération token JWT
        String token = jwtTokenProvider.genererToken(utilisateur);
        String refreshToken = jwtTokenProvider.genererRefreshToken(utilisateur);

        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .utilisateurId(utilisateur.getId())
            .nomComplet(utilisateur.getNomComplet())
            .telephone(utilisateur.getTelephone())
            .role(utilisateur.getRole().name())
            .build();
    }

    // ── Connexion ───────────────────────────────────────────────

    /**
     * Authentifie un utilisateur par téléphone + mot de passe.
     * Fonctionne pour les 3 rôles : PATIENT, PHARMACIE, ADMIN.
     */
    @Transactional(readOnly = true)
    public AuthResponse seConnecter(LoginRequest request) {

        // Spring Security valide les credentials
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.telephone(),
                request.motDePasse()
            )
        );

        Utilisateur utilisateur = utilisateurRepository
            .findByTelephone(request.telephone())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Utilisateur introuvable : " + request.telephone()
            ));

        if (!utilisateur.getActif()) {
            throw new UnauthorizedException("Compte désactivé. Contactez l'administrateur.");
        }

        log.info("Connexion réussie : {} [{}]", utilisateur.getTelephone(), utilisateur.getRole());

        String token = jwtTokenProvider.genererToken(utilisateur);
        String refreshToken = jwtTokenProvider.genererRefreshToken(utilisateur);

        return AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .utilisateurId(utilisateur.getId())
            .nomComplet(utilisateur.getNomComplet())
            .telephone(utilisateur.getTelephone())
            .role(utilisateur.getRole().name())
            .build();
    }

    // ── Refresh token ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse rafraichirToken(String refreshToken) {
        String telephone = jwtTokenProvider.extraireTelephone(refreshToken);

        Utilisateur utilisateur = utilisateurRepository
            .findByTelephone(telephone)
            .orElseThrow(() -> new UnauthorizedException("Token invalide"));

        if (jwtTokenProvider.estExpire(refreshToken)) {
            throw new UnauthorizedException("Session expirée. Veuillez vous reconnecter.");
        }

        String nouveauToken = jwtTokenProvider.genererToken(utilisateur);

        return AuthResponse.builder()
            .token(nouveauToken)
            .refreshToken(refreshToken)
            .utilisateurId(utilisateur.getId())
            .nomComplet(utilisateur.getNomComplet())
            .telephone(utilisateur.getTelephone())
            .role(utilisateur.getRole().name())
            .build();
    }
}
