package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LTWGivingMachineUiControllerTest {

    @Test
    fun controllerStartsFromValidatedHomePeekState() {
        val controller = LTWGivingMachineUiController()

        assertEquals("Giving Machine", controller.uiState.presentationState.givingMachinePeekState?.label)
        assertNull(controller.uiState.presentationState.givingMachineDestinationState)
        assertTrue(
            controller.uiState.presentationState.accessibilityPresentationState
                ?.availableActions
                ?.contains(GivingMachineAccessibilityAction.OPEN_MACHINE) == true,
        )
    }

    @Test
    fun controllerPreservesExplicitAddHandoffAndSuccessfulAddReentry() {
        val controller = LTWGivingMachineUiController()

        controller.openMachine()
        val visibleSlotNumber = requireNotNull(
            controller.uiState.presentationState.machineWindowState?.visibleSlotItems?.get(1)?.slotNumber,
        )
        controller.armVisibleSlot(visibleSlotNumber)

        assertEquals(visibleSlotNumber, controller.uiState.presentationState.slotSelectionState?.armedSlotNumber)
        assertEquals(
            AddToCartConfirmationStateValue.REQUIRED,
            controller.uiState.presentationState.addToCartConfirmationState?.confirmationState,
        )

        controller.confirmAddToCart()
        val handoff = controller.uiState.pendingConfirmedAddHandoff

        assertNotNull(handoff)
        assertTrue(controller.uiState.sessionGifts.isEmpty())

        controller.acceptSuccessfulAddResult(requireNotNull(handoff))

        assertNull(controller.uiState.pendingConfirmedAddHandoff)
        assertEquals(
            DispenseAnimationPresentationState.PRESENTING,
            controller.uiState.presentationState.dispenseAnimationState?.presentationState,
        )
        assertEquals(1, controller.uiState.sessionGifts.size)
        assertEquals(visibleSlotNumber, controller.uiState.sessionGifts.single().slotNumber)

        controller.continuePresentation()

        assertEquals(
            DispenseAnimationPresentationState.COMPLETED,
            controller.uiState.presentationState.dispenseAnimationState?.presentationState,
        )
    }

    @Test
    fun controllerSupportsDetailDrivenAddFlowUsingSamePresentationHandoff() {
        val controller = LTWGivingMachineUiController()

        controller.openMachine()
        val visibleSlotNumber = requireNotNull(
            controller.uiState.presentationState.machineWindowState?.visibleSlotItems?.first()?.slotNumber,
        )

        controller.confirmGiftFromDetail(visibleSlotNumber)

        val handoff = controller.uiState.pendingConfirmedAddHandoff
        assertNotNull(handoff)
        assertEquals(visibleSlotNumber, handoff?.slotNumber)
        assertEquals(
            AddToCartConfirmationStateValue.CONFIRMED,
            controller.uiState.presentationState.addToCartConfirmationState?.confirmationState,
        )

        controller.acceptSuccessfulAddResult(requireNotNull(handoff))

        assertEquals(
            DispenseAnimationPresentationState.PRESENTING,
            controller.uiState.presentationState.dispenseAnimationState?.presentationState,
        )
    }

    @Test
    fun controllerRoutesCartAndInfoTransitionsThroughPresentationSlice() {
        val controller = LTWGivingMachineUiController()

        controller.openMachine()
        controller.openCartOrCheckout()

        assertEquals(
            GivingMachineVisibleContext.CART_OR_CHECKOUT,
            controller.uiState.presentationState.givingMachineDestinationState?.visibleContext,
        )

        controller.returnFromCartOrCheckout()

        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            controller.uiState.presentationState.givingMachineDestinationState?.visibleContext,
        )

        controller.openInfo()

        assertEquals(
            GivingMachineVisibleContext.INFO,
            controller.uiState.presentationState.givingMachineDestinationState?.visibleContext,
        )

        controller.returnFromInfo()

        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            controller.uiState.presentationState.givingMachineDestinationState?.visibleContext,
        )
    }

    @Test
    fun controllerKeepsExpandedEmptyMachineStateAccessible() {
        val controller = LTWGivingMachineUiController(
            environment = GivingMachinePresentationEnvironment(
                homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
                catalogContext = GivingMachinePresentationCatalogContext(
                    givingMachineCatalog = emptyList(),
                    currentCatalogState = GivingMachineCatalogPresentationState.EMPTY,
                ),
                infoPresentationContent = LTWGivingMachineSeedContent.defaultEnvironment().infoPresentationContent,
            ),
        )

        controller.openMachine()

        assertEquals(
            GivingMachineVisibleContext.MACHINE_BROWSE,
            controller.uiState.presentationState.givingMachineDestinationState?.visibleContext,
        )
        assertNotNull(controller.uiState.presentationState.emptyMachineState)
        assertTrue(
            controller.uiState.presentationState.accessibilityPresentationState?.visibleSlotNumbers?.isEmpty() == true,
        )
        assertTrue(
            controller.uiState.presentationState.accessibilityPresentationState
                ?.availableActions
                ?.contains(GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT) == true,
        )
        assertTrue(
            controller.uiState.presentationState.accessibilityPresentationState
                ?.availableActions
                ?.contains(GivingMachineAccessibilityAction.OPEN_INFO) == true,
        )
    }
}
