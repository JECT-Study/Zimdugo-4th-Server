# 로컬 실행

## 1) 인프라 실행

1. Docker Desktop 실행
2. 프로젝트 루트에서 아래 명령 실행
   - `docker compose up -d`

기본 포트:
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

## 2) 백엔드 실행

- Windows: `.\gradlew bootRun`
- Mac/Linux: `./gradlew bootRun`

## 3) OAuth 로그인 테스트 (선택)

- Google/Naver/Kakao 로그인 테스트가 필요하면 로컬 환경변수에
  Client ID / Client Secret 값을 설정합니다.
- 각자 본인 키를 사용하며, 실제 키 값은 git에 커밋하지 않습니다.

## 참고

- 기본 API 실행은 `application.yaml`의 local fallback 값으로 가능합니다.
- `JWT_SECRET`도 local fallback이 있어 로컬 부팅 시 필수 입력이 아닙니다.
