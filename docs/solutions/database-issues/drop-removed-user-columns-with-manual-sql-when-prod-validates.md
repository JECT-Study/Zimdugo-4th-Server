---
module: user
problem_type: database-issues
tags:
  - user
  - ddl-auto
  - validate
  - postgres
---

# Drop removed user columns with manual SQL when prod validates

## Context

The `users` table can keep removed legacy columns in production because the production profile uses `ddl-auto: validate`, not automatic schema mutation.

## Decision

When a field is fully removed from the backend domain and JPA entity, add an explicit SQL migration/init script for local startup and plan the same SQL for production rollout.

## Applied here

- removed `nickname` from the user domain/entity flow
- added `scripts/db/04-drop-user-nickname.sql`
- wired the script into `application-local.yaml`

## Guardrail

If production uses `validate`, entity changes alone are not enough for destructive schema updates such as column drops. Pair the code change with explicit SQL and communicate the required production execution before deployment.
