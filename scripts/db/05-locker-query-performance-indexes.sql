-- Run manually in production after schema bootstrap.
-- CONCURRENTLY avoids blocking reads and writes while the existing mapping table is indexed.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_locker_realtime_mappings_locker_id
    ON public.locker_realtime_mappings (locker_id);
