package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.AddToCartConfirmationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.AddToCartConfirmationStateValue
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.DispenseAnimationPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.DispenseAnimationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSlotSelectionState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSlotSelectionStateValue
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.PresentedGivingMachineItem
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.SuccessfulAddToCartPresentationResult

class StubGivingMachineSelectionPresenter(
    private val dispensePresentationAvailable: Boolean = true,
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : GivingMachineSelectionPresenter {

    private var pendingDispenseCompletion: SuccessfulAddToCartPresentationResult? = null

    override fun presentSelectionState(
        input: GivingMachineSelectionInput,
    ): GivingMachineSelectionResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_selection_state_input",
            details = mapOf(
                "visibleSlotCount" to input.machineWindowState?.visibleSlotItems?.size,
                "requestedSlotNumber" to input.slotSelectionRequest?.slotNumber,
                "confirmationRequested" to input.addToCartConfirmationRequest?.confirmed,
                "currentArmedSlotNumber" to input.currentArmedSlotNumber,
                "successfulAddSlotNumber" to input.successfulAddToCartResult?.slotNumber,
                "pendingDispenseCompletion" to pendingDispenseCompletion?.slotNumber,
                "dispensePresentationAvailable" to dispensePresentationAvailable,
            ),
        )

        val response = when {
            input.slotSelectionRequest != null -> {
                pendingDispenseCompletion = null
                presentArmState(
                    machineWindowState = input.machineWindowState,
                    currentArmedSlotNumber = input.currentArmedSlotNumber,
                    requestedSlotNumber = input.slotSelectionRequest.slotNumber,
                )
            }

            input.addToCartConfirmationRequest?.confirmed == true -> {
                pendingDispenseCompletion = null
                presentConfirmationState(
                    machineWindowState = input.machineWindowState,
                    currentArmedSlotNumber = input.currentArmedSlotNumber,
                )
            }

            input.successfulAddToCartResult != null -> {
                presentSuccessfulAddResult(
                    machineWindowState = input.machineWindowState,
                    successfulAddResult = input.successfulAddToCartResult,
                )
            }

            pendingDispenseCompletion != null -> {
                val completedDispense = pendingDispenseCompletion!!
                pendingDispenseCompletion = null
                GivingMachineSelectionResponse(
                    slotSelectionState = clearedSelectionState(input.machineWindowState),
                    addToCartConfirmationState = AddToCartConfirmationState(
                        armedSlotNumber = null,
                        confirmationState = AddToCartConfirmationStateValue.NOT_AVAILABLE,
                    ),
                    dispenseAnimationState = DispenseAnimationState(
                        itemIdentifier = completedDispense.itemIdentifier,
                        slotNumber = completedDispense.slotNumber,
                        presentationState = DispenseAnimationPresentationState.COMPLETED,
                    ),
                    failureResponse = null,
                )
            }

            else -> GivingMachineSelectionResponse(
                slotSelectionState = selectionState(
                    machineWindowState = input.machineWindowState,
                    armedSlotNumber = input.currentArmedSlotNumber,
                ),
                addToCartConfirmationState = confirmationStateFor(
                    armedSlotNumber = armedSlotNumberIfVisible(
                        machineWindowState = input.machineWindowState,
                        armedSlotNumber = input.currentArmedSlotNumber,
                    ),
                ),
                dispenseAnimationState = null,
                failureResponse = null,
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "present_selection_state_output",
            details = mapOf(
                "decision" to when {
                    response.failureResponse != null -> "selection_presentation_failed"
                    response.dispenseAnimationState?.presentationState == DispenseAnimationPresentationState.PRESENTING ->
                        "dispense_animation_presenting"
                    response.dispenseAnimationState?.presentationState == DispenseAnimationPresentationState.NON_ANIMATED_CONFIRMATION ->
                        "successful_add_preserved_without_dispense_animation"
                    response.dispenseAnimationState?.presentationState == DispenseAnimationPresentationState.COMPLETED ->
                        "dispense_animation_completed_and_armed_state_cleared"
                    response.addToCartConfirmationState?.confirmationState == AddToCartConfirmationStateValue.CONFIRMED ->
                        "explicit_add_confirmation_presented"
                    response.addToCartConfirmationState?.confirmationState == AddToCartConfirmationStateValue.REQUIRED ->
                        "slot_armed_and_confirmation_required"
                    else -> "selection_state_presented"
                },
                "armedSlotNumber" to response.slotSelectionState?.armedSlotNumber,
                "confirmationState" to response.addToCartConfirmationState?.confirmationState,
                "dispenseState" to response.dispenseAnimationState?.presentationState,
                "failureReason" to response.failureResponse?.reason,
            ),
        )

        return response
    }

    private fun presentArmState(
        machineWindowState: MachineWindowState?,
        currentArmedSlotNumber: String?,
        requestedSlotNumber: String,
    ): GivingMachineSelectionResponse {
        val visibleItems = machineWindowState?.visibleSlotItems.orEmpty()
        val targetItem = visibleItems.firstOrNull { it.slotNumber == requestedSlotNumber }
            ?: return failureResponse(
                machineWindowState = machineWindowState,
                armedSlotNumber = currentArmedSlotNumber,
                reason = GivingMachineSelectionFailureReason.SLOT_NOT_VISIBLE,
                message = "Requested Giving Machine slot is not currently visible.",
            )

        return GivingMachineSelectionResponse(
            slotSelectionState = selectionState(
                machineWindowState = machineWindowState,
                armedSlotNumber = targetItem.slotNumber,
            ),
            addToCartConfirmationState = AddToCartConfirmationState(
                armedSlotNumber = targetItem.slotNumber,
                confirmationState = AddToCartConfirmationStateValue.REQUIRED,
            ),
            dispenseAnimationState = null,
            failureResponse = null,
        )
    }

    private fun presentConfirmationState(
        machineWindowState: MachineWindowState?,
        currentArmedSlotNumber: String?,
    ): GivingMachineSelectionResponse {
        val armedSlotNumber = armedSlotNumberIfVisible(machineWindowState, currentArmedSlotNumber)
            ?: return failureResponse(
                machineWindowState = machineWindowState,
                armedSlotNumber = currentArmedSlotNumber,
                reason = GivingMachineSelectionFailureReason.NO_ARMED_SLOT,
                message = "Add-to-cart confirmation requires an armed Giving Machine slot.",
            )

        return GivingMachineSelectionResponse(
            slotSelectionState = selectionState(
                machineWindowState = machineWindowState,
                armedSlotNumber = armedSlotNumber,
            ),
            addToCartConfirmationState = AddToCartConfirmationState(
                armedSlotNumber = armedSlotNumber,
                confirmationState = AddToCartConfirmationStateValue.CONFIRMED,
            ),
            dispenseAnimationState = null,
            failureResponse = null,
        )
    }

    private fun presentSuccessfulAddResult(
        machineWindowState: MachineWindowState?,
        successfulAddResult: SuccessfulAddToCartPresentationResult,
    ): GivingMachineSelectionResponse {
        val clearedSelectionState = clearedSelectionState(machineWindowState)
        return if (dispensePresentationAvailable) {
            pendingDispenseCompletion = successfulAddResult
            GivingMachineSelectionResponse(
                slotSelectionState = clearedSelectionState,
                addToCartConfirmationState = AddToCartConfirmationState(
                    armedSlotNumber = successfulAddResult.slotNumber,
                    confirmationState = AddToCartConfirmationStateValue.CONFIRMED,
                ),
                dispenseAnimationState = DispenseAnimationState(
                    itemIdentifier = successfulAddResult.itemIdentifier,
                    slotNumber = successfulAddResult.slotNumber,
                    presentationState = DispenseAnimationPresentationState.PRESENTING,
                ),
                failureResponse = null,
            )
        } else {
            pendingDispenseCompletion = null
            GivingMachineSelectionResponse(
                slotSelectionState = clearedSelectionState,
                addToCartConfirmationState = AddToCartConfirmationState(
                    armedSlotNumber = successfulAddResult.slotNumber,
                    confirmationState = AddToCartConfirmationStateValue.CONFIRMED,
                ),
                dispenseAnimationState = DispenseAnimationState(
                    itemIdentifier = successfulAddResult.itemIdentifier,
                    slotNumber = successfulAddResult.slotNumber,
                    presentationState = DispenseAnimationPresentationState.NON_ANIMATED_CONFIRMATION,
                ),
                failureResponse = CopFailureResponse(
                    reason = GivingMachineSelectionFailureReason.DISPENSE_ANIMATION_UNAVAILABLE,
                    message = "Dispense animation presentation is unavailable, so success is preserved without animation.",
                ),
            )
        }
    }

    private fun failureResponse(
        machineWindowState: MachineWindowState?,
        armedSlotNumber: String?,
        reason: GivingMachineSelectionFailureReason,
        message: String,
    ): GivingMachineSelectionResponse = GivingMachineSelectionResponse(
        slotSelectionState = selectionState(
            machineWindowState = machineWindowState,
            armedSlotNumber = armedSlotNumberIfVisible(machineWindowState, armedSlotNumber),
        ),
        addToCartConfirmationState = confirmationStateFor(
            armedSlotNumber = armedSlotNumberIfVisible(machineWindowState, armedSlotNumber),
        ),
        dispenseAnimationState = null,
        failureResponse = CopFailureResponse(
            reason = reason,
            message = message,
        ),
    )

    private fun confirmationStateFor(
        armedSlotNumber: String?,
    ): AddToCartConfirmationState = AddToCartConfirmationState(
        armedSlotNumber = armedSlotNumber,
        confirmationState = if (armedSlotNumber == null) {
            AddToCartConfirmationStateValue.NOT_AVAILABLE
        } else {
            AddToCartConfirmationStateValue.REQUIRED
        },
    )

    private fun selectionState(
        machineWindowState: MachineWindowState?,
        armedSlotNumber: String?,
    ): GivingMachineSlotSelectionState? {
        val visibleItems = machineWindowState?.visibleSlotItems ?: return null
        return GivingMachineSlotSelectionState(
            armedSlotNumber = armedSlotNumber,
            visibleSlotItems = visibleItems.map { item ->
                item.copy(
                    selectionState = if (item.slotNumber == armedSlotNumber) {
                        GivingMachineSlotSelectionStateValue.ARMED
                    } else {
                        GivingMachineSlotSelectionStateValue.UNSELECTED
                    },
                )
            },
        )
    }

    private fun clearedSelectionState(
        machineWindowState: MachineWindowState?,
    ): GivingMachineSlotSelectionState? = selectionState(
        machineWindowState = machineWindowState,
        armedSlotNumber = null,
    )

    private fun armedSlotNumberIfVisible(
        machineWindowState: MachineWindowState?,
        armedSlotNumber: String?,
    ): String? {
        if (armedSlotNumber == null) {
            return null
        }
        val visibleItems = machineWindowState?.visibleSlotItems.orEmpty()
        return armedSlotNumber.takeIf { slotNumber ->
            visibleItems.any { it.slotNumber == slotNumber }
        }
    }

    companion object {
        private const val MODULE_NAME = "StubGivingMachineSelectionPresenter"
    }
}
