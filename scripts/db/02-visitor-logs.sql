CREATE TABLE IF NOT EXISTS visitor_logs (
    id BIGSERIAL PRIMARY KEY,
    visitor_identifier VARCHAR(100) NOT NULL,
    accessed_date DATE NOT NULL,
    accessed_at TIMESTAMP(6) NOT NULL,
    user_id BIGINT
);;

CREATE UNIQUE INDEX IF NOT EXISTS uk_visitor_logs_identifier_date
    ON visitor_logs (visitor_identifier, accessed_date);;

CREATE INDEX IF NOT EXISTS idx_visitor_logs_accessed_date
    ON visitor_logs (accessed_date);;
