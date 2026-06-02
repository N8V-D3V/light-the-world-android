# Protocol: Challenge Reminder

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-service-challenge-experience.md`

---

## 1. Name
ChallengeReminderProtocol

---

## 2. Purpose
Manage optional daily challenge reminders using the user's local time, permission state, reminder preference, validated current-day challenge content, and completion state.

---

## 3. Inputs
- reminder preference: state - whether reminders are enabled.
- notification permission: state - whether notification permission is granted.
- campaign window: date range - annual challenge schedule from December 1 through December 25.
- current local date: date - user's current local date.
- current local time: time - user's current local time.
- current day challenge: object or null - authoritative challenge content for the current local date after calendar validation.
- completion state: state - whether the current day's challenge is completed.

---

## 4. Outputs
- reminder schedule state: object - whether the 10:00 AM and 6:00 PM reminders are `scheduled` or `not_scheduled`.
- failure response: object or null - explicit failure when reminders cannot be scheduled.

---

## 5. Data Model Expectations
- `ReminderPreference`
  - `reminders enabled: boolean` - whether the user wants reminders.
  - `notification permission granted: boolean` - whether reminder delivery is allowed.
- `ReminderSchedule`
  - `evaluated date: date` - current local date being evaluated.
  - `early reminder local time: time` - `10:00 AM`.
  - `later reminder local time: time` - `6:00 PM`.
  - `early reminder state: enum` - `scheduled` or `not_scheduled`.
  - `later reminder state: enum` - `scheduled` or `not_scheduled`.

---

## 6. Behavior Requirements
1. Must return both reminders as `not_scheduled` when the current local date falls outside the campaign window.
2. Must require the current day's challenge content to be validated before reminder scheduling can succeed for a date inside the campaign window.
3. Must return an explicit failure when the current local date is inside the campaign window but current-day challenge content cannot be validated.
4. Must schedule reminders only when reminder preference is enabled and notification permission is granted.
5. Must mark the `10:00 AM` reminder as `scheduled` when evaluation occurs before `10:00 AM` on a valid campaign date.
6. Must mark the `10:00 AM` reminder as `not_scheduled` when evaluation occurs at `10:00 AM` or later on a valid campaign date.
7. Must mark the `6:00 PM` reminder as `scheduled` only when evaluation occurs before `6:00 PM` on a valid campaign date and the current day's challenge is incomplete.
8. Must mark the `6:00 PM` reminder as `not_scheduled` when the current day's challenge is already completed.
9. Must mark the `6:00 PM` reminder as `not_scheduled` when evaluation occurs at `6:00 PM` or later on a valid campaign date.
10. Must determine reminder timing using the user's current local date and local time.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `NOTIFICATION_PERMISSION_REQUIRED`
  - `REMINDERS_DISABLED`
  - `CHALLENGE_CONTENT_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- None.
