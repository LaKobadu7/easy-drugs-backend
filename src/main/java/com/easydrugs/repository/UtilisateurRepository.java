package com.easydrugs.repository;

import com.easydrugs.entity.Utilisateur;
import com.easydrugs.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Utilisateur — EASY-DRUGS.
 * L'identifiant unique de connexion est le numéro de téléphone.
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    /** Utilisé par Spring Security pour l'authentification */
    Optional<Utilisateur> findByTelephone(String telephone);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByActif(boolean actif);

    @Query("SELECT u FROM Utilisateur u WHERE u.actif = true AND u.role = :role")
    List<Utilisateur> findActifsByRole(Role role);
}
