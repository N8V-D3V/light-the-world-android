# Protocol: Giving Machine Window

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/contracts/giving-machine-donation-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
GivingMachineWindowProtocol

---

## 2. Purpose
Provide the machine-window browse presentation for catalog items, including visible numbered slots, window position, continuation peeks, and empty machine state without defining catalog availability business rules.

---

## 3. Inputs
- `giving machine catalog: collection` - ordered set of catalog items available for Giving Machine presentation.
- `current catalog state: state` - whether catalog content is available, unavailable, or empty for presentation.
- `machine browse request: event` - user action requesting movement through the visible machine window by exactly one visible row.

---

## 4. Outputs
- `machine window state: object` - visible numbered-slot window used to browse catalog items.
- `empty machine state: object or null` - non-transactional empty presentation shown when no Giving Machine items are available.
- `failure response: object or null` - explicit failure when machine-window presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `PresentedGivingMachineItem`
  - `item identifier: identifier` - presented catalog item reference.
  - `slot number: string or integer` - numbered slot identity assigned to the item according to catalog order.
  - `title: string` - presented catalog item title.
  - `description: string` - short presented catalog item description.
  - `selection state: enum` - `unselected` or `armed`.
- `MachineWindowState`
  - `visible slot items: collection` - currently visible numbered slots and their presented items.
  - `window position state: enum` - `top`, `middle`, `bottom`, or `single_window`.
  - `peek continuation state: enum` - `peek_above_and_below`, `peek_above_only`, `peek_below_only`, or `no_peek`.
- `EmptyMachineState`
  - `message: string` - user-visible message indicating that no Giving Machine items are available.
  - `interaction state: enum` - `informational`.

---

## 6. Behavior Requirements
1. Must present the catalog through a machine-window metaphor when the catalog is available and contains items.
2. Must preserve an immersive machine-like browse presentation, as though the user is looking through a real machine window behind glass.
3. Must show three rows by three columns of numbered slots at one time when enough catalog items exist.
4. Must show additional slots partially peeking above or below the visible window when more catalog items exist above or below.
5. Must show only lower continuation peeks when the visible machine window is at the top of the catalog and more items exist below.
6. Must show only upper continuation peeks when the visible machine window is at the bottom of the catalog and more items exist above.
7. Must present only available items when the catalog contains fewer visible items than a full three-by-three window.
8. Must not fabricate unavailable catalog items or slots.
9. Must move the visible slot window through the catalog for machine-window browse requests while preserving numbered slot presentation.
10. Must advance the visible slot window by exactly one visible row for each machine-window browse request.
11. Must assign slot numbers according to catalog order.
12. Must keep a slot number consistent for an item while that item remains present in the catalog.
13. Must present a non-transactional empty state when the catalog is available but empty.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `GIVING_MACHINE_CATALOG_UNAVAILABLE`
  - `REQUIRED_SLOT_CONTENT_MISSING`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine donation availability rules beyond presented state, pricing rules, cart math, checkout, payment, receipt, or refund behavior
- Must preserve the approved machine-window metaphor and numbered-slot presentation
- Must preserve the approved one-visible-row browse-step granularity for each machine-window browse request

---

## 9. Open Questions
- None at this time.
