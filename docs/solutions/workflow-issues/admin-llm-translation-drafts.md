---
title: Keep LLM translation output as admin-reviewed drafts
date: 2026-06-19
category: docs/solutions/workflow-issues
module: admin translation
problem_type: workflow_issue
component: admin_translation
severity: medium
applies_when:
  - "Low-cost or free LLM APIs are introduced for production content translation"
  - "User-submitted reports need multilingual content before being exposed in the app"
tags: [llm, translation, admin, gemini, i18n]
---

# Keep LLM translation output as admin-reviewed drafts

## Context

Locker reports and admin documents need multilingual content before they can be safely applied to production screens. A free or low-cost LLM API is attractive for cost reasons, but using model output directly as app content would make translations nondeterministic and difficult to audit.

## Guidance

Treat the LLM as a draft generator only. The production read path should continue to use persisted i18n tables such as `place_translations`, `locker_translations`, alias tables, and admin document translation tables. Admin screens can call Gemini to produce a structured draft, but the draft must be reviewed and explicitly saved through the existing admin i18n replacement flow before it affects users.

Keep the integration behind a narrow interface:

```java
public interface LockerReportTranslationDraftGenerator {

    AdminTranslationDraftResult generate(LockerReportTranslationSource source);
}
```

This keeps the rest of the admin workflow independent from Gemini-specific request and response shapes. If free-tier limits, quality, or privacy constraints change, the generator implementation can be replaced without changing the translation review page.

## Why This Matters

LLM output can vary by model, prompt, provider, and rate-limit fallback. Persisted admin-approved translations give search indexing, caching, support investigation, and production rollback a stable source of truth. This is especially important for place names, addresses, and aliases because small wording changes can affect discoverability.

## When to Apply

- Admins use an LLM to translate user-submitted place or locker content.
- Generated aliases affect search or autocomplete behavior.
- Free-tier LLM terms allow experimentation but are not a durable production contract.
- Model output must be traceable before users see it.

## Examples

Preferred flow:

```text
Report or document source
-> Gemini draft
-> Admin review
-> Existing i18n save API
-> Search index sync after commit
```

For admin review screens, keep the draft and persisted form side-by-side. The draft apply action should copy values into the editable form only; it must not persist data by itself. Add an unsaved-change warning because applying drafts creates local edits that can be lost by navigation or draft regeneration.

For admin pages that use `fetch` for state changes, handle expired sessions explicitly. Spring form login can return the login page as a followed redirect, which may look like a successful HTML response to JavaScript. Check `response.redirected`, the final response URL, and HTML content type before normal success handling, then move the browser to `/admin/login`.

For list ordering, prefer direct manipulation only when the target order is obvious. If using native drag and drop, remove separate ordering columns, prevent drag starts from interactive controls, and show an insertion marker by expanding the target row boundary before saving the new ID order.

Avoid:

```text
Report or document source
-> Gemini response
-> App response or search document
```

## Related

- `src/main/java/com/zimdugo/admin/translation/LockerReportTranslationDraftGenerator.java`
- `src/main/java/com/zimdugo/admin/i18n/LockerContentI18nAdminService.java`
- `docs/solutions/workflow-issues/compound-review-after-verification.md`
