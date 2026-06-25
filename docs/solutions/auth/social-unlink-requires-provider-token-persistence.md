---
title: Persist provider tokens if withdrawal must unlink social accounts
date: 2026-06-26
category: docs/solutions/auth
module: auth
problem_type: integration_design
component: social_login_withdrawal
severity: high
applies_when:
  - "회원탈퇴 시 구글/네이버/카카오 연동 해제를 함께 처리해야 할 때"
  - "JWT 세션만으로는 provider unlink 호출에 필요한 토큰을 다시 만들 수 없을 때"
tags: [oauth2, withdrawal, unlink, provider-token, redis]
---

# Persist provider tokens if withdrawal must unlink social accounts

## Context

회원탈퇴 내부 처리만 할 때는 `User` 소프트 삭제와 리프레시 토큰 삭제만으로 충분하다. 하지만 탈퇴 시점에 구글, 네이버, 카카오 provider 쪽 연동도 끊어야 하면 우리 JWT만으로는 외부 unlink API를 호출할 수 없다.

`providerUserId` 만으로 충분한 경우도 일부 있지만, 실제로는 provider access token 또는 refresh token 이 필요한 경우가 많다. 특히 구글/네이버는 로그인 성공 직후 받은 provider 토큰을 저장해두지 않으면 탈퇴 시점에 외부 연동 해제를 안정적으로 수행하기 어렵다.

## Guidance

1. OAuth 로그인 성공 직후 provider access token / refresh token 을 별도 저장한다.
2. 저장 위치는 탈퇴 전에 조회하기 쉬운 저장소면 되며, 이 프로젝트에서는 Redis 를 사용했다.
3. 회원탈퇴 서비스는 내부 soft delete 전에 provider unlink 를 먼저 호출한다.
4. unlink 가 성공한 뒤에만:
   - 사용자 익명화
   - 소셜 계정 row 삭제
   - provider token 삭제
   - refresh token 삭제
5. unlink 실패 시 내부 탈퇴를 먼저 진행하지 않는다.

## Why This Matters

회원탈퇴가 내부 DB 기준으로만 완료되고 provider 연동은 남아 있으면, 사용자 기대와 실제 상태가 어긋난다. 반대로 unlink 를 위해 필요한 토큰을 저장하지 않으면 탈퇴 시도 자체가 provider 정책에 막힐 수 있다.

## Related

- `src/main/java/com/zimdugo/auth/entrypoint/oauth2/OAuth2SuccessHandler.java`
- `src/main/java/com/zimdugo/auth/application/AccountWithdrawalService.java`
