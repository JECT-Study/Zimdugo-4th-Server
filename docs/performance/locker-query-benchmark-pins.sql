-- locker-query-benchmark.sql 에서 화면 범위 변수를 바꿔 가며 포함한다.

\echo '=== [pins-':view'/v1-before-120] BETWEEN, 애플리케이션 필터'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT l.id, l.latitude, l.longitude, l.place_id, ld.locker_type, ld.indoor_outdoor_type, ld.locker_size
FROM lockers l
JOIN places p ON p.id = l.place_id
JOIN locker_details ld ON ld.locker_id = l.id
WHERE l.latitude BETWEEN :sw_lat AND :ne_lat
  AND l.longitude BETWEEN :sw_lng AND :ne_lng
  AND l.place_id IS NOT NULL
  AND l.publication_status = 'ACTIVE'
  AND p.publication_status = 'ACTIVE'
ORDER BY l.id ASC;

\echo '=== [pins-':view'/v2-geography] geography &&, DB 필터'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT l.id, ST_Y(l.location::geometry), ST_X(l.location::geometry), l.place_id,
       ld.locker_type, ld.indoor_outdoor_type
FROM lockers l
JOIN places p ON p.id = l.place_id
JOIN locker_details ld ON ld.locker_id = l.id
WHERE l.location && ST_MakeEnvelope(:sw_lng, :sw_lat, :ne_lng, :ne_lat, 4326)::geography
  AND l.place_id IS NOT NULL
  AND l.publication_status = 'ACTIVE'
  AND p.publication_status = 'ACTIVE'
  AND EXISTS (SELECT 1 FROM locker_size_types lst WHERE lst.locker_id = l.id
              AND lst.size_type = ANY(string_to_array(:'size_filter', ',')))
  AND ld.indoor_outdoor_type = ANY(string_to_array(:'indoor_filter', ','))
ORDER BY l.id ASC;

\echo '=== [pins-':view'/v3-between] BETWEEN, DB 필터'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT l.id, ST_Y(l.location::geometry), ST_X(l.location::geometry), l.place_id,
       ld.locker_type, ld.indoor_outdoor_type
FROM lockers l
JOIN places p ON p.id = l.place_id
JOIN locker_details ld ON ld.locker_id = l.id
WHERE l.latitude BETWEEN :sw_lat AND :ne_lat
  AND l.longitude BETWEEN :sw_lng AND :ne_lng
  AND l.place_id IS NOT NULL
  AND l.publication_status = 'ACTIVE'
  AND p.publication_status = 'ACTIVE'
  AND EXISTS (SELECT 1 FROM locker_size_types lst WHERE lst.locker_id = l.id
              AND lst.size_type = ANY(string_to_array(:'size_filter', ',')))
  AND ld.indoor_outdoor_type = ANY(string_to_array(:'indoor_filter', ','))
ORDER BY l.id ASC;

\echo '=== [pins-':view'/v3-no-filter] BETWEEN, 필터 없음'
EXPLAIN (ANALYZE, BUFFERS, COSTS OFF, SUMMARY ON)
SELECT l.id, ST_Y(l.location::geometry), ST_X(l.location::geometry), l.place_id,
       ld.locker_type, ld.indoor_outdoor_type
FROM lockers l
JOIN places p ON p.id = l.place_id
JOIN locker_details ld ON ld.locker_id = l.id
WHERE l.latitude BETWEEN :sw_lat AND :ne_lat
  AND l.longitude BETWEEN :sw_lng AND :ne_lng
  AND l.place_id IS NOT NULL
  AND l.publication_status = 'ACTIVE'
  AND p.publication_status = 'ACTIVE'
ORDER BY l.id ASC;
