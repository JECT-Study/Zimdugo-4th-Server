---
title: Verify the resolved OCI HTTP client stack under Spring Boot dependency management
date: 2026-08-03
category: test-failures
module: object-storage
problem_type: dependency_conflict
component: oci_java_sdk
severity: high
applies_when:
  - "Using the OCI Java SDK Jersey 3 HTTP client in a Spring Boot application"
  - "SDK boundary tests mock ObjectStorage and never construct the real HTTP client"
tags: [oci, object-storage, jersey, dependency-management, smoke-test]
---

# Verify the resolved OCI HTTP client stack under Spring Boot dependency management

## Context

Adding `oci-java-sdk-common-httpclient-jersey3` did not keep its entire HTTP stack on Jersey 3. Spring Boot dependency management selected Jersey client, common, media, and HK2 modules at 4.x plus Jakarta REST 4.x, while OCI still supplied the Apache connector at 3.0.8. Compilation and mocked `ObjectStorage` tests passed because they never initialized the SDK HTTP client.

## Guidance

Treat the resolved `runtimeClasspath`, not the declared OCI dependency name, as the compatibility boundary. Pin every module in the OCI-supported family so dependency management wins consistently:

- Jersey client, common, media JSON, entity filtering, HK2 integration, and Apache connector: `3.0.8`
- Jakarta REST API: `3.0.0`
- Verify the resolved HK2 implementation remains on its Jersey 3-compatible line.

After changing dependency management, inspect the complete graph and reject both 4.x selections and version override arrows:

```bash
./gradlew dependencies --configuration runtimeClasspath
```

Add a credential-free smoke test that constructs and closes the real `ObjectStorageClient`. A synthetic authentication provider annotated with `@AuthCachingPolicy(cacheKeyId = false, cachePrivateKey = false)` lets construction reach the HTTP layer without reading a key or contacting OCI. Make every credential getter throw so the test also proves construction stays credential-free.

Keep request behavior tests mocked at the OCI service boundary; the construction smoke test complements them by detecting classpath linkage and runtime initialization failures.

## Why This Matters

A provider such as `oci-java-sdk-common-httpclient-jersey3` can coexist with a different resolved Jersey major when a stronger platform manages its transitives. Mocked client tests hide this because no Jersey classes are initialized. Verifying both dependency convergence and real client construction catches two different failure modes before deployment.
