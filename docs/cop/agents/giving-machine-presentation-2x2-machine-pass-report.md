# Giving Machine Presentation 2x2 Machine Pass Report

## Work Completed

- `docs/cop/contracts/giving-machine-presentation-experience.md`
- `docs/cop/protocols/giving-machine-window-protocol.md`
- `docs/cop/architecture/giving-machine-presentation-experience-architecture-plan.md`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/GivingMachinePresentationTypes.kt`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/window/StubGivingMachineWindowPresenter.kt`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/LTWGivingMachineExperienceScaffold.kt`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/LTWJsonGivingMachinePresentationContentSource.kt`
- `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/LTWGivingMachineSeedContent.kt`
- `LighttheWorld/app/src/main/assets/giving_machine_content.json`
- `LighttheWorld/app/src/test/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/GivingMachinePresentationModulesTest.kt`
- `LighttheWorld/app/src/test/java/com/n8vd3v/lighttheworld/features/donation/givingmachinepresentation/GivingMachinePresentationSliceOrchestratorTest.kt`

## Summary of Changes

- Updated the Giving Machine presentation source of truth from a 3x3 visible machine window to a 2x2 visible machine window with row-aligned top and bottom continuation peeks.
- Added approved direct-scroll browse behavior to the contract and protocol, while preserving one-visible-row progression and keeping the arrow controls as optional browse affordances.
- Added an optional image reference to presented Giving Machine items so content remains replaceable and can grow into richer item presentation later without changing the slice boundary.
- Updated the window presenter and focused module/orchestrator tests to preserve the new 2x2 capacity, two-item row step, top/middle/bottom/single-window states, empty behavior, and failure behavior.
- Reworked the real Compose machine UI to remove the separate footer, move explicit add confirmation back into the machine cabinet, improve contrast, remove the large visible slot number from the main item cards, add swipe or drag browse behavior on the machine viewport, and make the continuation peeks feel closer to a physical machine ledge.
- Kept runtime machine content asset-backed through `giving_machine_content.json`, and shortened item and info copy for readability while preserving the local-source fallback path.
- Improved the `Info` and cart surfaces with explicit readable text colors and stronger content-card contrast.

## Open Questions

- Should a future pass bundle real item artwork for some or all `image reference` values, or should the remote content source own the first real image rollout?
- Should the current drag-to-browse interaction later be expanded into a snapped lazy-grid scroll model, or is the present row-step drag behavior the desired long-term machine interaction?

## Ambiguities or Risks

- The presentation slice now supports optional image references, but no real bundled product imagery is shipped yet, so the richer visual layout still depends mostly on typography and machine chrome.
- The direct-scroll browse behavior is implemented in the UI layer as row-step drag interaction that feeds the existing browse boundary; if a future pass wants fully inertial catalog scrolling, that would need another source-of-truth review to avoid bypassing the approved presentation slice.

## Next Recommended Steps

- Run coordinator review against the updated 2x2 machine layout, in-machine confirmation affordance, and higher-contrast info/cart surfaces.
- If the 2x2 machine window is approved upstream, cascade the same expectation into any remaining validation-stage or coordinator-facing artifacts that still describe the older 3x3 machine window.
- If the visual direction is approved, add real optional item imagery through the existing asset-backed content source before any remote content migration.
