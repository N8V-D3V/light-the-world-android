# Protocol: Challenge Card Accessibility

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-challenge-card-presentation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
ChallengeCardAccessibilityProtocol

---

## 2. Purpose
Expose accessible presentation state and assistive actions for browsing the card sequence, changing the active card face, and fully reading long back-face content without relying on gesture-only interaction.

---

## 3. Inputs
- `challenge card list: collection` - ordered set of presented challenge cards available for accessible presentation.
- `current active card: identifier or null` - currently centered card, if one exists.
- `card face state: object` - whether the active card shows its front face or back face.
- `accessibility browse request: event` - assistive action requesting movement to the previous or next card in sequence.
- `accessibility face request: event` - assistive action requesting that the active card show its front face or back face.
- `back-face reading request: event` - assistive reading action for long active-card back-face content.

---

## 4. Outputs
- `accessibility presentation state: object` - active card label, sequence position, current face, and available actions.
- `active card state: object` - the single centered active card after an accessibility browse request.
- `card face state: object` - active card face after an accessibility face request.
- `failure response: object or null` - explicit failure when accessible presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `AccessibilityPresentationContext`
  - `active card position: string` - sequence position for the active card such as first, last, or one card within a larger set.
  - `active card face: enum` - `front` or `back`.
  - `available actions: collection` - browse previous, browse next, show front, or show back when applicable.
- `PresentedChallengeCard`
  - `challenge date: date` - date content exposed for active card presentation.
  - `short summary: string` - front-face summary content shown for the challenge.
  - `detail description: string` - back-face detail content shown for the challenge.
  - `suggestions: collection` - back-face suggestion content shown for the challenge.
  - `completion state: enum` - `completed` or `incomplete`.
  - `future state: enum` - `future` or `non_future`.

---

## 6. Behavior Requirements
1. Must expose the active card date through accessibility presentation state.
2. Must expose the active card position in the sequence through accessibility presentation state.
3. Must expose the active card current face through accessibility presentation state.
4. Must expose whether previous and next browse actions are available.
5. Must move active focus one card at a time in sequence order for accessibility browse requests.
6. Must preserve a single centered active-card presentation after accessibility browse requests.
7. Must allow accessibility face requests to change the active card between front and back without relying on gesture-only interaction.
8. Must expose show-front and show-back actions only when applicable to the active card face state.
9. Must allow long active-card back-face content to be read fully when that content exceeds one card view.
10. Must preserve the active card and visible face while long back-face content is read.
11. Must not expose unlabeled interactive card content when required accessibility state cannot be provided.
12. Must preserve accessible interaction across active, adjacent, completed, incomplete, future, and non-future card states.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `ACCESSIBILITY_STATE_UNAVAILABLE`
  - `NO_ACTIVE_CARD`
  - `ACTIVE_CARD_NOT_FOUND`
  - `BACK_FACE_READING_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine challenge completion eligibility, reminder behavior, sharing behavior, or completion persistence rules
- Must not rely on gesture-only interaction for browse or face changes
- Must not expose the interaction as unlabeled interactive card content

---

## 9. Open Questions
- None at this time.
