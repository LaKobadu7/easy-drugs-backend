-- ================================================================
--  EASY-DRUGS — Migration V3 : Médicaments de test + stocks pilotes
-- ================================================================

-- ── Médicaments ─────────────────────────────────────────────────
INSERT INTO medicament (nom, forme_galenique, description, conditionnement, fabricant)
VALUES
    -- SOLIDES
    ('Paracétamol 500mg',      'SOLIDE',     'Antalgique et antipyrétique',         'Boîte de 16 comprimés',  'Sanofi CM'),
    ('Amoxicilline 500mg',     'SOLIDE',     'Antibiotique pénicilline',            'Boîte de 16 gélules',    'Pfizer CM'),
    ('Quinine 300mg',          'SOLIDE',     'Antipaludéen',                        'Boîte de 20 comprimés',  'Sanofi CM'),
    ('Artémether-Luméfantrine','SOLIDE',     'Antipaludéen combiné (Coartem)',      'Boîte de 24 comprimés',  'Novartis CM'),
    ('Ibuprofène 400mg',       'SOLIDE',     'Anti-inflammatoire non stéroïdien',   'Boîte de 20 comprimés',  'Pfizer CM'),
    ('Azithromycine 500mg',    'SOLIDE',     'Antibiotique macrolide',              'Boîte de 6 gélules',     'Sanofi CM'),
    ('Metronidazole 500mg',    'SOLIDE',     'Antibiotique et antiparasitaire',     'Boîte de 14 comprimés',  'GSK CM'),

    -- LIQUIDES
    ('Sirop Paracétamol 120mg/5ml', 'LIQUIDE', 'Antalgique pédiatrique sirop',     'Flacon 60ml',            'Sanofi CM'),
    ('Sirop Amoxicilline 125mg/5ml','LIQUIDE', 'Antibiotique pédiatrique sirop',   'Flacon 100ml',           'Pfizer CM'),
    ('Solution Chlorhexidine 4%',   'LIQUIDE', 'Antiseptique cutané',              'Flacon 500ml',           'Cooper CM'),
    ('Sérum physiologique 9‰',      'LIQUIDE', 'Solution isotonique',              'Flacon 250ml',           'Fresenius CM'),

    -- SEMI-SOLIDES
    ('Crème Bétaméthasone 0.05%',   'SEMI_SOLIDE', 'Corticoïde dermique',         'Tube 30g',               'GSK CM'),
    ('Gel Ibuprofène 5%',           'SEMI_SOLIDE', 'Anti-inflammatoire topique',   'Tube 50g',               'Pfizer CM'),
    ('Pommade Bacitracine',         'SEMI_SOLIDE', 'Antibiotique topique',         'Tube 15g',               'Johnson CM'),

    -- GAZ
    ('Ventoline 100µg (aérosol)',   'GAZ',  'Bronchodilatateur — asthme',          'Flacon pressurisé 200 doses', 'GSK CM'),
    ('Béclométasone 250µg',         'GAZ',  'Corticoïde inhalé — asthme',          'Flacon pressurisé 200 doses', 'GSK CM');

-- ── Stocks initiaux — Pharmacie La Référence ────────────────────────
INSERT INTO stock (pharmacie_id, medicament_id, quantite, disponible, prix, seuil_alerte)
SELECT
    (SELECT id FROM pharmacie WHERE nom = 'Pharmacie La Référence'),
    m.id,
    s.quantite,
    s.quantite > 0,
    s.prix,
    s.seuil
FROM (VALUES
    ('Paracétamol 500mg',           86,  600,   10),
    ('Amoxicilline 500mg',          48,  1200,  10),
    ('Quinine 300mg',               3,   900,   10),   -- Alerte stock faible !
    ('Artémether-Luméfantrine',     1,   2500,  5),    -- Critique !
    ('Ibuprofène 400mg',            34,  800,   10),
    ('Azithromycine 500mg',         17,  3200,  5),
    ('Sirop Paracétamol 120mg/5ml', 22,  800,   5),
    ('Crème Bétaméthasone 0.05%',   14,  1500,  5),
    ('Ventoline 100µg (aérosol)',   0,   4500,  3)    -- Rupture de stock !
) AS s(nom, quantite, prix, seuil)
JOIN medicament m ON m.nom = s.nom;

-- ── Stocks initiaux — Pharmacie Rosa Parks ───────────────────────
INSERT INTO stock (pharmacie_id, medicament_id, quantite, disponible, prix, seuil_alerte)
SELECT
    (SELECT id FROM pharmacie WHERE nom = 'Pharmacie Rosa Parks'),
    m.id,
    s.quantite,
    s.quantite > 0,
    s.prix,
    s.seuil
FROM (VALUES
    ('Amoxicilline 500mg',           12,  1350,  10),
    ('Paracétamol 500mg',            55,  600,   10),
    ('Quinine 300mg',                28,  900,   10),
    ('Sirop Amoxicilline 125mg/5ml', 10,  1800,  5),
    ('Gel Ibuprofène 5%',            9,   1200,  5),
    ('Béclométasone 250µg',          4,   5500,  3),
    ('Sérum physiologique 9‰',       18,  500,   5)
) AS s(nom, quantite, prix, seuil)
JOIN medicament m ON m.nom = s.nom;
