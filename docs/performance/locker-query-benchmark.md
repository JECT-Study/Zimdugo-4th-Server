# 지도 핀 · 보관함 상세 조회 쿼리 측정

지도 핀 조회(`findLockersWithinBounds`)와 보관함 상세 조회(`findDetailById`)의 개선 전후를 같은 데이터에서 비교한 기록입니다.

## 측정 방법

- 스크립트: [`locker-query-benchmark.sql`](locker-query-benchmark.sql), [`locker-query-benchmark-pins.sql`](locker-query-benchmark-pins.sql)
- 환경: PostgreSQL 16.16 + PostGIS 3, Docker(arm64), Apple M4 Pro
- 데이터: 운영 스키마 중 두 쿼리가 쓰는 컬럼만 재현했습니다. 보관함 좌표는 40%를 서울 도심에, 나머지를 국내 전역에 분포시켰습니다.
  - 운영 규모: 보관함 1,000개, 실시간 연동 매핑 240개 (2026-11 기준 운영 데이터는 보관함 약 990개, 매핑 236개)
  - 확장 규모: 보관함 100,000개, 실시간 연동 매핑 25,000개
- 지표: `EXPLAIN (ANALYZE)`의 Execution Time을 30회 측정한 중앙값입니다. 애플리케이션 전송·직렬화 비용은 포함하지 않습니다.
- 필터 조건: 대형 크기 + 실내

```bash
docker run -d --name zim-bench -e POSTGRES_PASSWORD=bench -e POSTGRES_DB=bench <postgis-image>
docker cp docs/performance/. zim-bench:/
docker exec zim-bench psql -U postgres -d bench -v locker_count=100000 -v mapping_count=25000 -f /locker-query-benchmark.sql
```

## 지도 핀 조회

| 버전 | 범위 조건 | 필터 위치 |
| --- | --- | --- |
| v1 (PR #120 이전) | `latitude/longitude BETWEEN` | 애플리케이션 (DB는 필터 전 전체 행 반환) |
| v2 (PR #120) | `location && ST_MakeEnvelope(...)::geography` | DB |
| v3 (현재) | `latitude/longitude BETWEEN` (복합 B-tree) | DB |

### 확장 규모 (보관함 100,000개)

| 화면 | v1 | v2 | v3 | v2 → v3 |
| --- | --- | --- | --- | --- |
| 넓은 화면 (서울 도심 전체) | 26.5ms / 17,967행 | 42.8ms / 10,718행 | **34.1ms / 8,650행** | 실행 시간 -20%, 반환 행 -19% |
| 좁은 화면 (역 주변) | 0.74ms / 172행 | 0.94ms / 101행 | **0.49ms / 80행** | 실행 시간 -48%, 반환 행 -21% |

### 운영 규모 (보관함 1,000개)

모든 버전이 0.5ms 미만입니다. 현재 데이터 규모에서는 범위 조건에 따른 응답 시간 차이가 의미 있는 수준이 아닙니다.

### 해석

- **v2의 geography `&&`는 화면 사각형보다 넓게 조회했습니다.** geography의 경계 상자 비교는 지구 중심 기준 3차원 상자로 이뤄져서, 서울 위도에서 화면 좌우로 약 0.007°(약 600m) 바깥 보관함까지 포함했습니다. 넓은 화면 기준으로 화면 밖 보관함 약 2,000개(19%)를 더 반환했고, 이 범위 비교에만 13.9ms가 걸렸습니다. 같은 범위를 위경도 복합 인덱스로 조회하면 1.9ms였습니다.
- **v3는 정확한 화면 범위만 반환합니다.** 운영 스키마에 이미 있는 `idx_lockers_latitude_longitude` 인덱스를 쓰기 때문에, 추가 인덱스 없이 배포할 수 있습니다.
- **v1 대비 v3**: 넓은 화면에서는 DB 실행 시간이 v1보다 길지만(필터용 크기 테이블 조인 비용), 애플리케이션으로 넘기는 행이 52% 줄어듭니다. 좁은 화면에서는 실행 시간과 반환 행이 모두 줄어듭니다.

## 보관함 상세 조회

| 버전 | 실시간 잔여 수량 집계 | 확장 규모 | 운영 규모 |
| --- | --- | --- | --- |
| 이전 | 전체 매핑 `GROUP BY` 후 `JOIN` | 0.093ms | 0.086ms |
| 현재 | 요청 보관함만 `LATERAL` 집계 | 0.036ms | 0.048ms |

PostgreSQL 플래너가 `l.id = :lockerId` 조건을 하위 쿼리 안으로 밀어 넣어서, 이전 쿼리도 실제로는 전체 매핑을 집계하지 않았습니다. 그래서 `LATERAL` 전환의 효과는 실행 계획이 단순해지는 정도에 그칩니다. 이 개선은 성능보다, 쿼리가 의도한 조회 범위를 명시적으로 드러낸다는 데 의미가 있습니다.
