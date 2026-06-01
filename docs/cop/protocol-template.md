# Protocol: <Capability Name>

Version: 0.1.0  
Status: Draft | In Progress | Final

Derived From:
- `<contract path>`

---

## 1. Name
<CapabilityName>

---

## 2. Purpose
Describe the capability this protocol provides.

---

## 3. Inputs
- `<name>: <type>` - description
- `<name>: <type>` - description

---

## 4. Outputs
- `<name>: <type>` - description
- `<name>: <type>` - description

---

## 5. Data Model Expectations
- `<ModelName>`
  - `<field>: <type>` - description
  - `<field>: <type>` - description

---

## 6. Behavior Requirements
1. Must ...
2. Must ...
3. Must ...

---

## 7. Failure Behavior
- Failures must be represented explicitly.
- When the capability does not succeed, success outputs must be null, empty, or otherwise contract-defined.
- Failure reason must be one of:
  - `<FAILURE_REASON>`
  - `<FAILURE_REASON>`

---

## 8. Constraints
- Must define this capability only
- Must not include implementation logic
- Must not introduce behavior not defined in the source contract
- Must remain reusable without embedding orchestration logic

---

## 9. Open Questions
- ...
- ...
