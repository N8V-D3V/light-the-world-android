# Contract: Daily Service Challenge Experience

Version: 0.3.0  
Status: Draft

---

## 1. Purpose
Define the behavior for presenting campaign daily challenges, showing challenge details, allowing eligible completion, supporting completion sharing, and sending optional reminder notifications.

---

## 2. Actors
- User — views challenge content, manages completion, manages reminder preference, and shares completion.
- Challenge Content Source — provides the dated challenge content presented by the system.
- Notification System — delivers reminder notifications when the user has granted permission and reminders are enabled.
- Social Share Destination — receives user-initiated shared completion content.

---

## 3. Inputs
- challenge calendar: collection — ordered set of daily challenges with dates, summaries, details, and suggestions.
- campaign window: date range — annual challenge schedule beginning on December 1 and ending on December 25.
- selected challenge date: date — the challenge the user opens or interacts with.
- current date: date — the date used to determine challenge completion eligibility.
- current time: time — the local time used to determine reminder scheduling state.
- completion request: event — user action requesting that a challenge be marked complete.
- completion reversal request: event — user action requesting that a completed challenge be returned to incomplete status.
- completion reversal confirmation: event — user confirmation approving or declining the reversal request.
- share request: event — user action requesting to share that a challenge was completed.
- reminder preference: state — whether the user allows challenge reminder notifications.
- notification permission: state — whether the user has granted notification permission.

---

## 4. Outputs
- challenge card list: collection — horizontally browsable challenge cards ordered by date.
- challenge front content: object — front-of-card content containing the challenge date and short summary.
- challenge back content: object — back-of-card content containing challenge details and suggestions.
- completion state: state — whether a challenge is incomplete or completed for the current user.
- completion eligibility: state — whether a challenge can currently be marked complete.
- completion reversal prompt: object — confirmation prompt shown before a completed challenge is returned to incomplete status.
- share payload: object — user-facing share content indicating challenge completion.
- reminder schedule state: object — whether the early reminder and later reminder are scheduled or not scheduled.
- failure response: object — explicit error or rejection response when a requested action cannot be completed.

---

## 5. Data Models

### Daily Challenge
- date: date — the calendar day assigned to the challenge.
- short summary: string — brief front-of-card description.
- detail description: string — fuller challenge explanation shown on the back of the card.
- suggestions: collection — required suggestions for how to accomplish the challenge.

### Challenge Progress
- challenge date: date — the challenge being tracked.
- status: enum — `incomplete` or `completed`.
- completed at: timestamp or null — when the user completed the challenge, if completed.

### Reminder Preference
- reminders enabled: boolean — whether the user wants challenge reminders.
- notification permission granted: boolean — whether delivery permission exists.

### Reminder Schedule
- evaluated date: date — the current local date evaluated for reminder scheduling.
- early reminder local time: time — `10:00 AM`.
- later reminder local time: time — `6:00 PM`.
- early reminder state: enum — `scheduled` or `not_scheduled`.
- later reminder state: enum — `scheduled` or `not_scheduled`.

### Share Content
- challenge date: date — the completed challenge being shared.
- challenge summary: string — summary associated with the completed challenge.
- completion message: string — `I completed "[challenge summary]" in Light the World today. Join me in sharing some light this Christmas: [app link]`.

---

## 6. Success Behavior

1. The system must present daily challenges as a horizontally scrollable sequence of cards ordered by challenge date.
2. The front of each card must show the challenge date and short summary.
3. When the user opens a card, the system must provide access to the back of the card containing the challenge details and suggestions.
4. The system must allow the user to browse future challenges without restriction.
5. The system must allow a challenge to be marked complete only when the current date is the challenge date or later.
6. When a valid completion request is made, the system must store the challenge as completed for that user.
7. When a completed challenge is viewed, the system must expose its completed state.
8. When the user requests to reverse completion for a completed challenge, the system must prompt the user to confirm that reversal before changing the completion state.
9. When the user confirms a valid completion reversal request, the system must return the challenge to incomplete status.
10. When the user requests to share a completed challenge, the system must provide share content indicating that the challenge was completed through the device's standard share flow.
11. When reminder schedule evaluation occurs for a current local date outside the campaign window, the system must return a reminder schedule state for that date with both reminders marked `not_scheduled`.
12. The system must validate that the current local date maps to authoritative challenge content in the challenge calendar before reminder scheduling can succeed for that date.
13. When notification permission is granted, reminders are enabled, the current local date is inside the campaign window, and the current local time is before `10:00 AM`, the system must mark the `10:00 AM` reminder as `scheduled` for that date.
14. When notification permission is granted, reminders are enabled, the current local date is inside the campaign window, and the current local time is `10:00 AM` or later, the system must mark the `10:00 AM` reminder as `not_scheduled` for that date.
15. When notification permission is granted, reminders are enabled, the current local date is inside the campaign window, the current local time is before `6:00 PM`, and that day's challenge is incomplete, the system must mark the `6:00 PM` reminder as `scheduled` for that date.
16. When the current local time is `6:00 PM` or later, the system must mark the `6:00 PM` reminder as `not_scheduled` for that date.
17. When that day's challenge is already completed, the system must mark the `6:00 PM` reminder as `not_scheduled` for that date.
18. The system must determine challenge completion eligibility and reminder scheduling using the user's current local date and local time.

---

## 7. Failure Modes

- Condition: challenge content for a requested date is missing
  - System must: return a failure response that the challenge cannot be displayed for that date

- Condition: user attempts to mark a challenge complete before its challenge date
  - System must: reject the completion request and preserve the challenge as incomplete

- Condition: user attempts to share a challenge that is not completed
  - System must: reject the share request and return a failure response

- Condition: user requests completion reversal for a challenge that is not completed
  - System must: reject the reversal request and preserve the challenge as incomplete

- Condition: user dismisses or declines the completion reversal confirmation
  - System must: preserve the challenge as completed

- Condition: reminders are enabled but notification permission is not granted
  - System must: report that reminders cannot be scheduled until permission is granted

- Condition: the current local date is inside the campaign window but authoritative challenge content for that date is missing or unavailable during reminder evaluation
  - System must: return a failure response and must not schedule reminders for that date

- Condition: a later reminder is due after the challenge has already been completed
  - System must: suppress the later reminder for that challenge date

- Condition: challenge calendar data is unavailable
  - System must: return a failure response and not fabricate challenge content

---

## 8. Edge Cases

- The user opens a future challenge and later returns on its actual date.
- The user completes a challenge after its scheduled day has passed.
- The user enables reminders after the campaign has already started.
- The user disables reminders after reminders were previously scheduled.
- Reminder schedule evaluation occurs before December 1 or after December 25.
- The user completes the current day's challenge before `10:00 AM`.
- Reminder schedule evaluation occurs between `10:00 AM` and `6:00 PM`.
- Reminder schedule evaluation occurs after `6:00 PM`.
- The user has no recorded progress for any challenge.
- The user marks a challenge complete by mistake and immediately reverses it.

---

## 9. Constraints

- Must not introduce challenge behavior outside this contract.
- Must use an annual challenge calendar window from December 1 through December 25.
- Must determine challenge availability and reminder scheduling using the user's current local date and local time.
- Must return both reminders as `not_scheduled` when the current local date is outside the campaign window.
- Must validate current-day challenge content through the challenge calendar before reminder scheduling can succeed.
- Must not allow completion before the challenge date.
- Must allow future challenge browsing.
- Must treat challenge completion as user-specific state.
- Must require user confirmation before changing a completed challenge back to incomplete.
- Must not send the later reminder if the challenge is already completed.
- Must not create share behavior for incomplete challenges.
- Must not invent challenge content when authoritative challenge data is unavailable.

---

## 10. Observability

### Events
- challenge_card_viewed
- challenge_detail_viewed
- challenge_completed
- challenge_completion_reversal_requested
- challenge_completion_reversed
- challenge_share_requested
- reminder_preference_changed
- challenge_reminder_scheduled
- challenge_reminder_suppressed

### Metrics
- daily challenge views
- challenge completion rate
- challenge completion reversal rate
- share rate for completed challenges
- reminder enablement rate
- reminder delivery eligibility rate

### Logs
- challenge content load result
- completion request outcome
- completion reversal outcome
- share request outcome
- reminder scheduling outcome

---

## 11. Acceptance Criteria

- [ ] Users can browse an ordered horizontal list of daily challenge cards.
- [ ] Each challenge card front shows its date and short summary.
- [ ] Each challenge card provides access to a back side with details and suggestions.
- [ ] Users can view future challenges before their dates.
- [ ] Users cannot mark a challenge complete before its date.
- [ ] Users can mark a challenge complete on its date or after.
- [ ] Users can reverse a completed challenge back to incomplete only after a confirmation prompt.
- [ ] Users can share a completed challenge through the app.
- [ ] Users can enable daily reminders when notification permission is granted.
- [ ] Reminder evaluation outside the December 1 through December 25 campaign window returns both reminders as `not_scheduled`.
- [ ] Reminder scheduling succeeds only after the current day's challenge content is validated through the challenge calendar.
- [ ] Before `10:00 AM`, the `10:00 AM` reminder is `scheduled` and the `6:00 PM` reminder is `scheduled` only when the day's challenge remains incomplete.
- [ ] Between `10:00 AM` and `6:00 PM`, the `10:00 AM` reminder is `not_scheduled` and the `6:00 PM` reminder is `scheduled` only when the day's challenge remains incomplete.
- [ ] After `6:00 PM`, both reminders are `not_scheduled` for that date.

---

## 12. Open Questions
- None at this time.
