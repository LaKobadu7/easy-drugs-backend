-- ================================================================
--  EASY-DRUGS — Migration V1 : Schéma initial PostgreSQL + PostGIS
--  Fichier : src/main/resources/db/migration/V1__init_schema.sql
--  Prototype : 2 pharmacies pilotes — Sangmélima, Cameroun
-- ================================================================

-- Activer l'extension PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- ── TABLE : utilisateur ─────────────────────────────────────────
CREATE TABLE utilisateur (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(100)  NOT NULL,
    prenom      VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  UNIQUE,
    telephone   VARCHAR(20)   NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role        VARCHAR(20)   NOT NULL CHECK (role IN ('PATIENT', 'PHARMACIE', 'ADMIN')),
    actif       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── TABLE : patient ─────────────────────────────────────────────
CREATE TABLE patient (
    id                  BIGSERIAL PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    adresse             VARCHAR(255),
    latitude_actuelle   DOUBLE PRECISION,
    longitude_actuelle  DOUBLE PRECISION,
    fcm_token           VARCHAR(500)   -- Token Firebase pour les push notifications
);

-- ── TABLE : pharmacie ───────────────────────────────────────────
CREATE TABLE pharmacie (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(150)  NOT NULL,
    adresse     VARCHAR(255)  NOT NULL,
    -- Colonne géospatiale PostGIS (Point WGS 84)
    coordonnees GEOMETRY(Point, 4326) NOT NULL,
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    statut      VARCHAR(20)   NOT NULL DEFAULT 'OUVERTE'
                    CHECK (statut IN ('OUVERTE', 'FERMEE', 'GARDE')),
    horaires    VARCHAR(255),
    est_de_garde BOOLEAN      NOT NULL DEFAULT FALSE,
    validee     BOOLEAN       NOT NULL DEFAULT FALSE,  -- Validation admin requise
    zone_geo    VARCHAR(50)   NOT NULL DEFAULT 'SANGMELIMA',
    admin_id    BIGINT        REFERENCES utilisateur(id),
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Index géospatial pour les requêtes de proximité
CREATE INDEX idx_pharmacie_coordonnees ON pharmacie USING GIST(coordonnees);
CREATE INDEX idx_pharmacie_statut ON pharmacie(statut);
CREATE INDEX idx_pharmacie_validee ON pharmacie(validee);

-- ── TABLE : medicament ──────────────────────────────────────────
CREATE TABLE medicament (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(200)  NOT NULL,
    forme_galenique VARCHAR(20)   NOT NULL
                        CHECK (forme_galenique IN ('SOLIDE', 'LIQUIDE', 'SEMI_SOLIDE', 'GAZ')),
    description     TEXT,
    conditionnement VARCHAR(150), -- Ex: "Boîte de 16 gélules"
    fabricant       VARCHAR(150),
    image_url       VARCHAR(500),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_medicament_nom ON medicament USING GIN(to_tsvector('french', nom));
CREATE INDEX idx_medicament_forme ON medicament(forme_galenique);

-- ── TABLE : stock ───────────────────────────────────────────────
CREATE TABLE stock (
    id              BIGSERIAL PRIMARY KEY,
    pharmacie_id    BIGINT        NOT NULL REFERENCES pharmacie(id) ON DELETE CASCADE,
    medicament_id   BIGINT        NOT NULL REFERENCES medicament(id) ON DELETE CASCADE,
    quantite        INTEGER       NOT NULL DEFAULT 0 CHECK (quantite >= 0),
    disponible      BOOLEAN       NOT NULL DEFAULT FALSE,
    prix            DECIMAL(10,2) NOT NULL,           -- Prix en FCFA
    seuil_alerte    INTEGER       NOT NULL DEFAULT 10,
    updated_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    UNIQUE (pharmacie_id, medicament_id)              -- Un stock par pharmacie/médicament
);

CREATE INDEX idx_stock_pharmacie ON stock(pharmacie_id);
CREATE INDEX idx_stock_medicament ON stock(medicament_id);
CREATE INDEX idx_stock_disponible ON stock(disponible);

-- ── TABLE : ordonnance ──────────────────────────────────────────
CREATE TABLE ordonnance (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT        NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    image_path      VARCHAR(500),
    date_emission   DATE,
    nom_medecin     VARCHAR(150),
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ── TABLE : notification ────────────────────────────────────────
CREATE TABLE notification (
    id          BIGSERIAL PRIMARY KEY,
    patient_id  BIGINT        NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    titre       VARCHAR(150)  NOT NULL,
    message     TEXT          NOT NULL,
    type        VARCHAR(30)   NOT NULL
                    CHECK (type IN ('STOCK_DISPONIBLE', 'PHARMACIE_GARDE', 'SYSTEME', 'LITIGE')),
    lue         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_patient ON notification(patient_id);
CREATE INDEX idx_notif_lue ON notification(lue);

-- ── TABLE : litige ──────────────────────────────────────────────
CREATE TABLE litige (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT        NOT NULL REFERENCES patient(id),
    admin_id        BIGINT        REFERENCES utilisateur(id),
    sujet           VARCHAR(200)  NOT NULL,
    description     TEXT          NOT NULL,
    statut          VARCHAR(20)   NOT NULL DEFAULT 'OUVERT'
                        CHECK (statut IN ('OUVERT', 'EN_COURS', 'RESOLU', 'REJETE')),
    date_ouverture  TIMESTAMP     NOT NULL DEFAULT NOW(),
    date_resolution TIMESTAMP
);

CREATE INDEX idx_litige_statut ON litige(statut);
CREATE INDEX idx_litige_patient ON litige(patient_id);

-- ── TABLE : historique_recherche ────────────────────────────────
CREATE TABLE historique_recherche (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT        NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    terme           VARCHAR(200)  NOT NULL,
    date_recherche  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_historique_patient ON historique_recherche(patient_id);

-- ── Trigger : mise à jour automatique de updated_at ────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_utilisateur_updated_at
    BEFORE UPDATE ON utilisateur
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_pharmacie_updated_at
    BEFORE UPDATE ON pharmacie
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_stock_updated_at
    BEFORE UPDATE ON stock
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
