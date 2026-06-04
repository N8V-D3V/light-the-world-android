package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.CartOrCheckoutPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineCartPresentationRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineInfoPresentationRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineReturnFromCartOrCheckoutRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineReturnFromInfoRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSurfaceState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.InfoPresentationContent
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.InfoPresentationState

enum class GivingMachineTransitionFailureReason {
    INFO_CONTENT_UNAVAILABLE,
}

data class GivingMachineTransitionInput(
    val givingMachineDestinationState: GivingMachineSurfaceState,
    val cartPresentationRequest: GivingMachineCartPresentationRequest?,
    val returnFromCartOrCheckoutRequest: GivingMachineReturnFromCartOrCheckoutRequest?,
    val infoPresentationRequest: GivingMachineInfoPresentationRequest?,
    val returnFromInfoRequest: GivingMachineReturnFromInfoRequest?,
    val infoPresentationContent: InfoPresentationContent?,
)

data class GivingMachineTransitionResponse(
    val cartOrCheckoutPresentationState: CartOrCheckoutPresentationState?,
    val infoPresentationState: InfoPresentationState?,
    val givingMachineDestinationState: GivingMachineSurfaceState?,
    val failureResponse: CopFailureResponse<GivingMachineTransitionFailureReason>? = null,
)

interface GivingMachineTransitionPresenter {
    fun presentTransitionState(
        input: GivingMachineTransitionInput,
    ): GivingMachineTransitionResponse
}
