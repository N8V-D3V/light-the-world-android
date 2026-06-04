package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityAction
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityPresentationContext
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBrowseDirection
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardFaceState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardSequencePosition
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisibleFace
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

class StubChallengeCardAccessibilityPresenter(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : ChallengeCardAccessibilityPresenter {

    override fun resolveAccessibilityPresentation(
        input: ChallengeCardAccessibilityInput,
    ): ChallengeCardAccessibilityResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_accessibility_presentation_input",
            details = mapOf(
                "challengeCardCount" to input.challengeCardList?.size,
                "currentActiveCardIdentifier" to input.currentActiveCardIdentifier,
                "cardFaceStateIdentifier" to input.cardFaceState?.cardIdentifier,
                "cardFaceStateFace" to input.cardFaceState?.visibleFace,
                "accessibilityBrowseDirection" to input.accessibilityBrowseRequest?.direction,
                "accessibilityFaceRequest" to input.accessibilityFaceRequest?.requestedFace,
                "backFaceReadingRequest" to input.backFaceReadingRequest?.cardIdentifier,
            ),
        )

        val cards = input.challengeCardList
            ?: return failureResponse(
                reason = ChallengeCardAccessibilityFailureReason.ACCESSIBILITY_STATE_UNAVAILABLE,
                message = "Accessibility presentation state is unavailable because the challenge card list is missing.",
            )

        val activeCardIdentifier = input.currentActiveCardIdentifier
            ?: return failureResponse(
                reason = ChallengeCardAccessibilityFailureReason.NO_ACTIVE_CARD,
                message = "No active card is available for accessibility presentation.",
            )

        val activeCard = cards.firstOrNull { it.cardIdentifier == activeCardIdentifier }
            ?: return failureResponse(
                reason = ChallengeCardAccessibilityFailureReason.ACTIVE_CARD_NOT_FOUND,
                message = "The active card is not present in the current challenge card list.",
            )

        val currentFaceState = input.cardFaceState
        if (currentFaceState == null || currentFaceState.cardIdentifier != activeCardIdentifier) {
            return failureResponse(
                reason = ChallengeCardAccessibilityFailureReason.ACCESSIBILITY_STATE_UNAVAILABLE,
                message = "Accessibility presentation state could not expose the active card face.",
            )
        }

        if (input.backFaceReadingRequest != null) {
            if (input.backFaceReadingRequest.cardIdentifier != activeCardIdentifier ||
                currentFaceState.visibleFace != ChallengeCardVisibleFace.BACK ||
                !activeCard.hasReadableBackFaceContent()
            ) {
                return failureResponse(
                    reason = ChallengeCardAccessibilityFailureReason.BACK_FACE_READING_UNAVAILABLE,
                    message = "Accessible back-face reading is unavailable for the requested active card.",
                )
            }

            return successResponse(
                cards = cards,
                activeCard = activeCard,
                visibleFace = ChallengeCardVisibleFace.BACK,
                activeCardChanged = false,
                decision = "accessible_back_face_reading_preserved",
            )
        }

        if (input.accessibilityBrowseRequest != null) {
            val nextActiveCard = moveOneCard(
                cards = cards,
                currentActiveCard = activeCard,
                direction = input.accessibilityBrowseRequest.direction,
            )
            return successResponse(
                cards = cards,
                activeCard = nextActiveCard,
                visibleFace = ChallengeCardVisibleFace.FRONT,
                activeCardChanged = true,
                decision = "accessible_browse_resolved_card_by_card",
            )
        }

        if (input.accessibilityFaceRequest != null) {
            return successResponse(
                cards = cards,
                activeCard = activeCard,
                visibleFace = input.accessibilityFaceRequest.requestedFace,
                activeCardChanged = false,
                decision = "accessible_face_change_resolved",
            )
        }

        return successResponse(
            cards = cards,
            activeCard = activeCard,
            visibleFace = currentFaceState.visibleFace,
            activeCardChanged = false,
            decision = "accessible_presentation_exposed",
        )
    }

    private fun moveOneCard(
        cards: List<PresentedChallengeCard>,
        currentActiveCard: PresentedChallengeCard,
        direction: ChallengeCardBrowseDirection,
    ): PresentedChallengeCard {
        val currentIndex = cards.indexOfFirst { it.cardIdentifier == currentActiveCard.cardIdentifier }
        if (currentIndex < 0) {
            return currentActiveCard
        }

        val targetIndex = when (direction) {
            ChallengeCardBrowseDirection.PREVIOUS -> (currentIndex - 1).coerceAtLeast(0)
            ChallengeCardBrowseDirection.NEXT -> (currentIndex + 1).coerceAtMost(cards.lastIndex)
        }
        return cards[targetIndex]
    }

    private fun PresentedChallengeCard.hasReadableBackFaceContent(): Boolean =
        detailDescription.isNotBlank() &&
            suggestions.isNotEmpty() &&
            suggestions.all { it.isNotBlank() }

    private fun successResponse(
        cards: List<PresentedChallengeCard>,
        activeCard: PresentedChallengeCard,
        visibleFace: ChallengeCardVisibleFace,
        activeCardChanged: Boolean,
        decision: String,
    ): ChallengeCardAccessibilityResponse {
        val activeCardState = ChallengeCardActiveState(
            cardIdentifier = activeCard.cardIdentifier,
            challengeDate = activeCard.challengeDate,
            sequencePosition = ChallengeCardSequencePosition(
                oneBasedIndex = cards.indexOfFirst { it.cardIdentifier == activeCard.cardIdentifier } + 1,
                totalCards = cards.size,
            ),
            visibleFace = visibleFace,
        )
        val cardFaceState = ChallengeCardFaceState(
            cardIdentifier = activeCard.cardIdentifier,
            visibleFace = visibleFace,
        )
        val accessibilityPresentationState = ChallengeCardAccessibilityPresentationContext(
            activeCardIdentifier = activeCard.cardIdentifier,
            activeCardDate = activeCard.challengeDate,
            activeCardPosition = activeCardState.sequencePosition,
            activeCardFace = visibleFace,
            availableActions = availableActionsFor(
                cards = cards,
                activeCard = activeCard,
                visibleFace = visibleFace,
            ),
        )
        val response = ChallengeCardAccessibilityResponse(
            accessibilityPresentationState = accessibilityPresentationState,
            activeCardState = if (activeCardChanged) activeCardState else activeCardState,
            cardFaceState = cardFaceState,
            failureResponse = null,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_accessibility_presentation_output",
            details = mapOf(
                "decision" to decision,
                "activeCardIdentifier" to response.accessibilityPresentationState?.activeCardIdentifier,
                "visibleFace" to response.cardFaceState?.visibleFace,
                "availableActions" to response.accessibilityPresentationState?.availableActions,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    private fun availableActionsFor(
        cards: List<PresentedChallengeCard>,
        activeCard: PresentedChallengeCard,
        visibleFace: ChallengeCardVisibleFace,
    ): Set<ChallengeCardAccessibilityAction> {
        val index = cards.indexOfFirst { it.cardIdentifier == activeCard.cardIdentifier }
        val availableActions = linkedSetOf<ChallengeCardAccessibilityAction>()
        if (index > 0) {
            availableActions += ChallengeCardAccessibilityAction.BROWSE_PREVIOUS
        }
        if (index < cards.lastIndex) {
            availableActions += ChallengeCardAccessibilityAction.BROWSE_NEXT
        }
        if (visibleFace == ChallengeCardVisibleFace.FRONT) {
            availableActions += ChallengeCardAccessibilityAction.SHOW_BACK
        } else {
            availableActions += ChallengeCardAccessibilityAction.SHOW_FRONT
        }
        return availableActions
    }

    private fun failureResponse(
        reason: ChallengeCardAccessibilityFailureReason,
        message: String,
    ): ChallengeCardAccessibilityResponse {
        val response = ChallengeCardAccessibilityResponse(
            accessibilityPresentationState = null,
            activeCardState = null,
            cardFaceState = null,
            failureResponse = CopFailureResponse(
                reason = reason,
                message = message,
            ),
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_accessibility_presentation_output",
            details = mapOf(
                "decision" to "accessibility_presentation_failed",
                "activeCardIdentifier" to null,
                "visibleFace" to null,
                "availableActions" to null,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    companion object {
        private const val MODULE_NAME = "StubChallengeCardAccessibilityPresenter"
    }
}
