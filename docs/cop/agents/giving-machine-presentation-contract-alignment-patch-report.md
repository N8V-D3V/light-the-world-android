# Work Completed

- `docs/cop/contracts/giving-machine-presentation-experience.md`

## Summary of Changes

- Updated the Giving Machine presentation contract to define that each machine-window browse request advances the visible machine window by exactly one visible row.
- Tightened the matching acceptance criterion so downstream protocol and module alignment can verify the same browse-step rule explicitly.

## Open Questions

- None at this time.

## Ambiguities or Risks

- The contract is now explicit on browse-step granularity, but downstream protocol and module artifacts still need alignment in later stages if they currently use a different browse-step interpretation.

## Next Recommended Steps

- Align `docs/cop/protocols/giving-machine-window-protocol.md` to the updated one-row browse-step rule.
- Run downstream architecture, module, and validation alignment against the updated contract wording.
