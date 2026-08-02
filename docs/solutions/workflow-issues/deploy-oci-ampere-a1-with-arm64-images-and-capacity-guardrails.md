---
title: Deploy OCI Ampere A1 with ARM64 images and capacity guardrails
date: 2026-08-02
category: workflow-issues
module: deployment
problem_type: workflow_issue
component: development_workflow
severity: high
applies_when:
  - "Deploying a containerized application from scratch to an OCI Ampere A1 ARM64 instance"
  - "Running PostgreSQL and PostGIS inside Docker without a suitable upstream ARM64 image"
  - "Operating Docker Compose within an explicitly fixed compute budget"
  - "Using a self-hosted GitHub Actions runner for production deployment"
related_components:
  - database
  - tooling
tags:
  - oci
  - ampere-a1
  - arm64
  - docker-compose
  - postgis
  - github-actions
  - self-hosted-runner
  - deployment-safety
---

# Deploy OCI Ampere A1 with ARM64 images and capacity guardrails

## Context

The deployment target is an OCI Ampere A1 instance whose authorized shape is fixed at 1 OCPU and 6 GB RAM. Capacity changes are a separate infrastructure decision: apparent free-tier headroom or deployment pressure does not authorize a resize.

The application needs PostgreSQL with PostGIS, Redis, Elasticsearch, Nginx, and a Spring Boot application image. The upstream `postgis/postgis` image used by local tests is amd64-only for the required version, so a direct production reference is unsuitable on A1. A fresh database also needs schema creation once because the production application normally runs with `ddl-auto=validate`.

## Guidance

Treat the requested instance shape as an immutable deployment constraint. Confirm it with read-only OCI commands before starting, and tune the workload within that budget. Do not call `oci compute instance update` or add storage unless the user explicitly authorizes that separate change.

Build PostGIS for ARM64 from the official `docker-postgis` recipe instead of choosing an unverified third-party image. Pin the PostgreSQL base digest and PostGIS package version, and preserve the official script modes:

```dockerfile
FROM docker.io/postgres:16-bullseye@sha256:<verified-digest>

ENV POSTGIS_MAJOR=3
ENV POSTGIS_VERSION=3.5.2+dfsg-1.pgdg110+1

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
         postgresql-$PG_MAJOR-postgis-$POSTGIS_MAJOR=$POSTGIS_VERSION \
         postgresql-$PG_MAJOR-postgis-$POSTGIS_MAJOR-scripts \
    && rm -rf /var/lib/apt/lists/*

COPY ./initdb-postgis.sh /docker-entrypoint-initdb.d/10_postgis.sh
COPY ./update-postgis.sh /usr/local/bin
```

`initdb-postgis.sh` must stay mode `0644`. PostgreSQL sources a non-executable init script into the entrypoint shell, where the official script can use the entrypoint-local `psql` array. Making it executable runs it in a child process and fails with `--dbname=template_postgis: command not found`. The standalone update script remains executable.

Validate the actual image architecture and initialize an empty database, not merely a reused volume:

```bash
docker build --platform linux/arm64 -t zimdugo-postgis:arm64-test docker/postgres/postgis
docker image inspect zimdugo-postgis:arm64-test --format '{{.Architecture}}'
```

The empty-volume smoke test must verify the PostGIS extension and project-specific function/event-trigger setup. Init scripts do not run again once `PGDATA` exists, so a reused volume can hide broken script permissions.

Keep ordinary entity DDL under Hibernate ownership and vendor-specific PostGIS extensions, functions, triggers, and indexes in the database init SQL. For the one-time Hibernate bootstrap, write a dedicated completion marker only after the update-mode application logs a successful startup. If the job is interrupted before then, the missing marker causes the idempotent bootstrap to run again.

```bash
cleanup_bootstrap() {
  docker rm -f zimdugo-schema-bootstrap >/dev/null 2>&1 || true
}
trap cleanup_bootstrap EXIT INT TERM

# Start update-mode application and wait for "Started ZimdugoApplication".
# Only then create public.zimdugo_schema_bootstrap_v1.
```

Production deployment requires these release guards:

- Require a pre-provisioned, non-symlink `.env` owned by the runner user with mode `0600`, and validate required non-empty values before changing services.
- Allow the production self-hosted runner only on a push to `main`; pull requests may build ARM64 images but must not deploy.
- Give image-building jobs package write permission and deployment jobs package read permission.
- Capture the previous immutable application tag before updating `.env`. If new readiness fails, restore the previous tag, verify rollback readiness, and still fail the workflow.
- Use immutable content-derived tags for state-service images so ordinary application commits do not restart PostgreSQL or Elasticsearch.
- Bound container memory and heap sizes rather than resizing the A1 instance.

## Why This Matters

ARM64 incompatibility often appears only when the target pulls or starts an image. Reusing the official PostGIS recipe preserves expected initialization behavior while making the architecture reviewable and testable.

Completion markers and rollback address different partial-failure windows. A marker written too early can permanently skip incomplete DDL; a release without rollback can replace a healthy application with one that never becomes ready. Main-only deployment also prevents an arbitrary branch from executing repository-controlled deployment files on a production runner with Docker and sudo access.

The capacity guardrail is an authorization boundary, not a tuning suggestion. Deployment work may change software and configuration within the chosen instance, but it must not silently broaden into infrastructure expansion.

## When to Apply

- A production target is ARM64 while a required upstream image is amd64-only.
- PostgreSQL init scripts depend on entrypoint-local shell state.
- A fresh database needs one-time schema creation before normal validation mode.
- GitHub Actions deploys through a persistent production runner.
- CPU, memory, or storage capacity is explicitly fixed by the user.

Repeat the empty-volume test whenever the PostgreSQL digest, PostGIS package, or init scripts change. Repeat rollback validation whenever deployment orchestration changes.

## Examples

Avoid deploying the amd64-only upstream image directly on A1:

```yaml
postgres:
  image: postgis/postgis:16-3.5
```

Use the repository-owned ARM64 image and explicit memory limits:

```yaml
postgres:
  image: ghcr.io/${GHCR_OWNER}/zimdugo-postgis:${POSTGIS_IMAGE_TAG}
  mem_limit: 1g

elasticsearch:
  mem_limit: 1g
  environment:
    ES_JAVA_OPTS: "-Xms512m -Xmx512m"

app:
  mem_limit: 1280m
  environment:
    JAVA_TOOL_OPTIONS: "-Xms256m -Xmx768m"
```

An IP-based HTTP readiness check proves initial reachability, not full production readiness. DNS and TLS remain prerequisites for secure cookies and OAuth redirects, and real AWS/S3/CloudFront and OAuth credentials must replace placeholders before those features work.

## Related

- [Remove duplicated DDL from local startup SQL](../database-issues/remove-ddl-duplicated-init-scripts.md)
- [Align Nginx and multipart request limits](../runtime-errors/align-nginx-and-multipart-request-limits.md)
- [Use PostGIS geography for distance](../best-practices/use-postgis-geography-for-distance.md)
