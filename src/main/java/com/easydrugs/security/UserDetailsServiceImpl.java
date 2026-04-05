package com.easydrugs.security;

import com.easydrugs.entity.Utilisateur;
import com.easydrugs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation de UserDetailsService.
 * Charge un utilisateur par son numéro de téléphone (identifiant unique).
 * Utilisé par Spring Security lors de l'authentification.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String telephone)
            throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository
            .findByTelephone(telephone)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Aucun utilisateur trouvé avec le téléphone : " + telephone
            ));

        if (!utilisateur.getActif()) {
            throw new UsernameNotFoundException(
                "Compte désactivé : " + telephone
            );
        }

        return utilisateur;
    }
}
