package com.easydrugs.entity;

/**
 * L'administrateur n'est pas une entité JPA séparée.
 * Un administrateur EST un Utilisateur avec role = ADMIN.
 * La distinction se fait via le champ Role dans Utilisateur.
 *
 * Pour créer un admin : créer un Utilisateur avec Role.ADMIN
 * Pour vérifier si admin : utilisateur.getRole() == Role.ADMIN
 */
public class Administrateur {
    // Pas d'annotation @Entity — aucune table en base requise
}
