-- ============================================
-- Deletion & Archiving Rules
-- ============================================

-- Add soft-delete (hide) flag to tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS visible BOOLEAN NOT NULL DEFAULT TRUE;

-- Strong-entity hard deletes must block (RESTRICT) instead of cascading.
-- assignments.event_id keeps CASCADE (deleting a weak event flushes its own assignments).

ALTER TABLE assignments DROP CONSTRAINT IF EXISTS fk_assignments_task;
ALTER TABLE assignments
    ADD CONSTRAINT fk_assignments_task
        FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE RESTRICT;

ALTER TABLE assignments DROP CONSTRAINT IF EXISTS fk_assignments_worker;
ALTER TABLE assignments
    ADD CONSTRAINT fk_assignments_worker
        FOREIGN KEY (worker_id) REFERENCES workers(id) ON DELETE RESTRICT;