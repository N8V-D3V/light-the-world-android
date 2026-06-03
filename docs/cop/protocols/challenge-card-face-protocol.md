# Protocol: Challenge Card Face

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-challenge-card-presentation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
ChallengeCardFaceProtocol

---

## 2. Purpose
Manage the visible face of the active challenge card, including front-face presentation, back-face presentation, face changes, and in-place vertical reading for long back-face content.

---

## 3. Inputs
- `challenge card list: collection` - ordered set of presented challenge cards with required front-face and back-face content.
- `current active card: identifier or null` - currently centered card, if one exists.
- `card face request: event` - user action requesting that the active card show its front face or back face.
- `active card request: event` - user action requesting that a different card become the centered active card.
- `back-face reading request: event` - user action requesting vertical reading movement within the active card back face.

---

## 4. Outputs
- `card face state: object` - whether the active card shows its front face or back face.
- `motion state: object` - user-visible face-change transition state.
- `active card state: object` - the active card preserved during face changes and back-face reading.
- `failure response: object or null` - explicit failure when face presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `PresentedChallengeCard`
  - `challenge date: date` - front-face date content shown for the challenge.
  - `short summary: string` - front-face summary content shown for the challenge.
  - `detail description: string` - back-face detail content shown for the challenge.
  - `suggestions: collection` - back-face suggestion content shown for the challenge.
  - `completion state: enum` - `completed` or `incomplete`.
  - `future state: enum` - `future` or `non_future`.
- `CardPresentationState`
  - `card identifier: identifier` - unique presented card reference.
  - `emphasis state: enum` - `active`, `adjacent`, or `offscreen`.
  - `visible face: enum` - `front` or `back`.
  - `snap state: enum` - `free_scrolling`, `snapping`, or `centered`.

---

## 6. Behavior Requirements
1. Must present the active card front face with the challenge date and short summary as primary visible content.
2. Must present the active card back face with the detail description and suggestions as primary visible content.
3. Must allow only the centered active card to change to its back face.
4. Must change only the active card face when the opposite face is requested.
5. Must keep the card centered and active when its face changes.
6. Must return the previously active card to its front face before it becomes adjacent or offscreen when a different card becomes active.
7. Must keep adjacent preview cards on their front-face view only.
8. Must allow future active cards to show both front and back faces.
9. Must preserve the same face-change capability for completed and incomplete active cards.
10. Must support in-place vertical reading of detail description and suggestions when back-face content exceeds one card view.
11. Must preserve the current active card and visible back face while the user scrolls back-face content vertically.
12. Must not treat vertical back-face reading movement as a horizontal card-browsing request.
13. Must represent face-change motion as calm and clean presentation state.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `NO_ACTIVE_CARD`
  - `REQUIRED_CARD_CONTENT_MISSING`
  - `BACK_FACE_READING_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine challenge completion eligibility, reminder behavior, sharing behavior, or completion persistence rules
- Must not hide required front-face or back-face content when that face is presented
- Must preserve the approved in-place vertical reading behavior for long back-face content

---

## 9. Open Questions
- None at this time.
