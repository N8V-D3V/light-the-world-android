# Protocol: Giving Machine Transition

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/contracts/giving-machine-donation-experience.md`

Reference Context:
- `docs/cop/product/product-vision.md`

---

## 1. Name
GivingMachineTransitionProtocol

---

## 2. Purpose
Manage presentation transitions from machine browsing into cart or checkout presentation, the separate non-transactional `Info` screen, and returns to the Giving Machine surface without defining donation checkout behavior.

---

## 3. Inputs
- `giving machine destination state: object` - expanded full-height Giving Machine presentation.
- `cart presentation request: event` - user action requesting entry into cart or checkout presentation.
- `return from cart or checkout request: event` - user action requesting return from cart or checkout presentation to machine browsing within the Giving Machine destination.
- `info presentation request: event` - user action requesting entry into the separate info screen.
- `return from info request: event` - user action requesting return from the info screen to machine browsing within the Giving Machine destination.
- `info presentation content: object or null` - acknowledgements and app-information content required by the info screen.

---

## 4. Outputs
- `cart or checkout presentation state: object` - clearer less theatrical task-focused surface for cart review and checkout entry.
- `info presentation state: object` - separate non-transactional info screen for acknowledgements and app information.
- `giving machine destination state: object` - updated expanded destination context after transitions.
- `failure response: object or null` - explicit failure when transition or info presentation cannot be fulfilled.

---

## 5. Data Model Expectations
- `GivingMachineSurfaceState`
  - `sheet state: enum` - `peek`, `expanded`, or `closed`.
  - `visible context: enum` - `machine_browse`, `cart_or_checkout`, or `info`.
  - `dismiss actions: collection` - visible actions that allow the user to close the Giving Machine surface.
- `InfoPresentationState`
  - `screen title: string` - visible info-screen title.
  - `content sections: collection` - acknowledgements and app-information sections presented on the info screen.
  - `interaction state: enum` - `informational`.

---

## 6. Behavior Requirements
1. Must keep cart or checkout presentation visually connected to the Giving Machine experience.
2. Must make cart or checkout presentation clearer, calmer, and less theatrical than machine browsing.
3. Must shift emphasis from immersive machine browsing toward clear task completion and review when entering cart or checkout presentation.
4. Must accept an explicit return from cart or checkout request.
5. Must produce an updated Giving Machine destination state with `sheet state` of `expanded` and `visible context` of `machine_browse` when returning from cart or checkout presentation.
6. Must preserve the broader Giving Machine destination context when returning from cart or checkout presentation to machine browsing.
7. Must provide access to a separate `Info` screen for acknowledgements and app information.
8. Must keep the `Info` screen non-transactional.
9. Must not arm items or add items to cart from the `Info` screen.
10. Must accept an explicit return from info request.
11. Must produce an updated Giving Machine destination state with `sheet state` of `expanded` and `visible context` of `machine_browse` when returning from the `Info` screen.
12. Must return the user from the `Info` screen to the Giving Machine surface unless the user explicitly dismisses the Giving Machine surface.
13. Must return a failure response and must not fabricate acknowledgements or app information when info content is unavailable.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `INFO_CONTENT_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic
- Must not redefine donation pricing, cart math, checkout, payment, receipt, or refund behavior
- Must preserve the approved separate `Info` screen role
- Must preserve the approved calmer less-theatrical cart or checkout presentation requirement
- Must produce an explicit `machine_browse` destination state for return transitions from both cart or checkout and info contexts

---

## 9. Open Questions
- None at this time.
