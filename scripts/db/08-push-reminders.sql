CREATE TABLE push_reminders (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES push_devices(id),
    locker_id BIGINT NOT NULL REFERENCES lockers(id),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    total_usage_minutes INTEGER NOT NULL,
    remind_before_minutes INTEGER,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_push_reminders_active_device_end_at
    ON push_reminders (device_id, end_at)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uq_push_reminders_active_device
    ON push_reminders (device_id)
    WHERE status = 'ACTIVE';

CREATE TABLE push_reminder_jobs (
    id BIGSERIAL PRIMARY KEY,
    reminder_id BIGINT NOT NULL REFERENCES push_reminders(id),
    type VARCHAR(16) NOT NULL,
    fire_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (reminder_id, type)
);

CREATE INDEX idx_push_reminder_jobs_pending_fire_at
    ON push_reminder_jobs (next_attempt_at)
    WHERE processed_at IS NULL;
