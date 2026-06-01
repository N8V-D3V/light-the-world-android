# Protocol: Donation Receipt

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-donation-experience.md`

---

## 1. Name
DonationReceiptProtocol

---

## 2. Purpose
Provide donation receipt content and manage requested receipt delivery by email, text message, or both for successful donations.

---

## 3. Inputs
- donation confirmation: object - successful donation confirmation.
- receipt delivery selection: object - requested receipt delivery method.
- receipt delivery contact information: object - delivery contact information for the selected method.

---

## 4. Outputs
- receipt state: object or null - whether receipt delivery is available and whether it was sent by email, text message, or both.
- failure response: object or null - explicit failure when receipt delivery cannot be completed.

---

## 5. Data Model Expectations
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
  - `receipt delivery method: enum` - `email`, `text`, `both`, or `none`.
  - `receipt delivery status: enum` - `not_requested`, `pending`, `sent`, or `failed`.

---

## 6. Behavior Requirements
1. Must provide receipt content for successful donations.
2. Must support receipt delivery by email, text message, or both.
3. Must preserve a successful donation even when receipt delivery fails.
4. Must return explicit receipt delivery status.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `RECEIPT_CONTACT_INVALID`
  - `RECEIPT_DELIVERY_FAILED`
  - `DONATION_CONFIRMATION_REQUIRED`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- What final tax-document requirements apply to completed donations, if any?
