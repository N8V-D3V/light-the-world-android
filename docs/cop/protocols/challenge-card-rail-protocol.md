# Protocol: Challenge Card Rail

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-challenge-card-presentation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
ChallengeCardRailProtocol

---

## 2. Purpose
Provide the horizontally arranged challenge card rail presentation, including ordered cards, active-card emphasis, adjacent-card previews, snap state, and presentation visual state without defining challenge business behavior.

---

## 3. Inputs
- `challenge card list: collection` - ordered set of presented challenge cards with front-face and back-face content ready for presentation.
- `card presentation states: collection` - per-card visual states indicating completed or incomplete presentation and future or non-future presentation.
- `current active card: identifier or null` - currently centered card, if one exists.
- `browse request: event` - user action requesting horizontal movement through the card sequence.

---

## 4. Outputs
- `challenge card rail: collection` - horizontally arranged challenge card presentation ordered by challenge date.
- `empty card rail state: object or null` - non-interactive empty presentation shown when no challenge cards are available to present.
- `active card state: object` - the single centered and visually emphasized card when one or more cards are available.
- `adjacent card preview state: object` - partially visible neighboring card presentation at the active card edges when neighbors exist.
- `card visual emphasis state: object` - visible distinction for active, adjacent, completed, incomplete, future, and non-future cards.
- `motion state: object` - user-visible scrolling and snap behavior state.
- `failure response: object or null` - explicit failure when the rail presentation cannot be fulfilled.

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
- `EmptyCardRailState`
  - `message: string` - user-visible message indicating that no challenges are available.
  - `interaction state: enum` - `non_interactive`.

---

## 6. Behavior Requirements
1. Must present daily challenge cards as a horizontally scrolling sequence ordered by challenge date.
2. Must present a non-interactive empty state with a user-visible message that no challenges are available when the challenge card list is available but empty.
3. Must not present an active card or adjacent card previews when the challenge card list is available but empty.
4. Must present exactly one centered active card when one or more cards are available.
5. Must assign the strongest visual emphasis to the centered active card.
6. Must keep neighboring cards partially visible at the active card edges when neighboring cards exist.
7. Must show only the next adjacent card preview when the first card is active.
8. Must show only the previous adjacent card preview when the last card is active.
9. Must present a single available card as the active card without adjacent previews.
10. Must snap to one centered active card after a browse request ends.
11. Must represent scrolling and snap motion as calm, clean, and deliberate presentation state.
12. Must present adjacent preview cards in their front-face view only.
13. Must preserve visibility and browsability for future cards supplied to the experience.
14. Must preserve readability and interaction for completed, incomplete, future, and non-future card states.
15. Must expose presentation state sufficient to preserve the contract-defined Light the World tone without specifying implementation details.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `CHALLENGE_CARD_LIST_UNAVAILABLE`
  - `REQUIRED_CARD_CONTENT_MISSING`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine challenge completion eligibility, reminder behavior, sharing behavior, or completion persistence rules
- Must present an empty state instead of a fabricated card rail when the challenge card list is available but empty
- Must not fabricate card content, visual state, or rail state when required presentation inputs are unavailable
- Must not encode raw visual implementation details beyond the presentation needs defined by the source contract

---

## 9. Open Questions
- None at this time.
