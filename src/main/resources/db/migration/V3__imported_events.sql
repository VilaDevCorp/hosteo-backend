-- ============================================
-- Imported Events (replaces imp_bookings)
-- ============================================

DROP TABLE IF EXISTS imp_bookings CASCADE;

CREATE TABLE failed_imported_events (
    id UUID PRIMARY KEY,
    apartment_id UUID,
    name VARCHAR(255),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    source VARCHAR(50) NOT NULL,
    error VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_failed_imported_events_apartment FOREIGN KEY (apartment_id) REFERENCES apartments(id) ON DELETE CASCADE,
    CONSTRAINT fk_failed_imported_events_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Used for chronological ordering and apartment lookups
CREATE INDEX idx_imported_events_apartment_id ON failed_imported_events(apartment_id);
CREATE INDEX idx_imported_events_start_date ON failed_imported_events(start_date);