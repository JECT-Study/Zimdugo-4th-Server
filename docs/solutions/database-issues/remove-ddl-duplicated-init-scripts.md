---
module: common
problem_type: database-issues
tags:
  - jpa
  - ddl-auto
  - sql-init
  - postgres
---

# Remove DDL-duplicated init scripts

## Context

Local startup loaded `scripts/db` SQL files even for tables already modeled as JPA entities. That split schema ownership between Hibernate DDL and manual SQL.

## Decision

Keep only SQL init scripts that Hibernate `ddl-auto` cannot express safely, such as:

- extension installation
- trigger/function creation
- PostGIS geography column backfill
- vendor-specific procedural SQL

Remove scripts that only create ordinary tables, indexes, and unique constraints when those structures already exist in entity metadata.

## Applied here

- kept `scripts/db/01-postgis.sql`
- removed `scripts/db/02-visitor-logs.sql`
- removed `scripts/db/03-keyword-counts.sql`
- moved `visitor_logs.accessed_date` index definition into `VisitorLogEntity`
- removed deleted script references from `application-local.yaml`

## Guardrail

Before deleting an init script, verify that entity annotations still preserve every required schema detail. If a script adds an index or constraint missing from the entity, move that definition into JPA first.
