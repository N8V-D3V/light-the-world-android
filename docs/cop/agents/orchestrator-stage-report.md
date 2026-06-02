# COP Report

## Work Completed

- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/appshell/AppShellOrchestrator.kt`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/dailychallenge/DailyServiceChallengeExperienceOrchestrator.kt`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/GivingMachineDonationExperienceOrchestrator.kt`
- `LighttheWorld/app/src/test/java/com/n8vd3v/lighttheworld/features/appshell/AppShellOrchestratorTest.kt`
- `LighttheWorld/app/src/test/java/com/n8vd3v/lighttheworld/features/dailychallenge/DailyServiceChallengeExperienceOrchestratorTest.kt`
- `LighttheWorld/app/src/test/java/com/n8vd3v/lighttheworld/features/donation/GivingMachineDonationExperienceOrchestratorTest.kt`

## Summary of Changes

- Added an app-shell orchestrator that hosts the daily challenge and donation experiences without locking a concrete top-level UX.
- Added a daily challenge orchestrator that coordinates challenge calendar, progress, reminder, and share protocols while keeping protocol-owned logic inside the stub modules.
- Added a donation orchestrator that coordinates catalog, cart, checkout, and receipt protocols, invokes receipt handling only after successful checkout, and preserves successful donation results when receipt delivery fails.
- Added orchestration-focused tests covering happy paths, sequencing, calendar-boundary failures, local-date/local-time reminder evaluation, catalog-boundary enforcement, and donation receipt failure preservation.

## Open Questions

- The current contracts define the December 1 through December 25 campaign window for challenge content, but they do not explicitly define reminder-evaluation behavior when the current local date falls outside that window.
- The current contracts and protocols do not define whether app-shell experience hosting should remember a last-active experience across launches or sessions.

## Ambiguities or Risks

- The app shell intentionally avoids defining concrete navigation or entry UX, so later validation should confirm that any UI layer built on top of it preserves that non-prescriptive boundary.
- The reminder protocol does not carry challenge content, so validation should confirm whether later stages need a separate contract-approved way to include challenge description text in delivered reminders.
- The checkout protocol returns a confirmation with receipt status before receipt handling runs; the donation orchestrator currently updates the confirmation's receipt status from receipt-handling output, which should be validated as the approved coordination behavior.

## Next Recommended Steps

- Run validation-stage review against the contracts, protocols, architecture plans, and new orchestration tests to confirm there is no boundary leakage.
- Decide and document out-of-campaign reminder behavior if reminders must be evaluated outside the December 1 through December 25 content window.
- Decide whether the app shell needs a contract-defined persistence rule for active experience selection before any UI implementation relies on one.
