# Protocol: Giving Machine Accessibility

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/contracts/giving-machine-donation-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
GivingMachineAccessibilityProtocol

---

## 2. Purpose
Expose accessible Giving Machine presentation state and assistive actions for entry, browse, arming, explicit confirmation, cart access, info access, and dismissal without relying on gesture-only interaction.

---

## 3. Inputs
- `giving machine peek state: object` - persistent bottom-sheet entry surface labeled `Giving Machine`.
- `giving machine destination state: object` - expanded, closed, or contextual Giving Machine surface state.
- `machine window state: object or null` - visible numbered-slot window used to browse catalog items.
- `slot selection state: object or null` - currently selected and armed slot presentation.
- `add-to-cart confirmation state: object or null` - explicit confirmation affordance state for the armed item.
- `cart or checkout presentation state: object or null` - cart or checkout presentation state.
- `info presentation state: object or null` - separate info-screen presentation state.
- `accessibility action request: event` - assistive action requesting entry, browse, arm, confirm, cart or checkout, info, or dismiss behavior.

---

## 4. Outputs
- `accessibility presentation state: object` - current visible context, expanded or closed state, visible slot numbers, armed-slot state, and available actions.
- `failure response: object or null` - explicit failure when accessible presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `GivingMachineSurfaceState`
  - `sheet state: enum` - `peek`, `expanded`, or `closed`.
  - `visible context: enum` - `machine_browse`, `cart_or_checkout`, or `info`.
  - `dismiss actions: collection` - visible actions that allow the user to close the Giving Machine surface.
- `MachineWindowState`
  - `visible slot items: collection` - currently visible numbered slots and their presented items.
  - `window position state: enum` - `top`, `middle`, `bottom`, or `single_window`.
  - `peek continuation state: enum` - `peek_above_and_below`, `peek_above_only`, `peek_below_only`, or `no_peek`.
- `AddToCartConfirmationState`
  - `armed slot: identifier` - currently armed slot awaiting explicit confirmation.
  - `confirmation state: enum` - `required`, `confirmed`, or `not_available`.

---

## 6. Behavior Requirements
1. Must expose the `Giving Machine` entry surface to accessibility users.
2. Must expose expanded or closed Giving Machine state.
3. Must expose the current visible context.
4. Must expose available dismissal actions.
5. Must expose visible slot numbers.
6. Must expose armed-slot state.
7. Must expose the add-to-cart confirmation action when applicable.
8. Must expose cart or checkout entry action.
9. Must expose info-screen entry action.
10. Must allow accessibility users to open the Giving Machine surface without relying on gesture-only interaction.
11. Must allow accessibility users to browse the machine window without relying on gesture-only interaction.
12. Must allow accessibility users to arm a visible slot without relying on gesture-only interaction.
13. Must allow accessibility users to confirm add to cart without relying on gesture-only interaction.
14. Must allow accessibility users to open cart or checkout presentation, open the info screen, and dismiss the Giving Machine surface without relying on gesture-only interaction.
15. Must return a failure response and must not expose unlabeled interactive machine content when the current visible context, slot identity, armed state, or available actions cannot be exposed.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `ACCESSIBILITY_STATE_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine donation pricing, cart math, checkout, payment, receipt, or refund behavior
- Must not rely on gesture-only interaction for required Giving Machine actions
- Must not expose unlabeled interactive machine content

---

## 9. Open Questions
- None at this time.
