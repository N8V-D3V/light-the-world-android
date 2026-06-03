# Protocol: Challenge Card Focus

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-challenge-card-presentation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
ChallengeCardFocusProtocol

---

## 2. Purpose
Manage active challenge card selection and focus state for the card rail while preserving the single centered active-card presentation required by the contract.

---

## 3. Inputs
- `challenge card list: collection` - ordered set of presented challenge cards available in the rail.
- `current date: date or null` - current local date used only to choose the initial active card when no active card is already set.
- `current active card: identifier or null` - currently centered card, if one exists.
- `active card request: event` - user action requesting that a specific card become the centered active card.
- `browse request: event` - user action requesting horizontal movement through the card sequence.

---

## 4. Outputs
- `active card state: object` - the currently centered card and its sequence position.
- `adjacent card preview state: object` - previous and next neighboring card preview availability for the active card.
- `card visual emphasis state: object` - active, adjacent, and offscreen emphasis state after focus resolution.
- `motion state: object` - focus movement and snap completion state.
- `failure response: object or null` - explicit failure when focus cannot be changed.

---

## 5. Data Model Expectations
- `CardPresentationState`
  - `card identifier: identifier` - unique presented card reference.
  - `emphasis state: enum` - `active`, `adjacent`, or `offscreen`.
  - `visible face: enum` - `front` or `back`.
  - `snap state: enum` - `free_scrolling`, `snapping`, or `centered`.

---

## 6. Behavior Requirements
1. Must preserve exactly one centered active card when one or more cards are available.
2. Must choose the card matching the current date as the initial centered active card when the current active card is null and one card matches the current date.
3. Must choose the first card in sequence as the initial centered active card when the current active card is null and no card matches the current date.
4. Must return a failure response and must not present an arbitrary active card when the current active card is null, multiple cards are available, and the contract-defined initial active card cannot be determined.
5. Must allow a requested card in the current challenge card list to become the centered active card.
6. Must preserve the current active card state when an active card request targets a card not present in the current challenge card list.
7. Must update adjacent card preview availability when the active card changes.
8. Must expose previous-adjacent availability only when a card exists before the active card in sequence.
9. Must expose next-adjacent availability only when a card exists after the active card in sequence.
10. Must keep the active card centered after focus changes.
11. Must snap to a single centered active card after horizontal browsing ends.
12. Must require the previously active card to return to its front face before becoming adjacent or offscreen when a different card becomes active.
13. Must allow future cards supplied to this experience to become active.
14. Must preserve the same focus model for completed and incomplete cards.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `ACTIVE_CARD_NOT_FOUND`
  - `CHALLENGE_CARD_LIST_UNAVAILABLE`
  - `INITIAL_ACTIVE_CARD_UNDETERMINED`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine challenge completion eligibility, reminder behavior, sharing behavior, or completion persistence rules
- Must choose the initial active card deterministically when no active card is already set
- Must not treat vertical back-face reading movement as a horizontal card-browsing request

---

## 9. Open Questions
- None at this time.
