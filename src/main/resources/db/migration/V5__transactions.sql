-- ============================================
-- Finances: task cost + transactions
-- ============================================

-- Cost of a task used to generate debt transactions when assignments are completed.
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS cost DOUBLE PRECISION NOT NULL DEFAULT 0;

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    worker_id UUID NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    assignment_id UUID,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    CONSTRAINT fk_transactions_worker FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- One debt transaction per assignment (payments have NULL assignment_id).
CREATE UNIQUE INDEX idx_transactions_assignment_id ON transactions(assignment_id);
CREATE INDEX idx_transactions_worker_id ON transactions(worker_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Salary is no longer tracked on workers; money owed is derived from transactions.
ALTER TABLE workers DROP COLUMN IF EXISTS salary;
