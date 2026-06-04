package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.AddToCartConfirmationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.CartOrCheckoutPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineAccessibilityActionRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineAccessibilityPresentationContext
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachinePeekState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSlotSelectionState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSurfaceState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.InfoPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowState

enum class GivingMachineAccessibilityFailureReason {
    ACCESSIBILITY_STATE_UNAVAILABLE,
}

data class GivingMachineAccessibilityInput(
    val givingMachinePeekState: GivingMachinePeekState?,
    val givingMachineDestinationState: GivingMachineSurfaceState?,
    val machineWindowState: MachineWindowState?,
    val slotSelectionState: GivingMachineSlotSelectionState?,
    val addToCartConfirmationState: AddToCartConfirmationState?,
    val cartOrCheckoutPresentationState: CartOrCheckoutPresentationState?,
    val infoPresentationState: InfoPresentationState?,
    val accessibilityActionRequest: GivingMachineAccessibilityActionRequest?,
)

data class GivingMachineAccessibilityResponse(
    val accessibilityPresentationState: GivingMachineAccessibilityPresentationContext?,
    val failureResponse: CopFailureResponse<GivingMachineAccessibilityFailureReason>? = null,
)

interface GivingMachineAccessibilityPresenter {
    fun presentAccessibilityState(
        input: GivingMachineAccessibilityInput,
    ): GivingMachineAccessibilityResponse
}
