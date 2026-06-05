# Architecture Plan: Giving Machine Presentation Experience

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/contracts/giving-machine-donation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`
- `docs/cop/protocols/giving-machine-entry-protocol.md`
- `docs/cop/protocols/giving-machine-window-protocol.md`
- `docs/cop/protocols/giving-machine-selection-protocol.md`
- `docs/cop/protocols/giving-machine-transition-protocol.md`
- `docs/cop/protocols/giving-machine-accessibility-protocol.md`

---

## 1. Purpose
Describe how the approved Giving Machine presentation contract and protocol definitions will be translated into a focused Android presentation-slice structure for entry, expanded destination presentation, machine-window browsing, item-detail review, slot arming and explicit add confirmation, dispense presentation, contextual transitions, and accessibility interaction.

This plan keeps Giving Machine presentation behavior separate from donation business behavior such as catalog business rules, pricing, cart math, checkout processing, payment, and receipt delivery. The slice may consume catalog-derived presentation inputs and may hand a confirmed add-to-cart request across a presentation boundary, but it must not own donation business outcomes beyond contract-defined presentation responses to those outcomes.

---

## 2. Source Artifacts
- Contract: `docs/cop/contracts/giving-machine-presentation-experience.md`
- Contract: `docs/cop/contracts/giving-machine-donation-experience.md`
- Contract: `docs/cop/contracts/daily-service-challenge-experience.md`
- Protocols:
  - `docs/cop/protocols/giving-machine-entry-protocol.md`
  - `docs/cop/protocols/giving-machine-window-protocol.md`
  - `docs/cop/protocols/giving-machine-selection-protocol.md`
  - `docs/cop/protocols/giving-machine-transition-protocol.md`
  - `docs/cop/protocols/giving-machine-accessibility-protocol.md`
- Reference context:
  - `docs/cop/product/product-vision.md`
- Kotlin protocol surfaces:
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/GivingMachinePresentationTypes.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/entry/GivingMachineEntryPresenter.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/window/GivingMachineWindowPresenter.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/selection/GivingMachineSelectionPresenter.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/transition/GivingMachineTransitionPresenter.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/accessibility/GivingMachineAccessibilityPresenter.kt`

---

## 3. Module Breakdown
- `GivingMachinePresentationExperienceModule` - feature boundary for the Giving Machine presentation slice under the donation area; owns presentation-slice coordination inputs and outputs only.
- `GivingMachineEntryPresentationModule` - fulfills `GivingMachineEntryProtocol` and owns the persistent `Giving Machine` peek state on the daily challenge home surface, full-height destination entry state, dismissal state, and return-to-home presentation.
- `GivingMachineWindowPresentationModule` - fulfills `GivingMachineWindowProtocol` and owns the immersive machine-window browse presentation, numbered-slot window state, continuation peeks, and empty machine state.
- `GivingMachineSelectionPresentationModule` - fulfills `GivingMachineSelectionProtocol` and owns visible-slot arming, explicit add-to-cart confirmation presentation for selected-item detail flows, brief dispense presentation state, and post-dispense armed-state clearing.
- `GivingMachineTransitionPresentationModule` - fulfills `GivingMachineTransitionProtocol` and owns transitions into cart or checkout presentation, returns from cart or checkout, transitions into the separate `Info` screen, and returns from the `Info` screen.
- `GivingMachineAccessibilityPresentationModule` - fulfills `GivingMachineAccessibilityProtocol` and owns accessible entry, context, slot, armed-state, confirmation, cart-entry, info-entry, and dismissal presentation state.

---

## 4. Protocol-to-Module Map
- `GivingMachineEntryProtocol` -> `GivingMachineEntryPresentationModule`
- `GivingMachineWindowProtocol` -> `GivingMachineWindowPresentationModule`
- `GivingMachineSelectionProtocol` -> `GivingMachineSelectionPresentationModule`
- `GivingMachineTransitionProtocol` -> `GivingMachineTransitionPresentationModule`
- `GivingMachineAccessibilityProtocol` -> `GivingMachineAccessibilityPresentationModule`

---

## 5. Dependencies
- `GivingMachinePresentationExperienceModule` depends on `GivingMachineEntryProtocol`, `GivingMachineWindowProtocol`, `GivingMachineSelectionProtocol`, `GivingMachineTransitionProtocol`, and `GivingMachineAccessibilityProtocol`.
- `GivingMachinePresentationExperienceModule` depends on upstream-provided home-surface state, catalog presentation inputs, info content, and add-to-cart result inputs only; it must not own catalog availability rules, pricing, cart math, checkout, payment, receipt, or refund behavior.
- `GivingMachineEntryPresentationModule` depends on home-surface state, machine entry requests, and machine dismiss requests only.
- `GivingMachineWindowPresentationModule` depends on catalog presentation inputs and machine browse requests only.
- `GivingMachineSelectionPresentationModule` depends on visible machine-window state, selected-item detail flows, slot-selection requests, add-to-cart confirmation requests, current armed slot, and successful add-to-cart result inputs only.
- `GivingMachineTransitionPresentationModule` depends on current expanded destination state, cart-presentation requests, return-from-cart-or-checkout requests, info-presentation requests, return-from-info requests, and info presentation content only.
- `GivingMachineAccessibilityPresentationModule` depends on current peek state, destination state, machine-window state, slot-selection state, add-to-cart confirmation state, cart-or-checkout presentation state, info presentation state, and accessibility action requests only.
- `GivingMachineWindowPresentationModule` must not redefine entry, dismissal, slot arming, confirmation, or transition rules outside machine-window browsing.
- `GivingMachineSelectionPresentationModule` must not perform cart updates or compute cart state; it must stop at the explicit confirmation boundary and wait for upstream add-to-cart results.
- `GivingMachineTransitionPresentationModule` must not own checkout processing or cart review business state; it must own only the presentation transition into and out of those contexts.
- `GivingMachineAccessibilityPresentationModule` must not create an alternative business path; it must expose and invoke the same presentation boundaries already defined by entry, window, selection, and transition behavior.

---

## 6. Orchestration Boundaries
- `GivingMachinePresentationSliceOrchestrator` coordinates `GivingMachineEntryProtocol`, `GivingMachineWindowProtocol`, `GivingMachineSelectionProtocol`, `GivingMachineTransitionProtocol`, and `GivingMachineAccessibilityProtocol` within this presentation slice only.
- `GivingMachinePresentationSliceOrchestrator` must not embed donation business logic for catalog availability rules, pricing, cart math, checkout, payment, receipt delivery, or refund handling.
- `GivingMachinePresentationSliceOrchestrator` must treat `GivingMachineEntryProtocol` as the authoritative source for persistent peek entry state, expanded full-height destination state, dismissal presentation, and return-to-home presentation.
- `GivingMachinePresentationSliceOrchestrator` must treat `GivingMachineWindowProtocol` as the authoritative source for visible slot window state, numbered-slot presentation, continuation peek behavior, and empty machine presentation.
- `GivingMachinePresentationSliceOrchestrator` must treat `GivingMachineSelectionProtocol` as the authoritative source for armed-slot state, explicit confirmation presentation, and dispense animation or non-animated confirmation presentation after an upstream successful add result.
- `GivingMachinePresentationSliceOrchestrator` must treat `GivingMachineTransitionProtocol` as the authoritative source for contextual transitions into `cart_or_checkout` and `info`, and for explicit returns to `machine_browse`.
- `GivingMachinePresentationSliceOrchestrator` must use `GivingMachineAccessibilityProtocol` to expose accessible state and actions without bypassing the standard presentation boundaries.
- `GivingMachinePresentationSliceOrchestrator` must preserve the explicit handoff boundary where a confirmed add-to-cart request leaves the presentation slice and donation or cart behavior returns a success result later for presentation.
- `GivingMachinePresentationSliceOrchestrator` must preserve the contract-defined rule that dispense animation is presentation-only and must not determine whether an item was actually added.
- `GivingMachinePresentationSliceOrchestrator` must stop or continue only according to contract-defined success and failure behavior.

---

## 7. Data Flow
1. Persistent peek entry state:
   `home challenge surface state` enters `GivingMachineEntryProtocol`.
   `GivingMachineEntryProtocol` returns a persistent `giving machine peek state` labeled `Giving Machine` while the daily challenge home surface remains visible.
   `GivingMachinePresentationSliceOrchestrator` keeps this entry state available without changing the daily challenge home experience into a donation-owned home surface.
2. Expand and dismiss behavior:
   `machine entry request` enters `GivingMachineEntryProtocol`.
   `GivingMachineEntryProtocol` returns an expanded `giving machine destination state` with a full-height destination presentation.
   Later, `machine dismiss request` enters `GivingMachineEntryProtocol`, which returns `return-to-home state` and closes the destination while returning the user to the daily challenge home surface.
3. Machine-window browse behavior:
   `giving machine catalog`, `current catalog state`, and `machine browse request` enter `GivingMachineWindowProtocol`.
   `GivingMachineWindowProtocol` returns either `machine window state` with a two-by-two visible slot window, slot identity, window position, and continuation peeks, or `empty machine state` when the catalog is available but empty.
   `GivingMachinePresentationSliceOrchestrator` uses that output as the current browse context for selection and accessibility presentation.
4. Item detail, armed-slot selection, and explicit add confirmation:
   The UI layer may open an item-detail presentation directly from a visible item tap so full copy and optional image can be shown without overloading the browse card.
   `machine window state`, `slot selection request`, and `current armed slot` then enter `GivingMachineSelectionProtocol` only when the selected-item detail flow invokes add to cart.
   `GivingMachineSelectionProtocol` returns updated `slot selection state` and `add-to-cart confirmation state`, preserving the rule that the selected visible slot becomes armed but is not added by itself.
   When `add-to-cart confirmation request` enters `GivingMachineSelectionProtocol`, the presenter returns confirmation presentation state only; the confirmed request then leaves the presentation slice at the explicit handoff boundary to donation or cart behavior outside this architecture slice.
5. Dispense animation state:
   After upstream donation or cart behavior returns `successful add-to-cart result`, that result enters `GivingMachineSelectionProtocol`.
   `GivingMachineSelectionProtocol` returns either `dispense animation state` for a brief responsive presentation or a non-animated confirmation state if dispense presentation is unavailable.
   After completion of that presentation, the slice clears the armed state unless the user explicitly re-arms an item.
6. Cart or checkout transition and return:
   `giving machine destination state` and `cart presentation request` enter `GivingMachineTransitionProtocol`.
   `GivingMachineTransitionProtocol` returns `cart or checkout presentation state` plus an updated destination context that remains inside the Giving Machine surface while shifting emphasis to a calmer, clearer task-focused presentation.
   `return from cart or checkout request` then enters `GivingMachineTransitionProtocol`, which returns an updated expanded `giving machine destination state` with `visible context` of `machine_browse`.
7. Info-screen transition and return:
   `giving machine destination state`, `info presentation request`, and `info presentation content` enter `GivingMachineTransitionProtocol`.
   `GivingMachineTransitionProtocol` returns `info presentation state` as a separate non-transactional screen.
   `return from info request` then enters `GivingMachineTransitionProtocol`, which returns an updated expanded `giving machine destination state` with `visible context` of `machine_browse` rather than dismissing the experience entirely.
8. Accessibility interaction:
   `giving machine peek state`, `giving machine destination state`, `machine window state`, `slot selection state`, `add-to-cart confirmation state`, `cart or checkout presentation state`, `info presentation state`, and any `accessibility action request` enter `GivingMachineAccessibilityProtocol`.
   `GivingMachineAccessibilityProtocol` returns `accessibility presentation state` that exposes entry label, sheet state, visible context, dismissal actions, visible slot numbers, armed slot, and available actions.
   Accessibility-driven opening, browsing, arming, confirming, cart entry, info entry, and dismissal stay within the same presentation boundaries already defined for standard interaction and do not create a gesture-only dependency.

---

## 8. Testing Strategy
- Contract alignment tests:
  - Verify the presentation slice exposes only the approved Giving Machine entry, browse, selection, transition, and accessibility behavior and does not absorb donation business behavior.
  - Verify persistent peek entry behavior, full-height destination behavior, armed-then-confirmed add behavior, presentation-only dispense behavior, calmer cart-or-checkout presentation behavior, and separate `Info` screen behavior all match the approved contract.
- Protocol conformance tests:
  - Verify `GivingMachineEntryPresentationModule` returns the persistent peek state on the daily challenge home surface, expands to the full-height destination on entry request, and returns to home on dismissal.
  - Verify `GivingMachineWindowPresentationModule` returns a two-by-two machine window when enough items exist, correct continuation peeks for top, middle, bottom, and single-window states, preserves one-visible-row browse movement for arrows and direct scroll input, and returns a non-transactional empty state when the catalog is available but empty.
  - Verify `GivingMachineSelectionPresentationModule` arms only visible slots, supports selected-item detail-driven add flows, exposes separate confirmation state, rejects confirmation with no armed slot, and presents dispense or non-animated confirmation only after a successful upstream add result.
  - Verify `GivingMachineTransitionPresentationModule` returns calmer cart-or-checkout presentation state, separate info presentation state, and explicit returns to expanded `machine_browse` context.
  - Verify `GivingMachineAccessibilityPresentationModule` exposes labeled accessible state and all required actions without gesture-only dependence.
- Orchestration flow tests:
  - Verify the slice preserves the daily-challenge-home plus peeking-bottom-sheet entry model while the Giving Machine destination opens and closes.
  - Verify browse outputs feed selection and accessibility presentation without bypassing the machine-window boundary.
  - Verify confirmed add requests leave the presentation slice at the explicit handoff boundary and that dispense presentation occurs only after the upstream success result returns.
  - Verify cart-or-checkout and info transitions preserve the broader Giving Machine destination context and return to `machine_browse` rather than dismissing the surface.
- Failure path tests:
  - Verify catalog-unavailable failures, required-slot-content-missing failures, non-visible-slot selection failures, no-armed-slot confirmation failures, dispense-animation-unavailable fallback behavior, missing-dismiss-action failures, info-content-unavailable failures, and accessibility-state-unavailable failures.

---

## 9. Architectural Risks and Guardrails
- Risk: presentation code could absorb donation business behavior because the slice visually leads into cart and checkout.
  - Guardrail: keep all pricing, cart math, checkout processing, payment, receipt, and refund behavior outside this slice, and treat only confirmed add requests and successful add results as presentation-boundary handoffs.
- Risk: the persistent peek entry model and full-height destination model could drift into competing navigation concepts.
  - Guardrail: make `GivingMachineEntryPresentationModule` authoritative for the home-surface peek state, entry expansion, dismissal, and return-to-home presentation.
- Risk: slot arming and add confirmation could collapse into a single-tap add behavior.
  - Guardrail: keep `GivingMachineSelectionPresentationModule` authoritative for the armed-then-confirmed add flow, allow UI detail presentation only as a pre-confirmation review surface, and forbid add-to-cart presentation success without an explicit confirmation request.
- Risk: dispense animation could be mistaken for proof of donation or cart success.
  - Guardrail: require the selection module to present dispense state only after an upstream successful add result arrives, and preserve non-animated confirmation fallback without changing the success outcome.
- Risk: cart-or-checkout presentation could inherit the immersive machine tone strongly enough to blur the approved calmer review boundary.
  - Guardrail: keep all transitions into `cart_or_checkout` owned by `GivingMachineTransitionPresentationModule` and treat calmer task-focused emphasis as a required presentation-state boundary.
- Risk: the separate `Info` screen could start participating in browsing or transaction flows.
  - Guardrail: keep `Info` presentation non-transactional and route all info entry and return behavior only through `GivingMachineTransitionPresentationModule`.
- Risk: accessibility interaction could become a second inconsistent control model.
  - Guardrail: require accessibility actions to expose and invoke the same presentation boundaries already defined by entry, window, selection, and transition behavior.

---

## 10. Open Questions
- None at this time.
