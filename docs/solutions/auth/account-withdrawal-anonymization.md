---
title: Anonymize personal data on withdrawal while allowing re-signup
date: 2026-06-24
module: auth
problem_type: design-decision
tags:
  - auth
  - withdrawal
  - soft-delete
  - social-login
---

# Context

Our withdrawal flow uses soft delete because user-linked historical data such as reports should remain available after account withdrawal.

# Decision

When a user withdraws:

- mark the user status as `DELETED`
- anonymize direct personal fields such as `email`, `nickname`, and `profileImageUrl`
- delete linked social account rows so the same social provider account can sign up again later
- delete stored refresh tokens

# Why

Only switching the status to `DELETED` leaves personal data visible in the database even though the service treats the account as withdrawn. Anonymization better matches user expectations while preserving historical domain data.

Removing social account links keeps the withdrawal flow compatible with the current "login equals signup" social auth model and allows clean re-registration with the same provider account.

# Implementation note

Keep anonymization inside the `User` domain model so the withdrawal service does not hand-edit individual fields. This keeps the rule reusable and reduces the chance of future partial-withdrawal bugs.
