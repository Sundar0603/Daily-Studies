-- Run this script once to set up the database before starting the app.
-- mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS daily_studies;
USE daily_studies;

CREATE TABLE IF NOT EXISTS daily_studies (
    topic              VARCHAR(255) NOT NULL,
    count              INT          NOT NULL DEFAULT 0,
    item_to_study_next VARCHAR(500)          DEFAULT NULL,
    last_updated_date  DATE         NOT NULL,
    PRIMARY KEY (topic)
);

-- Seed data with initial counts. ON DUPLICATE KEY UPDATE is a no-op so
-- re-running the script will not overwrite counts already in the DB.
INSERT INTO daily_studies (topic, count, item_to_study_next, last_updated_date) VALUES
    ('Technical',    19, NULL, CURDATE()),
    ('Non-Technical', 0, NULL, CURDATE()),
    ('Investments',  21, NULL, CURDATE()),
    ('Security',     19, NULL, CURDATE()),
    ('AI',           18, NULL, CURDATE()),
    ('Stocks',       19, NULL, CURDATE())
ON DUPLICATE KEY UPDATE topic = topic;
