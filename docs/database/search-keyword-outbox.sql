CREATE TABLE search_keyword_outbox (
    id UUID PRIMARY KEY,
    raw_keyword VARCHAR(100) NOT NULL,
    normalized_keyword VARCHAR(100) NOT NULL,
    searched_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP NULL
);

CREATE INDEX idx_search_keyword_outbox_pending
    ON search_keyword_outbox (status, searched_at);
