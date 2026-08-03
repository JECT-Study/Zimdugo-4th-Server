# OCI Object Storage Migration Design

Date: 2026-08-03

Status: Accepted

## Context

The application currently uses the AWS SDK for S3 to issue direct-upload URLs, upload and delete admin notice images, and read stored images for metadata extraction. Deployment configuration also requires AWS S3 and CloudFront variables.

AWS S3 will no longer be used. OCI Object Storage replaces it for all future storage operations. Existing S3 objects and database URLs are explicitly out of scope: they will not be copied, rewritten, or retained for compatibility by this application change.

The target bucket already exists:

- Region: `ap-osaka-1`
- Namespace: `axuj36gr8lmm`
- Bucket: `zimdugo-bucket`
- OCID: `ocid1.bucket.oc1.ap-osaka-1.aaaaaaaaay34pvajfs7vw74econx3si3e6phunks6xf5x45vw4jogcbewrmq`
- Public access: object read without list

This migration must not resize the Ampere A1 instance, add block volumes, or create other billable infrastructure.

## Decision

Use the native OCI Java SDK with Instance Principal authentication. Use object-specific, short-lived Object Write pre-authenticated requests (PARs) for browser and mobile direct uploads.

The rejected alternatives are:

1. OCI's S3-compatible API. This minimizes code changes but preserves AWS SDK concepts and requires a long-lived Customer Secret Key.
2. Proxying every upload through the application. This gives the server complete control over each request but adds avoidable bandwidth, memory, and CPU load to the 1 OCPU / 6 GB instance.

## Scope

### Included

- Remove AWS SDK S3 dependencies, clients, presigners, configuration, environment variables, and provider-specific names.
- Add the OCI Object Storage SDK and production Instance Principal authentication.
- Preserve the existing client-facing direct-upload response shape so consumers do not need a storage-provider-specific API change.
- Store all new profile, report, and admin notice images in `zimdugo-bucket`.
- Read and delete new OCI objects through the native SDK.
- Generate OCI public object URLs for newly stored objects.
- Update automated tests, example configuration, Docker Compose, GitHub Actions deployment validation, and server deployment environment.
- Add narrowly scoped OCI IAM resources required by the existing A1 instance.

### Excluded

- Copying existing AWS S3 objects.
- Updating existing database image URLs.
- Reading or deleting legacy S3 URLs after this release.
- Running both AWS S3 and OCI Object Storage implementations.
- Changing instance shape, OCPU, memory, boot volume, or adding storage.

## Application Architecture

Provider-specific infrastructure will be renamed around OCI Object Storage while existing application/domain ports remain provider-neutral.

The storage configuration will use the `storage.oci` prefix and contain:

- region
- namespace
- bucket name
- public base URL, derived from or validated against the region, namespace, and bucket
- direct-upload expiration
- maximum accepted upload size
- authentication mode where needed for local development; production is fixed to Instance Principal

The public object base URL is:

`https://objectstorage.ap-osaka-1.oraclecloud.com/n/axuj36gr8lmm/b/zimdugo-bucket/o`

Object names continue to use generated, non-guessable keys and the existing logical prefixes. URL construction and reverse key resolution must percent-encode and decode object names safely rather than concatenate untrusted URLs.

### Direct upload flow

1. The client requests an upload URL using the current API.
2. The application validates the declared content type and content length using the existing limits.
3. The application creates an OCI PAR with `ObjectWrite`, scoped to exactly one generated object name and the configured short expiration.
4. The application returns the absolute PAR upload URL, final public object URL, and expiration time using the existing response contract.
5. The client uploads the object directly to OCI Object Storage.

OCI PARs do not cryptographically enforce the declared content length in the same way as the current S3 signed request. The API will continue rejecting oversized declared uploads before issuing a URL. Workflows that subsequently read the image retain their content and metadata validation. Introducing a new upload-confirmation protocol is outside this provider replacement.

### Server-side upload and delete flow

Admin notice uploads continue to validate file type, dimensions, and size before calling OCI `putObject`. Replaced notice images are deleted with OCI `deleteObject`. Empty images and cleanup failures retain the current application semantics, with provider-neutral log messages and OCI exceptions mapped at the infrastructure boundary.

### Image read and metadata flow

Report and image-dimension readers resolve only URLs belonging to the configured OCI public base URL, obtain the matching object with OCI `getObject`, and pass the response stream to the existing metadata extraction logic. Missing objects, unsupported content types, and OCI service failures preserve the current empty/fallback behavior where applicable.

Because legacy data compatibility is excluded, AWS and CloudFront URLs are not recognized by the new resolver.

## Authentication and IAM

Production uses `InstancePrincipalsAuthenticationDetailsProvider`; no OCI API key, Customer Secret Key, or AWS credential is stored in `.env` or GitHub Secrets.

OCI will receive only the non-billable IAM configuration necessary for this application:

- A dynamic group matching the exact existing compute instance OCID.
- A least-privilege policy scoped to `zimdugo-bucket` that permits required object read, create, overwrite, and delete operations plus PAR management.

The policy must not grant tenancy-wide compute, networking, volume, or bucket-creation permissions. The existing bucket's public visibility will not be broadened or otherwise changed.

## Configuration and CI/CD

AWS variables, including access key, secret key, S3 bucket, region, and CloudFront public base URL, are removed from application configuration, `.env.example`, Compose, and deployment validation.

OCI region, namespace, bucket name, and public base URL are non-secret deployment values. GitHub Actions continues deploying to the same instance over the existing path and must validate the new required values before starting the containers. Storage authentication happens on the instance through Instance Principal, not in GitHub Actions.

No disposable migration or diagnostic scripts are committed. Any object created solely for live verification is deleted immediately after verification.

## Error Handling and Observability

- Reject invalid upload declarations before requesting a PAR.
- Treat OCI 404 responses as missing objects using current fallback behavior.
- Preserve failure causes in server logs without logging PAR URLs, credentials, or full sensitive request data.
- Include bucket, object key, content type, and declared size in structured failure context where safe.
- Fail application startup when required OCI configuration is absent or internally inconsistent.

## Verification

Automated tests will cover:

- OCI configuration validation and public URL/key resolution.
- Single-object PAR creation, access URL expansion, and expiration.
- Upload declaration size and content-type validation.
- OCI put, get, and delete request construction and error mapping.
- Admin notice image workflows and report metadata extraction using mocked OCI clients.
- Absence of AWS SDK dependencies and AWS configuration references in production code and deployment files.

Release verification will include:

1. Full Gradle test and build execution.
2. GitHub Actions validation on the feature branch.
3. Deployment to the unchanged A1 instance.
4. Readiness/health checks.
5. One small OCI upload/read/delete smoke test whose object is removed afterward.
6. Confirmation that the instance remains `VM.Standard.A1.Flex` with 1 OCPU and 6 GB memory.

## Rollback

Application rollback uses the prior container image and prior environment file. IAM additions can remain harmlessly during an application rollback or be removed after rollback is confirmed. Rollback does not migrate or restore object data, and it does not authorize infrastructure resizing.
