package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.StubGivingMachineAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.StubGivingMachineEntryPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.StubGivingMachineSelectionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.StubGivingMachineTransitionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.StubGivingMachineWindowPresenter

data class LTWGivingMachineSessionGift(
    val itemIdentifier: String,
    val slotNumber: String,
    val title: String,
    val description: String,
)

data class LTWGivingMachineUiState(
    val environment: GivingMachinePresentationEnvironment,
    val presentationState: GivingMachinePresentationSliceState,
    val pendingConfirmedAddHandoff: GivingMachineConfirmedAddHandoff? = null,
    val sessionGifts: List<LTWGivingMachineSessionGift> = emptyList(),
    val latestFailureMessage: String? = null,
)

class LTWGivingMachineUiController(
    private val orchestrator: GivingMachinePresentationSliceOrchestrator = GivingMachinePresentationSliceOrchestrator(
        entryPresenter = StubGivingMachineEntryPresenter(),
        windowPresenter = StubGivingMachineWindowPresenter(),
        selectionPresenter = StubGivingMachineSelectionPresenter(),
        transitionPresenter = StubGivingMachineTransitionPresenter(),
        accessibilityPresenter = StubGivingMachineAccessibilityPresenter(),
    ),
    private val environment: GivingMachinePresentationEnvironment = LTWGivingMachineSeedContent.defaultEnvironment(),
) {

    var uiState by mutableStateOf(
        reduceResponse(
            response = orchestrator.presentHomePeek(environment),
            pendingConfirmedAddHandoff = null,
            sessionGifts = emptyList(),
        ),
    )
        private set

    fun openMachine(
        method: GivingMachineEntryMethod = GivingMachineEntryMethod.TAP,
    ) {
        applyResponse(
            orchestrator.openMachine(
                currentState = uiState.presentationState,
                environment = environment,
                entryRequest = GivingMachineEntryRequest(method = method),
            ),
        )
    }

    fun dismissMachine(
        method: GivingMachineDismissMethod = GivingMachineDismissMethod.VISIBLE_X,
    ) {
        applyResponse(
            orchestrator.dismissMachine(
                currentState = uiState.presentationState,
                environment = environment,
                dismissRequest = GivingMachineDismissRequest(method = method),
            ),
        )
    }

    fun browseMachineWindow(direction: MachineWindowBrowseDirection) {
        applyResponse(
            orchestrator.browseMachineWindow(
                currentState = uiState.presentationState,
                environment = environment,
                browseRequest = MachineWindowBrowseRequest(
                    direction = direction,
                    step = MachineWindowBrowseStep.ONE_VISIBLE_ROW,
                ),
            ),
        )
    }

    fun armVisibleSlot(slotNumber: String) {
        applyResponse(
            orchestrator.armVisibleSlot(
                currentState = uiState.presentationState,
                environment = environment,
                slotSelectionRequest = GivingMachineSlotSelectionRequest(slotNumber = slotNumber),
            ),
        )
    }

    fun confirmAddToCart() {
        applyResponse(
            orchestrator.confirmAddToCart(
                currentState = uiState.presentationState,
                environment = environment,
            ),
        )
    }

    fun confirmGiftFromDetail(slotNumber: String) {
        armVisibleSlot(slotNumber)
        confirmAddToCart()
    }

    fun acceptSuccessfulAddResult(handoff: GivingMachineConfirmedAddHandoff) {
        val successfulAddResult = SuccessfulAddToCartPresentationResult(
            itemIdentifier = handoff.itemIdentifier,
            slotNumber = handoff.slotNumber,
        )
        val nextSessionGift = uiState.environment.catalogContext.givingMachineCatalog
            .orEmpty()
            .firstOrNull { it.itemIdentifier == handoff.itemIdentifier }
            ?.let { item ->
                LTWGivingMachineSessionGift(
                    itemIdentifier = item.itemIdentifier,
                    slotNumber = handoff.slotNumber,
                    title = item.title,
                    description = item.description,
                )
            }
        val sessionGifts = if (nextSessionGift == null) {
            uiState.sessionGifts
        } else {
            uiState.sessionGifts + nextSessionGift
        }
        uiState = reduceResponse(
            response = orchestrator.presentSuccessfulAddResult(
                currentState = uiState.presentationState,
                environment = environment,
                successfulAddToCartResult = successfulAddResult,
            ),
            pendingConfirmedAddHandoff = null,
            sessionGifts = sessionGifts,
        )
    }

    fun continuePresentation() {
        applyResponse(
            orchestrator.continuePresentation(
                currentState = uiState.presentationState,
                environment = environment,
            ),
        )
    }

    fun openCartOrCheckout() {
        applyResponse(
            orchestrator.openCartOrCheckout(
                currentState = uiState.presentationState,
                environment = environment,
            ),
        )
    }

    fun returnFromCartOrCheckout() {
        applyResponse(
            orchestrator.returnFromCartOrCheckout(
                currentState = uiState.presentationState,
                environment = environment,
            ),
        )
    }

    fun openInfo() {
        applyResponse(
            orchestrator.openInfo(
                currentState = uiState.presentationState,
                environment = environment,
            ),
        )
    }

    fun returnFromInfo() {
        applyResponse(
            orchestrator.returnFromInfo(
                currentState = uiState.presentationState,
                environment = environment,
            ),
        )
    }

    private fun applyResponse(
        response: GivingMachinePresentationSliceResponse,
    ) {
        uiState = reduceResponse(
            response = response,
            pendingConfirmedAddHandoff = response.confirmedAddHandoff,
            sessionGifts = uiState.sessionGifts,
        )
    }

    private fun reduceResponse(
        response: GivingMachinePresentationSliceResponse,
        pendingConfirmedAddHandoff: GivingMachineConfirmedAddHandoff?,
        sessionGifts: List<LTWGivingMachineSessionGift>,
    ): LTWGivingMachineUiState = LTWGivingMachineUiState(
        environment = environment,
        presentationState = response.presentationState,
        pendingConfirmedAddHandoff = pendingConfirmedAddHandoff,
        sessionGifts = sessionGifts,
        latestFailureMessage = response.entryFailureResponse?.message
            ?: response.windowFailureResponse?.message
            ?: response.selectionFailureResponse?.message
            ?: response.transitionFailureResponse?.message
            ?: response.accessibilityFailureResponse?.message,
    )
}
