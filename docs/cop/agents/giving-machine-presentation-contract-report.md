# Giving Machine Presentation Contract Report

## Work Completed

- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/agents/giving-machine-presentation-contract-report.md`

## Summary of Changes

- Created a new COP contract for the Giving Machine Presentation Experience as a presentation and navigation artifact separate from donation business rules.
- Defined the relationship between the daily challenge home surface and Giving Machine through a persistent peeking bottom-sheet entry labeled `Giving Machine`, full-height destination behavior when opened, and explicit close or return behavior.
- Defined machine-window browsing behavior, including numbered slots, three-by-three visible slot windows, top and bottom continuation peeks, single-tap armed state, explicit add-to-cart confirmation, and short dispense animation after successful add.
- Defined that cart or checkout presentation must feel clearer and less theatrical than browsing, and that the separate `Info` screen serves acknowledgements and app information without acting as a transactional surface.
- Added accessibility, empty-state, and presentation-failure behavior so downstream protocol work can proceed without guessing.

## Open Questions

- None at this time.

## Ambiguities or Risks

- The contract fixes the immersive machine-window metaphor and explicit armed-then-confirmed add flow, so downstream protocol work should preserve that sequence rather than collapsing selection and add-to-cart into one action.
- The contract defines experience-level dispense animation behavior, but it intentionally does not lock a specific animation implementation or rendering mechanism.

## Next Recommended Steps

- Use the new contract to derive presentation-focused protocols for Giving Machine entry state, machine-window browse state, armed-slot state, add-confirmation state, cart or checkout presentation transitions, and info-screen transitions.
- Keep later protocol and architecture work separate from donation pricing, cart math, checkout, and payment contracts.
