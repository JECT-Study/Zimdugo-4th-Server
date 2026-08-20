CREATE TABLE IF NOT EXISTS locker_issue_reports (
    id BIGSERIAL PRIMARY KEY,
    locker_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    detail VARCHAR(1000),
    report_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_locker_issue_reports_locker
        FOREIGN KEY (locker_id) REFERENCES lockers (id)
);

CREATE INDEX IF NOT EXISTS idx_locker_issue_reports_locker_id
    ON locker_issue_reports (locker_id);
