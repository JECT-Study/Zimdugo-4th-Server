# Operational Logging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 개발 단계와 운영 환경에서 요청 추적, 장애 원인 파악, 주요 사용자 행위 확인에 필요한 로그를 일관된 레벨과 필드로 추가한다.

**Architecture:** 기존 `RequestTraceFilter`의 `traceId` MDC를 중심으로 요청 단위 로그를 묶고, 서비스 계층에는 비즈니스 이벤트 로그만 추가한다. 운영에서는 `INFO` 이상으로 요청 완료, 주요 상태 변경, 외부 연동 실패를 확인하고, 개발에서는 `DEBUG`로 검색 조건과 상세 분기 결과를 확인한다.

**Tech Stack:** Java 25, Spring Boot 4.0.4, Lombok `@Slf4j`, Logback through Spring Boot logging, JUnit 5, MockMvc.

---

## Logging Policy

- `ERROR`: 처리되지 않은 예외, 5xx 비즈니스 예외, 외부 저장소/검색 인덱스 동기화 실패처럼 운영 알림 대상인 실패.
- `WARN`: 보안 의심 요청, 인증 실패, 잘못된 OAuth/cookie payload, 사용자 요청으로 발생 가능한 4xx 중 운영에서 추세 확인이 필요한 실패.
- `INFO`: 요청 완료 요약, 로그인 성공, 회원 탈퇴, 제보 생성, 즐겨찾기/투표 변경, 관리자 콘텐츠 변경, presigned URL 발급처럼 상태가 바뀌는 정상 이벤트.
- `DEBUG`: 검색 조건, 필터 파싱, 조회 결과 개수, 분기 판단 등 개발에서 유용하지만 운영 기본 로그에는 불필요한 상세 정보.
- 로그 금지 값: access token, refresh token, JWT 원문, presigned upload URL 전체, OAuth raw attributes, cookie 값, 이메일 전체, 사용자 입력 원문 중 파일명/검색어처럼 개인정보가 될 수 있는 값의 과도한 노출.
- 공통 필드 이름: `traceId`, `method`, `path`, `status`, `durationMs`, `userId`, `lockerId`, `placeId`, `provider`, `category`, `resultCount`, `errorCode`.

## File Structure

- Modify: `src/main/java/com/zimdugo/common/filter/RequestTraceFilter.java`
  - 요청 시작/완료 로그, 지연 시간, 응답 상태, MDC 정리 담당.
- Modify: `src/main/resources/application.yaml`
  - local/prod 로깅 레벨과 로그 패턴 설정. 운영은 애플리케이션 `INFO`, SQL 로그 비활성.
- Modify: `src/main/java/com/zimdugo/common/exception/GlobalExceptionHandler.java`
  - 4xx/validation 실패는 `debug` 또는 필요 시 `warn`, 5xx는 `error`로 정리.
- Modify: `src/main/java/com/zimdugo/locker/domain/search/LockerSearchFilter.java`
  - `System.out.println` 제거, `@Slf4j` 기반 `debug` 로그로 교체.
- Modify: `src/main/java/com/zimdugo/auth/application/CustomOAuth2UserService.java`
  - OAuth provider별 사용자 생성/동기화 결과 로그.
- Modify: `src/main/java/com/zimdugo/auth/application/AccountWithdrawalService.java`
  - 회원 탈퇴 성공/중복 탈퇴 시도 로그.
- Modify: `src/main/java/com/zimdugo/locker/application/report/LockerReportCommandService.java`
  - 보관함 제보 생성 로그.
- Modify: `src/main/java/com/zimdugo/locker/application/favorite/FavoriteLockerCommandService.java`
  - 즐겨찾기 추가/중복/삭제 로그.
- Modify: `src/main/java/com/zimdugo/locker/application/vote/LockerVoteCommandService.java`
  - 투표 생성/변경/취소 로그.
- Modify: `src/main/java/com/zimdugo/locker/application/search/LockerSearchQueryService.java`
  - 검색 요청의 필터 유무, 매칭 타입, 결과 개수 `debug` 로그.
- Modify: `src/main/java/com/zimdugo/image/application/S3PresignedImageUploadService.java`
  - presigned URL 발급 성공 로그. URL 자체는 남기지 않고 key/category/contentType/contentLength만 기록.
- Modify: `src/main/java/com/zimdugo/admin/i18n/LockerContentI18nAdminService.java`
  - 관리자 다국어 콘텐츠 변경 로그.
- Test: `src/test/java/com/zimdugo/common/filter/RequestTraceFilterTest.java`
  - trace id 헤더/MDC/응답 헤더 동작 보강.
- Test: `src/test/java/com/zimdugo/locker/domain/search/LockerSearchFilterTest.java`
  - 표준 출력 제거 회귀 테스트.

### Task 1: Request Completion Logging

**Files:**
- Modify: `src/main/java/com/zimdugo/common/filter/RequestTraceFilter.java`
- Test: `src/test/java/com/zimdugo/common/filter/RequestTraceFilterTest.java`

- [ ] **Step 1: Add failing test for response trace id and MDC cleanup**

```java
@Test
void setsTraceIdHeaderAndClearsMdc() throws Exception {
    RequestTraceFilter filter = new RequestTraceFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/lockers");
    request.addHeader(RequestTraceFilter.TRACE_ID_HEADER, "request-trace-id");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (servletRequest, servletResponse) ->
        assertThat(MDC.get("traceId")).isEqualTo("request-trace-id")
    );

    assertThat(response.getHeader(RequestTraceFilter.TRACE_ID_HEADER)).isEqualTo("request-trace-id");
    assertThat(request.getAttribute(RequestTraceFilter.TRACE_ID_ATTRIBUTE)).isEqualTo("request-trace-id");
    assertThat(MDC.get("traceId")).isNull();
}
```

- [ ] **Step 2: Run filter test and verify current behavior**

Run: `./gradlew test --tests com.zimdugo.common.filter.RequestTraceFilterTest`

Expected: PASS if the current trace behavior is already correct. If the class does not expose `doFilter` directly in the test context, adapt the test to use the existing servlet filter test style in `AcceptLanguageVaryFilterTest`.

- [ ] **Step 3: Add `@Slf4j` and completion log**

Update `RequestTraceFilter` with this shape:

```java
@Slf4j
@Component
public class RequestTraceFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_ATTRIBUTE = "requestTraceId";
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String traceId = resolveOrCreateTraceId(request);
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        MDC.put(MDC_TRACE_ID_KEY, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            logRequestCompleted(request, response, durationMs);
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }

    private void logRequestCompleted(
        HttpServletRequest request,
        HttpServletResponse response,
        long durationMs
    ) {
        int status = response.getStatus();
        if (status >= 500) {
            log.warn("요청 처리 완료. method={}, path={}, status={}, durationMs={}",
                request.getMethod(), request.getRequestURI(), status, durationMs);
            return;
        }
        log.info("요청 처리 완료. method={}, path={}, status={}, durationMs={}",
            request.getMethod(), request.getRequestURI(), status, durationMs);
    }
}
```

- [ ] **Step 4: Run filter test**

Run: `./gradlew test --tests com.zimdugo.common.filter.RequestTraceFilterTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zimdugo/common/filter/RequestTraceFilter.java src/test/java/com/zimdugo/common/filter/RequestTraceFilterTest.java
git commit -m "feat: 요청 완료 로그 추가"
```

### Task 2: Profile-Specific Logging Configuration

**Files:**
- Modify: `src/main/resources/application.yaml`

- [ ] **Step 1: Configure local logging for development**

Under the `local` profile `logging:` block, keep SQL visibility and add application debug logging:

```yaml
logging:
  pattern:
    level: "%5p [traceId:%X{traceId:-}]"
  level:
    com.zimdugo: debug
    org.hibernate.SQL: debug
    org.hibernate.orm.jdbc.bind: trace
```

- [ ] **Step 2: Configure prod logging for operation**

Under the `prod` profile, add:

```yaml
logging:
  pattern:
    level: "%5p [traceId:%X{traceId:-}]"
  level:
    root: info
    com.zimdugo: info
    org.hibernate.SQL: warn
    org.hibernate.orm.jdbc.bind: warn
```

- [ ] **Step 3: Run configuration validation**

Run: `./gradlew test --tests com.zimdugo.common.filter.RequestTraceFilterTest`

Expected: PASS and Spring context/logback configuration loads without YAML parse errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/application.yaml
git commit -m "chore: 프로필별 로그 레벨 설정 추가"
```

### Task 3: Exception Logging Cleanup

**Files:**
- Modify: `src/main/java/com/zimdugo/common/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/zimdugo/common/exception/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Add test for trace id propagation on validation errors**

Add or keep an assertion in `GlobalExceptionHandlerTest`:

```java
assertThat(response.getBody().getTraceId()).isEqualTo("generated-trace-id");
```

- [ ] **Step 2: Add controlled 4xx logs**

In `handleBusinessException`, keep 5xx as `error` and add `debug` for ordinary business failures:

```java
if (errorCode.getStatus().is5xxServerError()) {
    log.error("서버 오류가 발생했습니다. code={}, method={}, path={}",
        errorCode.getCode(), request.getMethod(), request.getRequestURI(), ex);
    return errorResponse(errorCode, errorCode.getMessage(), null, request);
}
log.debug("비즈니스 예외가 발생했습니다. code={}, method={}, path={}",
    errorCode.getCode(), request.getMethod(), request.getRequestURI());
return errorResponse(errorCode, ex.getMessage(), null, request);
```

- [ ] **Step 3: Add warn logs for malformed request bodies and parameter format failures**

In `handleHttpMessageNotReadable` and `handleTypeMismatch`, add logs without request body values:

```java
log.warn("요청 본문 형식이 올바르지 않습니다. method={}, path={}",
    servletRequest(request).getMethod(), servletRequest(request).getRequestURI());
```

Use a private helper:

```java
private HttpServletRequest servletRequest(WebRequest request) {
    return ((ServletWebRequest) request).getRequest();
}
```

- [ ] **Step 4: Run exception tests**

Run: `./gradlew test --tests com.zimdugo.common.exception.GlobalExceptionHandlerTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zimdugo/common/exception/GlobalExceptionHandler.java src/test/java/com/zimdugo/common/exception/GlobalExceptionHandlerTest.java
git commit -m "fix: 예외 처리 로그 레벨 정리"
```

### Task 4: Remove Standard Output Debug Logs

**Files:**
- Modify: `src/main/java/com/zimdugo/locker/domain/search/LockerSearchFilter.java`
- Test: `src/test/java/com/zimdugo/locker/domain/search/LockerSearchFilterTest.java`

- [ ] **Step 1: Add regression test that filter parsing does not write to standard output**

```java
@Test
void parsingDoesNotWriteToStandardOutput() {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
    try {
        LockerSearchFilter.from(Set.of("SMALL"), Set.of("INDOOR"), Set.of("FREE"));
    } finally {
        System.setOut(originalOut);
    }

    assertThat(output.toString()).isBlank();
}
```

- [ ] **Step 2: Run test and verify it fails before implementation**

Run: `./gradlew test --tests com.zimdugo.locker.domain.search.LockerSearchFilterTest.parsingDoesNotWriteToStandardOutput`

Expected: FAIL because current code writes `[DEBUG]` lines to `System.out`.

- [ ] **Step 3: Replace `System.out.println` with `@Slf4j` debug logging**

```java
@Slf4j
public record LockerSearchFilter(...) {
    public static LockerSearchFilter from(...) {
        log.debug("검색 필터 파싱 시작. sizeTypes={}, indoorOutdoorTypes={}, lockerTypes={}",
            sizeTypes, indoorOutdoorTypes, lockerTypes);
        ...
    }
}
```

- [ ] **Step 4: Run filter tests**

Run: `./gradlew test --tests com.zimdugo.locker.domain.search.LockerSearchFilterTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zimdugo/locker/domain/search/LockerSearchFilter.java src/test/java/com/zimdugo/locker/domain/search/LockerSearchFilterTest.java
git commit -m "fix: 검색 필터 표준 출력 로그 제거"
```

### Task 5: Authentication and Account Event Logs

**Files:**
- Modify: `src/main/java/com/zimdugo/auth/application/CustomOAuth2UserService.java`
- Modify: `src/main/java/com/zimdugo/auth/application/AccountWithdrawalService.java`
- Existing tests: `src/test/java/com/zimdugo/auth/entrypoint/AuthControllerTest.java`

- [ ] **Step 1: Add `@Slf4j` to OAuth and withdrawal services**

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
}
```

Apply the same pattern to `AccountWithdrawalService`.

- [ ] **Step 2: Log OAuth load result**

After `User user = findOrCreateUser(userInfo);`, add:

```java
log.info("OAuth 사용자 인증 완료. provider={}, userId={}, role={}",
    registrationId, user.getId(), user.getRoleOrDefault());
```

- [ ] **Step 3: Log new user creation and existing social account sync**

In `syncAndGetUser`:

```java
log.debug("OAuth 소셜 계정 동기화 완료. provider={}, userId={}",
    userInfo.getProvider(), saved.getUser().getId());
```

In `createNewUser`:

```java
log.info("OAuth 신규 사용자 생성 완료. provider={}, userId={}",
    userInfo.getProvider(), savedUser.getId());
```

- [ ] **Step 4: Log withdrawal validation and success**

In `withdraw`:

```java
if (user.getStatus() == UserStatus.DELETED) {
    log.warn("이미 탈퇴한 사용자의 탈퇴 요청입니다. userId={}", userId);
    throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
}
...
log.info("회원 탈퇴 완료. userId={}", userId);
```

- [ ] **Step 5: Run auth tests**

Run: `./gradlew test --tests com.zimdugo.auth.entrypoint.AuthControllerTest`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zimdugo/auth/application/CustomOAuth2UserService.java src/main/java/com/zimdugo/auth/application/AccountWithdrawalService.java
git commit -m "feat: 인증 계정 이벤트 로그 추가"
```

### Task 6: Locker Command Event Logs

**Files:**
- Modify: `src/main/java/com/zimdugo/locker/application/report/LockerReportCommandService.java`
- Modify: `src/main/java/com/zimdugo/locker/application/favorite/FavoriteLockerCommandService.java`
- Modify: `src/main/java/com/zimdugo/locker/application/vote/LockerVoteCommandService.java`
- Tests: existing command service/controller tests under `src/test/java/com/zimdugo/locker`

- [ ] **Step 1: Add `@Slf4j` to the three command services**

```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LockerReportCommandService {
}
```

Repeat for `FavoriteLockerCommandService` and `LockerVoteCommandService`.

- [ ] **Step 2: Log locker report creation**

After `SavedLockerReport report = lockerReportStore.create(...)`:

```java
log.info("보관함 제보 생성 완료. userId={}, reportId={}, lockerName={}",
    userId, report.id(), command.name());
```

If `SavedLockerReport` does not expose `id()`, use the existing accessor from that domain type.

- [ ] **Step 3: Log favorite add/remove**

In `add`:

```java
if (favoriteLockerReader.exists(userId, lockerId)) {
    log.debug("이미 등록된 즐겨찾기 요청입니다. userId={}, lockerId={}", userId, lockerId);
    return;
}
favoriteLockerStore.save(userId, lockerId);
log.info("즐겨찾기 등록 완료. userId={}, lockerId={}", userId, lockerId);
```

In `remove`:

```java
favoriteLockerStore.delete(userId, lockerId);
log.info("즐겨찾기 삭제 요청 처리 완료. userId={}, lockerId={}", userId, lockerId);
```

- [ ] **Step 4: Log vote action result**

Introduce a local `String action` in `toggleVote`:

```java
String action;
if (existingVote.isPresent()) {
    LockerVote vote = existingVote.get();
    if (vote.voteType() == voteType) {
        action = "CANCEL";
        ...
    } else {
        action = "CHANGE";
        ...
    }
} else {
    action = "CREATE";
    ...
}
...
log.info("보관함 투표 처리 완료. userId={}, lockerId={}, voteType={}, action={}",
    userId, lockerId, voteType, action);
```

- [ ] **Step 5: Run focused locker tests**

Run:

```bash
./gradlew test \
  --tests com.zimdugo.locker.entrypoint.LockerReportControllerTest \
  --tests com.zimdugo.locker.entrypoint.LockerFavoriteControllerTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zimdugo/locker/application/report/LockerReportCommandService.java src/main/java/com/zimdugo/locker/application/favorite/FavoriteLockerCommandService.java src/main/java/com/zimdugo/locker/application/vote/LockerVoteCommandService.java
git commit -m "feat: 보관함 변경 이벤트 로그 추가"
```

### Task 7: Search, Image Upload, and Admin Change Logs

**Files:**
- Modify: `src/main/java/com/zimdugo/locker/application/search/LockerSearchQueryService.java`
- Modify: `src/main/java/com/zimdugo/image/application/S3PresignedImageUploadService.java`
- Modify: `src/main/java/com/zimdugo/admin/i18n/LockerContentI18nAdminService.java`

- [ ] **Step 1: Add `@Slf4j` to target services**

Add `import lombok.extern.slf4j.Slf4j;` and `@Slf4j` above each class declaration.

- [ ] **Step 2: Log search summary at debug**

After `candidateResult` is read and before return:

```java
log.debug("보관함 검색 후보 조회 완료. keywordPresent={}, filterEmpty={}, matchType={}, resultCount={}",
    keyword != null && !keyword.isBlank(),
    filter.isEmpty(),
    candidateResult.matchType(),
    candidateResult.candidates().size());
```

After assembly:

```java
List<LockerSuggestItemResult> results = lockerSearchAssembler.assemble(
    candidateResult.candidates(),
    candidateResult.matchType()
);
log.debug("보관함 검색 응답 생성 완료. resultCount={}", results.size());
return results;
```

- [ ] **Step 3: Log presigned upload creation without URL**

After `PresignedUpload upload = ...`:

```java
log.info("이미지 업로드 URL 발급 완료. category={}, userId={}, key={}, contentType={}, contentLength={}",
    category, userId, upload.key(), normalizedContentType, contentLength);
```

- [ ] **Step 4: Log admin i18n replacements**

In `replacePlace` after `eventPublisher.publishEvent(...)`:

```java
log.info("장소 다국어 콘텐츠 교체 완료. placeId={}, translationCount={}, aliasCount={}",
    placeId, request.translations().size(), aliases(request.aliases()).size());
```

In `replaceLocker`:

```java
log.info("보관함 다국어 콘텐츠 교체 완료. lockerId={}, translationCount={}, aliasCount={}",
    lockerId, request.translations().size(), lockerAliases(request.aliases()).size());
```

- [ ] **Step 5: Run related tests**

Run:

```bash
./gradlew test \
  --tests com.zimdugo.locker.application.search.LockerSearchQueryServiceTest \
  --tests com.zimdugo.admin.entrypoint.AdminLockerContentI18nControllerTest \
  --tests com.zimdugo.image.entrypoint.ImageUploadControllerTest
```

Expected: PASS. If one of the named test classes does not exist, replace it with the existing nearest controller/service test discovered by `rg --files src/test/java | rg '(Search|I18n|ImageUpload)'`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zimdugo/locker/application/search/LockerSearchQueryService.java src/main/java/com/zimdugo/image/application/S3PresignedImageUploadService.java src/main/java/com/zimdugo/admin/i18n/LockerContentI18nAdminService.java
git commit -m "feat: 검색 이미지 관리자 로그 추가"
```

### Task 8: Final Verification

**Files:**
- Create: `docs/solutions/workflow-issues/compound-review-after-verification.md`
- Modify or create: `AGENTS.md`

- [ ] **Step 1: Run Checkstyle**

Run: `./gradlew checkstyleMain checkstyleTest`

Expected: PASS.

- [ ] **Step 2: Run full tests**

Run: `./gradlew test`

Expected: PASS.

- [ ] **Step 3: Confirm no standard output debug logs remain**

Run: `rg -n "System\\.out|\\[DEBUG\\]" src/main/java src/test/java`

Expected: no production `System.out` debug statements. Test-only standard output capture in `LockerSearchFilterTest` is acceptable.

- [ ] **Step 4: Manual local smoke check**

Run: `./gradlew bootRun --args='--spring.profiles.active=local'`

Expected: application starts, request logs include `traceId`, method, path, status, and duration.

- [ ] **Step 5: Commit verification fixes if needed**

```bash
git add <files changed by verification fixes>
git commit -m "fix: 로그 적용 검증 오류 수정"
```

- [ ] **Step 6: Run compound review after verification**

Use the installed `ce-compound` process in headless/lightweight form to capture reusable lessons from the execution-review cycle. For this implementation, document:

```markdown
- checkstyle caught MagicNumber and MethodLength after logging changes
- final review should include a compound pass after fresh verification
- docs/solutions/ should be discoverable from root agent instructions
```

Write the learning to `docs/solutions/workflow-issues/compound-review-after-verification.md` and add a minimal `AGENTS.md` note if the repo still lacks project instructions that surface `docs/solutions/`.

## Self-Review

- Spec coverage: 개발 단계 확인용 `DEBUG`, 운영 확인용 `INFO/WARN/ERROR`, 요청 추적, 인증/계정, 보관함 변경, 검색, 이미지, 관리자 변경, 외부 연동 실패 로그 기준을 모두 포함.
- Placeholder scan: 계획에 미정 항목 없음. 테스트 클래스가 없는 경우 대체 명령을 명시.
- Type consistency: 기존 코드에서 확인한 `RequestTraceFilter`, `LockerSearchFilter`, `CustomOAuth2UserService`, `AccountWithdrawalService`, `LockerReportCommandService`, `FavoriteLockerCommandService`, `LockerVoteCommandService`, `LockerSearchQueryService`, `S3PresignedImageUploadService`, `LockerContentI18nAdminService` 이름과 경로 사용.
