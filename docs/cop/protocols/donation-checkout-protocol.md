# Protocol: Donation Checkout

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-donation-experience.md`

---

## 1. Name
DonationCheckoutProtocol

---

## 2. Purpose
Validate donor-submitted checkout information, submit payment for a multi-item donation cart, and return explicit success or failure outcomes.

---

## 3. Inputs
- donation cart: collection - donation items pending checkout.
- donor identity: object - donor name and donor email.
- payment details: object - payment information required for payment submission.
- receipt delivery selection: object - requested receipt delivery method.
- receipt delivery contact information: object - delivery contact information for the selected method.

---

## 4. Outputs
- payment result: object or null - success or explicit failure result for the donation attempt.
- donation confirmation: object or null - confirmation returned when donation succeeds.
- failure response: object or null - explicit failure when checkout cannot complete.

---

## 5. Data Model Expectations
- `DonationPaymentResult`
  - `status: enum` - `succeeded` or `failed`.
  - `confirmation id: identifier or null` - confirmation identifier when successful.
  - `failure reason: string or null` - explicit failure reason when unsuccessful.
- `DonationConfirmation`
  - `confirmation id: identifier` - completed donation identifier.
  - `donation timestamp: timestamp` - date and time of the successful donation.
  - `donated items: collection` - items included in the donation.
  - `line-item amounts: collection` - per-item donation amounts.
  - `subtotal: monetary value` - combined amount before tax and donor-applied fees.
  - `tax: monetary value` - tax amount, if any.
  - `donor-applied fees: monetary value` - user-facing additional fees, if any.
  - `total charged: monetary value` - final charged amount.
  - `payment method summary: string` - donor-visible payment method summary.

---

## 6. Behavior Requirements
1. Must require donor name and donor email before payment submission.
2. Must require payment information before payment submission.
3. Must require a phone number only when text receipt delivery is selected.
4. Must reject checkout when the cart contains no donation items.
5. Must return an explicit success or failure result for the donation attempt.
6. Must return a donation confirmation only when payment completion is confirmed.
7. Must default successful donations to non-refundable unless required otherwise by law or final campaign policy.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `DONATION_CART_EMPTY`
  - `DONOR_IDENTITY_REQUIRED`
  - `PAYMENT_DETAILS_INVALID`
  - `RECEIPT_CONTACT_INVALID`
  - `PAYMENT_FAILED`
  - `PAYMENT_CONFIRMATION_UNRESOLVED`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- What final campaign policy governs refund exceptions, cancellations, and support handling after a successful donation?
