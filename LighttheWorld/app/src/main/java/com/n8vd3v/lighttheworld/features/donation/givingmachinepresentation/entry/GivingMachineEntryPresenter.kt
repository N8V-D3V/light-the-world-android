package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineDismissRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineEntryRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineHomeSurfaceState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachinePeekState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineReturnToHomeState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSurfaceState

enum class GivingMachineEntryFailureReason {
    DISMISS_ACTION_UNAVAILABLE,
}

data class GivingMachineEntryInput(
    val homeChallengeSurfaceState: GivingMachineHomeSurfaceState,
    val machineEntryRequest: GivingMachineEntryRequest?,
    val machineDismissRequest: GivingMachineDismissRequest?,
)

data class GivingMachineEntryResponse(
    val givingMachinePeekState: GivingMachinePeekState?,
    val givingMachineDestinationState: GivingMachineSurfaceState?,
    val returnToHomeState: GivingMachineReturnToHomeState?,
    val failureResponse: CopFailureResponse<GivingMachineEntryFailureReason>? = null,
)

interface GivingMachineEntryPresenter {
    fun presentEntryState(
        input: GivingMachineEntryInput,
    ): GivingMachineEntryResponse
}
