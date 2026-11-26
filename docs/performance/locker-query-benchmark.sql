-- 지도 핀 / 보관함 상세 조회 쿼리 개선 전후 비교용 벤치마크
-- 사용법: psql -v locker_count=100000 -v mapping_count=25000 -f locker-query-benchmark.sql
--         데이터를 유지한 채 쿼리만 다시 측정할 때는 -v skip_setup=1 을 추가한다.
-- 운영 스키마 중 두 쿼리가 참조하는 컬럼만 재현하고, 좌표는 국내 범위에 서울 밀집 분포로 생성한다.

\set ON_ERROR_STOP on
\timing off

\if :{?skip_setup}
\else
DROP TABLE IF EXISTS locker_realtime_availabilities, locker_realtime_mappings,
    locker_size_types, locker_details, lockers, places CASCADE;

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    publication_status VARCHAR(20) NOT NULL
);

CREATE TABLE lockers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    road_address VARCHAR(200) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location geography(Point, 4326),
    place_id BIGINT REFERENCES places(id),
    publication_status VARCHAR(20) NOT NULL
);

CREATE TABLE locker_details (
    id BIGSERIAL PRIMARY KEY,
    locker_id BIGINT NOT NULL UNIQUE REFERENCES lockers(id),
    locker_type VARCHAR(30) NOT NULL,
    indoor_outdoor_type VARCHAR(10) NOT NULL,
    locker_size VARCHAR(30) NOT NULL
);

CREATE TABLE locker_size_types (
    locker_id BIGINT NOT NULL REFERENCES lockers(id),
    size_type VARCHAR(10) NOT NULL,
    PRIMARY KEY (locker_id, size_type)
);

CREATE TABLE locker_realtime_mappings (
    external_locker_id VARCHAR(100) PRIMARY KEY,
    locker_id BIGINT NOT NULL REFERENCES lockers(id)
);

CREATE TABLE locker_realtime_availabilities (
    external_locker_id VARCHAR(100) PRIMARY KEY REFERENCES locker_realtime_mappings(external_locker_id),
    small_available_count INTEGER NOT NULL,
    medium_available_count INTEGER NOT NULL,
    large_available_count INTEGER NOT NULL,
    fetched_at TIMESTAMP NOT NULL
);

SELECT setseed(0.42);

INSERT INTO places (name, publication_status)
SELECT 'place-' || g, CASE WHEN random() < 0.95 THEN 'ACTIVE' ELSE 'HIDDEN' END
FROM generate_series(1, GREATEST(:locker_count / 3, 1)) g;

-- 40%는 서울 도심, 나머지는 국내 전역에 분포
INSERT INTO lockers (name, road_address, latitude, longitude, place_id, publication_status)
SELECT 'locker-' || g,
       'road-' || g,
       CASE WHEN dense THEN 37.45 + random() * 0.25 ELSE 34.0 + random() * 4.3 END,
       CASE WHEN dense THEN 126.80 + random() * 0.35 ELSE 126.0 + random() * 3.3 END,
       1 + floor(random() * GREATEST(:locker_count / 3, 1))::BIGINT,
       CASE WHEN random() < 0.97 THEN 'ACTIVE' ELSE 'HIDDEN' END
FROM (SELECT g, random() < 0.4 AS dense FROM generate_series(1, :locker_count) g) s;

UPDATE lockers SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography;

INSERT INTO locker_details (locker_id, locker_type, indoor_outdoor_type, locker_size)
SELECT l.id,
       (ARRAY['MUSEUM','SUBWAY_STATION','DEPARTMENT_STORE','CONVENIENCE_STORE',
              'PUBLIC_OFFICE','PRIVATE_LOCKER','TRAIN_STATION'])[1 + floor(random() * 7)::INT],
       CASE WHEN random() < 0.8 THEN 'INDOOR' ELSE 'OUTDOOR' END,
       (ARRAY['SMALL','SMALL,MEDIUM','SMALL,MEDIUM,LARGE','MEDIUM,LARGE','LARGE'])[1 + floor(random() * 5)::INT]
FROM lockers l;

INSERT INTO locker_size_types (locker_id, size_type)
SELECT ld.locker_id, unnest(string_to_array(ld.locker_size, ','))
FROM locker_details ld;

-- 실시간 연동 보관함은 서울 도심 보관함 일부에만 존재한다.
INSERT INTO locker_realtime_mappings (external_locker_id, locker_id)
SELECT 'EXT-' || row_number() OVER (), id
FROM (
    SELECT id FROM lockers
    WHERE latitude BETWEEN 37.45 AND 37.70 AND longitude BETWEEN 126.80 AND 127.15
    ORDER BY random()
    LIMIT :mapping_count
) target;

INSERT INTO locker_realtime_availabilities
SELECT external_locker_id, floor(random() * 10), floor(random() * 6), floor(random() * 3), now()
FROM locker_realtime_mappings;

CREATE INDEX idx_lockers_location_geog ON lockers USING GIST (location);
CREATE INDEX idx_lockers_latitude_longitude ON lockers (latitude, longitude);
CREATE INDEX idx_locker_realtime_mappings_locker_id ON locker_realtime_mappings (locker_id);
VACUUM ANALYZE;

SELECT (SELECT count(*) FROM lockers) AS lockers,
       (SELECT count(*) FROM locker_realtime_mappings) AS realtime_mappings;

\endif

-- 측정 대상 보관함: 실시간 매핑이 있는 보관함 중 하나
SELECT locker_id AS target_locker_id FROM locker_realtime_mappings ORDER BY external_locker_id LIMIT 1 \gset

\echo '=== [detail/before] 전체 매핑 GROUP BY 후 단건 선택'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT l.id, ra.small_available_count, ra.medium_available_count, ra.large_available_count, ra.fetched_at
FROM lockers l
JOIN locker_details ld ON ld.locker_id = l.id
LEFT JOIN places p ON p.id = l.place_id
LEFT JOIN (
    SELECT rm.locker_id,
           SUM(ra.small_available_count) AS small_available_count,
           SUM(ra.medium_available_count) AS medium_available_count,
           SUM(ra.large_available_count) AS large_available_count,
           MAX(ra.fetched_at) AS fetched_at
    FROM locker_realtime_mappings rm
    JOIN locker_realtime_availabilities ra ON ra.external_locker_id = rm.external_locker_id
    GROUP BY rm.locker_id
) ra ON ra.locker_id = l.id
WHERE l.id = :target_locker_id;

\echo '=== [detail/after] 요청 보관함 매핑만 LATERAL 집계'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT l.id, ra.small_available_count, ra.medium_available_count, ra.large_available_count, ra.fetched_at
FROM lockers l
JOIN locker_details ld ON ld.locker_id = l.id
LEFT JOIN places p ON p.id = l.place_id
LEFT JOIN LATERAL (
    SELECT SUM(ra.small_available_count) AS small_available_count,
           SUM(ra.medium_available_count) AS medium_available_count,
           SUM(ra.large_available_count) AS large_available_count,
           MAX(ra.fetched_at) AS fetched_at
    FROM locker_realtime_mappings rm
    JOIN locker_realtime_availabilities ra ON ra.external_locker_id = rm.external_locker_id
    WHERE rm.locker_id = l.id
) ra ON true
WHERE l.id = :target_locker_id;

-- 지도 핀 조회 세 가지 버전 비교. 필터는 대형 + 실내.
--   v1: PR #120 이전  위경도 BETWEEN, 필터는 애플리케이션에서 처리(DB는 필터 전 전체 행 반환)
--   v2: PR #120       geography 경계 상자(&&) + DB 필터
--   v3: 현재          위경도 BETWEEN(복합 B-tree) + DB 필터
\set size_filter 'LARGE'
\set indoor_filter 'INDOOR'

-- 넓은 화면: 서울 도심 전체(줌 12 내외)
\set sw_lat 37.48
\set sw_lng 126.85
\set ne_lat 37.65
\set ne_lng 127.10
\set view wide
\ir locker-query-benchmark-pins.sql

-- 좁은 화면: 역 주변(줌 15 내외)
\set sw_lat 37.550
\set sw_lng 126.960
\set ne_lat 37.565
\set ne_lng 126.985
\set view narrow
\ir locker-query-benchmark-pins.sql
