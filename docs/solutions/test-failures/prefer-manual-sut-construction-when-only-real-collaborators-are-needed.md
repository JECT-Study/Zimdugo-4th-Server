---
module: test
problem_type: test-failures
tags:
  - mockito
  - injectmocks
  - unit-test
---

# Prefer manual SUT construction when only real collaborators are needed

## Context

Some unit tests used `@InjectMocks` with extra `@Mock` or `@Spy` fields only to satisfy constructor injection. That left unused test doubles in the file and made the test setup noisier than the assertions.

## Decision

When a collaborator does not need stubbing or verification, prefer constructing the SUT manually with a real instance or a no-op implementation.

## Applied here

- `LockerSearchResultQueryServiceTest` now constructs the service manually and uses only the doubles it actually asserts against.
- `LockerSearchQueryServiceTest` now constructs the service manually and uses a real `LockerSearchAssembler` instead of an unused spy.

## Guardrail

If a mock or spy is never stubbed or verified, remove it first and consider replacing `@InjectMocks` with explicit SUT construction.
