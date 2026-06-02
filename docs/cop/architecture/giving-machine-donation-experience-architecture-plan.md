# Architecture Plan: Giving Machine Donation Experience

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-donation-experience.md`
- `docs/cop/protocols/donation-catalog-protocol.md`
- `docs/cop/protocols/donation-cart-protocol.md`
- `docs/cop/protocols/donation-checkout-protocol.md`
- `docs/cop/protocols/donation-receipt-protocol.md`

---

## 1. Purpose
Describe how the approved Giving Machine donation contract and supporting protocols will be translated into a feature-based implementation boundary for browsing donation options, maintaining a multi-item cart, submitting checkout inside the app, and returning receipt delivery results.

This plan keeps catalog sourcing, payment submission, and receipt delivery abstract so the feature behavior remains contract-aligned even as the underlying integrations are selected later.

---

## 2. Source Artifacts
- Contract: `docs/cop/contracts/giving-machine-donation-experience.md`
- Protocols:
  - `docs/cop/protocols/donation-catalog-protocol.md`
  - `docs/cop/protocols/donation-cart-protocol.md`
  - `docs/cop/protocols/donation-checkout-protocol.md`
  - `docs/cop/protocols/donation-receipt-protocol.md`

---

## 3. Module Breakdown
- `GivingMachineDonationExperienceModule` - feature boundary for donation option browsing, option detail viewing, cart review, checkout submission, and receipt delivery result viewing.
- `DonationCatalogModule` - fulfills `DonationCatalogProtocol` and owns available donation option retrieval and donation option detail retrieval.
- `DonationCartModule` - fulfills `DonationCartProtocol` and owns multi-item cart add, update, remove, empty-cart handling, and cart summary generation.
- `DonationCheckoutModule` - fulfills `DonationCheckoutProtocol` and owns checkout validation, payment submission, and donation confirmation creation.
- `DonationReceiptModule` - fulfills `DonationReceiptProtocol` and owns receipt content availability and requested receipt delivery status reporting.

---

## 4. Protocol-to-Module Map
- `DonationCatalogProtocol` -> `DonationCatalogModule`
- `DonationCartProtocol` -> `DonationCartModule`
- `DonationCheckoutProtocol` -> `DonationCheckoutModule`
- `DonationReceiptProtocol` -> `DonationReceiptModule`

---

## 5. Dependencies
- `GivingMachineDonationExperienceModule` depends on `DonationCatalogProtocol`, `DonationCartProtocol`, `DonationCheckoutProtocol`, and `DonationReceiptProtocol`.
- `DonationCatalogModule` must not depend on cart, checkout, or receipt behavior.
- `DonationCartModule` depends on current cart state and donation selection inputs and must not embed payment or receipt behavior.
- `DonationCheckoutModule` depends on donation cart contents, donor identity, payment details, receipt delivery selection, and receipt delivery contact information only.
- `DonationReceiptModule` depends on successful donation confirmation and requested receipt delivery inputs only.
- `DonationCatalogModule` must treat the returned donation options collection as the authoritative set of currently available options for the current request.

---

## 6. Orchestration Boundaries
- `GivingMachineDonationExperienceOrchestrator` coordinates `DonationCatalogProtocol`, `DonationCartProtocol`, `DonationCheckoutProtocol`, and `DonationReceiptProtocol`.
- `GivingMachineDonationExperienceOrchestrator` must not embed catalog retrieval implementation, cart update rules, payment-processing logic, or receipt-delivery implementation logic.
- `GivingMachineDonationExperienceOrchestrator` must treat update and removal requests targeting items not present in the current cart as explicit `DONATION_SELECTION_INVALID` failures.
- `GivingMachineDonationExperienceOrchestrator` must stop checkout progression when the cart is empty or required donor, payment, or receipt contact inputs are missing or invalid.
- `GivingMachineDonationExperienceOrchestrator` must invoke `DonationReceiptProtocol` only after `DonationCheckoutProtocol` returns a successful donation confirmation.
- `GivingMachineDonationExperienceOrchestrator` must preserve a successful donation result even when receipt delivery later returns a failure state.

---

## 7. Data Flow
1. Donation option list and detail requests enter `DonationCatalogProtocol` to return available options or explicit catalog failures.
2. User cart update requests and donation selections enter `DonationCartProtocol` to return updated cart state and donation selection summary, or an explicit `DONATION_SELECTION_INVALID` failure when an update or removal targets an item not present in the current cart.
3. Before checkout, the current cart summary is returned to the user for review through the experience boundary.
4. Donation cart contents, donor identity, payment details, receipt delivery selection, and receipt delivery contact information enter `DonationCheckoutProtocol`.
5. `DonationCheckoutProtocol` returns either an explicit failed payment result or a successful donation confirmation containing the full contract-defined confirmation payload: confirmation identifier, donation timestamp, donated items, line-item amounts, subtotal, tax if any, donor-applied fees if any, total charged, donated amount, payment method summary, selected option summary, receipt delivery method, and receipt delivery status.
6. Successful donation confirmation and receipt delivery inputs enter `DonationReceiptProtocol` to attempt requested delivery and return the resulting receipt state for email, text message, both, or none.
7. When receipt delivery succeeds or fails after donation success, `DonationReceiptProtocol` returns updated receipt delivery state without changing the successful donation result already returned by checkout.

---

## 8. Testing Strategy
- Contract alignment tests:
  - Verify the feature supports available option browsing, option detail inspection, multi-item cart behavior, in-app checkout, and receipt delivery selection exactly as defined in the contract.
  - Verify the feature does not claim donation success until payment completion is confirmed.
- Protocol conformance tests:
  - Verify `DonationCatalogModule` returns only available options and explicit failures when catalog data is unavailable or an option is unavailable or missing.
  - Verify `DonationCartModule` allows add, update, and remove actions across multiple items, rejects update and remove requests for missing cart targets with `DONATION_SELECTION_INVALID`, and rejects empty-cart checkout.
  - Verify `DonationCheckoutModule` enforces donor identity, payment details, and phone-number requirements for text receipt delivery.
  - Verify `DonationReceiptModule` returns explicit receipt delivery status and preserves donation success when delivery fails.
- Orchestration flow tests:
  - Verify the feature coordinates catalog, cart, checkout, and receipt handling in the approved order.
  - Verify multi-item cart totals passed into checkout match the confirmation returned on success.
- Failure path tests:
  - Verify unavailable catalog behavior, option unavailability at donation time, empty-cart checkout, missing donor identity, invalid payment details, invalid receipt contact information, payment failure, unresolved payment confirmation, and receipt-delivery failure after successful donation.

---

## 9. Architectural Risks and Guardrails
- Risk: catalog availability can change while the user moves from browsing into cart and checkout.
  - Guardrail: validate donation option availability at each protocol boundary where the contract allows rejection and return explicit failures when an option is no longer available.
- Risk: cart summary logic and checkout confirmation logic can drift and produce inconsistent totals.
  - Guardrail: keep cart-summary ownership in `DonationCartModule` and require checkout to consume the cart output rather than re-deriving a different selection model.
- Risk: missing-target cart mutations can be treated as successful no-op outcomes and hide stale UI state or orchestration defects.
  - Guardrail: require `DonationCartProtocol` and `GivingMachineDonationExperienceOrchestrator` to surface missing update and removal targets as explicit `DONATION_SELECTION_INVALID` failures.
- Risk: payment success and receipt delivery success can be incorrectly treated as the same state.
  - Guardrail: keep donation success inside `DonationCheckoutModule` and keep receipt delivery state inside `DonationReceiptModule`.
- Risk: receipt contact requirements can be applied inconsistently across email and text selections.
  - Guardrail: enforce receipt-contact validation only through `DonationCheckoutProtocol` using the contract rule that phone number is required only when text receipt delivery is selected.
- Risk: refund or tax policy ambiguity can leak into checkout or receipt behavior before policy approval exists.
  - Guardrail: keep those behaviors limited to the contract-approved defaults and surface unresolved policy needs as open questions instead of adding new behavior.
- Risk: architecture can lose contract-defined confirmation fields by treating receipt-related fields as optional implementation detail.
  - Guardrail: require `DonationCheckoutModule` to produce the full contract-defined `DonationConfirmation` shape, and require `DonationReceiptModule` to update delivery outcome without removing those fields from the confirmation model.

---

## 10. Open Questions
- What final campaign policy governs support handling after a successful donation?
- What final tax-document requirements apply to completed donations, if any?
