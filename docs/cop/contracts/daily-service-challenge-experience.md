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
- selected challenge date: date — the challenge the user opens or interacts with.
- current date: date — the date used to determine challenge completion eligibility.
- completion request: event — user action requesting that a challenge be marked complete.
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
- share payload: object — user-facing share content indicating challenge completion.
- reminder schedule state: object — whether the early reminder and later reminder are scheduled or not scheduled.
- failure response: object — explicit error or rejection response when a requested action cannot be completed.

---

## 5. Data Models

### Daily Challenge
- date: date — the calendar day assigned to the challenge.
- short summary: string — brief front-of-card description.
- detail description: string — fuller challenge explanation shown on the back of the card.
- suggestions: collection — optional suggestions for how to accomplish the challenge.

### Challenge Progress
- challenge date: date — the challenge being tracked.
- status: enum — `incomplete` or `completed`.
- completed at: timestamp or null — when the user completed the challenge, if completed.

### Reminder Preference
- reminders enabled: boolean — whether the user wants challenge reminders.
- notification permission granted: boolean — whether delivery permission exists.

### Share Content
- challenge date: date — the completed challenge being shared.
- challenge summary: string — summary associated with the completed challenge.
- completion message: string — message indicating completion.

---

## 6. Success Behavior

1. The system must present daily challenges as a horizontally scrollable sequence of cards ordered by challenge date.
2. The front of each card must show the challenge date and short summary.
3. When the user opens a card, the system must provide access to the back of the card containing the challenge details and suggestions.
4. The system must allow the user to browse future challenges without restriction.
5. The system must allow a challenge to be marked complete only when the current date is the challenge date or later.
6. When a valid completion request is made, the system must store the challenge as completed for that user.
7. When a completed challenge is viewed, the system must expose its completed state.
8. When the user requests to share a completed challenge, the system must provide share content indicating that the challenge was completed.
9. When notification permission is granted and reminders are enabled, the system must schedule one early-day reminder describing the day's challenge.
10. When notification permission is granted and reminders are enabled, the system must schedule one later-day reminder only if that day's challenge has not yet been completed.

---

## 7. Failure Modes

- Condition: challenge content for a requested date is missing
  - System must: return a failure response that the challenge cannot be displayed for that date

- Condition: user attempts to mark a challenge complete before its challenge date
  - System must: reject the completion request and preserve the challenge as incomplete

- Condition: user attempts to share a challenge that is not completed
  - System must: reject the share request and return a failure response

- Condition: reminders are enabled but notification permission is not granted
  - System must: report that reminders cannot be scheduled until permission is granted

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
- The user has no recorded progress for any challenge.

---

## 9. Constraints

- Must not introduce challenge behavior outside this contract.
- Must not allow completion before the challenge date.
- Must allow future challenge browsing.
- Must treat challenge completion as user-specific state.
- Must not send the later reminder if the challenge is already completed.
- Must not create share behavior for incomplete challenges.
- Must not invent challenge content when authoritative challenge data is unavailable.

---

## 10. Observability

### Events
- challenge_card_viewed
- challenge_detail_viewed
- challenge_completed
- challenge_share_requested
- reminder_preference_changed
- challenge_reminder_scheduled
- challenge_reminder_suppressed

### Metrics
- daily challenge views
- challenge completion rate
- share rate for completed challenges
- reminder enablement rate
- reminder delivery eligibility rate

### Logs
- challenge content load result
- completion request outcome
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
- [ ] Users can share a completed challenge through the app.
- [ ] Users can enable daily reminders when notification permission is granted.
- [ ] The system schedules an early reminder for the day's challenge when reminders are enabled.
- [ ] The system sends or keeps scheduled a later reminder only when the day's challenge remains incomplete.

---

## 12. Open Questions

- What exact time defines the early reminder and later reminder for a user's day?
- How should time zone changes be handled for challenge eligibility and reminders?
- What exact share message content and campaign wording are required?
- Are suggestions required for every challenge or optional for some challenge dates?
- Should users be allowed to undo a completion after marking a challenge complete?
