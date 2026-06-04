package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.EmptyMachineState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineCatalogPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowBrowseRequest
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.PresentedGivingMachineItem

enum class GivingMachineWindowFailureReason {
    GIVING_MACHINE_CATALOG_UNAVAILABLE,
    REQUIRED_SLOT_CONTENT_MISSING,
}

data class GivingMachineWindowInput(
    val givingMachineCatalog: List<PresentedGivingMachineItem>?,
    val currentCatalogState: GivingMachineCatalogPresentationState,
    val machineBrowseRequest: MachineWindowBrowseRequest?,
)

data class GivingMachineWindowResponse(
    val machineWindowState: MachineWindowState?,
    val emptyMachineState: EmptyMachineState?,
    val failureResponse: CopFailureResponse<GivingMachineWindowFailureReason>? = null,
)

interface GivingMachineWindowPresenter {
    fun presentMachineWindow(
        input: GivingMachineWindowInput,
    ): GivingMachineWindowResponse
}
