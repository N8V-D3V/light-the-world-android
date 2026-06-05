package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.AddToCartConfirmationStateValue
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineAccessibilityAction
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineAccessibilityPresentationContext
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineDismissAction
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSheetState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineVisibleContext
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowPeekContinuationState

class StubGivingMachineAccessibilityPresenter(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : GivingMachineAccessibilityPresenter {

    override fun presentAccessibilityState(
        input: GivingMachineAccessibilityInput,
    ): GivingMachineAccessibilityResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_accessibility_state_input",
            details = mapOf(
                "peekVisible" to (input.givingMachinePeekState != null),
                "sheetState" to input.givingMachineDestinationState?.sheetState,
                "visibleContext" to input.givingMachineDestinationState?.visibleContext,
                "visibleSlotCount" to input.machineWindowState?.visibleSlotItems?.size,
                "armedSlotNumber" to input.slotSelectionState?.armedSlotNumber,
                "confirmationState" to input.addToCartConfirmationState?.confirmationState,
                "accessibilityAction" to input.accessibilityActionRequest?.action,
                "accessibilityTargetSlotNumber" to input.accessibilityActionRequest?.targetSlotNumber,
            ),
        )

        val peekState = input.givingMachinePeekState
            ?: return failureResponse("Giving Machine entry label is unavailable.")
        val destinationState = input.givingMachineDestinationState
        val sheetState = destinationState?.sheetState ?: GivingMachineSheetState.PEEK
        val visibleContext = destinationState?.visibleContext ?: GivingMachineVisibleContext.MACHINE_BROWSE
        val dismissActions = destinationState?.dismissActions.orEmpty()
        val visibleSlotNumbers = input.machineWindowState?.visibleSlotItems.orEmpty().map { it.slotNumber }
        val armedSlotNumber = input.slotSelectionState?.armedSlotNumber ?: input.addToCartConfirmationState?.armedSlotNumber

        val failureMessage = validateState(
            sheetState = sheetState,
            visibleContext = visibleContext,
            dismissActions = dismissActions,
            visibleSlotNumbers = visibleSlotNumbers,
            armedSlotNumber = armedSlotNumber,
            confirmationState = input.addToCartConfirmationState?.confirmationState,
            hasMachineWindowState = input.machineWindowState != null,
            hasCartPresentationState = input.cartOrCheckoutPresentationState != null,
            hasInfoPresentationState = input.infoPresentationState != null,
        )
        if (failureMessage != null) {
            return failureResponse(failureMessage)
        }

        val availableActions = availableActionsFor(
            sheetState = sheetState,
            visibleContext = visibleContext,
            dismissActions = dismissActions,
            machineWindowPeekState = input.machineWindowState?.peekContinuationState,
            visibleSlotNumbers = visibleSlotNumbers,
            armedSlotNumber = armedSlotNumber,
            confirmationState = input.addToCartConfirmationState?.confirmationState,
        )

        val response = GivingMachineAccessibilityResponse(
            accessibilityPresentationState = GivingMachineAccessibilityPresentationContext(
                entryLabel = peekState.label,
                sheetState = sheetState,
                visibleContext = visibleContext,
                dismissActions = dismissActions,
                visibleSlotNumbers = visibleSlotNumbers,
                armedSlotNumber = armedSlotNumber,
                availableActions = availableActions,
            ),
            failureResponse = null,
        )

        logger.logDecision(
            module = MODULE_NAME,
            action = "present_accessibility_state_output",
            details = mapOf(
                "decision" to "accessibility_state_presented",
                "sheetState" to response.accessibilityPresentationState?.sheetState,
                "visibleContext" to response.accessibilityPresentationState?.visibleContext,
                "availableActions" to response.accessibilityPresentationState?.availableActions,
                "failureReason" to response.failureResponse?.reason,
            ),
        )

        return response
    }

    private fun validateState(
        sheetState: GivingMachineSheetState,
        visibleContext: GivingMachineVisibleContext,
        dismissActions: Set<GivingMachineDismissAction>,
        visibleSlotNumbers: List<String>,
        armedSlotNumber: String?,
        confirmationState: AddToCartConfirmationStateValue?,
        hasMachineWindowState: Boolean,
        hasCartPresentationState: Boolean,
        hasInfoPresentationState: Boolean,
    ): String? {
        if (sheetState == GivingMachineSheetState.EXPANDED && dismissActions.isEmpty()) {
            return "Expanded Giving Machine accessibility state cannot expose dismissal actions."
        }
        if (visibleContext == GivingMachineVisibleContext.MACHINE_BROWSE && sheetState == GivingMachineSheetState.EXPANDED) {
            if (!hasMachineWindowState) {
                return "Machine browse accessibility state is unavailable because the visible machine window is missing."
            }
            if (visibleSlotNumbers.any { it.isBlank() }) {
                return "Machine browse accessibility state cannot expose visible slot identity."
            }
            if (armedSlotNumber != null && armedSlotNumber !in visibleSlotNumbers) {
                return "Machine browse accessibility state cannot expose the armed slot in the visible window."
            }
            if (confirmationState == AddToCartConfirmationStateValue.REQUIRED && armedSlotNumber == null) {
                return "Machine browse accessibility state cannot expose the armed slot required for confirmation."
            }
        }
        if (visibleContext == GivingMachineVisibleContext.CART_OR_CHECKOUT && !hasCartPresentationState) {
            return "Cart or checkout accessibility state is unavailable."
        }
        if (visibleContext == GivingMachineVisibleContext.INFO && !hasInfoPresentationState) {
            return "Info accessibility state is unavailable."
        }
        return null
    }

    private fun availableActionsFor(
        sheetState: GivingMachineSheetState,
        visibleContext: GivingMachineVisibleContext,
        dismissActions: Set<GivingMachineDismissAction>,
        machineWindowPeekState: MachineWindowPeekContinuationState?,
        visibleSlotNumbers: List<String>,
        armedSlotNumber: String?,
        confirmationState: AddToCartConfirmationStateValue?,
    ): Set<GivingMachineAccessibilityAction> {
        if (sheetState != GivingMachineSheetState.EXPANDED) {
            return setOf(GivingMachineAccessibilityAction.OPEN_MACHINE)
        }

        val actions = linkedSetOf<GivingMachineAccessibilityAction>()
        if (dismissActions.isNotEmpty()) {
            actions += GivingMachineAccessibilityAction.DISMISS_MACHINE
        }

        when (visibleContext) {
            GivingMachineVisibleContext.MACHINE_BROWSE -> {
                when (machineWindowPeekState) {
                    MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW -> {
                        actions += GivingMachineAccessibilityAction.BROWSE_PREVIOUS
                        actions += GivingMachineAccessibilityAction.BROWSE_NEXT
                    }
                    MachineWindowPeekContinuationState.PEEK_ABOVE_ONLY ->
                        actions += GivingMachineAccessibilityAction.BROWSE_PREVIOUS
                    MachineWindowPeekContinuationState.PEEK_BELOW_ONLY ->
                        actions += GivingMachineAccessibilityAction.BROWSE_NEXT
                    MachineWindowPeekContinuationState.NO_PEEK,
                    null,
                    -> Unit
                }
                if (visibleSlotNumbers.isNotEmpty()) {
                    actions += GivingMachineAccessibilityAction.ARM_SLOT
                }
                if (confirmationState == AddToCartConfirmationStateValue.REQUIRED && armedSlotNumber != null) {
                    actions += GivingMachineAccessibilityAction.CONFIRM_ADD_TO_CART
                }
                actions += GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT
                actions += GivingMachineAccessibilityAction.OPEN_INFO
            }

            GivingMachineVisibleContext.CART_OR_CHECKOUT -> {
                actions += GivingMachineAccessibilityAction.OPEN_INFO
            }

            GivingMachineVisibleContext.INFO -> {
                actions += GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT
            }
        }

        return actions
    }

    private fun failureResponse(
        message: String,
    ): GivingMachineAccessibilityResponse {
        val response = GivingMachineAccessibilityResponse(
            accessibilityPresentationState = null,
            failureResponse = CopFailureResponse(
                reason = GivingMachineAccessibilityFailureReason.ACCESSIBILITY_STATE_UNAVAILABLE,
                message = message,
            ),
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_accessibility_state_output",
            details = mapOf(
                "decision" to "accessibility_state_failed",
                "failureReason" to response.failureResponse?.reason,
                "message" to message,
            ),
        )
        return response
    }

    companion object {
        private const val MODULE_NAME = "StubGivingMachineAccessibilityPresenter"
    }
}
