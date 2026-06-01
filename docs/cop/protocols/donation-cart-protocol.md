# Protocol: Donation Cart

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-donation-experience.md`

---

## 1. Name
DonationCartProtocol

---

## 2. Purpose
Manage a multi-item donation cart, including adding items, removing items, updating items, and producing a cart summary before checkout.

---

## 3. Inputs
- cart update request: event - user action adding, removing, or changing donation items.
- donation selection: object - donation option and amount or quantity being added or updated.
- current cart state: collection - existing donation cart items.

---

## 4. Outputs
- donation cart: collection - selected donation items pending checkout.
- donation selection summary: object - summary of selected items and total amount.
- failure response: object or null - explicit failure when the cart cannot be updated or submitted.

---

## 5. Data Model Expectations
- `DonationSelection`
  - `selected option id: identifier` - chosen donation option.
  - `amount: monetary value` - donation amount for the selection.
  - `quantity: integer or null` - quantity when quantity-based.
- `DonationCart`
  - `items: collection` - selected donation items pending checkout.
  - `total amount: monetary value` - combined amount across all selected items.

---

## 6. Behavior Requirements
1. Must allow multiple donation selections to exist in the cart at the same time.
2. Must allow selected donation items to be added, updated, and removed before payment submission.
3. Must provide a cart summary including selected items and total amount.
4. Must reject checkout when the cart contains no donation items.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `DONATION_OPTION_UNAVAILABLE`
  - `DONATION_CART_EMPTY`
  - `DONATION_SELECTION_INVALID`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- None.
