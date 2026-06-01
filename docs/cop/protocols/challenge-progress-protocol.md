# Protocol: Challenge Progress

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-service-challenge-experience.md`

---

## 1. Name
ChallengeProgressProtocol

---

## 2. Purpose
Provide user-specific challenge completion state, determine completion eligibility using local time, mark challenges complete, and reverse completion only after confirmation.

---

## 3. Inputs
- challenge date: date - challenge being evaluated or updated.
- current local date: date - user's current local date for completion eligibility.
- completion request: event - request to mark a challenge complete.
- completion reversal request: event - request to reverse a completed challenge.
- completion reversal confirmation: event - confirmation approving or declining the reversal.

---

## 4. Outputs
- completion state: state - whether the challenge is incomplete or completed for the current user.
- completion eligibility: state - whether the challenge can currently be marked complete.
- completion reversal prompt: object or null - confirmation prompt before reversing completion.
- failure response: object or null - explicit failure when a progress action cannot be completed.

---

## 5. Data Model Expectations
- `ChallengeProgress`
  - `challenge date: date` - tracked challenge date.
  - `status: enum` - `incomplete` or `completed`.
  - `completed at: timestamp or null` - completion time when completed.

---

## 6. Behavior Requirements
1. Must determine completion eligibility using the user's current local date.
2. Must allow completion only when the current local date is the challenge date or later.
3. Must return user-specific completion state for a challenge.
4. Must require a confirmation step before changing a completed challenge back to incomplete.
5. Must preserve the completed state when reversal confirmation is declined or dismissed.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `COMPLETION_NOT_YET_ALLOWED`
  - `CHALLENGE_NOT_COMPLETED`
  - `REVERSAL_NOT_CONFIRMED`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- None.
