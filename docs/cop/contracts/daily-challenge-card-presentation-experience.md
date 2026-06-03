# Contract: Daily Challenge Card Presentation Experience

Version: 0.1.0  
Status: Draft

---

## 1. Purpose
Define the user-visible presentation and interaction behavior for browsing daily challenges as horizontally scrolling two-sided cards within the Light the World campaign experience.

---

## 2. Actors
- User — browses the challenge card rail, changes the active card, and changes the active card face between front and back.
- Accessibility Service User — browses the challenge card rail and changes the active card face through assistive actions that do not rely on gesture-only interaction.
- Challenge Presentation State Source — provides the ordered challenge card content and per-card visual state needed for presentation.

---

## 3. Inputs
- challenge card list: collection — ordered set of challenge cards with front-face and back-face content ready for presentation.
- card presentation states: collection — per-card visual states indicating completed or incomplete presentation and future or non-future presentation.
- current date: date or null — current local date used only to choose the initial active card when no active card is already set.
- browse request: event — user action requesting horizontal movement through the card sequence.
- active card request: event — user action requesting that a specific card become the centered active card.
- card face request: event — user action requesting that the active card show its front face or back face.
- current active card: identifier or null — currently centered card, if one exists.
- accessibility browse request: event — assistive action requesting movement to the previous or next card in sequence.
- accessibility face request: event — assistive action requesting the active card show its front face or back face.

---

## 4. Outputs
- challenge card rail: collection — horizontally arranged challenge card presentation.
- empty card rail state: object — non-interactive empty presentation shown when no challenge cards are available to present.
- active card state: object — the currently centered and visually emphasized card.
- adjacent card preview state: object — partially visible neighboring cards at the edges of the active card.
- card face state: object — whether the active card shows its front face or back face.
- card visual emphasis state: object — visible distinction for active, adjacent, completed, incomplete, future, and non-future cards.
- motion state: object — user-visible scrolling, snapping, and face-change transition state.
- accessibility presentation state: object — card labels, sequence position, face state, and available browse or face actions.
- failure response: object — explicit error or rejection response when presentation cannot be fulfilled.

---

## 5. Data Models

### Presented Challenge Card
- challenge date: date — front-face date content shown for the challenge.
- short summary: string — front-face summary content shown for the challenge.
- detail description: string — back-face detail content shown for the challenge.
- suggestions: collection — back-face suggestion content shown for the challenge.
- completion state: enum — `completed` or `incomplete`.
- future state: enum — `future` or `non_future`.

### Card Presentation State
- card identifier: identifier — unique presented card reference.
- emphasis state: enum — `active`, `adjacent`, or `offscreen`.
- visible face: enum — `front` or `back`.
- snap state: enum — `free_scrolling`, `snapping`, or `centered`.

### Accessibility Presentation Context
- active card position: string — sequence position for the active card such as first, last, or one card within a larger set.
- active card face: enum — `front` or `back`.
- available actions: collection — browse previous, browse next, show front, or show back when applicable.

### Empty Card Rail State
- message: string — user-visible message indicating that no challenges are available.
- interaction state: enum — `non_interactive`.

---

## 6. Success Behavior

1. The system must present daily challenges as a horizontally scrolling sequence of cards ordered by challenge date.
2. When the challenge card list is available but empty, the system must present a non-interactive empty state with a user-visible message that no challenges are available.
3. When the challenge card list is available but empty, the system must not present an active card or adjacent card previews.
4. When one or more cards are available, the system must present exactly one centered active card at a time.
5. When the current active card is null and one card matches the current date, the system must choose that card as the initial centered active card.
6. When the current active card is null and no card matches the current date, the system must choose the first card in sequence as the initial centered active card.
7. The centered active card must receive the strongest visual emphasis in the card rail.
8. When neighboring cards exist, the system must keep them partially visible at the edges of the active card.
9. When the first card is active, the system must show only the next adjacent card preview.
10. When the last card is active, the system must show only the previous adjacent card preview.
11. When only one card exists, the system must present that card as the active card without adjacent previews.
12. After a browse request ends, the system must snap to a single centered active card rather than leaving the rail resting between cards.
13. Scrolling motion and snap motion must feel calm, clean, and deliberate rather than flashy or abrupt.
14. Adjacent preview cards must present their front-face view only.
15. The front face of the active card must present the challenge date and short summary as the primary visible content.
16. The back face of the active card must present the detail description and suggestions as the primary visible content.
17. Only the centered active card may change to its back face.
18. When the user requests the opposite face for the active card, the system must change only that card face while keeping that card centered and active.
19. When a different card becomes active, the previously active card must return to its front face before becoming adjacent or offscreen.
20. When back-face content exceeds one card view, the active card back face must support in-place vertical scrolling of its detail description and suggestions while the card remains centered and active.
21. While the user scrolls back-face content vertically, the system must preserve the current active card and visible face rather than treating that movement as a horizontal card-browsing request.
22. Future cards supplied to this experience must remain visible in the same card rail, eligible to become the active card, and eligible to show both faces when active.
23. Completed cards must display a distinct completed visual treatment that preserves readability and does not block browsing, face changes, or back-face reading.
24. Incomplete cards must display the default non-completed visual treatment while preserving the same card structure and interaction model.
25. The card presentation must use a bold rounded card treatment and a calm Light the World visual tone with red as the dominant accent, white or cream as the clean base, navy as supporting contrast, and gold reserved for sparse emphasis.
26. Text and state indicators must remain readable across front-face, back-face, active, adjacent, completed, incomplete, future, and non-future card states.
27. Accessibility browse requests must move active focus one card at a time in sequence order while preserving a single centered active-card presentation.
28. Accessibility presentation must expose the active card's date, position in the sequence, current face, and whether previous or next browse actions are available.
29. Accessibility face requests must allow the active card to change between front and back without requiring gesture-only interaction.
30. Accessibility interaction must allow back-face content to be read fully when that content exceeds one card view.

---

## 7. Failure Modes

- Condition: challenge card list is unavailable when card presentation is requested
  - System must: return a failure response and must not fabricate a card rail

- Condition: current active card is null, multiple cards are available, and the system cannot determine the contract-defined initial active card
  - System must: return a failure response and must not present an arbitrary active card

- Condition: required front-face or back-face content is missing for a card requested for active presentation
  - System must: return a failure response and must not present that card as a partially defined interactive card

- Condition: active card request targets a card not present in the current challenge card list
  - System must: preserve the current active card state and return a failure response

- Condition: card face request occurs when no active card is available
  - System must: preserve the current visible state and return a failure response

- Condition: back-face content exceeds one card view but cannot be fully read through the presented back-face interaction
  - System must: return a failure response and must not claim the back-face presentation is complete

- Condition: accessibility presentation state cannot expose the active card identity, face, or available actions
  - System must: return a failure response and must not expose the interaction as unlabeled interactive card content

---

## 8. Edge Cases

- The first card becomes active while more cards remain to the right.
- The last card becomes active while more cards remain to the left.
- The challenge card list is available but empty.
- Only one challenge card is available for presentation.
- No current active card is set when multiple cards are available.
- A new card becomes active while the previous active card is showing its back face.
- A future card becomes active and is flipped to its back face.
- A completed card becomes active and is flipped to its back face.
- The user rapidly alternates between horizontal browsing and face changes.
- The back face contains enough detail description or suggestions to require vertical reading beyond one card view.
- An accessibility service user moves through the full card sequence one card at a time.

---

## 9. Constraints

- Must not introduce challenge behavior outside this contract.
- Must not redefine challenge eligibility, completion, reminder, or share business rules already defined elsewhere.
- Must use the ordered challenge card content supplied by the approved daily challenge experience.
- Must present an empty state instead of a fabricated card rail when the challenge card list is available but empty.
- Must preserve a horizontally scrolling card rail with one centered active card and partial adjacent card previews.
- Must choose the initial active card deterministically when no active card is already set.
- Must preserve calm, clean motion rather than flashy or distracting motion.
- Must preserve the Light the World campaign palette and tone defined in this contract.
- Must keep future cards visually viewable and browsable in the same presentation model.
- Must preserve readability and accessible interaction across all defined card states.
- Must allow the active back face to remain readable when its content exceeds one card view.
- Must not hide required front-face or back-face content when that face is presented.
- Must not fabricate card content, visual state, or accessibility state when required presentation inputs are unavailable.

---

## 10. Observability

### Events
- challenge_card_rail_viewed
- challenge_card_centered
- challenge_card_snap_completed
- challenge_card_face_changed
- challenge_card_accessibility_action_used
- challenge_card_presentation_failed

### Metrics
- challenge card rail view count
- active card change rate
- card face change rate
- snap completion rate
- card presentation failure rate
- accessibility browse action rate

### Logs
- challenge card rail load result
- active card change outcome
- card face change outcome
- accessibility presentation outcome
- card presentation failure reason

---

## 11. Acceptance Criteria

- [ ] Users can browse an ordered horizontal rail of daily challenge cards.
- [ ] When the challenge card list is available but empty, users see a non-interactive message that no challenges are available.
- [ ] Exactly one card is centered as the active card whenever one or more cards are available.
- [ ] When no active card is already set, the initial active card is today’s card when present, otherwise the first card in sequence.
- [ ] Neighboring cards remain partially visible at the edges of the centered active card when neighbors exist.
- [ ] The rail snaps to a single centered active card after horizontal browsing ends.
- [ ] The front face of the active card presents the challenge date and short summary.
- [ ] The back face of the active card presents the detail description and suggestions.
- [ ] Only the active centered card can change to its back face.
- [ ] When a different card becomes active, the previously active card returns to its front face before becoming adjacent or offscreen.
- [ ] When back-face content exceeds one card view, users can continue reading it through in-place vertical scrolling while the same card remains centered and active.
- [ ] Future cards remain visually viewable, can become active, and can show both faces when active.
- [ ] Completed cards have a distinct visual treatment that preserves readability and interaction.
- [ ] The card rail preserves a bold rounded Light the World visual tone with red as the dominant accent, white or cream as the base, navy as supporting contrast, and gold used sparingly for emphasis.
- [ ] Horizontal browsing, snap-to-center behavior, and face changes feel calm and clean rather than flashy.
- [ ] Accessibility users can move card-by-card through the sequence, change the active card between front and back without relying on gesture-only interaction, and fully read long back-face content.

---

## 12. Open Questions
- None at this time.
