# Protocol: Donation Catalog

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/giving-machine-donation-experience.md`

---

## 1. Name
DonationCatalogProtocol

---

## 2. Purpose
Provide available Giving Machine donation options and their details without exposing how catalog content is sourced.

---

## 3. Inputs
- current catalog state: state - whether catalog content is currently available.
- selected donation option id: identifier or null - option requested for detail viewing.

---

## 4. Outputs
- donation option list: collection - available donation options.
- donation option detail: object or null - details for a selected donation option.
- failure response: object or null - explicit failure when donation catalog content cannot be provided.

---

## 5. Data Model Expectations
- `DonationOption`
  - `option id: identifier` - unique donation option identifier.
  - `title: string` - name of the donation option.
  - `description: string` - explanation of what the donation supports.
  - `availability status: enum` - `available` or `unavailable`.

---

## 6. Behavior Requirements
1. Must provide a list of currently available donation options.
2. Must provide details for a selected donation option when it exists.
3. Must not expose or depend on a specific catalog storage mechanism in the protocol definition.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `DONATION_CATALOG_UNAVAILABLE`
  - `DONATION_OPTION_UNAVAILABLE`
  - `DONATION_OPTION_MISSING`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- What rules determine when a donation option is available or unavailable?
