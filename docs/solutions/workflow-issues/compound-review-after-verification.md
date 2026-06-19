---
title: Run compound review after verified implementation
date: 2026-06-19
category: docs/solutions/workflow-issues
module: development workflow
problem_type: workflow_issue
component: development_workflow
severity: medium
applies_when:
  - "Non-trivial implementation work reaches the final verification or review step"
  - "Execution revealed process mistakes, failed assumptions, checkstyle failures, or plan gaps"
tags: [compound, verification, review, workflow]
---

# Run compound review after verified implementation

## Context

During the operational logging change, the implementation passed focused tests but failed `checkstyleMain` because newly introduced logging code used magic numbers and made `toggleVote` exceed the method length limit. The issue was fixed quickly, but without a durable note the same execution mistake could recur in later logging or service changes.

The user also asked to install Compound Engineering skills and add the compound step to the final Superpowers review stage so execution-review lessons are captured instead of rediscovered.

## Guidance

For non-trivial work, treat the final review as incomplete until a compound pass has been considered after fresh verification. The sequence should be:

1. Run the planned verification commands.
2. Read the output and fix any failures.
3. Review the diff for process mistakes or plan gaps that caused rework.
4. Run a compound documentation pass for reusable lessons.
5. Add or update `docs/solutions/` and make the learning discoverable from project instructions.

For this repo, the relevant commands were:

```bash
./gradlew checkstyleMain checkstyleTest
./gradlew test
rg -n "System\\.out|\\[DEBUG\\]" src/main/java src/test/java
```

The concrete prevention from this run:

```java
private static final int SERVER_ERROR_STATUS_THRESHOLD = 500;
private static final long NANOS_PER_MILLISECOND = 1_000_000L;
```

Use named constants for logging thresholds and duration conversion. If adding logs to an already dense method, check method length before finishing; extracting a small result method can keep behavior and logs readable.

## Why This Matters

The value of the final review is not only proving the current diff works. It should also reduce the chance that the next similar change repeats the same avoidable failure. Capturing the lesson in `docs/solutions/` turns a one-off correction into searchable project knowledge.

## When to Apply

- Logging, validation, security, or service changes touch multiple files.
- A verification step fails for a style, workflow, or assumption reason rather than product behavior.
- The implementation plan missed a local constraint such as checkstyle limits, test naming, or profile-specific configuration.
- A new tool or skill is installed to support the workflow.

## Examples

Before finalizing a task after tests pass:

```text
Verification passed, but check whether the run taught a reusable lesson.
If yes, write or update a docs/solutions entry before the final handoff.
```

After a checkstyle failure:

```text
Record the rule and prevention, not just the code fix:
- MagicNumber requires named constants for operational thresholds.
- MethodLength can be triggered by adding logging to service methods.
```

## Related

- `docs/superpowers/plans/2026-06-19-operational-logging.md`
