# Architecture Plan: Light the World Android System

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-service-challenge-experience.md`
- `docs/cop/contracts/giving-machine-donation-experience.md`
- `docs/cop/protocols/challenge-calendar-protocol.md`
- `docs/cop/protocols/challenge-progress-protocol.md`
- `docs/cop/protocols/challenge-reminder-protocol.md`
- `docs/cop/protocols/challenge-share-protocol.md`
- `docs/cop/protocols/donation-catalog-protocol.md`
- `docs/cop/protocols/donation-cart-protocol.md`
- `docs/cop/protocols/donation-checkout-protocol.md`
- `docs/cop/protocols/donation-receipt-protocol.md`

---

## 1. Purpose
Describe how the approved daily challenge and donation contracts will be translated into one Android app structure while preserving strict behavioral separation between the two feature experiences.

This plan keeps feature boundaries aligned to contracts, keeps external integrations abstract behind protocol-owning modules, and preserves a clean path from bundled local JSON challenge content now to a future authoritative challenge source later without changing challenge feature behavior.

---

## 2. Source Artifacts
- Contract: `docs/cop/contracts/daily-service-challenge-experience.md`
- Contract: `docs/cop/contracts/giving-machine-donation-experience.md`
- Protocols:
  - `docs/cop/protocols/challenge-calendar-protocol.md`
  - `docs/cop/protocols/challenge-progress-protocol.md`
  - `docs/cop/protocols/challenge-reminder-protocol.md`
  - `docs/cop/protocols/challenge-share-protocol.md`
  - `docs/cop/protocols/donation-catalog-protocol.md`
  - `docs/cop/protocols/donation-cart-protocol.md`
  - `docs/cop/protocols/donation-checkout-protocol.md`
  - `docs/cop/protocols/donation-receipt-protocol.md`

---

## 3. Module Breakdown
- `AppShellExperienceModule` - hosts app entry and app-level navigation while keeping the daily challenge experience and the Giving Machine donation experience behaviorally separate.
- `DailyServiceChallengeExperienceModule` - feature boundary for browsing challenge cards, viewing details, evaluating completion state, requesting reminders, and requesting sharing.
- `ChallengeCalendarContentModule` - fulfills challenge calendar retrieval and detail access through `ChallengeCalendarProtocol`; owns the authoritative challenge content source boundary and can start with bundled local JSON content.
- `ChallengeProgressModule` - fulfills `ChallengeProgressProtocol` and owns user-specific completion eligibility, completion state, and completion reversal behavior.
- `ChallengeReminderModule` - fulfills `ChallengeReminderProtocol` and owns reminder scheduling decisions using reminder preference, permission state, campaign window, current local date and time, validated current-day challenge content, and current-day completion state.
- `ChallengeShareModule` - fulfills `ChallengeShareProtocol` and owns share payload creation for completed challenges.
- `GivingMachineDonationExperienceModule` - feature boundary for donation option browsing, cart management, checkout submission, and receipt delivery results.
- `DonationCatalogModule` - fulfills `DonationCatalogProtocol` and owns donation option list and detail retrieval.
- `DonationCartModule` - fulfills `DonationCartProtocol` and owns multi-item donation cart state and cart summary generation.
- `DonationCheckoutModule` - fulfills `DonationCheckoutProtocol` and owns checkout validation, payment submission, and donation confirmation generation.
- `DonationReceiptModule` - fulfills `DonationReceiptProtocol` and owns receipt content and requested receipt delivery state.

---

## 4. Protocol-to-Module Map
- `ChallengeCalendarProtocol` -> `ChallengeCalendarContentModule`
- `ChallengeProgressProtocol` -> `ChallengeProgressModule`
- `ChallengeReminderProtocol` -> `ChallengeReminderModule`
- `ChallengeShareProtocol` -> `ChallengeShareModule`
- `DonationCatalogProtocol` -> `DonationCatalogModule`
- `DonationCartProtocol` -> `DonationCartModule`
- `DonationCheckoutProtocol` -> `DonationCheckoutModule`
- `DonationReceiptProtocol` -> `DonationReceiptModule`

---

## 5. Dependencies
- `AppShellExperienceModule` depends on `DailyServiceChallengeExperienceModule` and `GivingMachineDonationExperienceModule` only at the experience-entry boundary.
- `DailyServiceChallengeExperienceModule` depends on `ChallengeCalendarProtocol`, `ChallengeProgressProtocol`, `ChallengeReminderProtocol`, and `ChallengeShareProtocol`.
- `ChallengeReminderModule` depends on current-day completion state and validated current-day challenge content provided through the daily challenge experience flow and must not retrieve challenge content directly.
- `ChallengeShareModule` depends on completed challenge state and challenge summary provided through the daily challenge experience flow and must not depend on reminder behavior.
- `GivingMachineDonationExperienceModule` depends on `DonationCatalogProtocol`, `DonationCartProtocol`, `DonationCheckoutProtocol`, and `DonationReceiptProtocol`.
- `DonationCheckoutModule` depends on the cart summary and checkout inputs only and must not depend on donation catalog retrieval once checkout begins.
- `DonationReceiptModule` depends on a successful donation confirmation and requested receipt delivery inputs only.
- Challenge modules must not depend on donation protocols.
- Donation modules must not depend on challenge protocols.

---

## 6. Orchestration Boundaries
- `AppShellOrchestrator` coordinates app entry and app-level navigation around the two feature experiences.
- `AppShellOrchestrator` must not embed challenge logic, donation logic, or cross-feature state mutation beyond experience hosting and navigation coordination.
- `DailyServiceChallengeExperienceOrchestrator` coordinates `ChallengeCalendarProtocol`, `ChallengeProgressProtocol`, `ChallengeReminderProtocol`, and `ChallengeShareProtocol`.
- `DailyServiceChallengeExperienceOrchestrator` must not embed content-source logic, progress persistence logic, notification delivery logic, or share formatting logic beyond protocol coordination.
- `DailyServiceChallengeExperienceOrchestrator` must validate the current local date against the campaign window before reminder evaluation, and it must obtain current-day challenge content through `ChallengeCalendarProtocol` before invoking `ChallengeReminderProtocol` for any in-window reminder scheduling decision.
- `GivingMachineDonationExperienceOrchestrator` coordinates `DonationCatalogProtocol`, `DonationCartProtocol`, `DonationCheckoutProtocol`, and `DonationReceiptProtocol`.
- `GivingMachineDonationExperienceOrchestrator` must not embed catalog sourcing, cart mutation rules, payment-processing logic, or receipt-delivery implementation logic beyond protocol coordination.
- `GivingMachineDonationExperienceOrchestrator` must treat cart update and removal requests targeting items not present in the current cart as explicit `DONATION_SELECTION_INVALID` failures rather than successful no-op outcomes.
- `GivingMachineDonationExperienceOrchestrator` must invoke receipt delivery only after checkout returns a successful donation confirmation.
- Each feature orchestrator must stop or continue only according to contract-defined success and failure behavior.

---

## 7. Data Flow
1. The user enters `AppShellExperienceModule`, which hosts app-level navigation around the daily challenge experience and the Giving Machine donation experience without prescribing a specific entry UX in this architecture artifact.
2. In the daily challenge path, campaign window and selected challenge date enter `ChallengeCalendarProtocol` to return ordered challenge cards or a challenge detail response.
3. Current local date and challenge date enter `ChallengeProgressProtocol` to return completion eligibility and completion state, and completion or reversal requests return updated progress state or explicit failure responses.
4. For reminder evaluation, the daily challenge experience first checks whether the current local date is inside the campaign window.
5. When the current local date is outside the campaign window, reminder preference, notification permission, campaign window, current local date, current local time, no current-day challenge content, and current-day completion state enter `ChallengeReminderProtocol` to return both reminders as `not_scheduled`.
6. When the current local date is inside the campaign window, the experience must validate current-day challenge content through `ChallengeCalendarProtocol` before entering `ChallengeReminderProtocol`.
7. Reminder preference, notification permission, campaign window, current local date, current local time, validated current-day challenge content, and current-day completion state enter `ChallengeReminderProtocol` to return reminder scheduling or suppression state.
8. Completed challenge date, completed challenge summary, completion state, and app link enter `ChallengeShareProtocol` to return a share payload or an explicit failure response.
9. In the donation path, donation option requests enter `DonationCatalogProtocol` to return currently available options or selected option detail.
10. Cart update requests and donation selections enter `DonationCartProtocol` to return updated cart contents and a current donation selection summary, or an explicit `DONATION_SELECTION_INVALID` failure when an update or removal targets an item not present in the current cart.
11. Donation cart contents, donor identity, payment details, receipt delivery selection, and receipt delivery contact information enter `DonationCheckoutProtocol` to return either an explicit failed payment result or a successful donation confirmation.
12. Successful donation confirmation and receipt delivery inputs enter `DonationReceiptProtocol` to return receipt delivery state while preserving a successful donation even if receipt delivery fails.

---

## 8. Testing Strategy
- Contract alignment tests:
  - Verify each module and orchestrator responsibility maps to approved contract and protocol behavior only.
  - Verify the two feature experiences remain behaviorally separate inside one app shell.
- Protocol conformance tests:
  - Verify each protocol-owning module returns only contract-defined outputs and failure reasons.
  - Verify challenge calendar behavior remains unchanged when the authoritative challenge source changes behind `ChallengeCalendarProtocol`.
- Orchestration flow tests:
  - Verify the daily challenge flow coordinates content, progress, reminders, and sharing without bypassing protocols.
  - Verify the donation flow coordinates catalog, cart, checkout, and receipt handling in the approved sequence.
- Failure path tests:
  - Verify missing challenge content, early completion requests, share-before-complete requests, reminder-permission failures, unavailable catalog responses, empty-cart checkout, payment failures, unresolved payment confirmation, and receipt-delivery failure handling.
  - Verify reminder evaluation returns both reminders as `not_scheduled` outside the campaign window, validates current-day content before in-window scheduling, and reflects the time-dependent `10:00 AM` and `6:00 PM` schedule-state rules.
  - Verify cart update and removal requests for items not present in the current cart return explicit `DONATION_SELECTION_INVALID` failures.

---

## 9. Architectural Risks and Guardrails
- Risk: one shared app shell could blur the contract boundary between the daily challenge and donation experiences.
  - Guardrail: keep separate feature orchestrators and prohibit direct protocol dependencies across the two feature boundaries.
- Risk: bundled local JSON challenge content could become coupled to challenge feature behavior.
  - Guardrail: keep the content-source decision entirely inside `ChallengeCalendarContentModule` so `ChallengeCalendarProtocol` stays unchanged when the authoritative source changes later.
- Risk: local date and local time evaluation could drift between challenge completion and reminder scheduling.
  - Guardrail: require the daily challenge experience flow to pass explicit current local date and current local time inputs into the relevant protocols for each evaluation.
- Risk: reminder scheduling could bypass the challenge-calendar boundary and schedule reminders for unvalidated or out-of-window dates.
  - Guardrail: require the daily challenge experience flow to gate reminder evaluation by campaign-window status first and by validated current-day challenge content second before invoking `ChallengeReminderProtocol`.
- Risk: donation cart mutations could hide stale UI state or orchestration defects by treating missing update or removal targets as successful no-op outcomes.
  - Guardrail: require `DonationCartProtocol` and the donation orchestrator to surface missing update or removal targets as explicit `DONATION_SELECTION_INVALID` failures.
- Risk: checkout completion and receipt delivery could become conflated and misreport donation success.
  - Guardrail: keep `DonationCheckoutModule` responsible for donation success only and keep `DonationReceiptModule` responsible for receipt delivery status only.
- Risk: payment, notifications, sharing, and content retrieval could leak platform details into feature orchestration.
  - Guardrail: keep those implementation details inside protocol-owning modules and expose only contract-defined inputs, outputs, and failure responses.
- Risk: architecture artifacts could accidentally lock a specific app-entry or navigation UX before product behavior defines it.
  - Guardrail: keep app-shell language limited to hosting and separating feature experiences, and treat any concrete top-level navigation design as a later product decision unless a contract defines it.

---

## 10. Open Questions
- What final campaign policy governs post-donation support handling after a successful donation?
- What final tax-document requirements apply to completed donations, if any?
