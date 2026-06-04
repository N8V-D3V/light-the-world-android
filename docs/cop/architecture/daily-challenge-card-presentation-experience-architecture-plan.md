# Architecture Plan: Daily Challenge Card Presentation Experience

Version: 0.1.0  
Status: Draft

Derived From:
- `docs/cop/contracts/daily-challenge-card-presentation-experience.md`
- `docs/cop/contracts/daily-service-challenge-experience.md`
- `docs/cop/protocols/challenge-card-rail-protocol.md`
- `docs/cop/protocols/challenge-card-focus-protocol.md`
- `docs/cop/protocols/challenge-card-face-protocol.md`
- `docs/cop/protocols/challenge-card-accessibility-protocol.md`

---

## 1. Purpose
Describe how the approved daily challenge card presentation contract and protocol definitions will be translated into a focused Android presentation-slice structure for rail presentation, active-card focus, face presentation, long back-face reading, and accessibility interaction.

This plan keeps card presentation behavior separate from challenge business behavior such as completion eligibility, reminders, sharing, and persistence. The slice may consume challenge content and card visual-state inputs from the broader daily challenge experience, but it must treat those inputs as read-only presentation inputs rather than as business logic it owns.

---

## 2. Source Artifacts
- Contract: `docs/cop/contracts/daily-challenge-card-presentation-experience.md`
- Contract: `docs/cop/contracts/daily-service-challenge-experience.md`
- Protocols:
  - `docs/cop/protocols/challenge-card-rail-protocol.md`
  - `docs/cop/protocols/challenge-card-focus-protocol.md`
  - `docs/cop/protocols/challenge-card-face-protocol.md`
  - `docs/cop/protocols/challenge-card-accessibility-protocol.md`
- Reference context:
  - `docs/cop/product/product-vision.md`
- Kotlin protocol surfaces:
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/dailychallenge/cardpresentation/ChallengeCardPresentationTypes.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/dailychallenge/cardpresentation/rail/ChallengeCardRailProtocol.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/dailychallenge/cardpresentation/focus/ChallengeCardFocusProtocol.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/dailychallenge/cardpresentation/face/ChallengeCardFaceProtocol.kt`
  - `LighttheWorld/app/src/main/java/com/n8vd3v/lighttheworld/features/dailychallenge/cardpresentation/accessibility/ChallengeCardAccessibilityProtocol.kt`

---

## 3. Module Breakdown
- `DailyChallengeCardPresentationExperienceModule` - feature boundary for the presentation slice under the daily challenge area; owns presentation-slice coordination inputs and outputs only.
- `ChallengeCardRailPresentationModule` - fulfills `ChallengeCardRailProtocol` and owns horizontal rail presentation, empty rail state, active-card emphasis presentation, adjacent preview presentation, and calm browse/snap presentation state.
- `ChallengeCardFocusResolutionModule` - fulfills `ChallengeCardFocusProtocol` and owns deterministic initial active-card selection, active-card changes, browse-driven focus movement, and centered active-card preservation.
- `ChallengeCardFacePresentationModule` - fulfills `ChallengeCardFaceProtocol` and owns active-card face changes, front/back face state, reset-to-front behavior when a different card becomes active, and in-place vertical reading state for long back-face content.
- `ChallengeCardAccessibilityPresentationModule` - fulfills `ChallengeCardAccessibilityProtocol` and owns accessible card labels, accessible sequence position, available assistive browse/face actions, and accessible long-content reading behavior.

---

## 4. Protocol-to-Module Map
- `ChallengeCardRailProtocol` -> `ChallengeCardRailPresentationModule`
- `ChallengeCardFocusProtocol` -> `ChallengeCardFocusResolutionModule`
- `ChallengeCardFaceProtocol` -> `ChallengeCardFacePresentationModule`
- `ChallengeCardAccessibilityProtocol` -> `ChallengeCardAccessibilityPresentationModule`

---

## 5. Dependencies
- `DailyChallengeCardPresentationExperienceModule` depends on `ChallengeCardRailProtocol`, `ChallengeCardFocusProtocol`, `ChallengeCardFaceProtocol`, and `ChallengeCardAccessibilityProtocol`.
- `DailyChallengeCardPresentationExperienceModule` depends on upstream-provided `PresentedChallengeCard` content and presentation-state inputs only; it must not depend on or mutate completion persistence, eligibility rules, reminder scheduling, or share behavior.
- `ChallengeCardRailPresentationModule` depends on challenge card content, card presentation states, current active card identity, and browse requests only.
- `ChallengeCardFocusResolutionModule` depends on challenge card content, current date, current active card identity, active-card requests, and browse requests only.
- `ChallengeCardFacePresentationModule` depends on challenge card content, current active card identity, face requests, active-card requests, and back-face reading requests only.
- `ChallengeCardAccessibilityPresentationModule` depends on challenge card content, current active card identity, card face state, accessibility browse requests, accessibility face requests, and back-face reading requests only.
- `ChallengeCardRailPresentationModule` must not redefine initial active-card selection rules already owned by `ChallengeCardFocusResolutionModule`.
- `ChallengeCardFacePresentationModule` must not redefine horizontal browse or snap behavior already owned by `ChallengeCardFocusResolutionModule` and `ChallengeCardRailPresentationModule`.
- `ChallengeCardAccessibilityPresentationModule` must not redefine standard focus rules or face rules; it must expose and invoke them through accessible presentation and actions.

---

## 6. Orchestration Boundaries
- `DailyChallengeCardPresentationSliceOrchestrator` coordinates `ChallengeCardFocusProtocol`, `ChallengeCardFaceProtocol`, `ChallengeCardRailProtocol`, and `ChallengeCardAccessibilityProtocol` within this presentation slice only.
- `DailyChallengeCardPresentationSliceOrchestrator` must not embed challenge business logic for completion eligibility, reminders, sharing, or persistence.
- `DailyChallengeCardPresentationSliceOrchestrator` must treat `ChallengeCardFocusProtocol` as the authoritative source for initial active-card resolution, direct active-card changes, and browse-driven focus outcomes.
- `DailyChallengeCardPresentationSliceOrchestrator` must treat `ChallengeCardFaceProtocol` as the authoritative source for active-card visible-face state and long back-face reading state.
- `DailyChallengeCardPresentationSliceOrchestrator` must use `ChallengeCardRailProtocol` to assemble the contract-defined rail presentation, empty-state presentation, adjacent previews, and motion presentation from the current presentation inputs.
- `DailyChallengeCardPresentationSliceOrchestrator` must use `ChallengeCardAccessibilityProtocol` to expose accessible presentation state and accessible actions without bypassing focus or face rules.
- `DailyChallengeCardPresentationSliceOrchestrator` must preserve the contract-defined rule that when a different card becomes active, the previously active card returns to its front face before becoming adjacent or offscreen.
- `DailyChallengeCardPresentationSliceOrchestrator` must preserve the contract-defined rule that vertical back-face reading remains in place on the active card and is not treated as horizontal browsing.
- `DailyChallengeCardPresentationSliceOrchestrator` must stop or continue only according to contract-defined success and failure behavior.

---

## 7. Data Flow
1. Empty rail state:
   `challenge card list` enters `ChallengeCardRailProtocol`.
   When the list is available but empty, `ChallengeCardRailProtocol` returns `empty card rail state` with no active card state and no adjacent preview state.
   `DailyChallengeCardPresentationSliceOrchestrator` returns the non-interactive empty presentation without invoking business behavior outside this slice.
2. Initial active-card selection:
   `challenge card list`, `current date`, and `current active card: null` enter `ChallengeCardFocusProtocol`.
   `ChallengeCardFocusProtocol` returns the initial `active card state`, `adjacent card preview state`, `card visual emphasis state`, and `motion state` according to the approved deterministic rule: current-date match first, otherwise first card in sequence.
   The resolved active-card identity becomes the current presentation input for `ChallengeCardRailProtocol` and `ChallengeCardFaceProtocol`.
3. Browse and snap behavior:
   `browse request` enters `ChallengeCardFocusProtocol` to resolve which card becomes centered active when browsing changes or ends.
   The updated active-card identity and emphasis context enter `ChallengeCardRailProtocol`, which returns the rail presentation and calm browse/snap `motion state`.
   When the browse phase ends, the slice returns one centered active card rather than an in-between resting state.
4. Face changes:
   `card face request`, `current active card`, and `challenge card list` enter `ChallengeCardFaceProtocol`.
   `ChallengeCardFaceProtocol` returns the current `card face state`, preserves the active card, and returns face-change `motion state`.
   When an `active card request` or browse-driven active-card change makes a different card active, `ChallengeCardFaceProtocol` returns the previous active card to `front` before it becomes adjacent or offscreen.
5. In-place vertical reading of long back-face content:
   `back-face reading request`, `current active card`, and `challenge card list` enter `ChallengeCardFaceProtocol`.
   `ChallengeCardFaceProtocol` returns preserved `active card state`, preserved `card face state` of `back`, and `motion state` representing vertical reading rather than horizontal browsing.
   `DailyChallengeCardPresentationSliceOrchestrator` keeps the same card centered and active while long detail description and suggestions continue to be read in place.
6. Accessibility interaction:
   `challenge card list`, `current active card`, `card face state`, and any `accessibility browse request`, `accessibility face request`, or `back-face reading request` enter `ChallengeCardAccessibilityProtocol`.
   `ChallengeCardAccessibilityProtocol` returns `accessibility presentation state` plus any resulting `active card state` or `card face state`.
   Accessibility-driven card movement stays one card at a time in sequence order, accessibility-driven face changes stay on the active card only, and long back-face content remains fully readable without requiring gesture-only interaction.

---

## 8. Testing Strategy
- Contract alignment tests:
  - Verify the presentation slice exposes only the approved card-rail presentation behavior and does not absorb completion eligibility, reminders, sharing, or persistence logic.
  - Verify empty-state behavior, deterministic initial active-card selection, centered-active-card behavior, front/back presentation behavior, in-place vertical reading, and accessibility behavior all match the approved contract.
- Protocol conformance tests:
  - Verify `ChallengeCardRailPresentationModule` returns a non-interactive empty state when the challenge card list is available but empty, and does not fabricate rail content when the list is unavailable.
  - Verify `ChallengeCardFocusResolutionModule` resolves the initial active card using the current-date rule first and the first-card fallback second, and rejects invalid requested active cards without changing focus.
  - Verify `ChallengeCardFacePresentationModule` changes only the active card face, resets the previous active card to front before it becomes adjacent or offscreen, and preserves the active card during long-content reading.
  - Verify `ChallengeCardAccessibilityPresentationModule` exposes labeled accessible state, valid available actions, and accessible long-content reading behavior without gesture-only dependence.
- Orchestration flow tests:
  - Verify the slice coordinates focus resolution before final rail presentation when no active card is set.
  - Verify browse-end requests result in a centered active card and calm snap presentation state.
  - Verify face changes and active-card changes remain synchronized so the previously active card never persists on its back face while adjacent or offscreen.
  - Verify accessibility requests move through the same presentation slice rules rather than bypassing focus or face boundaries.
- Failure path tests:
  - Verify challenge-card-list unavailable failures, required-card-content-missing failures, initial-active-card-undetermined failures, invalid active-card request failures, no-active-card face failures, back-face-reading-unavailable failures, and accessibility-state-unavailable failures.

---

## 9. Architectural Risks and Guardrails
- Risk: presentation slice code could absorb challenge business behavior because cards display completion and future state.
  - Guardrail: treat completion and future information as read-only presentation inputs only, and forbid this slice from owning completion eligibility, reminders, sharing, or persistence decisions.
- Risk: focus resolution and rail presentation can drift because both protocol surfaces expose active-card and adjacent-preview related outputs.
  - Guardrail: treat `ChallengeCardFocusResolutionModule` as authoritative for active-card selection and adjacent availability rules, and require rail presentation to reflect that resolved focus state rather than introducing competing focus rules.
- Risk: face state can drift during active-card changes, leaving an adjacent or offscreen card on its back face.
  - Guardrail: require active-card change coordination through `ChallengeCardFaceProtocol` so the previously active card returns to front before leaving active emphasis.
- Risk: long back-face reading could be mistaken for horizontal browsing and break the approved in-place reading behavior.
  - Guardrail: route vertical reading requests only through `ChallengeCardFaceProtocol` and `ChallengeCardAccessibilityProtocol`, and explicitly preserve the current active card and visible face during that reading state.
- Risk: accessibility interaction could become a second, inconsistent navigation model.
  - Guardrail: require accessibility browse and face actions to resolve through the same focus and face rules already defined for the standard presentation model.
- Risk: calm-motion and Light the World visual tone could leak into raw implementation-specific styling rules at the architecture stage.
  - Guardrail: keep motion and visual guidance expressed only as contract-defined presentation-state requirements, not as concrete animation or UI-technology prescriptions.

---

## 10. Open Questions
- None at this time.
