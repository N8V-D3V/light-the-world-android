# Protocol: Challenge Calendar

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-service-challenge-experience.md`

---

## 1. Name
ChallengeCalendarProtocol

---

## 2. Purpose
Provide the ordered daily challenge content needed to browse challenge cards, view challenge details, and honor the annual campaign window without exposing how the content source is implemented.

---

## 3. Inputs
- current local date: date - the user's current local date.
- campaign window: date range - annual challenge schedule from December 1 through December 25.
- selected challenge date: date or null - specific challenge date to retrieve when detail content is requested.

---

## 4. Outputs
- challenge card list: collection - ordered challenge cards for the campaign window.
- challenge detail: object or null - full challenge content for the selected challenge date.
- failure response: object or null - explicit failure when challenge content cannot be provided.

---

## 5. Data Model Expectations
- `DailyChallenge`
  - `date: date` - assigned challenge date.
  - `short summary: string` - front-of-card summary.
  - `detail description: string` - back-of-card description.
  - `suggestions: collection` - optional suggestions for completing the challenge.

---

## 6. Behavior Requirements
1. Must provide an ordered set of challenge cards for the annual campaign window.
2. Must provide challenge detail content for a requested challenge date when content exists.
3. Must allow challenge content for future dates to be returned without restriction.
4. Must not expose or depend on a specific underlying content storage mechanism in the protocol definition.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `CHALLENGE_CONTENT_UNAVAILABLE`
  - `CHALLENGE_DATE_MISSING`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- Are suggestions required for every challenge or optional for some challenge dates?
