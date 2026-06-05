package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

data class GivingMachineHomeSurfaceState(
    val isVisible: Boolean,
)

data class GivingMachinePeekState(
    val label: String = "Giving Machine",
)

enum class GivingMachineSheetState {
    PEEK,
    EXPANDED,
    CLOSED,
}

enum class GivingMachineVisibleContext {
    MACHINE_BROWSE,
    CART_OR_CHECKOUT,
    INFO,
}

enum class GivingMachineDismissAction {
    VISIBLE_X,
    SWIPE_DOWN,
}

data class GivingMachineSurfaceState(
    val sheetState: GivingMachineSheetState,
    val visibleContext: GivingMachineVisibleContext,
    val dismissActions: Set<GivingMachineDismissAction>,
)

data class GivingMachineReturnToHomeState(
    val homeSurfaceResumed: Boolean,
)

enum class GivingMachineEntryMethod {
    TAP,
    SWIPE_UP,
}

data class GivingMachineEntryRequest(
    val method: GivingMachineEntryMethod,
)

enum class GivingMachineDismissMethod {
    VISIBLE_X,
    SWIPE_DOWN,
}

data class GivingMachineDismissRequest(
    val method: GivingMachineDismissMethod,
)

enum class GivingMachineCatalogPresentationState {
    AVAILABLE,
    UNAVAILABLE,
    EMPTY,
}

enum class GivingMachineSlotSelectionStateValue {
    UNSELECTED,
    ARMED,
}

data class PresentedGivingMachineItem(
    val itemIdentifier: String,
    val slotNumber: String,
    val title: String,
    val description: String,
    val imageReference: String? = null,
    val selectionState: GivingMachineSlotSelectionStateValue,
)

enum class MachineWindowPositionState {
    TOP,
    MIDDLE,
    BOTTOM,
    SINGLE_WINDOW,
}

enum class MachineWindowPeekContinuationState {
    PEEK_ABOVE_AND_BELOW,
    PEEK_ABOVE_ONLY,
    PEEK_BELOW_ONLY,
    NO_PEEK,
}

data class MachineWindowState(
    val visibleSlotItems: List<PresentedGivingMachineItem>,
    val windowPositionState: MachineWindowPositionState,
    val peekContinuationState: MachineWindowPeekContinuationState,
)

enum class GivingMachineEmptyInteractionState {
    INFORMATIONAL,
}

data class EmptyMachineState(
    val message: String,
    val interactionState: GivingMachineEmptyInteractionState = GivingMachineEmptyInteractionState.INFORMATIONAL,
)

enum class MachineWindowBrowseDirection {
    PREVIOUS,
    NEXT,
}

enum class MachineWindowBrowseStep {
    ONE_VISIBLE_ROW,
}

data class MachineWindowBrowseRequest(
    val direction: MachineWindowBrowseDirection,
    val step: MachineWindowBrowseStep = MachineWindowBrowseStep.ONE_VISIBLE_ROW,
)

data class GivingMachineSlotSelectionRequest(
    val slotNumber: String,
)

data class GivingMachineSlotSelectionState(
    val armedSlotNumber: String?,
    val visibleSlotItems: List<PresentedGivingMachineItem>,
)

data class GivingMachineAddToCartConfirmationRequest(
    val confirmed: Boolean,
)

enum class AddToCartConfirmationStateValue {
    REQUIRED,
    CONFIRMED,
    NOT_AVAILABLE,
}

data class AddToCartConfirmationState(
    val armedSlotNumber: String?,
    val confirmationState: AddToCartConfirmationStateValue,
)

data class SuccessfulAddToCartPresentationResult(
    val itemIdentifier: String,
    val slotNumber: String,
)

enum class DispenseAnimationPresentationState {
    NOT_STARTED,
    PRESENTING,
    COMPLETED,
    NON_ANIMATED_CONFIRMATION,
}

data class DispenseAnimationState(
    val itemIdentifier: String?,
    val slotNumber: String?,
    val presentationState: DispenseAnimationPresentationState,
)

data class GivingMachineCartPresentationRequest(
    val requested: Boolean,
)

data class GivingMachineReturnFromCartOrCheckoutRequest(
    val requested: Boolean,
)

enum class CartOrCheckoutPresentationEmphasis {
    CLEAR_TASK_FOCUSED_REVIEW,
}

data class CartOrCheckoutPresentationState(
    val visibleContext: GivingMachineVisibleContext = GivingMachineVisibleContext.CART_OR_CHECKOUT,
    val emphasis: CartOrCheckoutPresentationEmphasis,
)

data class GivingMachineInfoPresentationRequest(
    val requested: Boolean,
)

data class GivingMachineReturnFromInfoRequest(
    val requested: Boolean,
)

data class InfoContentSection(
    val title: String,
    val body: String,
)

data class InfoPresentationContent(
    val screenTitle: String,
    val contentSections: List<InfoContentSection>,
)

enum class InfoInteractionState {
    INFORMATIONAL,
}

data class InfoPresentationState(
    val screenTitle: String,
    val contentSections: List<InfoContentSection>,
    val interactionState: InfoInteractionState = InfoInteractionState.INFORMATIONAL,
)

enum class GivingMachineAccessibilityAction {
    OPEN_MACHINE,
    BROWSE_PREVIOUS,
    BROWSE_NEXT,
    ARM_SLOT,
    CONFIRM_ADD_TO_CART,
    OPEN_CART_OR_CHECKOUT,
    OPEN_INFO,
    DISMISS_MACHINE,
}

data class GivingMachineAccessibilityActionRequest(
    val action: GivingMachineAccessibilityAction,
    val targetSlotNumber: String? = null,
)

data class GivingMachineAccessibilityPresentationContext(
    val entryLabel: String,
    val sheetState: GivingMachineSheetState,
    val visibleContext: GivingMachineVisibleContext,
    val dismissActions: Set<GivingMachineDismissAction>,
    val visibleSlotNumbers: List<String>,
    val armedSlotNumber: String?,
    val availableActions: Set<GivingMachineAccessibilityAction>,
)
