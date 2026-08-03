---
title: Close the OCI GetObject response input stream explicitly
date: 2026-08-03
category: architecture-patterns
module: object-storage
problem_type: resource_lifecycle
component: oci_java_sdk
severity: high
applies_when:
  - "Reading an OCI Object Storage object with GetObject"
  - "Migrating code from AWS ResponseInputStream to OCI GetObjectResponse"
tags: [oci, object-storage, get-object, input-stream, resource-lifecycle]
---

# Close the OCI GetObject response input stream explicitly

## Context

OCI Java SDK 3.93.0 returns a `GetObjectResponse` that is not `AutoCloseable`.
Unlike AWS `ResponseInputStream`, closing the response is therefore not available as
the resource-management boundary. Leaving `response.getInputStream()` open can
exhaust HTTP connections during image or document reads.

## Guidance

Build and validate the request key before invoking OCI. Once `getObject` returns,
close exactly the returned stream with try-with-resources:

```java
GetObjectResponse response = objectStorage.getObject(request);
try (InputStream inputStream = response.getInputStream()) {
    // validate response metadata and consume inputStream
}
```

Test this with a close-tracking stream, and separately verify malformed or
wrong-bucket public URLs fail before the OCI client is requested. Do not log
untrusted image URLs, PAR tokens, or response bodies when converting OCI failures
to application exceptions.

## Why This Matters

The SDK response type can make an AWS-to-OCI migration look mechanically similar
while silently changing resource ownership. An explicit stream-close test protects
connection reuse and makes the ownership rule visible at the call site.
