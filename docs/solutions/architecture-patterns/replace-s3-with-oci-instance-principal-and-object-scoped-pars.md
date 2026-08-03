---
title: Replace AWS S3 with OCI Object Storage using instance principals and object-scoped PARs
date: 2026-08-03
category: architecture-patterns
module: object-storage
problem_type: architecture_pattern
component: service_object
severity: high
applies_when:
  - "Replacing AWS S3 with OCI Object Storage in a Spring Boot application"
  - "Allowing browser uploads without distributing long-lived cloud credentials"
  - "Running the application on an OCI compute instance authorized through a dynamic group"
  - "Retiring a storage provider completely instead of maintaining compatibility paths"
related_components:
  - "authentication"
  - "development_workflow"
  - "tooling"
tags:
  - "oci-object-storage"
  - "instance-principal"
  - "objectwrite-par"
  - "spring-boot-4"
  - "storage-migration"
  - "url-validation"
  - "dependency-convergence"
  - "ci-guardrails"
---

# Replace AWS S3 with OCI Object Storage using instance principals and object-scoped PARs

## Context

The application replaced AWS S3 and CloudFront completely with native OCI Object Storage. This was not a compatibility migration: old S3 objects and database URLs were out of scope, and no dual-provider fallback remained.

The change crossed four trust boundaries at once:

- Browser uploads needed a short-lived write capability without application or cloud credentials.
- Server-side admin writes, deletes, and report reads needed OCI-native authentication.
- Public object URLs became inputs to later read and delete operations.
- Runtime dependencies, IAM, container configuration, CI, and production deployment all had to retire AWS together.

OCI's S3-compatible API was rejected because it would preserve AWS SDK concepts and require a long-lived Customer Secret Key. Proxying all uploads through the application was also a poor fit for a fixed 1 OCPU / 6 GB Ampere A1 instance.

## Guidance

### Separate server, browser, and public-read capabilities

Use the native OCI Java SDK behind provider-neutral application interfaces:

- Production server access uses Instance Principal authentication.
- Local storage testing may use an OCI config-file profile.
- Browser uploads use a short-lived PAR restricted to one generated object.
- Public reads use the bucket's canonical public object URL, without PAR material.

Keep OCI client construction lazy. Ordinary application startup and unrelated Spring tests should not require a local OCI profile or immediate metadata access. The production container must still be able to reach OCI instance metadata when the first storage operation initializes the client.

Preserve provider-neutral API fields such as `uploadUrl`, `fileUrl`, `key`, and `expiresAt`. An internal provider replacement should not force browser clients to learn OCI SDK concepts.

### Issue only exact-object ObjectWrite PARs

Bind every direct-upload PAR to the generated object name and a short expiry:

```java
CreatePreauthenticatedRequestDetails details =
    CreatePreauthenticatedRequestDetails.builder()
        .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite)
        .objectName(key)
        .timeExpires(Date.from(expiresAt))
        .build();
```

The PAR URL is the temporary credential. Browser PUT requests must not attach the application's bearer token, cookies, or other authorization headers to it.

An OCI ObjectWrite PAR does not cryptographically enforce the application's declared content length. Validate the declared size and content type before asking OCI to create the PAR, and prove invalid requests cause no OCI interaction. If server-enforced post-upload size is required, add a separate confirmation or validation protocol.

### Treat public URLs and object prefixes as authorization boundaries

Encode the complete object key as one path segment, including slashes inside the key. When resolving it back, parse the URI and require:

- HTTPS and the exact configured Object Storage host.
- No user info, custom port, query, or fragment.
- The exact namespace and bucket path.
- One decoding pass and a nonblank key.

Then enforce an operation-specific prefix before OCI access. A report reader may accept only `reports/`; a notice deleter may accept only its notice-image prefix. Host validation alone does not prevent one feature from deleting another feature's object.

Reject PAR-shaped URLs as object identifiers. Never store or log a PAR access URI where a canonical public URL is expected.

### Preserve stream ownership and compensation behavior

For server-side PUT, validate every file before uploading any file, open the multipart stream with try-with-resources, and delete already-uploaded objects if a later upload fails.

For OCI SDK 3.93.0 GET, `GetObjectResponse` is not `AutoCloseable`. Close the returned input stream explicitly:

```java
GetObjectResponse response = objectStorage.getObject(request);
try (InputStream inputStream = response.getInputStream()) {
    // validate metadata and consume the object
}
```

For best-effort cleanup deletes, resolve and validate the key before obtaining the OCI client. Invalid URLs and unexpected prefixes must never reach OCI.

### Sanitize provider failures at every logging boundary

Do not log raw PAR URLs, untrusted image URLs, object bodies, OCI response messages, or provider throwable causes. A local log statement can look safe while the raw `BmcException` remains attached to a 5xx application exception and is later printed by a global exception handler.

Log only bounded fields such as status code, service code, request ID, operation, bucket, generated key, content type, and size. Throw the stable application error without the raw provider exception as its cause.

Use a real marker-bearing `BmcException` in tests. Assert that the marker, endpoint, object key, response detail, throwable proxy, and propagated cause are absent from both adapter and global error paths.

### Inventory consumers before removing a provider

Provider-named files are not the complete migration surface. A notice-storage implementation was also consumed by a locker controller, so replacing the shared bean silently changed another workflow.

Before deleting AWS, inspect:

- Application ports and every Spring injection point.
- Controller constructors and context wiring.
- Runtime dependencies and packaged JAR contents.
- Main and test configuration.
- Environment examples, Compose, workflows, and active deployment docs.

Use focused Spring wiring tests for shared or renamed ports. Migrate hidden consumers explicitly before removing the old SDK.

### Keep the OCI HTTP runtime on one compatible major

Spring Boot 4 dependency management can select Jersey and Jakarta REST 4.x while OCI's Jersey 3 Apache connector remains on 3.0.8. Compilation and mocked Object Storage tests do not exercise that mixed runtime.

Pin the OCI-supported HTTP family to Jersey 3.0.8 and Jakarta REST 3.0.0, inspect the resolved `runtimeClasspath`, and construct and close a real OCI client with a credential-free synthetic authentication provider. Keep request behavior tests mocked separately.

### Remove AWS atomically and make CI fail closed

Remove the AWS BOM and SDK modules, S3 classes, properties, environment variables, Compose metadata overrides, workflow validation, and stale operational documentation in one migration.

The CI absence scan must not match its own workflow source and must distinguish scanner outcomes:

```bash
command -v rg >/dev/null 2>&1
aws_prefix='AWS_'
pattern="software\\.amazon\\.awssdk|${aws_prefix}S3_|${aws_prefix}CLOUDFRONT|storage\\.s3|S3(Client|Storage|Image|Presigned|Locker)"

scan_status=0
rg -n "$pattern" build.gradle.kts src/main src/test .env.example docker-compose.deploy.yml .github/workflows/ci-cd.yml \
  || scan_status=$?

case "$scan_status" in
  0) exit 1 ;;
  1) ;;
  *) exit "$scan_status" ;;
esac
```

Status 0 means forbidden references were found, status 1 means a clean scan, and every other status is a scanner failure. Execute the actual workflow block in a test with controlled statuses 0, 1, and 2.

### Use exact-instance and bucket-conditioned IAM

Match only the application instance in the dynamic group:

```text
ALL {instance.id = '<application-instance-ocid>'}
```

Grant object CRUD and PAR management only for the application bucket:

```text
Allow dynamic-group <group-name> to manage objects in tenancy where all {target.bucket.name = '<bucket-name>', any {request.permission = 'OBJECT_CREATE', request.permission = 'OBJECT_OVERWRITE', request.permission = 'OBJECT_READ', request.permission = 'OBJECT_DELETE'}}

Allow dynamic-group <group-name> to manage buckets in tenancy where all {target.bucket.name = '<bucket-name>', request.permission = 'PAR_MANAGE'}
```

`PAR_MANAGE` is a bucket permission separate from object CRUD. Do not add object-list, bucket-create, lifecycle, retention, compute, networking, or volume permissions.

### Guard server configuration and capacity

Before editing the production environment file, require a regular non-symlink file owned by the deployment user, create a dated mode-0600 backup, remove only the named AWS storage keys, add OCI's non-secret coordinates exactly once, and preserve every unrelated secret without printing it.

Treat the authorized instance shape as immutable. Verify the exact instance remains running at its approved CPU and memory before and after IAM, deployment, and smoke operations. An application storage migration does not authorize a resize, new volume, bucket replacement, or state-service restart.

## Why This Matters

A storage migration is not a dependency swap. Upload credentials, public URL trust, stream ownership, Spring wiring, provider exception contents, dependency resolution, IAM, CI shell semantics, and production capacity can each fail independently.

Executable boundaries caught issues that compilation and mocked happy paths missed:

- No-OCI-interaction tests for invalid sizes and URLs.
- Exact request capture for PAR, PUT, GET, and DELETE.
- Prefix-rejection and close-tracking stream tests.
- Marker-bearing log and cause sanitization tests.
- A real credential-free OCI client construction test.
- Spring context wiring tests for hidden consumers.
- Runtime and boot-JAR AWS absence checks.
- Controlled exit-status tests for the real CI guard.
- Read-before/read-after checks for IAM, environment mode, bucket contents, and instance shape.

## When to Apply

- Replacing an object-storage provider while preserving existing client API contracts.
- Using OCI direct browser uploads through PARs.
- Running OCI SDK clients under Spring Boot dependency management.
- Authenticating a containerized service with Instance Principal.
- Removing a provider completely across code, configuration, IAM, CI/CD, and deployment.

## Examples

Finish the migration with a disposable end-to-end smoke:

1. Deploy an immutable ARM64 application image and restart only the app.
2. Create a short-lived application JWT only in memory.
3. Ask the application for an upload URL and keep the PAR URL out of logs.
4. Verify OCI CORS allows the browser's PUT request.
5. PUT a tiny body with only its content type.
6. GET the canonical public URL and compare the body.
7. Delete the exact smoke key and require a later HEAD to return 404.
8. Unset token, PAR, response, and key variables.
9. Reconfirm application readiness and unchanged instance capacity.

Do not commit the smoke script or leave the smoke object behind.

## Related

- [Deploy OCI Ampere A1 with ARM64 images and capacity guardrails](../workflow-issues/deploy-oci-ampere-a1-with-arm64-images-and-capacity-guardrails.md)
- [Verify the resolved OCI HTTP client stack under Spring Boot dependency management](../test-failures/verify-oci-http-client-runtime-stack.md)
- [Close the OCI GetObject response input stream explicitly](close-oci-get-object-response-stream.md)
- [Accepted OCI Object Storage migration design](../../superpowers/specs/2026-08-03-oci-object-storage-design.md)
