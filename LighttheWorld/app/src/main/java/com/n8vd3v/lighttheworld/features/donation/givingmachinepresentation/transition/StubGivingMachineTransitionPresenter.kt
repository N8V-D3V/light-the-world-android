package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.CartOrCheckoutPresentationEmphasis
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.CartOrCheckoutPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSheetState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSurfaceState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineVisibleContext
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.InfoPresentationContent
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.InfoPresentationState

class StubGivingMachineTransitionPresenter(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : GivingMachineTransitionPresenter {

    override fun presentTransitionState(
        input: GivingMachineTransitionInput,
    ): GivingMachineTransitionResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_transition_state_input",
            details = mapOf(
                "sheetState" to input.givingMachineDestinationState.sheetState,
                "visibleContext" to input.givingMachineDestinationState.visibleContext,
                "cartRequested" to input.cartPresentationRequest?.requested,
                "returnFromCartRequested" to input.returnFromCartOrCheckoutRequest?.requested,
                "infoRequested" to input.infoPresentationRequest?.requested,
                "returnFromInfoRequested" to input.returnFromInfoRequest?.requested,
                "infoContentSectionCount" to input.infoPresentationContent?.contentSections?.size,
            ),
        )

        val response = when {
            input.returnFromCartOrCheckoutRequest?.requested == true -> GivingMachineTransitionResponse(
                cartOrCheckoutPresentationState = null,
                infoPresentationState = null,
                givingMachineDestinationState = input.givingMachineDestinationState.returnToMachineBrowse(),
                failureResponse = null,
            )

            input.returnFromInfoRequest?.requested == true -> GivingMachineTransitionResponse(
                cartOrCheckoutPresentationState = null,
                infoPresentationState = null,
                givingMachineDestinationState = input.givingMachineDestinationState.returnToMachineBrowse(),
                failureResponse = null,
            )

            input.infoPresentationRequest?.requested == true && !input.infoPresentationContent.hasRequiredInfoContent() ->
                GivingMachineTransitionResponse(
                    cartOrCheckoutPresentationState = null,
                    infoPresentationState = null,
                    givingMachineDestinationState = input.givingMachineDestinationState,
                    failureResponse = CopFailureResponse(
                        reason = GivingMachineTransitionFailureReason.INFO_CONTENT_UNAVAILABLE,
                        message = "Info presentation content is unavailable.",
                    ),
                )

            input.infoPresentationRequest?.requested == true -> GivingMachineTransitionResponse(
                cartOrCheckoutPresentationState = null,
                infoPresentationState = InfoPresentationState(
                    screenTitle = input.infoPresentationContent!!.screenTitle,
                    contentSections = input.infoPresentationContent.contentSections,
                ),
                givingMachineDestinationState = input.givingMachineDestinationState.copy(
                    sheetState = GivingMachineSheetState.EXPANDED,
                    visibleContext = GivingMachineVisibleContext.INFO,
                ),
                failureResponse = null,
            )

            input.cartPresentationRequest?.requested == true -> GivingMachineTransitionResponse(
                cartOrCheckoutPresentationState = CartOrCheckoutPresentationState(
                    emphasis = CartOrCheckoutPresentationEmphasis.CLEAR_TASK_FOCUSED_REVIEW,
                ),
                infoPresentationState = null,
                givingMachineDestinationState = input.givingMachineDestinationState.copy(
                    sheetState = GivingMachineSheetState.EXPANDED,
                    visibleContext = GivingMachineVisibleContext.CART_OR_CHECKOUT,
                ),
                failureResponse = null,
            )

            else -> GivingMachineTransitionResponse(
                cartOrCheckoutPresentationState = null,
                infoPresentationState = null,
                givingMachineDestinationState = input.givingMachineDestinationState,
                failureResponse = null,
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "present_transition_state_output",
            details = mapOf(
                "decision" to when {
                    response.failureResponse != null -> "transition_failed"
                    response.infoPresentationState != null -> "info_presentation_presented"
                    response.cartOrCheckoutPresentationState != null -> "cart_or_checkout_presented"
                    response.givingMachineDestinationState?.visibleContext == GivingMachineVisibleContext.MACHINE_BROWSE &&
                        (input.returnFromCartOrCheckoutRequest?.requested == true || input.returnFromInfoRequest?.requested == true) ->
                        "returned_to_machine_browse"
                    else -> "destination_state_preserved"
                },
                "visibleContext" to response.givingMachineDestinationState?.visibleContext,
                "sheetState" to response.givingMachineDestinationState?.sheetState,
                "failureReason" to response.failureResponse?.reason,
            ),
        )

        return response
    }

    private fun InfoPresentationContent?.hasRequiredInfoContent(): Boolean =
        this != null &&
            screenTitle.isNotBlank() &&
            contentSections.isNotEmpty() &&
            contentSections.all { it.title.isNotBlank() && it.body.isNotBlank() }

    private fun GivingMachineSurfaceState.returnToMachineBrowse(): GivingMachineSurfaceState =
        copy(
            sheetState = GivingMachineSheetState.EXPANDED,
            visibleContext = GivingMachineVisibleContext.MACHINE_BROWSE,
        )

    companion object {
        private const val MODULE_NAME = "StubGivingMachineTransitionPresenter"
    }
}
