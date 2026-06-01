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
Manage optional daily challenge reminders using the user's local time, permission state, reminder preference, and completion state.

---

## 3. Inputs
- reminder preference: state - whether reminders are enabled.
- notification permission: state - whether notification permission is granted.
- current local date: date - user's current local date.
- current local time: time - user's current local time.
- completion state: state - whether the current day's challenge is completed.

---

## 4. Outputs
- reminder schedule state: object - whether the 10:00 AM and 6:00 PM reminders are scheduled or suppressed.
- failure response: object or null - explicit failure when reminders cannot be scheduled.

---

## 5. Data Model Expectations
- `ReminderPreference`
  - `reminders enabled: boolean` - whether the user wants reminders.
  - `notification permission granted: boolean` - whether reminder delivery is allowed.
- `ReminderSchedule`
  - `early reminder local time: time` - `10:00 AM`.
  - `later reminder local time: time` - `6:00 PM`.

---

## 6. Behavior Requirements
1. Must schedule reminders only when reminder preference is enabled and notification permission is granted.
2. Must schedule the first reminder for 10:00 AM local time for the current day's challenge.
3. Must schedule the second reminder for 6:00 PM local time only when the current day's challenge is incomplete.
4. Must suppress the second reminder when the current day's challenge is already completed.
5. Must determine reminder timing using the user's current local date and local time.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `NOTIFICATION_PERMISSION_REQUIRED`
  - `REMINDERS_DISABLED`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- None.
