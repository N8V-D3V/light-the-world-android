package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.GivingMachineAccessibilityFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.StubGivingMachineAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.GivingMachineEntryFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.StubGivingMachineEntryPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.StubGivingMachineSelectionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.StubGivingMachineTransitionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowFailureReason
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.StubGivingMachineWindowPresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GivingMachinePresentationSliceOrchestratorTest {

    @Test
    fun homePeekPresentationExposesPersistentPeekAndAccessibilityOpenAction() {
        val orchestrator = orchestrator()

        val response = orchestrator.presentHomePeek(
            environment = environment(catalog = catalog(size = 12)),
        )

        assertNull(response.entryFailureResponse)
        assertEquals("Giving Machine", response.presentationState.givingMachinePeekState?.label)
        assertNull(response.presentationState.givingMachineDestinationState)
        assertEquals(
            GivingMachineSheetState.PEEK,
            response.presentationState.accessibilityPresentationState?.sheetState,
        )
        assertTrue(
            response.presentationState.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_MACHINE,
            ),
        )
    }

    @Test
    fun openAndDismissFlowPreservesPeekAndReturnsToHome() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 12))

        val homeResponse = orchestrator.presentHomePeek(environment)
        val openResponse = orchestrator.openMachine(
            currentState = homeResponse.presentationState,
            environment = environment,
            entryRequest = GivingMachineEntryRequest(
                method = GivingMachineEntryMethod.SWIPE_UP,
            ),
        )

        assertNull(openResponse.entryFailureResponse)
        assertEquals(
            GivingMachineSheetState.EXPANDED,
            openResponse.presentationState.givingMachineDestinationState?.sheetState,
        )
        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            openResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertEquals(9, openResponse.presentationState.machineWindowState?.visibleSlotItems?.size)

        val dismissResponse = orchestrator.dismissMachine(
            currentState = openResponse.presentationState,
            environment = environment,
            dismissRequest = GivingMachineDismissRequest(
                method = GivingMachineDismissMethod.VISIBLE_X,
            ),
        )

        assertNull(dismissResponse.entryFailureResponse)
        assertNull(dismissResponse.presentationState.givingMachineDestinationState)
        assertTrue(dismissResponse.presentationState.returnToHomeState?.homeSurfaceResumed == true)
        assertEquals("Giving Machine", dismissResponse.presentationState.givingMachinePeekState?.label)
        assertTrue(
            dismissResponse.presentationState.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_MACHINE,
            ),
        )
    }

    @Test
    fun browseFlowMovesByOneVisibleRow() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 21))
        val openResponse = orchestrator.openMachine(
            currentState = orchestrator.presentHomePeek(environment).presentationState,
            environment = environment,
        )

        assertEquals("1", openResponse.presentationState.machineWindowState?.visibleSlotItems?.first()?.slotNumber)
        assertEquals("9", openResponse.presentationState.machineWindowState?.visibleSlotItems?.last()?.slotNumber)
        assertEquals(
            MachineWindowPeekContinuationState.PEEK_BELOW_ONLY,
            openResponse.presentationState.machineWindowState?.peekContinuationState,
        )

        val browseResponse = orchestrator.browseMachineWindow(
            currentState = openResponse.presentationState,
            environment = environment,
            browseRequest = MachineWindowBrowseRequest(
                direction = MachineWindowBrowseDirection.NEXT,
            ),
        )

        assertNull(browseResponse.windowFailureResponse)
        assertEquals("4", browseResponse.presentationState.machineWindowState?.visibleSlotItems?.first()?.slotNumber)
        assertEquals("12", browseResponse.presentationState.machineWindowState?.visibleSlotItems?.last()?.slotNumber)
        assertEquals(
            MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW,
            browseResponse.presentationState.machineWindowState?.peekContinuationState,
        )
    }

    @Test
    fun emptyCatalogOpenMachinePreservesExpandedAccessibleEmptyBrowseState() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = emptyList())

        val response = orchestrator.openMachine(
            currentState = orchestrator.presentHomePeek(environment).presentationState,
            environment = environment,
        )

        assertNull(response.windowFailureResponse)
        assertNull(response.accessibilityFailureResponse)
        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            response.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertEquals("No Giving Machine items are available.", response.presentationState.emptyMachineState?.message)
        assertNotNull(response.presentationState.machineWindowState)
        assertTrue(response.presentationState.machineWindowState!!.visibleSlotItems.isEmpty())
        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            response.presentationState.accessibilityPresentationState?.visibleContext,
        )
        assertTrue(response.presentationState.accessibilityPresentationState!!.visibleSlotNumbers.isEmpty())
        assertTrue(
            response.presentationState.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT,
            ),
        )
        assertTrue(
            response.presentationState.accessibilityPresentationState!!.availableActions.contains(
                GivingMachineAccessibilityAction.OPEN_INFO,
            ),
        )
    }

    @Test
    fun selectionRequiresExplicitConfirmationAndProducesConfirmedAddHandoff() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 12))
        val openResponse = orchestrator.openMachine(
            currentState = orchestrator.presentHomePeek(environment).presentationState,
            environment = environment,
        )

        val armResponse = orchestrator.armVisibleSlot(
            currentState = openResponse.presentationState,
            environment = environment,
            slotSelectionRequest = GivingMachineSlotSelectionRequest(
                slotNumber = "2",
            ),
        )

        assertNull(armResponse.selectionFailureResponse)
        assertEquals("2", armResponse.presentationState.slotSelectionState?.armedSlotNumber)
        assertEquals(
            AddToCartConfirmationStateValue.REQUIRED,
            armResponse.presentationState.addToCartConfirmationState?.confirmationState,
        )
        assertNull(armResponse.confirmedAddHandoff)

        val confirmResponse = orchestrator.confirmAddToCart(
            currentState = armResponse.presentationState,
            environment = environment,
        )

        assertNull(confirmResponse.selectionFailureResponse)
        assertEquals(
            AddToCartConfirmationStateValue.CONFIRMED,
            confirmResponse.presentationState.addToCartConfirmationState?.confirmationState,
        )
        assertEquals("2", confirmResponse.confirmedAddHandoff?.slotNumber)
        assertEquals("item-2", confirmResponse.confirmedAddHandoff?.itemIdentifier)
    }

    @Test
    fun successfulAddResultReentersForDispensePresentationAndCompletion() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 12))
        val confirmResponse = confirmedAddResponse(
            orchestrator = orchestrator,
            environment = environment,
        )
        val confirmedHandoff = requireNotNull(confirmResponse.confirmedAddHandoff)

        val successfulAddResponse = orchestrator.presentSuccessfulAddResult(
            currentState = confirmResponse.presentationState,
            environment = environment,
            successfulAddToCartResult = SuccessfulAddToCartPresentationResult(
                itemIdentifier = confirmedHandoff.itemIdentifier,
                slotNumber = confirmedHandoff.slotNumber,
            ),
        )

        assertNull(successfulAddResponse.confirmedAddHandoff)
        assertEquals(
            DispenseAnimationPresentationState.PRESENTING,
            successfulAddResponse.presentationState.dispenseAnimationState?.presentationState,
        )
        assertNull(successfulAddResponse.presentationState.slotSelectionState?.armedSlotNumber)

        val completedResponse = orchestrator.continuePresentation(
            currentState = successfulAddResponse.presentationState,
            environment = environment,
        )

        assertEquals(
            DispenseAnimationPresentationState.COMPLETED,
            completedResponse.presentationState.dispenseAnimationState?.presentationState,
        )
        assertNull(completedResponse.presentationState.slotSelectionState?.armedSlotNumber)
    }

    @Test
    fun cartTransitionReturnsToMachineBrowse() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 12))
        val armedBrowseState = armedBrowseState(
            orchestrator = orchestrator,
            environment = environment,
        )

        val cartResponse = orchestrator.openCartOrCheckout(
            currentState = armedBrowseState,
            environment = environment,
        )

        assertNull(cartResponse.transitionFailureResponse)
        assertEquals(
            GivingMachineVisibleContext.CART_OR_CHECKOUT,
            cartResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertNotNull(cartResponse.presentationState.cartOrCheckoutPresentationState)
        assertNull(cartResponse.presentationState.slotSelectionState)
        assertNull(cartResponse.presentationState.addToCartConfirmationState)

        val returnResponse = orchestrator.returnFromCartOrCheckout(
            currentState = cartResponse.presentationState,
            environment = environment,
        )

        assertNull(returnResponse.transitionFailureResponse)
        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            returnResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertNotNull(returnResponse.presentationState.machineWindowState)
    }

    @Test
    fun infoTransitionReturnsToMachineBrowse() {
        val orchestrator = orchestrator()
        val environment = environment(
            catalog = catalog(size = 12),
            infoPresentationContent = InfoPresentationContent(
                screenTitle = "About Giving Machine",
                contentSections = listOf(
                    InfoContentSection(
                        title = "Acknowledgements",
                        body = "Thanks to our partners.",
                    ),
                ),
            ),
        )
        val armedBrowseState = armedBrowseState(
            orchestrator = orchestrator,
            environment = environment,
        )

        val infoResponse = orchestrator.openInfo(
            currentState = armedBrowseState,
            environment = environment,
        )

        assertNull(infoResponse.transitionFailureResponse)
        assertEquals(
            GivingMachineVisibleContext.INFO,
            infoResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertEquals("About Giving Machine", infoResponse.presentationState.infoPresentationState?.screenTitle)
        assertNull(infoResponse.presentationState.slotSelectionState)
        assertNull(infoResponse.presentationState.addToCartConfirmationState)

        val returnResponse = orchestrator.returnFromInfo(
            currentState = infoResponse.presentationState,
            environment = environment,
        )

        assertNull(returnResponse.transitionFailureResponse)
        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            returnResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertNotNull(returnResponse.presentationState.machineWindowState)
    }

    @Test
    fun accessibilityDrivenOpenBrowseArmConfirmAndDismissUsesSameBoundaries() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 21))

        val homeResponse = orchestrator.presentHomePeek(environment)
        val openResponse = orchestrator.performAccessibilityAction(
            currentState = homeResponse.presentationState,
            environment = environment,
            accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                action = GivingMachineAccessibilityAction.OPEN_MACHINE,
            ),
        )

        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            openResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )

        val browseResponse = orchestrator.performAccessibilityAction(
            currentState = openResponse.presentationState,
            environment = environment,
            accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                action = GivingMachineAccessibilityAction.BROWSE_NEXT,
            ),
        )

        assertEquals("4", browseResponse.presentationState.machineWindowState?.visibleSlotItems?.first()?.slotNumber)

        val armResponse = orchestrator.performAccessibilityAction(
            currentState = browseResponse.presentationState,
            environment = environment,
            accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                action = GivingMachineAccessibilityAction.ARM_SLOT,
                targetSlotNumber = "4",
            ),
        )

        assertEquals("4", armResponse.presentationState.slotSelectionState?.armedSlotNumber)

        val confirmResponse = orchestrator.performAccessibilityAction(
            currentState = armResponse.presentationState,
            environment = environment,
            accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                action = GivingMachineAccessibilityAction.CONFIRM_ADD_TO_CART,
            ),
        )

        assertEquals("4", confirmResponse.confirmedAddHandoff?.slotNumber)

        val dismissResponse = orchestrator.performAccessibilityAction(
            currentState = confirmResponse.presentationState,
            environment = environment,
            accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                action = GivingMachineAccessibilityAction.DISMISS_MACHINE,
            ),
        )

        assertNull(dismissResponse.presentationState.givingMachineDestinationState)
        assertTrue(dismissResponse.presentationState.returnToHomeState?.homeSurfaceResumed == true)
    }

    @Test
    fun confirmAddToCartFromInfoDoesNotProduceHandoff() {
        val orchestrator = orchestrator()
        val environment = environment(
            catalog = catalog(size = 12),
            infoPresentationContent = InfoPresentationContent(
                screenTitle = "About Giving Machine",
                contentSections = listOf(
                    InfoContentSection(
                        title = "Acknowledgements",
                        body = "Thanks to our partners.",
                    ),
                ),
            ),
        )
        val infoResponse = orchestrator.openInfo(
            currentState = armedBrowseState(
                orchestrator = orchestrator,
                environment = environment,
            ),
            environment = environment,
        )

        val confirmResponse = orchestrator.confirmAddToCart(
            currentState = infoResponse.presentationState,
            environment = environment,
        )

        assertEquals(
            GivingMachineVisibleContext.INFO,
            confirmResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertNull(confirmResponse.confirmedAddHandoff)
        assertEquals(
            GivingMachineSelectionFailureReason.NO_ARMED_SLOT,
            confirmResponse.selectionFailureResponse?.reason,
        )
    }

    @Test
    fun confirmAddToCartFromCartOrCheckoutDoesNotProduceHandoff() {
        val orchestrator = orchestrator()
        val environment = environment(catalog = catalog(size = 12))
        val cartResponse = orchestrator.openCartOrCheckout(
            currentState = armedBrowseState(
                orchestrator = orchestrator,
                environment = environment,
            ),
            environment = environment,
        )

        val confirmResponse = orchestrator.confirmAddToCart(
            currentState = cartResponse.presentationState,
            environment = environment,
        )

        assertEquals(
            GivingMachineVisibleContext.CART_OR_CHECKOUT,
            confirmResponse.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertNull(confirmResponse.confirmedAddHandoff)
        assertEquals(
            GivingMachineSelectionFailureReason.NO_ARMED_SLOT,
            confirmResponse.selectionFailureResponse?.reason,
        )
    }

    @Test
    fun orchestratorPropagatesModuleFailures() {
        val entryFailureOrchestrator = orchestrator(
            entryPresenter = StubGivingMachineEntryPresenter(
                expandedDismissActions = setOf(GivingMachineDismissAction.SWIPE_DOWN),
            ),
        )
        val entryFailureResponse = entryFailureOrchestrator.openMachine(
            currentState = null,
            environment = environment(catalog = catalog(size = 12)),
            entryRequest = GivingMachineEntryRequest(
                method = GivingMachineEntryMethod.TAP,
            ),
        )
        assertEquals(
            GivingMachineEntryFailureReason.DISMISS_ACTION_UNAVAILABLE,
            entryFailureResponse.entryFailureResponse?.reason,
        )

        val windowFailureResponse = orchestrator().openMachine(
            currentState = null,
            environment = environment(
                catalog = listOf(
                    item(slotNumber = "1", title = "", description = "Description 1"),
                ),
            ),
        )
        assertEquals(
            GivingMachineWindowFailureReason.REQUIRED_SLOT_CONTENT_MISSING,
            windowFailureResponse.windowFailureResponse?.reason,
        )

        val selectionFailureResponse = orchestrator().confirmAddToCart(
            currentState = orchestrator().openMachine(
                currentState = null,
                environment = environment(catalog = catalog(size = 12)),
            ).presentationState,
            environment = environment(catalog = catalog(size = 12)),
        )
        assertEquals(
            GivingMachineSelectionFailureReason.NO_ARMED_SLOT,
            selectionFailureResponse.selectionFailureResponse?.reason,
        )

        val transitionFailureResponse = orchestrator().openInfo(
            currentState = orchestrator().openMachine(
                currentState = null,
                environment = environment(catalog = catalog(size = 12)),
            ).presentationState,
            environment = environment(
                catalog = catalog(size = 12),
                infoPresentationContent = null,
            ),
        )
        assertEquals(
            GivingMachineTransitionFailureReason.INFO_CONTENT_UNAVAILABLE,
            transitionFailureResponse.transitionFailureResponse?.reason,
        )

        val accessibilityFailureResponse = orchestrator().performAccessibilityAction(
            currentState = GivingMachinePresentationSliceState(
                homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
                givingMachinePeekState = GivingMachinePeekState(),
                givingMachineDestinationState = GivingMachineSurfaceState(
                    sheetState = GivingMachineSheetState.EXPANDED,
                    visibleContext = GivingMachineVisibleContext.MACHINE_BROWSE,
                    dismissActions = setOf(
                        GivingMachineDismissAction.VISIBLE_X,
                    ),
                ),
            ),
            environment = environment(catalog = catalog(size = 12)),
            accessibilityActionRequest = GivingMachineAccessibilityActionRequest(
                action = GivingMachineAccessibilityAction.OPEN_INFO,
            ),
        )
        assertEquals(
            GivingMachineAccessibilityFailureReason.ACCESSIBILITY_STATE_UNAVAILABLE,
            accessibilityFailureResponse.accessibilityFailureResponse?.reason,
        )
    }

    private fun orchestrator(
        entryPresenter: StubGivingMachineEntryPresenter = StubGivingMachineEntryPresenter(),
        windowPresenter: StubGivingMachineWindowPresenter = StubGivingMachineWindowPresenter(),
        selectionPresenter: StubGivingMachineSelectionPresenter = StubGivingMachineSelectionPresenter(),
        transitionPresenter: StubGivingMachineTransitionPresenter = StubGivingMachineTransitionPresenter(),
        accessibilityPresenter: StubGivingMachineAccessibilityPresenter = StubGivingMachineAccessibilityPresenter(),
    ): GivingMachinePresentationSliceOrchestrator = GivingMachinePresentationSliceOrchestrator(
        entryPresenter = entryPresenter,
        windowPresenter = windowPresenter,
        selectionPresenter = selectionPresenter,
        transitionPresenter = transitionPresenter,
        accessibilityPresenter = accessibilityPresenter,
    )

    private fun confirmedAddResponse(
        orchestrator: GivingMachinePresentationSliceOrchestrator,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse {
        val openResponse = orchestrator.openMachine(
            currentState = orchestrator.presentHomePeek(environment).presentationState,
            environment = environment,
        )
        val armResponse = orchestrator.armVisibleSlot(
            currentState = openResponse.presentationState,
            environment = environment,
            slotSelectionRequest = GivingMachineSlotSelectionRequest(
                slotNumber = "2",
            ),
        )
        return orchestrator.confirmAddToCart(
            currentState = armResponse.presentationState,
            environment = environment,
        )
    }

    private fun armedBrowseState(
        orchestrator: GivingMachinePresentationSliceOrchestrator,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceState {
        val openResponse = orchestrator.openMachine(
            currentState = orchestrator.presentHomePeek(environment).presentationState,
            environment = environment,
        )
        return orchestrator.armVisibleSlot(
            currentState = openResponse.presentationState,
            environment = environment,
            slotSelectionRequest = GivingMachineSlotSelectionRequest(
                slotNumber = "2",
            ),
        ).presentationState
    }

    private fun environment(
        catalog: List<PresentedGivingMachineItem>,
        homeVisible: Boolean = true,
        infoPresentationContent: InfoPresentationContent? = null,
    ): GivingMachinePresentationEnvironment = GivingMachinePresentationEnvironment(
        homeChallengeSurfaceState = GivingMachineHomeSurfaceState(
            isVisible = homeVisible,
        ),
        catalogContext = GivingMachinePresentationCatalogContext(
            givingMachineCatalog = catalog,
            currentCatalogState = when {
                catalog.isEmpty() -> GivingMachineCatalogPresentationState.EMPTY
                else -> GivingMachineCatalogPresentationState.AVAILABLE
            },
        ),
        infoPresentationContent = infoPresentationContent,
    )

    private fun catalog(
        size: Int,
    ): List<PresentedGivingMachineItem> = (1..size).map { index ->
        item(
            slotNumber = index.toString(),
            itemIdentifier = "item-$index",
        )
    }

    private fun item(
        slotNumber: String,
        itemIdentifier: String = "item-$slotNumber",
        title: String = "Item $slotNumber",
        description: String = "Description $slotNumber",
    ): PresentedGivingMachineItem = PresentedGivingMachineItem(
        itemIdentifier = itemIdentifier,
        slotNumber = slotNumber,
        title = title,
        description = description,
        selectionState = GivingMachineSlotSelectionStateValue.UNSELECTED,
    )
}
