---
module: locker
problem_type: architecture-patterns
tags:
  - search
  - naming
  - refactoring
---

# Align search component names with active endpoints

## Context

After removing the old `/keyword` endpoint, some classes still carried `keyword`-centric names even though they were now supporting the `/search` flow. That made `LockerSearchResultQueryService` look like it depended on a legacy API path instead of a search concern.

## Decision

Keep `keyword` only where it is the actual domain value or persisted column, and rename orchestration components to the active feature boundary.

## Applied here

- `KeywordCountCommandService` -> `SearchKeywordCountCommandService`
- `KeywordCountStore` -> `SearchKeywordCountStore`
- `KeywordCountStoreAdapter` -> `SearchKeywordCountStoreAdapter`
- `KeywordCountRepository` -> `SearchKeywordCountRepository`
- `KeywordCountEntity` -> `SearchKeywordCountEntity`

The table name `keyword_counts` remains unchanged because it describes stored data, not endpoint structure.

## Guardrail

When an endpoint is removed or merged, clean up leftover service and port names in the same pass. Otherwise the code keeps implying an old boundary that no longer exists.
