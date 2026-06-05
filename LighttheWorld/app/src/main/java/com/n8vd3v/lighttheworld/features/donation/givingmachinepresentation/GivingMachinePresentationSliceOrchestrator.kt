package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.GivingMachineAccessibilityInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.accessibility.GivingMachineAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.GivingMachineEntryInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.entry.GivingMachineEntryPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionPresenter
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowInput
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowPresenter

class GivingMachinePresentationSliceOrchestrator(
    private val entryPresenter: GivingMachineEntryPresenter,
    private val windowPresenter: GivingMachineWindowPresenter,
    private val selectionPresenter: GivingMachineSelectionPresenter,
    private val transitionPresenter: GivingMachineTransitionPresenter,
    private val accessibilityPresenter: GivingMachineAccessibilityPresenter,
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) {

    fun presentHomePeek(
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "present_home_peek",
        currentState = null,
        environment = environment,
    )

    fun openMachine(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        entryRequest: GivingMachineEntryRequest = GivingMachineEntryRequest(
            method = GivingMachineEntryMethod.TAP,
        ),
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "open_machine",
        currentState = currentState,
        environment = environment,
        entryRequest = entryRequest,
    )

    fun dismissMachine(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        dismissRequest: GivingMachineDismissRequest,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "dismiss_machine",
        currentState = currentState,
        environment = environment,
        dismissRequest = dismissRequest,
    )

    fun browseMachineWindow(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        browseRequest: MachineWindowBrowseRequest,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "browse_machine_window",
        currentState = currentState,
        environment = environment,
        browseRequest = browseRequest,
    )

    fun armVisibleSlot(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        slotSelectionRequest: GivingMachineSlotSelectionRequest,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "arm_visible_slot",
        currentState = currentState,
        environment = environment,
        slotSelectionRequest = slotSelectionRequest,
    )

    fun confirmAddToCart(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "confirm_add_to_cart",
        currentState = currentState,
        environment = environment,
        addToCartConfirmationRequest = GivingMachineAddToCartConfirmationRequest(
            confirmed = true,
        ),
    )

    fun presentSuccessfulAddResult(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        successfulAddToCartResult: SuccessfulAddToCartPresentationResult,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "present_successful_add_result",
        currentState = currentState,
        environment = environment,
        successfulAddToCartResult = successfulAddToCartResult,
    )

    fun continuePresentation(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "continue_presentation",
        currentState = currentState,
        environment = environment,
    )

    fun openCartOrCheckout(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "open_cart_or_checkout",
        currentState = currentState,
        environment = environment,
        cartPresentationRequest = GivingMachineCartPresentationRequest(
            requested = true,
        ),
    )

    fun returnFromCartOrCheckout(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "return_from_cart_or_checkout",
        currentState = currentState,
        environment = environment,
        returnFromCartOrCheckoutRequest = GivingMachineReturnFromCartOrCheckoutRequest(
            requested = true,
        ),
    )

    fun openInfo(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "open_info",
        currentState = currentState,
        environment = environment,
        infoPresentationRequest = GivingMachineInfoPresentationRequest(
            requested = true,
        ),
    )

    fun returnFromInfo(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
    ): GivingMachinePresentationSliceResponse = orchestrate(
        action = "return_from_info",
        currentState = currentState,
        environment = environment,
        returnFromInfoRequest = GivingMachineReturnFromInfoRequest(
            requested = true,
        ),
    )

    fun performAccessibilityAction(
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        accessibilityActionRequest: GivingMachineAccessibilityActionRequest,
    ): GivingMachinePresentationSliceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "perform_accessibility_action_input",
            details = mapOf(
                "action" to accessibilityActionRequest.action,
                "targetSlotNumber" to accessibilityActionRequest.targetSlotNumber,
            ),
        )

        val baselineState = currentState ?: presentHomePeek(environment).presentationState
        val precheck = presentAccessibilityState(
            state = baselineState,
            accessibilityActionRequest = accessibilityActionRequest,
        )
        if (precheck.accessibilityFailureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "perform_accessibility_action_output",
                details = mapOf(
                    "decision" to "accessibility_action_rejected_accessibility_failure",
                    "failureReason" to precheck.accessibilityFailureResponse.reason,
                ),
            )
            return precheck
        }

        val response = when (accessibilityActionRequest.action) {
            GivingMachineAccessibilityAction.OPEN_MACHINE -> openMachine(
                currentState = baselineState,
                environment = environment,
                entryRequest = GivingMachineEntryRequest(
                    method = GivingMachineEntryMethod.TAP,
                ),
            )

            GivingMachineAccessibilityAction.BROWSE_PREVIOUS -> browseMachineWindow(
                currentState = baselineState,
                environment = environment,
                browseRequest = MachineWindowBrowseRequest(
                    direction = MachineWindowBrowseDirection.PREVIOUS,
                ),
            )

            GivingMachineAccessibilityAction.BROWSE_NEXT -> browseMachineWindow(
                currentState = baselineState,
                environment = environment,
                browseRequest = MachineWindowBrowseRequest(
                    direction = MachineWindowBrowseDirection.NEXT,
                ),
            )

            GivingMachineAccessibilityAction.ARM_SLOT -> armVisibleSlot(
                currentState = baselineState,
                environment = environment,
                slotSelectionRequest = GivingMachineSlotSelectionRequest(
                    slotNumber = accessibilityActionRequest.targetSlotNumber.orEmpty(),
                ),
            )

            GivingMachineAccessibilityAction.CONFIRM_ADD_TO_CART -> confirmAddToCart(
                currentState = baselineState,
                environment = environment,
            )

            GivingMachineAccessibilityAction.OPEN_CART_OR_CHECKOUT -> openCartOrCheckout(
                currentState = baselineState,
                environment = environment,
            )

            GivingMachineAccessibilityAction.OPEN_INFO -> openInfo(
                currentState = baselineState,
                environment = environment,
            )

            GivingMachineAccessibilityAction.DISMISS_MACHINE -> dismissMachine(
                currentState = baselineState,
                environment = environment,
                dismissRequest = dismissRequestFor(baselineState),
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "perform_accessibility_action_output",
            details = mapOf(
                "decision" to if (response.accessibilityFailureResponse == null) {
                    "accessibility_action_completed"
                } else {
                    "accessibility_action_returned_accessibility_failure"
                },
                "handoffCreated" to (response.confirmedAddHandoff != null),
                "visibleContext" to response.presentationState.givingMachineDestinationState?.visibleContext,
                "failureReason" to response.accessibilityFailureResponse?.reason,
            ),
        )
        return response
    }

    private fun orchestrate(
        action: String,
        currentState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        entryRequest: GivingMachineEntryRequest? = null,
        dismissRequest: GivingMachineDismissRequest? = null,
        browseRequest: MachineWindowBrowseRequest? = null,
        slotSelectionRequest: GivingMachineSlotSelectionRequest? = null,
        addToCartConfirmationRequest: GivingMachineAddToCartConfirmationRequest? = null,
        successfulAddToCartResult: SuccessfulAddToCartPresentationResult? = null,
        cartPresentationRequest: GivingMachineCartPresentationRequest? = null,
        returnFromCartOrCheckoutRequest: GivingMachineReturnFromCartOrCheckoutRequest? = null,
        infoPresentationRequest: GivingMachineInfoPresentationRequest? = null,
        returnFromInfoRequest: GivingMachineReturnFromInfoRequest? = null,
    ): GivingMachinePresentationSliceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "${action}_input",
            details = mapOf(
                "homeVisible" to environment.homeChallengeSurfaceState.isVisible,
                "catalogState" to environment.catalogContext.currentCatalogState,
                "entryMethod" to entryRequest?.method,
                "dismissMethod" to dismissRequest?.method,
                "browseDirection" to browseRequest?.direction,
                "requestedSlotNumber" to slotSelectionRequest?.slotNumber,
                "confirmAddRequested" to addToCartConfirmationRequest?.confirmed,
                "successfulAddSlotNumber" to successfulAddToCartResult?.slotNumber,
                "cartRequested" to cartPresentationRequest?.requested,
                "returnFromCartRequested" to returnFromCartOrCheckoutRequest?.requested,
                "infoRequested" to infoPresentationRequest?.requested,
                "returnFromInfoRequested" to returnFromInfoRequest?.requested,
            ),
        )

        val entryResponse = entryPresenter.presentEntryState(
            GivingMachineEntryInput(
                homeChallengeSurfaceState = environment.homeChallengeSurfaceState,
                machineEntryRequest = entryRequest,
                machineDismissRequest = dismissRequest,
            ),
        )

        val baseDestinationState = when {
            entryResponse.failureResponse != null -> currentState?.givingMachineDestinationState
            dismissRequest != null -> null
            entryResponse.givingMachineDestinationState != null -> entryResponse.givingMachineDestinationState
            else -> currentState?.givingMachineDestinationState
        }

        val transitionResponse =
            if (baseDestinationState != null && hasTransitionRequest(
                    cartPresentationRequest = cartPresentationRequest,
                    returnFromCartOrCheckoutRequest = returnFromCartOrCheckoutRequest,
                    infoPresentationRequest = infoPresentationRequest,
                    returnFromInfoRequest = returnFromInfoRequest,
                )
            ) {
                transitionPresenter.presentTransitionState(
                    GivingMachineTransitionInput(
                        givingMachineDestinationState = baseDestinationState,
                        cartPresentationRequest = cartPresentationRequest,
                        returnFromCartOrCheckoutRequest = returnFromCartOrCheckoutRequest,
                        infoPresentationRequest = infoPresentationRequest,
                        returnFromInfoRequest = returnFromInfoRequest,
                        infoPresentationContent = environment.infoPresentationContent,
                    ),
                )
            } else {
                null
            }

        val finalDestinationState = transitionResponse?.givingMachineDestinationState ?: baseDestinationState
        val browseVisible = finalDestinationState?.visibleContext == GivingMachineVisibleContext.MACHINE_BROWSE
        val destinationClosed = finalDestinationState == null

        val windowResponse =
            if (browseVisible) {
                windowPresenter.presentMachineWindow(
                    GivingMachineWindowInput(
                        givingMachineCatalog = environment.catalogContext.givingMachineCatalog,
                        currentCatalogState = environment.catalogContext.currentCatalogState,
                        machineBrowseRequest = browseRequest,
                    ),
                )
            } else {
                null
            }

        val selectionWindowState =
            if (browseVisible) {
                windowResponse?.machineWindowState
            } else {
                null
            }
        val shouldPresentSelection = browseVisible ||
            slotSelectionRequest != null ||
            addToCartConfirmationRequest != null ||
            successfulAddToCartResult != null ||
            currentState?.dispenseAnimationState?.presentationState == DispenseAnimationPresentationState.PRESENTING

        val selectionResponse =
            if (!destinationClosed && shouldPresentSelection) {
                selectionPresenter.presentSelectionState(
                    GivingMachineSelectionInput(
                        machineWindowState = selectionWindowState,
                        slotSelectionRequest = slotSelectionRequest,
                        addToCartConfirmationRequest = addToCartConfirmationRequest,
                        currentArmedSlotNumber = currentArmedSlotNumber(
                            state = currentState,
                            browseVisible = browseVisible,
                        ),
                        successfulAddToCartResult = successfulAddToCartResult,
                    ),
                )
            } else {
                null
            }

        val presentationState = buildPresentationState(
            previousState = currentState,
            environment = environment,
            entryPeekState = entryResponse.givingMachinePeekState,
            finalDestinationState = finalDestinationState,
            returnToHomeState = entryResponse.returnToHomeState,
            transitionResponse = transitionResponse,
            windowResponse = windowResponse,
            selectionResponse = selectionResponse,
        )
        val accessibilityResponse = presentAccessibilityState(
            state = presentationState,
            accessibilityActionRequest = null,
        )
        val finalState = presentationState.copy(
            accessibilityPresentationState = accessibilityResponse.presentationState.accessibilityPresentationState,
        )
        val confirmedAddHandoff = confirmedAddHandoff(
            addToCartConfirmationRequest = addToCartConfirmationRequest,
            machineWindowState = selectionWindowState,
            selectionResponse = selectionResponse,
        )

        val response = GivingMachinePresentationSliceResponse(
            presentationState = finalState,
            confirmedAddHandoff = confirmedAddHandoff,
            entryFailureResponse = entryResponse.failureResponse,
            windowFailureResponse = windowResponse?.failureResponse,
            selectionFailureResponse = selectionResponse?.failureResponse,
            transitionFailureResponse = transitionResponse?.failureResponse,
            accessibilityFailureResponse = accessibilityResponse.accessibilityFailureResponse,
        )

        logger.logDecision(
            module = MODULE_NAME,
            action = "${action}_output",
            details = mapOf(
                "decision" to when {
                    response.confirmedAddHandoff != null -> "presentation_handoff_created"
                    response.entryFailureResponse != null -> "entry_failure_returned"
                    response.windowFailureResponse != null -> "window_failure_returned"
                    response.selectionFailureResponse != null -> "selection_failure_returned"
                    response.transitionFailureResponse != null -> "transition_failure_returned"
                    response.accessibilityFailureResponse != null -> "accessibility_failure_returned"
                    else -> "presentation_state_updated"
                },
                "sheetState" to response.presentationState.givingMachineDestinationState?.sheetState,
                "visibleContext" to response.presentationState.givingMachineDestinationState?.visibleContext,
                "peekVisible" to (response.presentationState.givingMachinePeekState != null),
                "windowVisibleSlots" to response.presentationState.machineWindowState?.visibleSlotItems?.size,
                "armedSlotNumber" to response.presentationState.slotSelectionState?.armedSlotNumber,
                "confirmationState" to response.presentationState.addToCartConfirmationState?.confirmationState,
                "dispenseState" to response.presentationState.dispenseAnimationState?.presentationState,
                "handoffSlotNumber" to response.confirmedAddHandoff?.slotNumber,
            ),
        )
        return response
    }

    private fun buildPresentationState(
        previousState: GivingMachinePresentationSliceState?,
        environment: GivingMachinePresentationEnvironment,
        entryPeekState: GivingMachinePeekState?,
        finalDestinationState: GivingMachineSurfaceState?,
        returnToHomeState: GivingMachineReturnToHomeState?,
        transitionResponse: com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.transition.GivingMachineTransitionResponse?,
        windowResponse: com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window.GivingMachineWindowResponse?,
        selectionResponse: com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionResponse?,
    ): GivingMachinePresentationSliceState {
        if (finalDestinationState == null) {
            return GivingMachinePresentationSliceState(
                homeChallengeSurfaceState = environment.homeChallengeSurfaceState,
                givingMachinePeekState = entryPeekState,
                givingMachineDestinationState = null,
                returnToHomeState = returnToHomeState,
                accessibilityPresentationState = null,
            )
        }

        val browseVisible = finalDestinationState.visibleContext == GivingMachineVisibleContext.MACHINE_BROWSE
        val machineWindowState = when {
            browseVisible -> windowResponse?.machineWindowState
            else -> previousState?.machineWindowState
        }
        val emptyMachineState = when {
            browseVisible -> windowResponse?.emptyMachineState
            else -> previousState?.emptyMachineState
        }
        val slotSelectionState = when {
            selectionResponse != null -> selectionResponse.slotSelectionState
            browseVisible -> null
            else -> null
        }
        val addToCartConfirmationState = when {
            selectionResponse != null -> selectionResponse.addToCartConfirmationState
            browseVisible -> null
            else -> null
        }
        val dispenseAnimationState = when {
            selectionResponse != null -> selectionResponse.dispenseAnimationState
            browseVisible -> null
            else -> previousState?.dispenseAnimationState
        }
        val cartOrCheckoutPresentationState = when (finalDestinationState.visibleContext) {
            GivingMachineVisibleContext.CART_OR_CHECKOUT ->
                transitionResponse?.cartOrCheckoutPresentationState ?: previousState?.cartOrCheckoutPresentationState

            else -> null
        }
        val infoPresentationState = when (finalDestinationState.visibleContext) {
            GivingMachineVisibleContext.INFO ->
                transitionResponse?.infoPresentationState ?: previousState?.infoPresentationState

            else -> null
        }

        return GivingMachinePresentationSliceState(
            homeChallengeSurfaceState = environment.homeChallengeSurfaceState,
            givingMachinePeekState = entryPeekState,
            givingMachineDestinationState = finalDestinationState,
            returnToHomeState = returnToHomeState,
            machineWindowState = machineWindowState,
            emptyMachineState = emptyMachineState,
            slotSelectionState = slotSelectionState,
            addToCartConfirmationState = addToCartConfirmationState,
            dispenseAnimationState = dispenseAnimationState,
            cartOrCheckoutPresentationState = cartOrCheckoutPresentationState,
            infoPresentationState = infoPresentationState,
            accessibilityPresentationState = null,
        )
    }

    private fun presentAccessibilityState(
        state: GivingMachinePresentationSliceState,
        accessibilityActionRequest: GivingMachineAccessibilityActionRequest?,
    ): GivingMachinePresentationSliceResponse {
        val accessibilityResponse = accessibilityPresenter.presentAccessibilityState(
            GivingMachineAccessibilityInput(
                givingMachinePeekState = state.givingMachinePeekState,
                givingMachineDestinationState = state.givingMachineDestinationState,
                machineWindowState = state.machineWindowState,
                slotSelectionState = state.slotSelectionState,
                addToCartConfirmationState = state.addToCartConfirmationState,
                cartOrCheckoutPresentationState = state.cartOrCheckoutPresentationState,
                infoPresentationState = state.infoPresentationState,
                accessibilityActionRequest = accessibilityActionRequest,
            ),
        )
        return GivingMachinePresentationSliceResponse(
            presentationState = state.copy(
                accessibilityPresentationState = accessibilityResponse.accessibilityPresentationState,
            ),
            accessibilityFailureResponse = accessibilityResponse.failureResponse,
        )
    }

    private fun confirmedAddHandoff(
        addToCartConfirmationRequest: GivingMachineAddToCartConfirmationRequest?,
        machineWindowState: MachineWindowState?,
        selectionResponse: com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.selection.GivingMachineSelectionResponse?,
    ): GivingMachineConfirmedAddHandoff? {
        if (addToCartConfirmationRequest?.confirmed != true) {
            return null
        }
        if (selectionResponse?.addToCartConfirmationState?.confirmationState !=
            AddToCartConfirmationStateValue.CONFIRMED
        ) {
            return null
        }

        val armedSlotNumber = selectionResponse.addToCartConfirmationState.armedSlotNumber ?: return null
        val armedItem = machineWindowState
            ?.visibleSlotItems
            ?.firstOrNull { it.slotNumber == armedSlotNumber }
            ?: return null

        return GivingMachineConfirmedAddHandoff(
            itemIdentifier = armedItem.itemIdentifier,
            slotNumber = armedSlotNumber,
        )
    }

    private fun dismissRequestFor(
        state: GivingMachinePresentationSliceState,
    ): GivingMachineDismissRequest = when {
        state.givingMachineDestinationState?.dismissActions?.contains(
            GivingMachineDismissAction.VISIBLE_X,
        ) == true -> GivingMachineDismissRequest(
            method = GivingMachineDismissMethod.VISIBLE_X,
        )

        else -> GivingMachineDismissRequest(
            method = GivingMachineDismissMethod.SWIPE_DOWN,
        )
    }

    private fun currentArmedSlotNumber(
        state: GivingMachinePresentationSliceState?,
        browseVisible: Boolean,
    ): String? = if (browseVisible) {
        state?.slotSelectionState?.armedSlotNumber
        ?: state?.addToCartConfirmationState?.armedSlotNumber
    } else {
        null
    }

    private fun hasTransitionRequest(
        cartPresentationRequest: GivingMachineCartPresentationRequest?,
        returnFromCartOrCheckoutRequest: GivingMachineReturnFromCartOrCheckoutRequest?,
        infoPresentationRequest: GivingMachineInfoPresentationRequest?,
        returnFromInfoRequest: GivingMachineReturnFromInfoRequest?,
    ): Boolean = cartPresentationRequest?.requested == true ||
        returnFromCartOrCheckoutRequest?.requested == true ||
        infoPresentationRequest?.requested == true ||
        returnFromInfoRequest?.requested == true

    companion object {
        private const val MODULE_NAME = "GivingMachinePresentationSliceOrchestrator"
    }
}
