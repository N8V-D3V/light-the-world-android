package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import com.n8vd3v.lighttheworld.cop.InMemoryStubDecisionLogger
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.GivingMachineAccessibilityFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.GivingMachineAccessibilityInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.StubGivingMachineAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.GivingMachineEntryFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.GivingMachineEntryInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.StubGivingMachineEntryPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.StubGivingMachineSelectionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.StubGivingMachineTransitionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.StubGivingMachineWindowPresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GivingMachinePresentationModulesTest {

    @Test
    fun entryModulePresentsPersistentPeekExpandedDestinationAndReturnHome() {
        val logger = InMemoryStubDecisionLogger()
        val module = StubGivingMachineEntryPresenter(logger = logger)

        val peekResponse = module.presentEntryState(
            GivingMachineEntryInput(
                homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
                machineEntryRequest = null,
                machineDismissRequest = null,
            ),
        )
        val openResponse = module.presentEntryState(
            GivingMachineEntryInput(
                homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
                machineEntryRequest = GivingMachineEntryRequest(GivingMachineEntryMethod.TAP),
                machineDismissRequest = null,
            ),
        )
        val dismissResponse = module.presentEntryState(
            GivingMachineEntryInput(
                homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
                machineEntryRequest = null,
                machineDismissRequest = GivingMachineDismissRequest(GivingMachineDismissMethod.VISIBLE_X),
            ),
        )

        assertEquals("Giving Machine", peekResponse.givingMachinePeekState?.label)
        assertNull(peekResponse.givingMachineDestinationState)

        assertNull(openResponse.failureResponse)
        assertEquals(GivingMachineSheetState.EXPANDED, openResponse.givingMachineDestinationState?.sheetState)
        assertEquals(GivingMachineVisibleContext.MACHINE_BROWSE, openResponse.givingMachineDestinationState?.visibleContext)
        assertTrue(openResponse.givingMachineDestinationState!!.dismissActions.contains(GivingMachineDismissAction.VISIBLE_X))

        assertNull(dismissResponse.failureResponse)
        assertTrue(dismissResponse.returnToHomeState?.homeSurfaceResumed == true)
        assertTrue(logger.entries.isNotEmpty())
    }

    @Test
    fun entryModuleFailsWhenExpandedStateCannotExposeVisibleDismissalAction() {
        val module = StubGivingMachineEntryPresenter(
            expandedDismissActions = setOf(GivingMachineDismissAction.SWIPE_DOWN),
        )

        val response = module.presentEntryState(
            GivingMachineEntryInput(
                homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
                machineEntryRequest = GivingMachineEntryRequest(GivingMachineEntryMethod.SWIPE_UP),
                machineDismissRequest = null,
            ),
        )

        assertEquals(
            GivingMachineEntryFailureReason.DISMISS_ACTION_UNAVAILABLE,
            response.failureResponse?.reason,
        )
        assertNull(response.givingMachineDestinationState)
    }

    @Test
    fun windowModuleAdvancesByOneVisibleRowWhilePreservingWindowStatesAndPeeks() {
        val module = StubGivingMachineWindowPresenter()
        val catalog = catalog(size = 21)

        val topResponse = module.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = catalog,
                currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                machineBrowseRequest = null,
            ),
        )
        val middleResponse = module.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = catalog,
                currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                machineBrowseRequest = MachineWindowBrowseRequest(
                    direction = MachineWindowBrowseDirection.NEXT,
                    step = MachineWindowBrowseStep.ONE_VISIBLE_ROW,
                ),
            ),
        )
        val lowerMiddleResponse = module.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = catalog,
                currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                machineBrowseRequest = MachineWindowBrowseRequest(
                    direction = MachineWindowBrowseDirection.NEXT,
                    step = MachineWindowBrowseStep.ONE_VISIBLE_ROW,
                ),
            ),
        )
        repeat(3) {
            module.presentMachineWindow(
                GivingMachineWindowInput(
                    givingMachineCatalog = catalog,
                    currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                    machineBrowseRequest = MachineWindowBrowseRequest(
                        direction = MachineWindowBrowseDirection.NEXT,
                        step = MachineWindowBrowseStep.ONE_VISIBLE_ROW,
                    ),
                ),
            )
        }
        val bottomResponse = module.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = catalog,
                currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                machineBrowseRequest = MachineWindowBrowseRequest(
                    direction = MachineWindowBrowseDirection.NEXT,
                    step = MachineWindowBrowseStep.ONE_VISIBLE_ROW,
                ),
            ),
        )

        assertEquals(9, topResponse.machineWindowState?.visibleSlotItems?.size)
        assertEquals(MachineWindowPositionState.TOP, topResponse.machineWindowState?.windowPositionState)
        assertEquals(MachineWindowPeekContinuationState.PEEK_BELOW_ONLY, topResponse.machineWindowState?.peekContinuationState)
        assertEquals("1", topResponse.machineWindowState?.visibleSlotItems?.first()?.slotNumber)
        assertEquals("9", topResponse.machineWindowState?.visibleSlotItems?.last()?.slotNumber)

        assertEquals(MachineWindowPositionState.MIDDLE, middleResponse.machineWindowState?.windowPositionState)
        assertEquals(MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW, middleResponse.machineWindowState?.peekContinuationState)
        assertEquals("4", middleResponse.machineWindowState?.visibleSlotItems?.first()?.slotNumber)
        assertEquals("12", middleResponse.machineWindowState?.visibleSlotItems?.last()?.slotNumber)

        assertEquals(MachineWindowPositionState.MIDDLE, lowerMiddleResponse.machineWindowState?.windowPositionState)
        assertEquals(MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW, lowerMiddleResponse.machineWindowState?.peekContinuationState)
        assertEquals("7", lowerMiddleResponse.machineWindowState?.visibleSlotItems?.first()?.slotNumber)
        assertEquals("15", lowerMiddleResponse.machineWindowState?.visibleSlotItems?.last()?.slotNumber)

        assertEquals(MachineWindowPositionState.BOTTOM, bottomResponse.machineWindowState?.windowPositionState)
        assertEquals(MachineWindowPeekContinuationState.PEEK_ABOVE_ONLY, bottomResponse.machineWindowState?.peekContinuationState)
        assertEquals(9, bottomResponse.machineWindowState?.visibleSlotItems?.size)
        assertEquals("13", bottomResponse.machineWindowState?.visibleSlotItems?.first()?.slotNumber)
        assertEquals("21", bottomResponse.machineWindowState?.visibleSlotItems?.last()?.slotNumber)
    }

    @Test
    fun windowModuleReturnsSingleWindowAndEmptyStateAndExplicitFailures() {
        val singleWindowModule = StubGivingMachineWindowPresenter()
        val singleResponse = singleWindowModule.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = catalog(size = 4),
                currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                machineBrowseRequest = null,
            ),
        )

        assertEquals(MachineWindowPositionState.SINGLE_WINDOW, singleResponse.machineWindowState?.windowPositionState)
        assertEquals(MachineWindowPeekContinuationState.NO_PEEK, singleResponse.machineWindowState?.peekContinuationState)
        assertEquals(4, singleResponse.machineWindowState?.visibleSlotItems?.size)

        val emptyResponse = singleWindowModule.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = emptyList(),
                currentCatalogState = GivingMachineCatalogPresentationState.EMPTY,
                machineBrowseRequest = null,
            ),
        )
        assertNull(emptyResponse.failureResponse)
        assertEquals("No Giving Machine items are available.", emptyResponse.emptyMachineState?.message)
        assertNotNull(emptyResponse.machineWindowState)
        assertTrue(emptyResponse.machineWindowState!!.visibleSlotItems.isEmpty())
        assertEquals(MachineWindowPositionState.SINGLE_WINDOW, emptyResponse.machineWindowState?.windowPositionState)
        assertEquals(MachineWindowPeekContinuationState.NO_PEEK, emptyResponse.machineWindowState?.peekContinuationState)

        val unavailableResponse = singleWindowModule.presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = null,
                currentCatalogState = GivingMachineCatalogPresentationState.UNAVAILABLE,
                machineBrowseRequest = null,
            ),
        )
        assertEquals(
            GivingMachineWindowFailureReason.GIVING_MACHINE_CATALOG_UNAVAILABLE,
            unavailableResponse.failureResponse?.reason,
        )

        val missingContentResponse = StubGivingMachineWindowPresenter().presentMachineWindow(
            GivingMachineWindowInput(
                givingMachineCatalog = listOf(
                    item(slotNumber = "1", title = "", description = "desc"),
                ),
                currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
                machineBrowseRequest = null,
            ),
        )
        assertEquals(
            GivingMachineWindowFailureReason.REQUIRED_SLOT_CONTENT_MISSING,
            missingContentResponse.failureResponse?.reason,
        )
    }

    @Test
    fun selectionModuleArmsVisibleSlotAndRequiresSeparateConfirmation() {
        val module = StubGivingMachineSelectionPresenter()

        val armResponse = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = machineWindowState(slotNumbers = listOf("1", "2", "3")),
                slotSelectionRequest = GivingMachineSlotSelectionRequest(slotNumber = "2"),
                addToCartConfirmationRequest = null,
                currentArmedSlotNumber = null,
                successfulAddToCartResult = null,
            ),
        )

        assertNull(armResponse.failureResponse)
        assertEquals("2", armResponse.slotSelectionState?.armedSlotNumber)
        assertEquals(
            listOf(GivingMachineSlotSelectionStateValue.ARMED),
            armResponse.slotSelectionState!!.visibleSlotItems
                .filter { it.slotNumber == "2" }
                .map { it.selectionState }
                .distinct(),
        )
        assertEquals(AddToCartConfirmationStateValue.REQUIRED, armResponse.addToCartConfirmationState?.confirmationState)

        val confirmResponse = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = machineWindowState(slotNumbers = listOf("1", "2", "3")),
                slotSelectionRequest = null,
                addToCartConfirmationRequest = GivingMachineAddToCartConfirmationRequest(confirmed = true),
                currentArmedSlotNumber = "2",
                successfulAddToCartResult = null,
            ),
        )

        assertNull(confirmResponse.failureResponse)
        assertEquals(AddToCartConfirmationStateValue.CONFIRMED, confirmResponse.addToCartConfirmationState?.confirmationState)
        assertEquals("2", confirmResponse.slotSelectionState?.armedSlotNumber)
    }

    @Test
    fun selectionModuleFailsForMissingVisibleTargetAndMissingArmedConfirmation() {
        val module = StubGivingMachineSelectionPresenter()
        val visibleWindow = machineWindowState(slotNumbers = listOf("1", "2", "3"))

        val missingSlotResponse = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = visibleWindow,
                slotSelectionRequest = GivingMachineSlotSelectionRequest(slotNumber = "9"),
                addToCartConfirmationRequest = null,
                currentArmedSlotNumber = "2",
                successfulAddToCartResult = null,
            ),
        )

        assertEquals(GivingMachineSelectionFailureReason.SLOT_NOT_VISIBLE, missingSlotResponse.failureResponse?.reason)
        assertEquals("2", missingSlotResponse.slotSelectionState?.armedSlotNumber)

        val missingArmedResponse = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = visibleWindow,
                slotSelectionRequest = null,
                addToCartConfirmationRequest = GivingMachineAddToCartConfirmationRequest(confirmed = true),
                currentArmedSlotNumber = null,
                successfulAddToCartResult = null,
            ),
        )

        assertEquals(GivingMachineSelectionFailureReason.NO_ARMED_SLOT, missingArmedResponse.failureResponse?.reason)
        assertEquals(AddToCartConfirmationStateValue.NOT_AVAILABLE, missingArmedResponse.addToCartConfirmationState?.confirmationState)
    }

    @Test
    fun selectionModulePresentsDispenseThenClearsArmedStateAfterCompletion() {
        val module = StubGivingMachineSelectionPresenter()
        val visibleWindow = machineWindowState(slotNumbers = listOf("1", "2", "3"))

        val presentingResponse = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = visibleWindow,
                slotSelectionRequest = null,
                addToCartConfirmationRequest = null,
                currentArmedSlotNumber = "2",
                successfulAddToCartResult = SuccessfulAddToCartPresentationResult(
                    itemIdentifier = "item-2",
                    slotNumber = "2",
                ),
            ),
        )

        assertNull(presentingResponse.failureResponse)
        assertEquals(
            DispenseAnimationPresentationState.PRESENTING,
            presentingResponse.dispenseAnimationState?.presentationState,
        )

        val completedResponse = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = visibleWindow,
                slotSelectionRequest = null,
                addToCartConfirmationRequest = null,
                currentArmedSlotNumber = null,
                successfulAddToCartResult = null,
            ),
        )

        assertNull(completedResponse.failureResponse)
        assertEquals(
            DispenseAnimationPresentationState.COMPLETED,
            completedResponse.dispenseAnimationState?.presentationState,
        )
        assertNull(completedResponse.slotSelectionState?.armedSlotNumber)
        assertTrue(
            completedResponse.slotSelectionState!!.visibleSlotItems.all {
                it.selectionState == GivingMachineSlotSelectionStateValue.UNSELECTED
            },
        )
    }

    @Test
    fun selectionModulePreservesSuccessWhenDispensePresentationIsUnavailable() {
        val module = StubGivingMachineSelectionPresenter(
            dispensePresentationAvailable = false,
        )

        val response = module.presentSelectionState(
            GivingMachineSelectionInput(
                machineWindowState = machineWindowState(slotNumbers = listOf("1", "2", "3")),
                slotSelectionRequest = null,
                addToCartConfirmationRequest = null,
                currentArmedSlotNumber = "3",
                successfulAddToCartResult = SuccessfulAddToCartPresentationResult(
                    itemIdentifier = "item-3",
                    slotNumber = "3",
                ),
            ),
        )

        assertEquals(
            GivingMachineSelectionFailureReason.DISPENSE_ANIMATION_UNAVAILABLE,
            response.failureResponse?.reason,
        )
        assertEquals(
            DispenseAnimationPresentationState.NON_ANIMATED_CONFIRMATION,
            response.dispenseAnimationState?.presentationState,
        )
        assertEquals(AddToCartConfirmationStateValue.CONFIRMED, response.addToCartConfirmationState?.confirmationState)
        assertNull(response.slotSelectionState?.armedSlotNumber)
    }

    @Test
    fun transitionModuleSupportsCartAndInfoTransitionsAndFailsForMissingInfoContent() {
        val module = StubGivingMachineTransitionPresenter()
        val destination = expandedMachineBrowseState()

        val cartResponse = module.presentTransitionState(
            GivingMachineTransitionInput(
                givingMachineDestinationState = destination,
                cartPresentationRequest = GivingMachineCartPresentationRequest(requested = true),
                returnFromCartOrCheckoutRequest = null,
                infoPresentationRequest = null,
                returnFromInfoRequest = null,
                infoPresentationContent = null,
            ),
        )
        assertNull(cartResponse.failureResponse)
        assertEquals(GivingMachineVisibleContext.CART_OR_CHECKOUT, cartResponse.givingMachineDestinationState?.visibleContext)
        assertEquals(
            CartOrCheckoutPresentationEmphasis.CLEAR_TASK_FOCUSED_REVIEW,
            cartResponse.cartOrCheckoutPresentationState?.emphasis,
        )

        val returnFromCartResponse = module.presentTransitionState(
            GivingMachineTransitionInput(
                givingMachineDestinationState = cartResponse.givingMachineDestinationState!!,
                cartPresentationRequest = null,
                returnFromCartOrCheckoutRequest = GivingMachineReturnFromCartOrCheckoutRequest(requested = true),
                infoPresentationRequest = null,
                returnFromInfoRequest = null,
                infoPresentationContent = null,
            ),
        )
        assertEquals(GivingMachineVisibleContext.MACHINE_BROWSE, returnFromCartResponse.givingMachineDestinationState?.visibleContext)

        val infoFailureResponse = module.presentTransitionState(
            GivingMachineTransitionInput(
                givingMachineDestinationState = destination,
                cartPresentationRequest = null,
                returnFromCartOrCheckoutRequest = null,
                infoPresentationRequest = GivingMachineInfoPresentationRequest(requested = true),
                returnFromInfoRequest = null,
                infoPresentationContent = null,
            ),
        )
        assertEquals(
            GivingMachineTransitionFailureReason.INFO_CONTENT_UNAVAILABLE,
            infoFailureResponse.failureResponse?.reason,
        )

        val infoResponse = module.presentTransitionState(
            GivingMachineTransitionInput(
                givingMachineDestinationState = destination,
                cartPresentationRequest = null,
                returnFromCartOrCheckoutRequest = null,
                infoPresentationRequest = GivingMachineInfoPresentationRequest(requested = true),
                returnFromInfoRequest = null,
                infoPresentationContent = InfoPresentationContent(
                    screenTitle = "Info",
                    contentSections = listOf(
                        InfoContentSection(
                            title = "Acknowledgements",
                            body = "Thanks to our partners.",
                        ),
                    ),
                ),
            ),
        )
        assertNull(infoResponse.failureResponse)
        assertEquals(GivingMachineVisibleContext.INFO, infoResponse.givingMachineDestinationState?.visibleContext)
        assertEquals(InfoInteractionState.INFORMATIONAL, infoResponse.infoPresentationState?.interactionState)
        assertFalse(infoResponse.infoPresentationState!!.contentSections.isEmpty())
    }

    @Test
    fun accessibilityModuleExposesContextAndActionsAndFailsWhenBrowseStateCannotBeLabeled() {
        val logger = InMemoryStubDecisionLogger()
        val module = StubGivingMachineAccessibilityPresenter(logger = logger)

        val successResponse = module.presentAccessibilityState(
            GivingMachineAccessibilityInput(
                givingMachinePeekState = GivingMachinePeekState(),
                givingMachineDestinationState = expandedMachineBrowseState(),
                machineWindowState = MachineWindowState(
                    visibleSlotItems = listOf(
                        item(slotNumber = "1"),
                        item(slotNumber = "2"),
                    ),
                    windowPositionState = MachineWindowPositionState.TOP,
                    peekContinuationState = MachineWindowPeekContinuationState.PEEK_BELOW_ONLY,
                ),
                slotSelectionState = GivingMachineSlotSelectionState(
                    armedSlotNumber = "2",
                    visibleSlotItems = listOf(
                        item(slotNumber = "1"),
                        item(slotNumber = "2", selectionState = GivingMachineSlotSelectionStateValue.ARMED),
                    ),
                ),
                addToCartConfirmationState = AddToCartConfirmationState(
                    armedSlotNumber = "2",
                    confirmationState = AddToCartConfirmationStateValue.REQUIRED,
                ),
                cartOrCheckoutPresentationState = null,
                infoPresentationState = null,
                accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                    action = GivingMachineAccessibilityAction.ARM_SLOT,
                    targetSlotNumber = "2",
                ),
            ),
        )

        assertNull(successResponse.failureResponse)
        assertEquals("Giving Machine", successResponse.accessibilityPresentationState?.entryLabel)
        assertEquals(GivingMachineVisibleContext.MACHINE_BROWSE, successResponse.accessibilityPresentationState?.visibleContext)
        assertEquals(listOf("1", "2"), successResponse.accessibilityPresentationState?.visibleSlotNumbers)
        assertTrue(
            successResponse.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.CONFIRM_ADD_TO_CART,
            ),
        )
        assertTrue(
            successResponse.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT,
            ),
        )
        assertTrue(logger.entries.isNotEmpty())

        val emptyBrowseResponse = module.presentAccessibilityState(
            GivingMachineAccessibilityInput(
                givingMachinePeekState = GivingMachinePeekState(),
                givingMachineDestinationState = expandedMachineBrowseState(),
                machineWindowState = MachineWindowState(
                    visibleSlotItems = emptyList(),
                    windowPositionState = MachineWindowPositionState.SINGLE_WINDOW,
                    peekContinuationState = MachineWindowPeekContinuationState.NO_PEEK,
                ),
                slotSelectionState = null,
                addToCartConfirmationState = null,
                cartOrCheckoutPresentationState = null,
                infoPresentationState = null,
                accessibilityActionRequest = null,
            ),
        )

        assertNull(emptyBrowseResponse.failureResponse)
        assertEquals(GivingMachineVisibleContext.MACHINE_BROWSE, emptyBrowseResponse.accessibilityPresentationState?.visibleContext)
        assertTrue(emptyBrowseResponse.accessibilityPresentationState!!.visibleSlotNumbers.isEmpty())
        assertTrue(
            emptyBrowseResponse.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT,
            ),
        )
        assertTrue(
            emptyBrowseResponse.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_INFO,
            ),
        )
        assertTrue(
            emptyBrowseResponse.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.DISMISS_MACHINE,
            ),
        )

        val failureResponse = module.presentAccessibilityState(
            GivingMachineAccessibilityInput(
                givingMachinePeekState = GivingMachinePeekState(),
                givingMachineDestinationState = expandedMachineBrowseState(),
                machineWindowState = null,
                slotSelectionState = null,
                addToCartConfirmationState = null,
                cartOrCheckoutPresentationState = null,
                infoPresentationState = null,
                accessibilityActionRequest = null,
            ),
        )

        assertEquals(
            GivingMachineAccessibilityFailureReason.ACCESSIBILITY_STATE_UNAVAILABLE,
            failureResponse.failureResponse?.reason,
        )
        assertNull(failureResponse.accessibilityPresentationState)
    }

    private fun catalog(
        size: Int,
    ): List<PresentedGivingMachineItem> = (1..size).map { index ->
        item(slotNumber = index.toString(), itemIdentifier = "item-$index")
    }

    private fun machineWindowState(
        slotNumbers: List<String>,
    ): MachineWindowState = MachineWindowState(
        visibleSlotItems = slotNumbers.map { slotNumber ->
            item(slotNumber = slotNumber, itemIdentifier = "item-$slotNumber")
        },
        windowPositionState = MachineWindowPositionState.TOP,
        peekContinuationState = MachineWindowPeekContinuationState.PEEK_BELOW_ONLY,
    )

    private fun expandedMachineBrowseState(): GivingMachineSurfaceState = GivingMachineSurfaceState(
        sheetState = GivingMachineSheetState.EXPANDED,
        visibleContext = GivingMachineVisibleContext.MACHINE_BROWSE,
        dismissActions = setOf(
            GivingMachineDismissAction.VISIBLE_X,
            GivingMachineDismissAction.SWIPE_DOWN,
        ),
    )

    private fun item(
        slotNumber: String,
        itemIdentifier: String = "item-$slotNumber",
        title: String = "Item $slotNumber",
        description: String = "Description $slotNumber",
        selectionState: GivingMachineSlotSelectionStateValue = GivingMachineSlotSelectionStateValue.UNSELECTED,
    ): PresentedGivingMachineItem = PresentedGivingMachineItem(
        itemIdentifier = itemIdentifier,
        slotNumber = slotNumber,
        title = title,
        description = description,
        selectionState = selectionState,
    )
}
