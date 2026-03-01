# Checkstyle 설정 가이드

Gradle 플러그인과 IntelliJ CheckStyle-IDEA 플러그인을 사용하여 코딩 컨벤션을 검사합니다.

## Gradle Checkstyle

`build.gradle.kts`에 설정되어 있어 빌드 시 자동으로 검사됩니다.

```kotlin
plugins {
    checkstyle
}

checkstyle {
    toolVersion = "13.2.0"
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    maxWarnings = 0
    maxErrors = 0
}
```

- `./gradlew checkstyleMain` — 메인 소스 검사
- `./gradlew checkstyleTest` — 테스트 소스 검사
- 위반 사항이 하나라도 있으면 빌드 실패
- 리포트: `build/reports/checkstyle/` (XML, HTML)

## IntelliJ CheckStyle-IDEA 플러그인

### 플러그인 설치

1. `Settings` → `Plugins` → `Marketplace`
2. **CheckStyle-IDEA** 검색 후 설치
3. IntelliJ 재시작

### 플러그인 설정

1. `Settings` → `Tools` → `Checkstyle`
2. **Checkstyle version**: `13.2.0` 선택
3. **Scan Scope**: `Only Java sources (but not tests)` 선택
4. **Configuration File** 추가:
    - `+` 버튼 클릭
    - **Description**: `zimdugo`
    - **Use a local Checkstyle file** 선택 → `config/checkstyle/checkstyle.xml`
    - Properties 설정: `config_loc` = `config/checkstyle`
5. 추가한 설정을 **Active** 체크

### 사용 방법

- 하단 **Checkstyle** 탭에서 현재 파일 또는 프로젝트 전체 검사 가능
- 에디터에서 실시간으로 위반 사항 표시

## Git Pre-commit Hook

커밋 시 자동으로 `checkstyleMain`을 실행합니다.

### 설치

첫 빌드 시 자동 설치됩니다. (`scripts/pre-commit` → `.git/hooks/pre-commit` 복사)

수동 설치:

```bash
./gradlew installGitHooks
```

### 우회

hook을 건너뛰어야 할 때:

```bash
git commit --no-verify -m "메시지"
```

### 재설치

스크립트 수정 후 재설치가 필요하면:

```bash
rm .git/hooks/pre-commit
./gradlew installGitHooks
```

## 설정 파일 구조

```
config/checkstyle/
├── checkstyle.xml       # 메인 규칙 설정
└── suppressions.xml     # 테스트 코드 예외 규칙
scripts/
└── pre-commit           # Git pre-commit hook 스크립트
```

## 주요 규칙 요약

### 네이밍

| 대상  | 규칙               | 예시                |
|-----|------------------|-------------------|
| 클래스 | PascalCase       | `UserService`     |
| 메서드 | camelCase        | `findById`        |
| 변수  | camelCase        | `userName`        |
| 상수  | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | 소문자              | `com.zimdugo`     |

### 코드 품질

| 규칙     | 제한                       |
|--------|--------------------------|
| 줄 길이   | 최대 120자 (import, URL 제외) |
| 파일 길이  | 최대 500줄                  |
| 메서드 길이 | 최대 30줄 (빈 줄 제외)          |
| 파라미터 수 | 최대 5개 (`@Override` 제외)   |
| 매직 넘버  | 금지 (-1, 0, 1, 2 허용)      |

### 기타

- 접근제어자 순서 강제 (`public static final` 순)
- `import *` 금지
- 미사용 import 금지
- if/for/while/do 중괄호 필수
- 빈 블록 금지
- `equals()` 오버라이드 시 `hashCode()` 필수
- switch문 default 필수, fall-through 금지
- String 비교 시 `equals()` 사용 강제
- 파일당 최상위 클래스 1개

### 테스트 코드 예외 (suppressions.xml)

테스트 코드(`src/test/`)에서는 다음 규칙이 완화됩니다:

- 메서드명 snake_case 허용 (예: `should_return_ok_when_valid_request`)
- 메서드 길이 제한 해제
- 매직 넘버 허용
- 파일 길이 제한 해제
- 상수명 규칙 완화

## 규칙 예외 처리

특정 코드에서 규칙을 무시해야 할 때:

```java

@SuppressWarnings("checkstyle:MagicNumber")
public void example() {
    int timeout = 3000;
}
```
