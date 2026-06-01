# Architecture Plan: <Feature or System Name>

Version: 0.1.0  
Status: Draft | In Progress | Final

Derived From:
- `<contract path>`
- `<protocol path>`

---

## 1. Purpose
Describe how the approved contract and protocol definitions will be translated into implementation structure.

---

## 2. Source Artifacts
- Contract: `<path>`
- Protocols:
  - `<path>`
  - `<path>`

---

## 3. Module Breakdown
- `<ModuleName>` - responsibility
- `<ModuleName>` - responsibility

---

## 4. Protocol-to-Module Map
- `<ProtocolName>` -> `<ModuleName>`
- `<ProtocolName>` -> `<ModuleName>`

---

## 5. Dependencies
- `<ModuleName>` depends on `<ProtocolName>`
- `<OrchestratorName>` depends on `<ProtocolName>`

---

## 6. Orchestration Boundaries
- `<OrchestratorName>` coordinates:
  - `<ProtocolName>`
  - `<ProtocolName>`
- Must not embed module implementation logic
- Must stop or continue only according to contract-defined behavior

---

## 7. Data Flow
1. `<Input>` enters `<ProtocolName>`
2. `<Output>` from `<ProtocolName>` becomes input to `<ProtocolName>`
3. `<Final output>` is returned according to the contract

---

## 8. Testing Strategy
- Contract alignment tests:
  - ...
- Protocol conformance tests:
  - ...
- Orchestration flow tests:
  - ...
- Failure path tests:
  - ...

---

## 9. Architectural Risks and Guardrails
- Risk: ...
  - Guardrail: ...
- Risk: ...
  - Guardrail: ...

---

## 10. Open Questions
- ...
- ...
