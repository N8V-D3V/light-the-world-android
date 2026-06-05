package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineDismissAction
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachinePeekState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineReturnToHomeState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSheetState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSurfaceState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineVisibleContext

class StubGivingMachineEntryPresenter(
    private val expandedDismissActions: Set<GivingMachineDismissAction> = setOf(
        GivingMachineDismissAction.VISIBLE_X,
        GivingMachineDismissAction.SWIPE_DOWN,
    ),
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : GivingMachineEntryPresenter {

    override fun presentEntryState(
        input: GivingMachineEntryInput,
    ): GivingMachineEntryResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_entry_state_input",
            details = mapOf(
                "homeVisible" to input.homeChallengeSurfaceState.isVisible,
                "entryMethod" to input.machineEntryRequest?.method,
                "dismissMethod" to input.machineDismissRequest?.method,
                "expandedDismissActions" to expandedDismissActions,
            ),
        )

        val peekState = input.homeChallengeSurfaceState
            .takeIf { it.isVisible }
            ?.let { GivingMachinePeekState() }

        val response = when {
            input.machineDismissRequest != null -> GivingMachineEntryResponse(
                givingMachinePeekState = peekState,
                givingMachineDestinationState = null,
                returnToHomeState = GivingMachineReturnToHomeState(homeSurfaceResumed = true),
                failureResponse = null,
            )

            input.machineEntryRequest != null && !expandedDismissActions.contains(GivingMachineDismissAction.VISIBLE_X) ->
                failureResponse(
                    peekState = peekState,
                    reason = GivingMachineEntryFailureReason.DISMISS_ACTION_UNAVAILABLE,
                    message = "Expanded Giving Machine presentation cannot expose a visible dismissal action.",
                )

            input.machineEntryRequest != null -> GivingMachineEntryResponse(
                givingMachinePeekState = peekState,
                givingMachineDestinationState = GivingMachineSurfaceState(
                    sheetState = GivingMachineSheetState.EXPANDED,
                    visibleContext = GivingMachineVisibleContext.MACHINE_BROWSE,
                    dismissActions = expandedDismissActions,
                ),
                returnToHomeState = null,
                failureResponse = null,
            )

            else -> GivingMachineEntryResponse(
                givingMachinePeekState = peekState,
                givingMachineDestinationState = null,
                returnToHomeState = null,
                failureResponse = null,
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "present_entry_state_output",
            details = mapOf(
                "decision" to when {
                    response.failureResponse != null -> "entry_presentation_failed"
                    response.returnToHomeState != null -> "returned_to_home_surface"
                    response.givingMachineDestinationState != null -> "expanded_destination_presented"
                    response.givingMachinePeekState != null -> "persistent_peek_presented"
                    else -> "no_visible_home_surface"
                },
                "peekVisible" to (response.givingMachinePeekState != null),
                "sheetState" to response.givingMachineDestinationState?.sheetState,
                "visibleContext" to response.givingMachineDestinationState?.visibleContext,
                "returnToHome" to response.returnToHomeState?.homeSurfaceResumed,
                "failureReason" to response.failureResponse?.reason,
            ),
        )

        return response
    }

    private fun failureResponse(
        peekState: GivingMachinePeekState?,
        reason: GivingMachineEntryFailureReason,
        message: String,
    ): GivingMachineEntryResponse = GivingMachineEntryResponse(
        givingMachinePeekState = peekState,
        givingMachineDestinationState = null,
        returnToHomeState = null,
        failureResponse = CopFailureResponse(
            reason = reason,
            message = message,
        ),
    )

    companion object {
        private const val MODULE_NAME = "StubGivingMachineEntryPresenter"
    }
}
