CREATE TABLE locker_size_types (
    locker_id BIGINT NOT NULL,
    size_type VARCHAR(20) NOT NULL,
    CONSTRAINT pk_locker_size_types PRIMARY KEY (locker_id, size_type),
    CONSTRAINT fk_locker_size_types_locker
        FOREIGN KEY (locker_id) REFERENCES lockers (id) ON DELETE CASCADE
);

INSERT INTO locker_size_types (locker_id, size_type)
SELECT DISTINCT
    ld.locker_id,
    TRIM(size_type)
FROM locker_details ld
CROSS JOIN LATERAL unnest(string_to_array(ld.locker_size, ',')) AS size_type
WHERE TRIM(size_type) <> '';

CREATE INDEX idx_locker_size_types_size_type_locker_id
    ON locker_size_types (size_type, locker_id);

ALTER TABLE locker_details DROP COLUMN locker_size;
