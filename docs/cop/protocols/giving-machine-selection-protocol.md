# Protocol: Giving Machine Selection

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/contracts/giving-machine-donation-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
GivingMachineSelectionProtocol

---

## 2. Purpose
Manage visible slot arming, detail-driven explicit add-to-cart confirmation presentation, and experience-level dispense animation state without performing cart updates or donation business behavior.

---

## 3. Inputs
- `machine window state: object` - visible numbered-slot window used to browse catalog items.
- `slot selection request: event` - presentation request placing the selected visible slot into the approved armed state before add confirmation.
- `add-to-cart confirmation request: event` - explicit add-to-cart action for the selected item, including when that action originates from item detail presentation.
- `current armed slot: identifier or null` - currently armed catalog slot, if one exists.
- `successful add-to-cart result: object or null` - indication from the donation flow that the confirmed item was successfully added to cart.

---

## 4. Outputs
- `slot selection state: object` - currently selected and armed slot presentation.
- `add-to-cart confirmation state: object` - explicit confirmation affordance required before an armed item is added to cart.
- `dispense animation state: object` - short user-visible dispense or fall presentation shown after a successful add-to-cart action.
- `failure response: object or null` - explicit failure when selection, confirmation, or dispense presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `PresentedGivingMachineItem`
  - `item identifier: identifier` - presented catalog item reference.
  - `slot number: string or integer` - numbered slot identity assigned to the item according to catalog order.
  - `title: string` - presented catalog item title.
  - `description: string` - short presented catalog item description.
  - `selection state: enum` - `unselected` or `armed`.
- `AddToCartConfirmationState`
  - `armed slot: identifier` - currently armed slot awaiting explicit confirmation.
  - `confirmation state: enum` - `required`, `confirmed`, or `not_available`.

---

## 6. Behavior Requirements
1. Must place the selected visible item's numbered slot into an armed state without adding the item to cart.
2. Must support presentation flows where the user first opens item detail and then invokes add to cart from that detail presentation.
3. Must return any previously armed item to the unselected state when one item becomes armed.
4. Must make any surfaced armed state visually distinct from unselected slots.
5. Must present a separate explicit add-to-cart confirmation affordance for the armed item, including when that affordance is hosted inside item detail presentation.
6. Must require explicit confirmation before the armed item is added to cart.
7. Must not add a previously armed item to cart if the user changes the selected item before confirming add to cart.
8. Must preserve the current armed state and return a failure response when a slot selection request targets an item not currently present in the visible machine window.
9. Must preserve the current visible state and return a failure response when add-to-cart confirmation is requested with no armed slot.
10. Must present a short dispense or fall animation after a successful add-to-cart confirmation result is returned by the donation flow.
11. Must keep the dispense animation brief and responsive rather than theatrical enough to block continued browsing.
12. Must clear the armed state after the dispense animation completes unless the user explicitly re-arms an item.
13. Must preserve the successful add result and return a non-animated confirmation state when dispense animation cannot be presented.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `SLOT_NOT_VISIBLE`
  - `NO_ARMED_SLOT`
  - `DISPENSE_ANIMATION_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not perform cart updates, cart math, pricing, checkout, payment, receipt, or refund behavior
- Must preserve the approved detail-opened and armed-then-confirmed add flow

---

## 9. Open Questions
- None at this time.
