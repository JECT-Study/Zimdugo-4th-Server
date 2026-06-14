# Zimdugo be

- [체크스타일](docs/checkstyle.md)
- 프로덕션 DB PostGIS 적용: `scripts/db/01-postgis-prod.sql` (스키마 생성 이후 1회 실행, 재실행 가능)
- 프로덕션 Elasticsearch 연결: `.env`에 `ELASTICSEARCH_URIS`가 없으면 `http://elasticsearch:9200` 기본값 사용
