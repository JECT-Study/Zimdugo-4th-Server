---
title: Separate endpoint removal from shared search model renaming
date: 2026-06-28
category: docs/solutions/workflow-issues
module: locker search
problem_type: workflow_issue
component: locker_search_api
severity: medium
applies_when:
  - "/keyword API is being removed while /search and /pins still share internal search models"
  - "Legacy naming cleanup touches both search orchestration and suggest query services"
tags: [search, pins, refactor, naming, api]
---

# Separate endpoint removal from shared search model renaming

## Context

The public `/keyword` endpoint had already been removed from the controller surface, but many shared classes still used `Keyword` in their names because `/search`, `/pins`, and place locker responses reused the same internal models. A broad search-and-replace accidentally renamed the existing `LockerSearchQueryService`, which broke suggest lookups and created duplicate class names.

## Guidance

Treat these as two different refactors:

1. Remove the public `/keyword` endpoint and request/response DTOs.
2. Rename only the shared orchestration and result models that now belong to `/search` and `/pins`.

Do not apply bulk replacements across the whole `application.search` package. The existing candidate lookup service `LockerSearchQueryService` is a different role from the orchestration layer that builds displayable search results.

Use names that make the split explicit:

- `LockerSearchQueryService`: raw candidate lookup
- `LockerSearchResultQueryService`: assembled `/search` and `/pins` result orchestration
- `LockerSearchCommand`, `LockerSearchResult`, `LockerSearchItemResult`, `LockerSearchLockerResult`

Keep follow-up renames small and compile after each group:

```bash
./gradlew compileJava compileTestJava
./gradlew test --tests com.zimdugo.locker.application.search.LockerSearchResultQueryServiceTest \
  --tests com.zimdugo.locker.application.pin.LockerPinQueryServiceTest \
  --tests com.zimdugo.locker.application.search.LockerSearchQueryServiceTest \
  --tests com.zimdugo.locker.application.suggest.LockerSuggestQueryServiceTest
```

## Why This Matters

The public API can be cleaned up before all internal names are perfect, but shared search models sit in the dependency path for `/search`, `/pins`, and `/suggest`. Renaming those classes without distinguishing orchestration from candidate lookup creates hidden breakage that looks unrelated to the original endpoint removal.

## When to Apply

- Removing a deprecated API while preserving shared internal behavior.
- Renaming search models that are still used by place responses, pin assembly, or suggest services.
- Large search-and-replace changes across `application.search` or `application.result`.

## Related

- `src/main/java/com/zimdugo/locker/application/search/LockerSearchQueryService.java`
- `src/main/java/com/zimdugo/locker/application/search/LockerSearchResultQueryService.java`
