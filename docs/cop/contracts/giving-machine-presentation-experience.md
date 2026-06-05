# Contract: Giving Machine Presentation Experience

Version: 0.1.0  
Status: Draft

---

## 1. Purpose
Define the user-visible presentation, navigation, and interaction behavior for entering, browsing, and exiting the Giving Machine experience from the daily challenge home experience.

---

## 2. Actors
- User — opens the Giving Machine surface, browses catalog items, selects an item, confirms add-to-cart, opens cart or checkout presentation, opens the info screen, and exits the Giving Machine surface.
- Accessibility Service User — performs Giving Machine entry, browse, selection, confirmation, cart access, info access, and exit actions without relying on gesture-only interaction.
- Giving Machine Presentation State Source — provides the catalog presentation state, slot content, cart-entry presentation state, and info presentation content required by this experience.

---

## 3. Inputs
- home challenge surface state: object — current daily challenge home experience on which the Giving Machine entry surface is presented.
- giving machine catalog: collection — ordered set of catalog items available for Giving Machine presentation.
- current catalog state: state — whether catalog content is available, unavailable, or empty for presentation.
- machine entry request: event — user action requesting that the Giving Machine surface open from its persistent peek state.
- machine dismiss request: event — user action requesting that the Giving Machine surface close.
- machine browse request: event — user action requesting movement through the visible machine window by browse control or direct scroll interaction.
- item detail request: event — user action requesting full detail for a visible presented Giving Machine item.
- add-to-cart confirmation request: event — user action explicitly confirming from the item detail presentation that the selected item should be added to cart.
- cart presentation request: event — user action requesting entry into cart or checkout presentation.
- info presentation request: event — user action requesting entry into the separate info screen.
- current armed slot: identifier or null — currently armed catalog slot, if one exists.

---

## 4. Outputs
- giving machine peek state: object — persistent bottom-sheet entry surface labeled `Giving Machine` shown on the daily challenge home experience.
- giving machine destination state: object — expanded full-height Giving Machine presentation.
- machine window state: object — visible numbered-slot window used to browse catalog items.
- empty machine state: object — non-transactional empty presentation shown when no Giving Machine items are available.
- item detail presentation state: object — modal or sheet presentation showing full item detail and explicit add-to-cart action for the selected item.
- slot selection state: object — currently selected and armed slot presentation.
- add-to-cart confirmation state: object — explicit confirmation affordance required before an armed item is added to cart.
- dispense animation state: object — short user-visible dispense or fall presentation shown after a successful add-to-cart action.
- cart or checkout presentation state: object — clearer less theatrical task-focused surface for cart review and checkout entry.
- info presentation state: object — separate non-transactional info screen for acknowledgements and app information.
- return-to-home state: object — resumed daily challenge home presentation after the Giving Machine surface closes.
- failure response: object — explicit error or rejection response when presentation cannot be fulfilled.

---

## 5. Data Models

### Presented Giving Machine Item
- item identifier: identifier — presented catalog item reference.
- slot number: string or integer — numbered slot identity assigned to the item according to catalog order.
- title: string — presented catalog item title.
- description: string — short presented catalog item description.
- image reference: optional reference — optional presentation image shown for the item when available.
- selection state: enum — `unselected` or `armed`.

### Giving Machine Surface State
- sheet state: enum — `peek`, `expanded`, or `closed`.
- visible context: enum — `machine_browse`, `cart_or_checkout`, or `info`.
- dismiss actions: collection — visible actions that allow the user to close the Giving Machine surface.

### Machine Window State
- visible slot items: collection — currently visible numbered slots and their presented items.
- window position state: enum — `top`, `middle`, `bottom`, or `single_window`.
- peek continuation state: enum — `peek_above_and_below`, `peek_above_only`, `peek_below_only`, or `no_peek`.

### Item Detail Presentation State
- item identifier: identifier — selected presented item reference.
- slot number: string or integer — slot identity for the selected item.
- title: string — full presented item title.
- description: string — full presented item description.
- image reference: optional reference — optional presentation image shown when available.
- action state: enum — `detail_only` or `add_available`.

### Add-To-Cart Confirmation State
- armed slot: identifier — currently armed slot awaiting explicit confirmation.
- confirmation state: enum — `required`, `confirmed`, or `not_available`.

### Info Presentation State
- screen title: string — visible info-screen title.
- content sections: collection — acknowledgements and app-information sections presented on the info screen.
- interaction state: enum — `informational`.

### Empty Machine State
- message: string — user-visible message indicating that no Giving Machine items are available.
- interaction state: enum — `informational`.

---

## 6. Success Behavior

1. The daily challenge home experience must remain the home surface of the app.
2. While the user is on the daily challenge home surface, the system must present a persistent peeking bottom sheet labeled `Giving Machine`.
3. The Giving Machine peek state must remain visible as an entry surface while the daily challenge home experience remains visible.
4. The system must allow the Giving Machine surface to open from the peek state by tap or swipe-up interaction.
5. When opened from the peek state, the Giving Machine surface must expand into a full-height destination that visually prioritizes the Giving Machine experience over the daily challenge home surface.
6. When the Giving Machine surface closes, the system must return the user to the daily challenge home surface rather than an unrelated app surface.
7. The system must allow the expanded Giving Machine surface to close by a visible `X` dismissal control or by swipe-down interaction.
8. When the Giving Machine catalog is available and contains items, the expanded Giving Machine browse surface must present the catalog through a machine-window metaphor.
9. The machine browse surface must feel immersive and machine-like, as though the user is looking through a real machine window behind glass.
10. When enough catalog items exist, the visible machine window must show two rows by two columns of slots at one time.
11. When more catalog items exist above or below the visible machine window, the system must show additional items partially peeking above or below the visible window in row-aligned continuation ledges to indicate continued browsing.
12. When the visible machine window is at the top of the catalog, the system must show only lower continuation peeks if more items exist below.
13. When the visible machine window is at the bottom of the catalog, the system must show only upper continuation peeks if more items exist above.
14. When the catalog contains fewer visible items than a full two-by-two window, the system must present only available items and must not fabricate unavailable catalog items.
15. Each machine-window browse request must move the visible slot window through the catalog by exactly one visible row while preserving slot identity.
16. Direct vertical scroll interaction on the machine browse surface must browse through the catalog using that same one-visible-row progression rather than creating a separate free-form browse model.
17. The system must preserve slot numbering identity for browse, selection, confirmation, and accessibility behavior, but the main visible item card should prioritize readable content and optional imagery rather than reserving primary card space for a large visible slot number.
18. Slot numbering must follow catalog order and remain consistent for an item while that item remains present in the catalog.
19. A single tap on a visible item must open an item detail presentation for that visible item and must not add the item to cart by itself.
20. The main visible item card may truncate title or description text as needed for a readable browse presentation, but the item detail presentation must expose the full selected-item content.
21. The item detail presentation must preserve slot identity and must present full title, description, optional image, and an explicit add-to-cart action for the selected item.
22. The system must require that explicit add-to-cart action from the item detail presentation before the selected item is added to cart.
23. If the detail presentation invokes add to cart, the system must use the same approved armed-then-confirmed selection boundary internally rather than bypassing it.
24. When one item becomes armed for add confirmation, any previously armed item must return to the unselected state.
25. Any surfaced armed state must remain visually distinct from unselected slots.
26. If the user opens a different item detail presentation before confirming add to cart, the previously viewed or armed item must not be added to cart.
27. After a successful add-to-cart confirmation, the system must present a short dispense or fall animation for the item at the experience level.
28. The dispense animation must feel brief and responsive rather than theatrical enough to block continued browsing.
29. After the dispense animation completes, the system must clear the armed state unless the user explicitly re-arms an item.
30. The cart or checkout presentation must remain visually connected to the Giving Machine experience but must feel clearer, calmer, and less theatrical than machine browsing.
31. Entering cart or checkout presentation must shift emphasis from immersive machine browsing toward clear task completion and review.
32. Returning from cart or checkout presentation to machine browsing within the Giving Machine surface must preserve the broader Giving Machine destination context.
33. The Giving Machine experience must provide access to a separate `Info` screen for acknowledgements and app information.
34. The `Info` screen must be non-transactional and must not arm items or add items to cart by itself.
35. Returning from the `Info` screen must return the user to the Giving Machine surface rather than exiting the experience entirely unless the user explicitly dismisses it.
36. The `Info` screen and item detail presentation must preserve readable contrast between text and background.
37. When the catalog is available but empty, the system must present a non-transactional empty state indicating that no Giving Machine items are available and must not fabricate visible slots.
38. Accessibility interaction must expose the `Giving Machine` entry surface, expanded or closed state, current visible context, available dismissal actions, visible slot numbers, add-to-cart confirmation action, cart or checkout entry action, and info-screen entry action.
39. Accessibility users must be able to open, browse, inspect item detail, confirm add to cart, open cart or checkout presentation, open the info screen, and dismiss the Giving Machine surface without relying on gesture-only interaction.

---

## 7. Failure Modes

- Condition: the Giving Machine catalog is unavailable when the Giving Machine surface is opened or browsed
  - System must: return a failure response and must not fabricate machine slots or catalog items

- Condition: the catalog is available but required slot content is missing for a visible presented item
  - System must: return a failure response and must not present that item as a selectable slot

- Condition: slot selection request targets an item not currently present in the visible machine window
  - System must: preserve the current armed state and return a failure response

- Condition: item detail request targets an item not currently present in the visible machine window
  - System must: preserve the current browse state and return a failure response

- Condition: add-to-cart confirmation request occurs when no slot is armed
  - System must: preserve the current visible state and return a failure response

- Condition: dispense animation cannot be presented after a successful add-to-cart result is returned by the donation flow
  - System must: preserve the successful add result and return a non-animated confirmation state instead of claiming the item was not added

- Condition: the Giving Machine surface cannot expose a visible dismissal action while expanded
  - System must: return a failure response and must not present the expanded Giving Machine surface as dismissible

- Condition: the info screen is requested but its informational content is unavailable
  - System must: return a failure response and must not fabricate acknowledgements or app information

- Condition: accessibility presentation cannot expose the current visible context, slot identity, armed state, or available actions
  - System must: return a failure response and must not expose unlabeled interactive machine content

---

## 8. Edge Cases

- The user opens the Giving Machine surface and immediately dismisses it without browsing.
- The catalog is available but empty.
- The catalog contains fewer than four items.
- The visible machine window is at the top of the catalog.
- The visible machine window is at the bottom of the catalog.
- The user arms one item and then taps a different visible item before confirming add to cart.
- The user opens one item detail presentation and then opens a different visible item detail presentation before confirming add to cart.
- The user arms an item and dismisses the Giving Machine surface before confirming add to cart.
- The user adds an item to cart and immediately continues browsing.
- The user moves from machine browsing into cart or checkout presentation and then returns to machine browsing.
- The user opens the `Info` screen and then returns to the Giving Machine surface.
- An accessibility service user browses slot-by-slot and confirms add to cart without gesture browsing.

---

## 9. Constraints

- Must not define donation behavior outside this contract.
- Must not redefine donation pricing, cart math, checkout, payment, receipt, or refund business rules already defined elsewhere.
- Must preserve the daily challenge home experience as the home surface of the app.
- Must present Giving Machine entry as a persistent peeking bottom sheet labeled `Giving Machine` on the home surface.
- Must allow Giving Machine to open by tap or swipe-up and close by visible `X` or swipe-down.
- Must make the expanded Giving Machine surface feel like a full-height destination.
- Must preserve an immersive machine-window browse metaphor for catalog browsing.
- Must preserve items in numbered slots and maintain slot identity even if visible card chrome does not prominently display the slot number inside the main card body.
- Must require explicit confirmation before an armed item is added to cart.
- Must allow the user-visible add confirmation action to live inside the selected item's detail presentation.
- Must keep cart or checkout presentation clearer and less theatrical than machine browsing.
- Must keep the `Info` screen behaviorally separate from machine browsing and cart or checkout presentation.
- Must not fabricate catalog items, slot content, or info content when required presentation inputs are unavailable.

---

## 10. Observability

### Events
- giving_machine_peek_viewed
- giving_machine_opened
- giving_machine_dismissed
- giving_machine_window_browsed
- giving_machine_item_detail_opened
- giving_machine_slot_armed
- giving_machine_add_confirmation_presented
- giving_machine_item_dispensed
- giving_machine_cart_presentation_opened
- giving_machine_info_opened
- giving_machine_presentation_failed

### Metrics
- giving machine open rate
- giving machine dismiss rate
- machine window browse rate
- item detail open rate
- slot arm rate
- add-to-cart confirmation rate
- dispense animation completion rate
- cart or checkout presentation entry rate
- info-screen entry rate
- giving machine presentation failure rate

### Logs
- giving machine entry outcome
- machine window browse outcome
- item detail presentation outcome
- slot arm outcome
- add-to-cart confirmation presentation outcome
- dispense animation outcome
- cart or checkout presentation transition outcome
- info-screen presentation outcome
- giving machine presentation failure reason

---

## 11. Acceptance Criteria

- [ ] Users see a persistent peeking bottom sheet labeled `Giving Machine` on the daily challenge home surface.
- [ ] Users can open the Giving Machine surface by tap or swipe-up and close it by visible `X` or swipe-down.
- [ ] When opened, the Giving Machine surface behaves like a full-height destination and returns users to the daily challenge home surface when dismissed.
- [ ] When catalog items are available, machine browsing presents an immersive machine-window view of numbered slots.
- [ ] When enough items exist, the visible machine window shows two rows by two columns of slots with additional items peeking above or below when more items exist, and each browse request or direct scroll advances the window by exactly one visible row.
- [ ] A single tap arms one visible item without adding it to cart.
- [ ] A single tap opens item detail for one visible item without adding it to cart.
- [ ] Item cards may truncate browse copy, but the item detail presentation exposes the full selected-item content with readable contrast.
- [ ] A separate explicit add-to-cart action is required from the item detail presentation before an item is added to cart.
- [ ] After a successful add-to-cart confirmation, the experience shows a short dispense or fall animation.
- [ ] Cart or checkout presentation feels clearer and less theatrical than machine browsing.
- [ ] The `Info` screen exists as a separate non-transactional screen for acknowledgements and app information, and both it and the item detail presentation remain readable with clear text-to-background contrast.
- [ ] When the catalog is available but empty, the Giving Machine surface shows an empty state rather than fabricated slots.
- [ ] Accessibility users can open, browse, inspect item detail, confirm add to cart, enter cart or checkout presentation, open the info screen, and dismiss the Giving Machine surface without relying on gesture-only interaction.

---

## 12. Open Questions
- None at this time.
