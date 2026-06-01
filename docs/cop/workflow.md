# COP Workflow

Version: 0.1.0

---

## Purpose

This document defines the standard COP workflow and artifact handoffs.

The goal is to keep contract, protocol, architecture, module, orchestration, and validation work explicit and separable.

---

## Standard Agent Sequence

1. Contract Agent
2. Protocol Agent
3. Architecture Agent
4. Module Agent
5. Orchestrator Agent
6. Validation Agent

---

## Artifact Flow

1. Contract document
   - Defines behavior, inputs, outputs, success, failure, edge cases, constraints, observability, and acceptance criteria

2. Protocol definitions
   - Derive capabilities from one or more contracts
   - Define interfaces and capability boundaries
   - Preserve contract behavior without implementation logic

3. Architecture plan
   - Maps protocols to modules
   - Defines dependencies, orchestration boundaries, data flow, testing strategy, risks, and guardrails
   - Does not include production code

4. Modules
   - Implement protocols as concrete, testable units
   - Stay inside module boundaries defined by the architecture plan

5. Orchestrators
   - Coordinate modules through protocols
   - Own sequencing and cross-module flow
   - Must not embed module implementation logic

6. Validation
   - Compares contracts against protocols, architecture, modules, orchestrators, and test results
   - Reports mismatches, gaps, risks, and required corrections

---

## Recommended Project Structure

```text
docs/
  cop/
    agents/
    contracts/
    protocols/
    architecture/
    product/
src/
  features/
    <capability-or-contract-name>/
tests/
```

---

## Folder Guidance

- `docs/cop/contracts/` contains contract documents created from `contract-template.md`
- `docs/cop/protocols/` contains protocol documents created from `protocol-template.md`
- `docs/cop/architecture/` contains implementation plans created from `architecture-plan-template.md`
- `src/features/<capability-or-contract-name>/` contains modules and supporting code organized by capability or contract boundary
- `tests/` contains validation, module, and orchestration tests aligned to contract acceptance criteria

---

## Rules

- Contracts remain the source of truth
- Protocols must be derived from contracts
- Architecture plans must be derived from contracts and protocols
- Modules must implement protocols
- Orchestrators must coordinate through protocols
- Validation must check behavior against contracts
- Do not introduce behavior in later stages that is not defined by the contract
