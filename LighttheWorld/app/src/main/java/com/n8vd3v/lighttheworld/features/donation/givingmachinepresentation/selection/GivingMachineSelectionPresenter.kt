package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.AddToCartConfirmationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.DispenseAnimationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineAddToCartConfirmationRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSlotSelectionRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSlotSelectionState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.SuccessfulAddToCartPresentationResult

enum class GivingMachineSelectionFailureReason {
    SLOT_NOT_VISIBLE,
    NO_ARMED_SLOT,
    DISPENSE_ANIMATION_UNAVAILABLE,
}

data class GivingMachineSelectionInput(
    val machineWindowState: MachineWindowState?,
    val slotSelectionRequest: GivingMachineSlotSelectionRequest?,
    val addToCartConfirmationRequest: GivingMachineAddToCartConfirmationRequest?,
    val currentArmedSlotNumber: String?,
    val successfulAddToCartResult: SuccessfulAddToCartPresentationResult?,
)

data class GivingMachineSelectionResponse(
    val slotSelectionState: GivingMachineSlotSelectionState?,
    val addToCartConfirmationState: AddToCartConfirmationState?,
    val dispenseAnimationState: DispenseAnimationState?,
    val failureResponse: CopFailureResponse<GivingMachineSelectionFailureReason>? = null,
)

interface GivingMachineSelectionPresenter {
    fun presentSelectionState(
        input: GivingMachineSelectionInput,
    ): GivingMachineSelectionResponse
}
