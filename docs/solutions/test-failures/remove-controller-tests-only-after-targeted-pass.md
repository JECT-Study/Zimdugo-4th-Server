---
module: test
problem_type: test-failures
tags:
  - controller-test
  - cleanup
  - verification
---

# Remove controller tests only after targeted pass

## Context

When deleting a broad class of tests, first verify that the exact target set currently passes. Otherwise cleanup can hide unrelated failures behind a large diff.

## Applied here

- enumerated `*Controller*Test` and `*ControllerTest`
- ran only those tests first
- removed them after a successful targeted run
- rechecked test-source compilation after deletion

## Guardrail

For future bulk test cleanup, identify the exact files, run them in isolation, then delete and verify compilation again.
