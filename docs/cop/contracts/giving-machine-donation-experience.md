# Contract: Giving Machine Donation Experience

Version: 0.3.0  
Status: Draft

---

## 1. Purpose
Define the behavior for presenting Giving Machine donation options and completing end-to-end in-app donations for campaign-aligned charitable causes.

---

## 2. Actors
- User — browses donation options, selects donations, submits payment, and receives donation confirmation.
- Donation Catalog Source — provides donation options and related descriptive information.
- Payment Processing System — authorizes and captures donation payment.
- Donation Fulfillment Recipient — receives the completed donation designation.

---

## 3. Inputs
- donation catalog: collection — available Giving Machine donation options and related descriptions.
- selected donation option: object — donation option chosen by the user.
- donation amount selection: object — donation amount or quantity selected by the user.
- payment submission: event — user action requesting donation payment completion.
- payment details: object — payment information required to complete the donation.
- current catalog state: state — whether donation options are currently available for selection.

---

## 4. Outputs
- donation option list: collection — available donation options presented to the user.
- donation option detail: object — fuller description of a selected donation option.
- donation selection summary: object — user-visible summary of selected donation items and amount.
- payment result: object — success or explicit failure response for the donation attempt.
- donation confirmation: object — confirmation of a successfully completed donation.
- receipt state: object — indication of whether donor receipt information is available.

---

## 5. Data Models

### Donation Option
- option id: identifier — unique identifier for the donation option.
- title: string — name of the donation option.
- description: string — explanation of what the donation supports.
- availability status: enum — `available` or `unavailable`.

### Donation Selection
- selected option id: identifier — chosen donation option.
- amount: monetary value — donation amount associated with the selection.
- quantity: integer or null — quantity when the option is quantity-based.

### Donation Payment Result
- status: enum — `succeeded` or `failed`.
- confirmation id: identifier or null — identifier returned when donation succeeds.
- failure reason: string or null — explicit failure reason when donation fails.

### Donation Confirmation
- confirmation id: identifier — completed donation identifier.
- donated amount: monetary value — amount successfully donated.
- selected option summary: string — summary of what the donation supported.

---

## 6. Success Behavior

1. The system must present a list of available Giving Machine donation options.
2. The system must allow the user to inspect the details of a donation option before donating.
3. The system must allow the user to select a donation option and its associated donation amount or quantity.
4. The system must present a clear summary of the donation selection before payment submission.
5. The system must allow the user to submit payment and complete the donation inside the app.
6. When payment succeeds, the system must mark the donation attempt as succeeded and return a donation confirmation.
7. The donation confirmation must identify the successful donation and the donated amount.

---

## 7. Failure Modes

- Condition: donation catalog is unavailable
  - System must: return a failure response and not fabricate donation options

- Condition: selected donation option is unavailable at the time of donation
  - System must: reject the selection and require the user to choose an available option

- Condition: required payment details are missing or invalid
  - System must: reject payment submission and return an explicit failure response

- Condition: payment processing fails
  - System must: return a failed payment result and must not report the donation as completed

- Condition: the system cannot confirm donation completion after payment submission
  - System must: return an explicit unresolved failure response rather than claiming success

---

## 8. Edge Cases

- A donation option becomes unavailable after the user has already opened it.
- The user changes donation selection before submitting payment.
- The user retries after a failed payment attempt.
- The donation catalog contains no available options.

---

## 9. Constraints

- Must not define donation behavior outside this contract.
- Must not claim a donation succeeded unless payment completion is confirmed.
- Must not fabricate donation catalog entries when catalog data is unavailable.
- Must complete the donation payment flow inside the app.
- Must represent payment failures explicitly.

---

## 10. Observability

### Events
- donation_option_list_viewed
- donation_option_viewed
- donation_selection_updated
- donation_payment_submitted
- donation_payment_succeeded
- donation_payment_failed

### Metrics
- donation option view count
- donation start rate
- donation completion rate
- donation failure rate
- total donated amount

### Logs
- donation catalog load result
- donation selection validation result
- payment submission outcome
- donation confirmation result

---

## 11. Acceptance Criteria

- [ ] Users can view available Giving Machine donation options in the app.
- [ ] Users can inspect donation option details before donating.
- [ ] Users can select a donation option and donation amount or quantity.
- [ ] Users can review a summary before submitting payment.
- [ ] Users can complete a donation payment inside the app.
- [ ] Successful donations return a confirmation with the donated amount.
- [ ] Failed donations return an explicit failure response and are not marked successful.

---

## 12. Open Questions

- What donor information is required before payment submission?
- Are donations one-at-a-time only, or can users combine multiple options in one payment flow?
- What confirmation and receipt details must be shown immediately after donation?
- What cancellation or refund behaviors are required after a donation succeeds?
- What rules determine when a donation option is available or unavailable?
