-- ================================================================
--  EASY-DRUGS — Migration V2 : Données initiales prototype
--  2 pharmacies pilotes de Sangmélima + 1 compte admin
-- ================================================================

-- ── Compte administrateur ───────────────────────────────────────
INSERT INTO utilisateur (nom, prenom, email, telephone, mot_de_passe, role)
VALUES (
    'EASY-DRUGS', 'Admin',
    'admin@easy-drugs.cm',
    '+237690000001',
    '$2a$12$AtmLm2eehAYUpE2gIzXDgub3aMZS12nXD0.Gt6W8XGqE9bXYbFXYu',
    'ADMIN'
);

-- ── Comptes pharmacies ──────────────────────────────────────────
INSERT INTO utilisateur (nom, prenom, email, telephone, mot_de_passe, role)
VALUES
    ('La Référence', 'Pharmacie', 'reference@easy-drugs.cm', '+237690000002',
     '$2a$12$PFCSkMMSsOh1X0sB8WfsJedCaaChpVxPpD840eSj2XksJm65PHKMK', 'PHARMACIE'),
    ('Rosa Parks', 'Pharmacie', 'rosaparks@easy-drugs.cm', '+237690000003',
     '$2a$12$138MwXcqquSMz2mW.OwglOrlxcPraVmNP4x2gE16MRwuS8en5FL6.', 'PHARMACIE');

-- ── Pharmacies pilotes avec coordonnées GPS Sangmélima ──────────
INSERT INTO pharmacie (nom, adresse, coordonnees, latitude, longitude, statut, horaires, validee, zone_geo, admin_id)
VALUES
    (
        'Pharmacie La Référence',
        'Quartier Avenue des Banques, Sangmélima',
        ST_SetSRID(ST_MakePoint(11.9820, 3.0172), 4326),
        3.0172, 11.9820,
        'OUVERTE',
        'Lun-Sam : 7h30-20h00 | Dim : 9h-13h',
        TRUE,
        'SANGMELIMA',
        (SELECT id FROM utilisateur WHERE telephone = '+237690000001')
    ),
    (
        'Pharmacie Rosa Parks',
        'Quartier Centre, Sangmélima',
        ST_SetSRID(ST_MakePoint(11.9850, 3.0155), 4326),
        3.0155, 11.9850,
        'OUVERTE',
        'Lun-Ven : 8h00-19h00 | Sam : 8h-17h',
        TRUE,
        'SANGMELIMA',
        (SELECT id FROM utilisateur WHERE telephone = '+237690000001')
    );
