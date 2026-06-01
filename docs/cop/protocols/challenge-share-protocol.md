# Protocol: Challenge Share

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-service-challenge-experience.md`

---

## 1. Name
ChallengeShareProtocol

---

## 2. Purpose
Generate share content for a completed challenge using the approved campaign-aligned completion message.

---

## 3. Inputs
- challenge date: date - completed challenge date being shared.
- challenge summary: string - completed challenge summary.
- completion state: state - whether the challenge is completed.
- app link: string - link included in the share message.

---

## 4. Outputs
- share payload: object or null - share content for the device's standard share flow.
- failure response: object or null - explicit failure when share content cannot be generated.

---

## 5. Data Model Expectations
- `ShareContent`
  - `challenge date: date` - completed challenge date being shared.
  - `challenge summary: string` - summary of the completed challenge.
  - `completion message: string` - `I completed "[challenge summary]" in Light the World today. Join me in sharing some light this Christmas: [app link]`.

---

## 6. Behavior Requirements
1. Must generate share content only for completed challenges.
2. Must include the completed challenge summary in the completion message.
3. Must include the app link in the completion message.
4. Must return share content suitable for the device's standard share flow.

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `CHALLENGE_NOT_COMPLETED`
  - `APP_LINK_UNAVAILABLE`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- None.
