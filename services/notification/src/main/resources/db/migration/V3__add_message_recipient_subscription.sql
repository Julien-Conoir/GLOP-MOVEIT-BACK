-- V3__add_recipient_subscription.sql
-- Refactoring: transformation de notification en modèle message+recipient
-- On retire user_id et is_read de notification (deviennent notification_recipient)

-- D'abord, retirer les colonnes user-specific de notification
ALTER TABLE notification DROP COLUMN notification_user_id;
ALTER TABLE notification DROP COLUMN is_read;

-- Supprimer les index qui utilisaient ces colonnes
DROP INDEX IF EXISTS idx_notification_user;
DROP INDEX IF EXISTS idx_notification_read;
DROP INDEX IF EXISTS idx_notification_user_read;
DROP INDEX IF EXISTS idx_notification_user_level;

-- Table des destinataires (une ligne par user ayant reçu la notification)
CREATE TABLE notification_recipient (
    recipient_id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Un user ne reçoit qu'une seule fois la même notification
    CONSTRAINT uq_notification_user UNIQUE (notification_id, user_id),
    
    CONSTRAINT fk_recipient_notification FOREIGN KEY (notification_id) 
        REFERENCES notification(notification_id) ON DELETE CASCADE
);

-- Index critiques pour performance
CREATE INDEX idx_recipient_user_read ON notification_recipient(user_id, is_read);
CREATE INDEX idx_recipient_user_created ON notification_recipient(user_id, created_at DESC);
CREATE INDEX idx_recipient_notification ON notification_recipient(notification_id);

-- Table d'abonnement aux notifications
CREATE TABLE notification_subscription (
    subscription_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    -- Filtres optionnels (NULL = tous)
    subscription_type VARCHAR(50),      -- NotificationType.name() ou NULL
    level_name VARCHAR(50),             -- Niveau ou NULL
    topic VARCHAR(100),                 -- Topic custom ou NULL
    
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour résolution rapide des destinataires
CREATE INDEX idx_subscription_user ON notification_subscription(user_id, active);
CREATE INDEX idx_subscription_type ON notification_subscription(subscription_type) WHERE active = true;
CREATE INDEX idx_subscription_level ON notification_subscription(level_name) WHERE active = true;
CREATE INDEX idx_subscription_topic ON notification_subscription(topic) WHERE active = true;

-- Note: La table "notification" est maintenant utilisée comme message central (sans user_id)
-- Les destinataires sont gérés via notification_recipient (relation many-to-many)
