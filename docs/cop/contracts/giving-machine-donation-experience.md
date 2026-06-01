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
- cart update request: event — user action adding, removing, or changing donation items in the cart.
- payment submission: event — user action requesting donation payment completion.
- donor identity: object — donor name and donor email required before payment submission.
- payment details: object — payment information required to complete the donation.
- receipt delivery selection: object — user selection for receipt delivery by email, text message, or both.
- receipt delivery contact information: object — email address, phone number, or both required for the selected receipt delivery method.
- current catalog state: state — whether donation options are currently available for selection.

---

## 4. Outputs
- donation option list: collection — available donation options presented to the user.
- donation option detail: object — fuller description of a selected donation option.
- donation cart: collection — selected donation items pending checkout.
- donation selection summary: object — user-visible summary of selected donation items and total amount.
- payment result: object — success or explicit failure response for the donation attempt.
- donation confirmation: object — confirmation of a successfully completed donation.
- receipt state: object — indication of whether receipt delivery is available and whether it was sent by email, text message, or both.

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

### Donation Cart
- items: collection — selected donation items pending checkout.
- total amount: monetary value — combined amount across all selected donation items.

### Donation Payment Result
- status: enum — `succeeded` or `failed`.
- confirmation id: identifier or null — identifier returned when donation succeeds.
- failure reason: string or null — explicit failure reason when donation fails.

### Donation Confirmation
- confirmation id: identifier — completed donation identifier.
- donation timestamp: timestamp — date and time of the successful donation.
- donated items: collection — donation items included in the successful donation.
- line-item amounts: collection — per-item donation amounts included in the successful donation.
- subtotal: monetary value — combined amount before tax and additional donor-applied fees.
- tax: monetary value — tax amount, if any.
- donor-applied fees: monetary value — user-facing additional fees, if any.
- total charged: monetary value — final amount charged to the donor.
- donated amount: monetary value — amount successfully donated.
- payment method summary: string — donor-visible summary of the payment method used.
- selected option summary: string — summary of what the donation supported.
- receipt delivery method: enum — `email`, `text`, `both`, or `none`.
- receipt delivery status: enum — `not_requested`, `pending`, `sent`, or `failed`.

---

## 6. Success Behavior

1. The system must present a list of available Giving Machine donation options.
2. The system must allow the user to inspect the details of a donation option before donating.
3. The system must allow the user to add multiple donation selections to a cart before checkout.
4. The system must allow the user to update the cart by changing or removing selected donation items before payment submission.
5. The system must present a clear summary of the donation cart and total amount before payment submission.
6. The system must require donor name and donor email before allowing payment submission.
7. The system must require payment information before allowing payment submission.
8. The system must allow the user to choose receipt delivery by email, text message, or both before completing donation payment.
9. When receipt delivery by text message is selected, the system must require a phone number before allowing payment submission.
10. The system must allow the user to submit payment and complete the donation inside the app.
11. When payment succeeds, the system must mark the donation attempt as succeeded and return a donation confirmation.
12. The donation confirmation must identify the successful donation, the charged amount, and the donation receipt details defined in this contract.
13. When receipt delivery is requested for a successful donation, the system must attempt to send the receipt using the selected delivery method or methods.

---

## 7. Failure Modes

- Condition: donation catalog is unavailable
  - System must: return a failure response and not fabricate donation options

- Condition: selected donation option is unavailable at the time of donation
  - System must: reject the selection and require the user to choose an available option

- Condition: the cart contains no donation items at the time of payment submission
  - System must: reject payment submission and return an explicit failure response

- Condition: donor name or donor email is missing at the time of payment submission
  - System must: reject payment submission and return an explicit failure response

- Condition: required payment details are missing or invalid
  - System must: reject payment submission and return an explicit failure response

- Condition: receipt delivery is requested but required delivery information is missing or invalid
  - System must: reject the receipt request and return an explicit failure response before donation completion

- Condition: payment processing fails
  - System must: return a failed payment result and must not report the donation as completed

- Condition: the system cannot confirm donation completion after payment submission
  - System must: return an explicit unresolved failure response rather than claiming success

- Condition: donation succeeds but receipt delivery fails
  - System must: preserve the successful donation result and return a receipt state indicating delivery failure

---

## 8. Edge Cases

- A donation option becomes unavailable after the user has already opened it.
- The user changes donation selection before submitting payment.
- The user removes the final remaining item from the cart.
- The user retries after a failed payment attempt.
- The donation catalog contains no available options.

---

## 9. Constraints

- Must not define donation behavior outside this contract.
- Must not claim a donation succeeded unless payment completion is confirmed.
- Must not fabricate donation catalog entries when catalog data is unavailable.
- Must support a cart containing multiple donation items in a single checkout flow.
- Must complete the donation payment flow inside the app.
- Must represent payment failures explicitly.
- Must require donor name, donor email, and payment information before payment submission.
- Must require a phone number only when text receipt delivery is selected.
- Must default successful donations to non-refundable unless required otherwise by law or final campaign policy.

---

## 10. Observability

### Events
- donation_option_list_viewed
- donation_option_viewed
- donation_selection_updated
- donation_cart_viewed
- donation_cart_updated
- donation_payment_submitted
- donation_payment_succeeded
- donation_payment_failed
- donation_receipt_requested
- donation_receipt_sent
- donation_receipt_failed

### Metrics
- donation option view count
- donation start rate
- average cart item count
- donation completion rate
- donation failure rate
- total donated amount

### Logs
- donation catalog load result
- donation selection validation result
- donation cart validation result
- payment submission outcome
- donation confirmation result
- receipt delivery outcome

---

## 11. Acceptance Criteria

- [ ] Users can view available Giving Machine donation options in the app.
- [ ] Users can inspect donation option details before donating.
- [ ] Users can add multiple donation options to a cart before checkout.
- [ ] Users can review a cart summary and total before submitting payment.
- [ ] Users must provide donor name and donor email before submitting payment.
- [ ] Users can choose receipt delivery by email, text message, or both.
- [ ] Users must provide a phone number when text receipt delivery is selected.
- [ ] Users can complete a donation payment inside the app.
- [ ] Successful donations return a confirmation with the donated amount.
- [ ] Successful donations can trigger receipt delivery through the selected delivery method or methods.
- [ ] Failed donations return an explicit failure response and are not marked successful.

---

## 12. Open Questions

- What rules determine when a donation option is available or unavailable?
- What final campaign policy governs refund exceptions, cancellations, and support handling after a successful donation?
- What final tax-document requirements apply to completed donations, if any?
