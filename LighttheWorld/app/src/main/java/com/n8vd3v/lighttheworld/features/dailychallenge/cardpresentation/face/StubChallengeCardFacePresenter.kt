package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardFaceState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionPhase
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardSequencePosition
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisibleFace
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

class StubChallengeCardFacePresenter(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : ChallengeCardFacePresenter {

    override fun resolveCardFace(
        input: ChallengeCardFaceInput,
    ): ChallengeCardFaceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_card_face_input",
            details = mapOf(
                "challengeCardCount" to input.challengeCardList?.size,
                "currentActiveCardIdentifier" to input.currentActiveCardIdentifier,
                "requestedFace" to input.cardFaceRequest?.requestedFace,
                "activeCardRequest" to input.activeCardRequest?.cardIdentifier,
                "backFaceReadingRequest" to input.backFaceReadingRequest?.cardIdentifier,
            ),
        )

        val cards = input.challengeCardList.orEmpty()
        val activeCard = cards.firstOrNull { it.cardIdentifier == input.currentActiveCardIdentifier }
            ?: return failureResponse(
                reason = ChallengeCardFaceFailureReason.NO_ACTIVE_CARD,
                message = "No active card is available for face presentation.",
                cards = cards,
            )

        val activeCardRequest = input.activeCardRequest?.cardIdentifier
        if (activeCardRequest != null && activeCardRequest != activeCard.cardIdentifier) {
            val requestedCard = cards.firstOrNull { it.cardIdentifier == activeCardRequest } ?: activeCard
            return successResponse(
                cards = cards,
                activeCard = requestedCard,
                visibleFace = ChallengeCardVisibleFace.FRONT,
                motionPhase = ChallengeCardMotionPhase.CENTERED,
                decision = "active_card_changed_previous_face_reset_to_front",
            )
        }

        if (!activeCard.hasRequiredContent()) {
                return failureResponse(
                    reason = ChallengeCardFaceFailureReason.REQUIRED_CARD_CONTENT_MISSING,
                    message = "Required card content is missing for face presentation.",
                    cards = cards,
                    preservedActiveCard = activeCard,
                )
        }

        val backFaceReadingRequest = input.backFaceReadingRequest?.cardIdentifier
        if (backFaceReadingRequest != null) {
            if (backFaceReadingRequest != activeCard.cardIdentifier || !activeCard.hasReadableBackFaceContent()) {
                return failureResponse(
                    reason = ChallengeCardFaceFailureReason.BACK_FACE_READING_UNAVAILABLE,
                    message = "Back-face reading is unavailable for the requested active card.",
                    cards = cards,
                    preservedActiveCard = activeCard,
                )
            }

            return successResponse(
                cards = cards,
                activeCard = activeCard,
                visibleFace = ChallengeCardVisibleFace.BACK,
                motionPhase = ChallengeCardMotionPhase.VERTICAL_READING,
                decision = "active_back_face_reading_preserved_in_place",
            )
        }

        val requestedFace = input.cardFaceRequest?.requestedFace ?: ChallengeCardVisibleFace.FRONT
        return successResponse(
            cards = cards,
            activeCard = activeCard,
            visibleFace = requestedFace,
            motionPhase = if (input.cardFaceRequest != null) {
                ChallengeCardMotionPhase.FACE_CHANGING
            } else {
                ChallengeCardMotionPhase.CENTERED
            },
            decision = if (requestedFace == ChallengeCardVisibleFace.BACK) {
                "active_card_flipped_to_back"
            } else {
                "active_card_front_face_presented"
            },
        )
    }

    private fun PresentedChallengeCard.hasRequiredContent(): Boolean =
        shortSummary.isNotBlank() &&
            detailDescription.isNotBlank() &&
            suggestions.isNotEmpty() &&
            suggestions.all { it.isNotBlank() }

    private fun PresentedChallengeCard.hasReadableBackFaceContent(): Boolean =
        detailDescription.isNotBlank() &&
            suggestions.isNotEmpty() &&
            suggestions.all { it.isNotBlank() }

    private fun successResponse(
        cards: List<PresentedChallengeCard>,
        activeCard: PresentedChallengeCard,
        visibleFace: ChallengeCardVisibleFace,
        motionPhase: ChallengeCardMotionPhase,
        decision: String,
    ): ChallengeCardFaceResponse {
        val response = ChallengeCardFaceResponse(
            cardFaceState = ChallengeCardFaceState(
                cardIdentifier = activeCard.cardIdentifier,
                visibleFace = visibleFace,
            ),
            motionState = ChallengeCardMotionState(motionPhase),
            activeCardState = activeCard.toActiveState(cards, visibleFace),
            failureResponse = null,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_card_face_output",
            details = mapOf(
                "decision" to decision,
                "activeCardIdentifier" to response.activeCardState?.cardIdentifier,
                "visibleFace" to response.cardFaceState?.visibleFace,
                "motionPhase" to response.motionState?.phase,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    private fun failureResponse(
        reason: ChallengeCardFaceFailureReason,
        message: String,
        cards: List<PresentedChallengeCard> = emptyList(),
        preservedActiveCard: PresentedChallengeCard? = null,
    ): ChallengeCardFaceResponse {
        val response = ChallengeCardFaceResponse(
            cardFaceState = preservedActiveCard?.let {
                ChallengeCardFaceState(
                    cardIdentifier = it.cardIdentifier,
                    visibleFace = ChallengeCardVisibleFace.FRONT,
                )
            },
            motionState = preservedActiveCard?.let {
                ChallengeCardMotionState(ChallengeCardMotionPhase.CENTERED)
            },
            activeCardState = preservedActiveCard?.toActiveState(
                cards = if (cards.isEmpty()) listOf(preservedActiveCard) else cards,
                visibleFace = ChallengeCardVisibleFace.FRONT,
            ),
            failureResponse = CopFailureResponse(
                reason = reason,
                message = message,
            ),
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_card_face_output",
            details = mapOf(
                "decision" to "card_face_resolution_failed",
                "activeCardIdentifier" to response.activeCardState?.cardIdentifier,
                "visibleFace" to response.cardFaceState?.visibleFace,
                "motionPhase" to response.motionState?.phase,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    private fun PresentedChallengeCard.toActiveState(
        cards: List<PresentedChallengeCard>,
        visibleFace: ChallengeCardVisibleFace,
    ): ChallengeCardActiveState {
        val index = cards.indexOfFirst { it.cardIdentifier == cardIdentifier }.let { resolvedIndex ->
            if (resolvedIndex >= 0) resolvedIndex else 0
        }
        return ChallengeCardActiveState(
            cardIdentifier = cardIdentifier,
            challengeDate = challengeDate,
            sequencePosition = ChallengeCardSequencePosition(
                oneBasedIndex = index + 1,
                totalCards = cards.size.coerceAtLeast(1),
            ),
            visibleFace = visibleFace,
        )
    }

    companion object {
        private const val MODULE_NAME = "StubChallengeCardFacePresenter"
    }
}
