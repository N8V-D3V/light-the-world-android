# Product Vision

Version: 0.1.0

---

## Overview

Light the World Android is a seasonal mobile product that helps users participate in a Christmas service campaign through two connected experiences: daily acts of kindness and charitable giving. The product presents each day's challenge in a simple, encouraging format and also provides an in-app giving experience for campaign-aligned donation options.

---

## Purpose

This product exists to turn campaign participation into clear daily action. It gives users a practical way to discover the current service invitation, follow through on it, remember it during the day, celebrate completion, and contribute financially to associated charitable causes without leaving the app.

This vision establishes the product boundaries needed for contract creation so each feature can be specified without guessing at behavior outside the campaign experience.

---

## Goals

- Help users participate in the campaign consistently through daily challenge discovery, completion, and reminder support.
- Enable users to contribute to campaign-aligned charitable giving through a complete in-app donation flow.
- Keep the product focused on encouragement, clarity, and low-friction participation rather than complexity or broad social features.

---

## Non-Goals

- The product is not intended to define or manage campaign content authoring workflows.
- The product is not intended to replace charity fulfillment, donation distribution, or post-donation nonprofit operations.
- The product is not intended to function as a general-purpose social network or user-generated challenge platform.

---

## Core Capabilities

- Present a sequence of daily service challenges that users can browse, open, and understand.
- Allow users to view short and detailed challenge content through a card-based interaction model.
- Allow users to mark eligible challenges as completed and share completion to social platforms.
- Allow users to opt into reminder notifications for the daily challenge experience.
- Present donation options for a Giving Machine experience and complete donations inside the app.

---

## Constraints

- The challenge calendar must support an annual campaign window from December 1 through December 25.
- The authoritative challenge content source must be replaceable without changing challenge feature behavior.
- Users must be able to view future challenges before their scheduled day.
- Users must not be allowed to mark a challenge complete before the challenge date.
- Reminder notifications are optional and must respect the user's notification preference.
- Challenge eligibility and reminders must be determined by the user's current local date and local time.
- When reminders are enabled, the product must support one reminder at 10:00 AM local time and one reminder at 6:00 PM local time.
- The later daily reminder must be conditioned on the challenge not yet being completed for that day.
- Donation behavior must be defined as an end-to-end in-app flow rather than a handoff to an undefined external process.
- Donation behavior must support a cart containing multiple donation items in one checkout flow.
- Donation confirmation must support sending a receipt by email or text message.
- Donation payment submission must require donor name, donor email, and payment information.
- Donation receipt delivery by text message must require a phone number.
- Donation confirmation must include a confirmation identifier, donation date and time, donated items, line-item amounts, subtotal, tax if any, donor-applied fees if any, total charged, payment method summary, and receipt delivery status.
- Donation refund behavior must default to non-refundable unless required by law or overridden by final campaign policy.
- Contracts must keep the daily challenge experience and the donation experience behaviorally separate, even if they live in one product.

---

## Open Questions

- What final campaign policy governs donation refunds, cancellations, and exception handling?
- What final tax-document requirements apply to donations, if any?
