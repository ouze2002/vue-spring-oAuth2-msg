-- Allow NULL password for social-login users
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;
