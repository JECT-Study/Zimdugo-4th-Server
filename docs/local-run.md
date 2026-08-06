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

## 4) OCI Object Storage 업로드 테스트 (선택)

- 애플리케이션 시작 시 비밀이 아닌 `OCI_OBJECT_STORAGE_NAMESPACE`와
  `OCI_OBJECT_STORAGE_BUCKET` 좌표는 설정되어 있어야 합니다. 다만 OCI 클라이언트와
  인증 정보는 스토리지 작업이 호출될 때 처음 초기화됩니다.
- 로컬 업로드 테스트에는 개발용 버킷에 접근할 수 있는 OCI CLI `DEFAULT` 프로필이
  필요합니다.
- 다음 환경변수를 명시합니다.
  - `OCI_OBJECT_STORAGE_NAMESPACE`
  - `OCI_OBJECT_STORAGE_BUCKET`
  - `OCI_AUTH_MODE=config-file`
- OCI 프로필이 참조하는 개인 키는 절대 저장소에 커밋하지 않습니다.

## 참고

- OCI namespace와 bucket 외의 기본 API 설정은 `application.yaml`의 local fallback 값을
  사용할 수 있습니다.
- `JWT_SECRET`도 local fallback이 있어 로컬 부팅 시 필수 입력이 아닙니다.
