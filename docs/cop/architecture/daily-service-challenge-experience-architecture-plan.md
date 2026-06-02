# Architecture Plan: Daily Service Challenge Experience

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-service-challenge-experience.md`
- `docs/cop/protocols/challenge-calendar-protocol.md`
- `docs/cop/protocols/challenge-progress-protocol.md`
- `docs/cop/protocols/challenge-reminder-protocol.md`
- `docs/cop/protocols/challenge-share-protocol.md`

---

## 1. Purpose
Describe how the approved daily challenge contract and supporting protocols will be translated into a feature-based implementation boundary for challenge browsing, detail viewing, eligible completion, completion reversal, optional reminders, and completed-challenge sharing.

This plan keeps challenge content source replacement isolated behind the calendar capability so bundled local JSON can satisfy the contract now without changing feature behavior when a future authoritative source is introduced later.

---

## 2. Source Artifacts
- Contract: `docs/cop/contracts/daily-service-challenge-experience.md`
- Protocols:
  - `docs/cop/protocols/challenge-calendar-protocol.md`
  - `docs/cop/protocols/challenge-progress-protocol.md`
  - `docs/cop/protocols/challenge-reminder-protocol.md`
  - `docs/cop/protocols/challenge-share-protocol.md`

---

## 3. Module Breakdown
- `DailyServiceChallengeExperienceModule` - feature boundary for user-visible challenge card browsing, detail access, completion actions, reminder preference actions, and share actions.
- `ChallengeCalendarContentModule` - fulfills `ChallengeCalendarProtocol` and owns ordered challenge card retrieval and challenge detail retrieval for the December 1 through December 25 campaign window.
- `ChallengeProgressModule` - fulfills `ChallengeProgressProtocol` and owns user-specific completion eligibility, completion state, completion updates, and completion reversal prompt handling.
- `ChallengeReminderModule` - fulfills `ChallengeReminderProtocol` and owns reminder scheduling and suppression decisions using current local date, current local time, campaign window, validated current-day challenge content, permission state, reminder preference, and current-day completion state.
- `ChallengeShareModule` - fulfills `ChallengeShareProtocol` and owns completed-challenge share payload generation for the standard device share flow.

---

## 4. Protocol-to-Module Map
- `ChallengeCalendarProtocol` -> `ChallengeCalendarContentModule`
- `ChallengeProgressProtocol` -> `ChallengeProgressModule`
- `ChallengeReminderProtocol` -> `ChallengeReminderModule`
- `ChallengeShareProtocol` -> `ChallengeShareModule`

---

## 5. Dependencies
- `DailyServiceChallengeExperienceModule` depends on `ChallengeCalendarProtocol`, `ChallengeProgressProtocol`, `ChallengeReminderProtocol`, and `ChallengeShareProtocol`.
- `ChallengeCalendarContentModule` must not depend on challenge progress, reminders, or sharing behavior.
- `ChallengeProgressModule` depends on challenge date and current local date inputs only and must not depend on how challenge content is stored or retrieved.
- `ChallengeReminderModule` depends on reminder preference, notification permission, campaign window, current local date, current local time, validated current-day challenge content, and current-day completion state supplied through the experience flow.
- `ChallengeShareModule` depends on completed challenge summary, completion state, challenge date, and app link supplied through the experience flow.

---

## 6. Orchestration Boundaries
- `DailyServiceChallengeExperienceOrchestrator` coordinates `ChallengeCalendarProtocol`, `ChallengeProgressProtocol`, `ChallengeReminderProtocol`, and `ChallengeShareProtocol`.
- `DailyServiceChallengeExperienceOrchestrator` must combine challenge content and progress state only to support contract-defined experience behavior.
- `DailyServiceChallengeExperienceOrchestrator` must not embed challenge source parsing, progress persistence, reminder scheduling implementation, or share message formatting logic.
- `DailyServiceChallengeExperienceOrchestrator` must request reversal confirmation before invoking the state change that returns a completed challenge to incomplete.
- `DailyServiceChallengeExperienceOrchestrator` must request share payload generation only for a challenge whose completion state is completed.
- `DailyServiceChallengeExperienceOrchestrator` must validate the current local date against the campaign window before reminder evaluation, and it must obtain current-day challenge content through `ChallengeCalendarProtocol` before invoking `ChallengeReminderProtocol` for any in-window reminder scheduling decision.
- `DailyServiceChallengeExperienceOrchestrator` must pass out-of-window reminder evaluations into `ChallengeReminderProtocol` without first requesting current-day challenge content from `ChallengeCalendarProtocol`.

---

## 7. Data Flow
1. Campaign window and optional selected challenge date enter `ChallengeCalendarProtocol` to return ordered challenge cards or the requested challenge detail.
2. Challenge date and current local date enter `ChallengeProgressProtocol` to return completion eligibility and current completion state.
3. Completion requests enter `ChallengeProgressProtocol` to return updated completed state or `COMPLETION_NOT_YET_ALLOWED`.
4. Completion reversal requests enter `ChallengeProgressProtocol` to return a reversal confirmation prompt, and confirmed reversals return updated incomplete state while declined reversals preserve completed state.
5. For reminder evaluation, the experience flow first checks whether the current local date is inside the campaign window.
6. When the current local date is outside the campaign window, reminder preference, notification permission, campaign window, current local date, current local time, no current-day challenge content, and current-day completion state enter `ChallengeReminderProtocol` to return both reminders as `not_scheduled`.
7. When the current local date is inside the campaign window, that date enters `ChallengeCalendarProtocol` to validate that authoritative challenge content exists for the day before reminder scheduling can succeed.
8. Reminder preference, notification permission, campaign window, current local date, current local time, validated current-day challenge content, and current-day completion state enter `ChallengeReminderProtocol` to return early-reminder and later-reminder scheduling or suppression state.
9. Completed challenge date, completed challenge summary, completion state, and app link enter `ChallengeShareProtocol` to return share payload content or an explicit share failure response.

---

## 8. Testing Strategy
- Contract alignment tests:
  - Verify the feature exposes card browsing, detail access, eligible completion, confirmed reversal, reminders, and sharing exactly as defined in the contract.
  - Verify future challenge browsing is allowed while pre-date completion remains disallowed.
- Protocol conformance tests:
  - Verify `ChallengeCalendarContentModule` returns ordered content for the full campaign window and explicit failures for missing content.
  - Verify `ChallengeProgressModule` returns only approved states, prompts, and failure reasons.
  - Verify `ChallengeReminderModule` returns `not_scheduled` for both reminders outside the campaign window.
  - Verify `ChallengeReminderModule` returns time-dependent `10:00 AM` and conditional `6:00 PM` schedule-state behavior before `10:00 AM`, between `10:00 AM` and `6:00 PM`, and after `6:00 PM`.
  - Verify `ChallengeReminderModule` returns an explicit failure when in-window challenge content cannot be validated.
  - Verify `ChallengeShareModule` returns share payload only for completed challenges.
- Orchestration flow tests:
  - Verify challenge detail viewing combines content and progress state without bypassing protocols.
  - Verify enabling reminders and completing the day's challenge affects later reminder suppression through the approved orchestration path.
  - Verify reminder evaluation validates current-day challenge content through `ChallengeCalendarProtocol` before `ChallengeReminderProtocol` is invoked for in-window dates.
- Failure path tests:
  - Verify missing challenge content, early completion requests, reversal requests for incomplete challenges, declined reversal confirmation, missing notification permission, unavailable challenge calendar data, and share requests for incomplete challenges.

---

## 9. Architectural Risks and Guardrails
- Risk: challenge content behavior could become coupled to a bundled-file implementation and make later source replacement invasive.
  - Guardrail: keep all authoritative content access inside `ChallengeCalendarContentModule` and preserve `ChallengeCalendarProtocol` as the only challenge content boundary.
- Risk: the initial bundled local JSON shape could blur challenge content and user progress responsibilities.
  - Guardrail: keep the initial content shape limited to contract-aligned challenge content fields only: `date`, `short summary`, `detail description`, and required `suggestions`; keep completion state out of content and inside `ChallengeProgressModule`.
- Risk: source-specific field names such as `title`, `summary`, or singular `suggestion` could leak into the feature boundary and fragment the contract-aligned challenge model.
  - Guardrail: keep any one-time source-to-contract mapping inside `ChallengeCalendarContentModule` before content crosses `ChallengeCalendarProtocol`.
- Risk: completion eligibility and reminder timing could disagree if they evaluate different local dates or times.
  - Guardrail: require the experience flow to pass explicit current local date and current local time inputs into each protocol evaluation.
- Risk: reminder scheduling could succeed for an in-window date whose challenge content has not been validated through the calendar boundary.
  - Guardrail: require the orchestrator to obtain current-day challenge content through `ChallengeCalendarProtocol` before invoking `ChallengeReminderProtocol` for in-window scheduling.
- Risk: reminder behavior could bypass progress state and send a later reminder after the challenge is already completed.
  - Guardrail: require `ChallengeReminderProtocol` evaluations to use current-day completion state from `ChallengeProgressProtocol` results.
- Risk: share behavior could be triggered for incomplete challenges.
  - Guardrail: keep `ChallengeShareModule` dependent on explicit completed state and reject share generation otherwise.
- Risk: completion reversal could occur without the contract-required confirmation step.
  - Guardrail: keep reversal confirmation as a distinct orchestration boundary before the progress state change is invoked.

---

## 10. Open Questions
- None at this architecture stage.
