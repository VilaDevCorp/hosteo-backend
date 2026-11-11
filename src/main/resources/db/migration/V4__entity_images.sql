-- ============================================
-- Entity images
-- ============================================

-- Tracks whether an image object exists in the bucket so DTOs can omit
-- signed URLs for entities without an image.
ALTER TABLE users ADD COLUMN IF NOT EXISTS has_image BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE apartments ADD COLUMN IF NOT EXISTS has_image BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workers ADD COLUMN IF NOT EXISTS has_image BOOLEAN NOT NULL DEFAULT FALSE;
