package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.GivingMachineAccessibilityFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.GivingMachineEntryFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowFailureReason

data class GivingMachinePresentationCatalogContext(
    val givingMachineCatalog: List<PresentedGivingMachineItem>?,
    val currentCatalogState: GivingMachineCatalogPresentationState,
)

data class GivingMachinePresentationEnvironment(
    val homeChallengeSurfaceState: GivingMachineHomeSurfaceState,
    val catalogContext: GivingMachinePresentationCatalogContext,
    val infoPresentationContent: InfoPresentationContent? = null,
)

data class GivingMachineConfirmedAddHandoff(
    val itemIdentifier: String,
    val slotNumber: String,
)

data class GivingMachinePresentationSliceState(
    val homeChallengeSurfaceState: GivingMachineHomeSurfaceState,
    val givingMachinePeekState: GivingMachinePeekState? = null,
    val givingMachineDestinationState: GivingMachineSurfaceState? = null,
    val returnToHomeState: GivingMachineReturnToHomeState? = null,
    val machineWindowState: MachineWindowState? = null,
    val emptyMachineState: EmptyMachineState? = null,
    val slotSelectionState: GivingMachineSlotSelectionState? = null,
    val addToCartConfirmationState: AddToCartConfirmationState? = null,
    val dispenseAnimationState: DispenseAnimationState? = null,
    val cartOrCheckoutPresentationState: CartOrCheckoutPresentationState? = null,
    val infoPresentationState: InfoPresentationState? = null,
    val accessibilityPresentationState: GivingMachineAccessibilityPresentationContext? = null,
)

data class GivingMachinePresentationSliceResponse(
    val presentationState: GivingMachinePresentationSliceState,
    val confirmedAddHandoff: GivingMachineConfirmedAddHandoff? = null,
    val entryFailureResponse: CopFailureResponse<GivingMachineEntryFailureReason>? = null,
    val windowFailureResponse: CopFailureResponse<GivingMachineWindowFailureReason>? = null,
    val selectionFailureResponse: CopFailureResponse<GivingMachineSelectionFailureReason>? = null,
    val transitionFailureResponse: CopFailureResponse<GivingMachineTransitionFailureReason>? = null,
    val accessibilityFailureResponse: CopFailureResponse<GivingMachineAccessibilityFailureReason>? = null,
)
