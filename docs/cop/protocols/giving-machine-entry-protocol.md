# Protocol: Giving Machine Entry

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
GivingMachineEntryProtocol

---

## 2. Purpose
Provide the persistent Giving Machine entry surface, expanded full-height destination state, dismissal state, and return-to-home presentation without defining donation business behavior.

---

## 3. Inputs
- `home challenge surface state: object` - current daily challenge home experience on which the Giving Machine entry surface is presented.
- `machine entry request: event` - user action requesting that the Giving Machine surface open from its persistent peek state.
- `machine dismiss request: event` - user action requesting that the Giving Machine surface close.

---

## 4. Outputs
- `giving machine peek state: object` - persistent bottom-sheet entry surface labeled `Giving Machine` shown on the daily challenge home experience.
- `giving machine destination state: object` - expanded full-height Giving Machine presentation.
- `return-to-home state: object` - resumed daily challenge home presentation after the Giving Machine surface closes.
- `failure response: object or null` - explicit failure when entry, expanded destination, or dismissal presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `GivingMachineSurfaceState`
  - `sheet state: enum` - `peek`, `expanded`, or `closed`.
  - `visible context: enum` - `machine_browse`, `cart_or_checkout`, or `info`.
  - `dismiss actions: collection` - visible actions that allow the user to close the Giving Machine surface.

---

## 6. Behavior Requirements
1. Must preserve the daily challenge home experience as the home surface of the app.
2. Must present a persistent peeking bottom sheet labeled `Giving Machine` while the daily challenge home experience remains visible.
3. Must allow the Giving Machine surface to open from the peek state by tap or swipe-up interaction.
4. Must expand the Giving Machine surface into a full-height destination that visually prioritizes the Giving Machine experience over the daily challenge home surface.
5. Must allow the expanded Giving Machine surface to close by a visible `X` dismissal control or by swipe-down interaction.
6. Must return the user to the daily challenge home surface when the Giving Machine surface closes.
7. Must return a failure response when the expanded Giving Machine surface cannot expose a visible dismissal action.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `DISMISS_ACTION_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine donation pricing, cart math, checkout, payment, receipt, or refund behavior
- Must preserve the approved persistent bottom-sheet entry and full-height destination behavior

---

## 9. Open Questions
- None at this time.
