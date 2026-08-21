CREATE TABLE IF NOT EXISTS locker_issue_reports (
    id BIGSERIAL PRIMARY KEY,
    locker_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    detail VARCHAR(1000),
    report_status VARCHAR(30) NOT NULL,
    reviewed_by VARCHAR(100),
    review_note VARCHAR(1000),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_locker_issue_reports_locker
        FOREIGN KEY (locker_id) REFERENCES lockers (id)
);

ALTER TABLE locker_issue_reports
    ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(100);

ALTER TABLE locker_issue_reports
    ADD COLUMN IF NOT EXISTS review_note VARCHAR(1000);

ALTER TABLE locker_issue_reports
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_locker_issue_reports_locker_id
    ON locker_issue_reports (locker_id);
