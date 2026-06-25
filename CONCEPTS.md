# Concepts

Shared domain vocabulary for this project — entities, named processes, and status concepts with project-specific meaning. Seeded with core domain vocabulary, then accretes as ce-compound and ce-compound-refresh process learnings; direct edits are fine. Glossary only, not a spec or catch-all.

## Locker Reports

### Place
A named real-world venue or area that groups one or more Lockers and owns the localized identity users use to recognize that destination.

### Locker
An operational storage installation at a Place, named with the more specific location detail needed to distinguish it from other Lockers at the same destination.

### Locker Report
A user-submitted claim that a locker exists at a location, kept separate from operational place and locker records until an administrator reviews it.

### Translation Readiness
The condition in which the Place and Locker created or selected from a Locker Report both have every required localized representation and are eligible for final approval.

Locker registration precedes Translation Readiness. Reaching it moves the report to Ready for Approval but does not itself make the Locker operational.

### Ready for Approval
The Locker Report state reached after Translation Readiness, where translations may still be corrected while an administrator's final approval remains pending.

Final Approval records operational application and permits search indexing only after Translation Readiness is revalidated.

## Admin Operations

### Today Visitor
A distinct browser identifier that accessed a public API during the current calendar day in the service's operating timezone, regardless of authentication state.

Today Visitor is an operational traffic metric rather than a count of unique Users; multiple browsers used by one User count separately.
