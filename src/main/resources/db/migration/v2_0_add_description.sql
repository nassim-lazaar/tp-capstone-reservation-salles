-- Sauvegarde prealable
CREATE TABLE IF NOT EXISTS backup_utilisateurs AS SELECT * FROM utilisateurs;
CREATE TABLE IF NOT EXISTS backup_reservations AS SELECT * FROM reservations;

-- Evolutions de schema
ALTER TABLE salles ADD COLUMN IF NOT EXISTS description VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS idx_utilisateur_email ON utilisateurs(email);
ALTER TABLE reservations ADD CONSTRAINT IF NOT EXISTS check_dates_coherentes CHECK (date_fin > date_debut);

-- Nettoyage des reservations obsoletes
DELETE FROM reservations WHERE date_fin < CURRENT_TIMESTAMP AND statut = 'ANNULEE';
