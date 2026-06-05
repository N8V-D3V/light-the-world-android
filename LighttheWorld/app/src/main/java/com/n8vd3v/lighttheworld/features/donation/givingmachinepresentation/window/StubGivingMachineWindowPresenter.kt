package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.window

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.EmptyMachineState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineCatalogPresentationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.GivingMachineSlotSelectionStateValue
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowBrowseDirection
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowBrowseStep
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowPeekContinuationState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowPositionState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.MachineWindowState
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.PresentedGivingMachineItem

class StubGivingMachineWindowPresenter(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : GivingMachineWindowPresenter {

    private var currentWindowStartIndex: Int = 0

    override fun presentMachineWindow(
        input: GivingMachineWindowInput,
    ): GivingMachineWindowResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_machine_window_input",
            details = mapOf(
                "catalogSize" to input.givingMachineCatalog?.size,
                "catalogState" to input.currentCatalogState,
                "browseDirection" to input.machineBrowseRequest?.direction,
                "browseStep" to input.machineBrowseRequest?.step,
                "currentWindowStartIndex" to currentWindowStartIndex,
            ),
        )

        val catalog = input.givingMachineCatalog
        val response = when {
            input.currentCatalogState == GivingMachineCatalogPresentationState.UNAVAILABLE || catalog == null ->
                failureResponse(
                    reason = GivingMachineWindowFailureReason.GIVING_MACHINE_CATALOG_UNAVAILABLE,
                    message = "Giving Machine catalog presentation is unavailable.",
                )

            input.currentCatalogState == GivingMachineCatalogPresentationState.EMPTY || catalog.isEmpty() -> {
                currentWindowStartIndex = 0
                GivingMachineWindowResponse(
                    machineWindowState = null,
                    emptyMachineState = EmptyMachineState(
                        message = "No Giving Machine items are available.",
                    ),
                    failureResponse = null,
                )
            }

            else -> {
                val lastWindowStartIndex = lastWindowStartIndex(catalog.size)
                currentWindowStartIndex = currentWindowStartIndex.coerceIn(0, lastWindowStartIndex)
                val rowStepSize = rowStepSizeFor(input.machineBrowseRequest?.step)
                currentWindowStartIndex = when (input.machineBrowseRequest?.direction) {
                    MachineWindowBrowseDirection.PREVIOUS -> (currentWindowStartIndex - rowStepSize).coerceAtLeast(0)
                    MachineWindowBrowseDirection.NEXT -> (currentWindowStartIndex + rowStepSize).coerceAtMost(lastWindowStartIndex)
                    null -> currentWindowStartIndex
                }

                val visibleItems = catalog.drop(currentWindowStartIndex).take(WINDOW_CAPACITY)
                val invalidVisibleItem = visibleItems.firstOrNull { !it.hasRequiredSlotContent() }
                if (invalidVisibleItem != null) {
                    failureResponse(
                        reason = GivingMachineWindowFailureReason.REQUIRED_SLOT_CONTENT_MISSING,
                        message = "Required slot content is missing for a visible Giving Machine item.",
                    )
                } else {
                    val normalizedVisibleItems = visibleItems.mapIndexed { index, item ->
                        item.copy(
                            slotNumber = (currentWindowStartIndex + index + 1).toString(),
                            selectionState = item.selectionState.takeIf {
                                it == GivingMachineSlotSelectionStateValue.ARMED
                            } ?: GivingMachineSlotSelectionStateValue.UNSELECTED,
                        )
                    }
                    GivingMachineWindowResponse(
                        machineWindowState = MachineWindowState(
                            visibleSlotItems = normalizedVisibleItems,
                            windowPositionState = windowPositionStateFor(
                                catalogSize = catalog.size,
                                startIndex = currentWindowStartIndex,
                            ),
                            peekContinuationState = peekContinuationStateFor(
                                catalogSize = catalog.size,
                                startIndex = currentWindowStartIndex,
                            ),
                        ),
                        emptyMachineState = null,
                        failureResponse = null,
                    )
                }
            }
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "present_machine_window_output",
            details = mapOf(
                "decision" to when {
                    response.failureResponse != null -> "machine_window_failed"
                    response.emptyMachineState != null -> "empty_machine_state_presented"
                    else -> "machine_window_presented"
                },
                "visibleSlotCount" to response.machineWindowState?.visibleSlotItems?.size,
                "windowPositionState" to response.machineWindowState?.windowPositionState,
                "peekContinuationState" to response.machineWindowState?.peekContinuationState,
                "failureReason" to response.failureResponse?.reason,
            ),
        )

        return response
    }

    private fun windowPositionStateFor(
        catalogSize: Int,
        startIndex: Int,
    ): MachineWindowPositionState {
        if (catalogSize <= WINDOW_CAPACITY) {
            return MachineWindowPositionState.SINGLE_WINDOW
        }
        val lastWindowStartIndex = lastWindowStartIndex(catalogSize)
        return when {
            startIndex == 0 -> MachineWindowPositionState.TOP
            startIndex >= lastWindowStartIndex -> MachineWindowPositionState.BOTTOM
            else -> MachineWindowPositionState.MIDDLE
        }
    }

    private fun peekContinuationStateFor(
        catalogSize: Int,
        startIndex: Int,
    ): MachineWindowPeekContinuationState {
        if (catalogSize <= WINDOW_CAPACITY) {
            return MachineWindowPeekContinuationState.NO_PEEK
        }
        val lastWindowStartIndex = lastWindowStartIndex(catalogSize)
        return when {
            startIndex == 0 -> MachineWindowPeekContinuationState.PEEK_BELOW_ONLY
            startIndex >= lastWindowStartIndex -> MachineWindowPeekContinuationState.PEEK_ABOVE_ONLY
            else -> MachineWindowPeekContinuationState.PEEK_ABOVE_AND_BELOW
        }
    }

    private fun lastWindowStartIndex(
        catalogSize: Int,
    ): Int = ((catalogSize - 1) / WINDOW_CAPACITY) * WINDOW_CAPACITY

    private fun rowStepSizeFor(
        step: MachineWindowBrowseStep?,
    ): Int = when (step ?: MachineWindowBrowseStep.ONE_VISIBLE_ROW) {
        MachineWindowBrowseStep.ONE_VISIBLE_ROW -> WINDOW_ROW_SIZE
    }

    private fun PresentedGivingMachineItem.hasRequiredSlotContent(): Boolean =
        itemIdentifier.isNotBlank() &&
            title.isNotBlank() &&
            description.isNotBlank()

    private fun failureResponse(
        reason: GivingMachineWindowFailureReason,
        message: String,
    ): GivingMachineWindowResponse = GivingMachineWindowResponse(
        machineWindowState = null,
        emptyMachineState = null,
        failureResponse = CopFailureResponse(
            reason = reason,
            message = message,
        ),
    )

    companion object {
        private const val MODULE_NAME = "StubGivingMachineWindowPresenter"
        private const val WINDOW_CAPACITY = 9
        private const val WINDOW_ROW_SIZE = 3
    }
}
