-- Données d'initialisation pour PostgreSQL
INSERT INTO users (username, email, created_at, updated_at) VALUES 
('admin', 'admin@glop-moveit.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1', 'user1@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user2', 'user2@example.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;