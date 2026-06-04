package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAdjacentPreviewState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBrowsePhase
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardEmphasisState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionPhase
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardPresentationState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardSequencePosition
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardSnapState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisibleFace
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisualEmphasisState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.EmptyCardRailState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

class ChallengeCardRailPresentationModule(
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) : ChallengeCardRailProtocol {

    override fun presentCardRail(
        input: ChallengeCardRailInput,
    ): ChallengeCardRailResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_card_rail_input",
            details = mapOf(
                "challengeCardCount" to input.challengeCardList?.size,
                "cardPresentationStateCount" to input.cardPresentationStates.size,
                "currentActiveCardIdentifier" to input.currentActiveCardIdentifier,
                "browseDirection" to input.browseRequest?.direction,
                "browsePhase" to input.browseRequest?.phase,
            ),
        )

        val cards = input.challengeCardList
            ?: return failureResponse(
                reason = ChallengeCardRailFailureReason.CHALLENGE_CARD_LIST_UNAVAILABLE,
                message = "Challenge card list is unavailable.",
            )

        if (cards.any { !it.hasRequiredContent() }) {
            return failureResponse(
                reason = ChallengeCardRailFailureReason.REQUIRED_CARD_CONTENT_MISSING,
                message = "Required challenge card content is missing for rail presentation.",
            )
        }

        if (cards.isEmpty()) {
            val response = ChallengeCardRailResponse(
                challengeCardRail = emptyList(),
                emptyCardRailState = EmptyCardRailState(message = "No challenges are available."),
                activeCardState = null,
                adjacentCardPreviewState = null,
                cardVisualEmphasisState = ChallengeCardVisualEmphasisState(emptyList()),
                motionState = null,
                failureResponse = null,
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "present_card_rail_output",
                details = mapOf(
                    "decision" to "empty_state_presented",
                    "activeCardIdentifier" to null,
                    "failureReason" to null,
                ),
            )
            return response
        }

        val activeCard = resolveActiveCard(cards, input)
        val motionPhase = motionPhaseFor(input.browseRequest?.phase)
        val activeCardState = activeCard?.toActiveState(cards, input.cardPresentationStates)
        val adjacentPreviewState = activeCard?.toAdjacentPreviewState(cards)
        val visualEmphasisState = ChallengeCardVisualEmphasisState(
            cardStates = cards.map { card ->
                val activeIdentifier = activeCard?.cardIdentifier
                val adjacentIdentifiers = activeCard?.toAdjacentIdentifiers(cards).orEmpty()
                ChallengeCardPresentationState(
                    cardIdentifier = card.cardIdentifier,
                    emphasisState = when {
                        activeIdentifier == null -> ChallengeCardEmphasisState.OFFSCREEN
                        card.cardIdentifier == activeIdentifier -> ChallengeCardEmphasisState.ACTIVE
                        adjacentIdentifiers.contains(card.cardIdentifier) -> ChallengeCardEmphasisState.ADJACENT
                        else -> ChallengeCardEmphasisState.OFFSCREEN
                    },
                    visibleFace = if (card.cardIdentifier == activeIdentifier) {
                        input.cardPresentationStates
                            .firstOrNull { it.cardIdentifier == card.cardIdentifier }
                            ?.visibleFace
                            ?: ChallengeCardVisibleFace.FRONT
                    } else {
                        ChallengeCardVisibleFace.FRONT
                    },
                    snapState = snapStateFor(motionPhase),
                )
            },
        )
        val response = ChallengeCardRailResponse(
            challengeCardRail = cards,
            emptyCardRailState = null,
            activeCardState = activeCardState,
            adjacentCardPreviewState = adjacentPreviewState,
            cardVisualEmphasisState = visualEmphasisState,
            motionState = motionPhase?.let(::ChallengeCardMotionState),
            failureResponse = null,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_card_rail_output",
            details = mapOf(
                "decision" to "rail_presented",
                "activeCardIdentifier" to response.activeCardState?.cardIdentifier,
                "motionPhase" to response.motionState?.phase,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    private fun resolveActiveCard(
        cards: List<PresentedChallengeCard>,
        input: ChallengeCardRailInput,
    ): PresentedChallengeCard? {
        val currentActiveCard = cards.firstOrNull { it.cardIdentifier == input.currentActiveCardIdentifier }
        if (currentActiveCard != null) {
            return currentActiveCard
        }

        val activeFromPresentationState = input.cardPresentationStates
            .firstOrNull { it.emphasisState == ChallengeCardEmphasisState.ACTIVE }
            ?.cardIdentifier
        if (activeFromPresentationState != null) {
            return cards.firstOrNull { it.cardIdentifier == activeFromPresentationState }
        }

        return cards.singleOrNull()
    }

    private fun PresentedChallengeCard.toActiveState(
        cards: List<PresentedChallengeCard>,
        cardPresentationStates: List<ChallengeCardPresentationState>,
    ): ChallengeCardActiveState {
        val index = cards.indexOfFirst { it.cardIdentifier == cardIdentifier }
        val visibleFace = cardPresentationStates.firstOrNull { it.cardIdentifier == cardIdentifier }?.visibleFace
            ?: ChallengeCardVisibleFace.FRONT
        return ChallengeCardActiveState(
            cardIdentifier = cardIdentifier,
            challengeDate = challengeDate,
            sequencePosition = ChallengeCardSequencePosition(
                oneBasedIndex = index + 1,
                totalCards = cards.size,
            ),
            visibleFace = visibleFace,
        )
    }

    private fun PresentedChallengeCard.toAdjacentPreviewState(
        cards: List<PresentedChallengeCard>,
    ): ChallengeCardAdjacentPreviewState {
        val index = cards.indexOfFirst { it.cardIdentifier == cardIdentifier }
        return ChallengeCardAdjacentPreviewState(
            previousCardIdentifier = cards.getOrNull(index - 1)?.cardIdentifier,
            nextCardIdentifier = cards.getOrNull(index + 1)?.cardIdentifier,
        )
    }

    private fun PresentedChallengeCard.toAdjacentIdentifiers(
        cards: List<PresentedChallengeCard>,
    ): Set<String> {
        val adjacentState = toAdjacentPreviewState(cards)
        return setOfNotNull(adjacentState.previousCardIdentifier, adjacentState.nextCardIdentifier)
    }

    private fun PresentedChallengeCard.hasRequiredContent(): Boolean =
        shortSummary.isNotBlank() &&
            detailDescription.isNotBlank() &&
            suggestions.isNotEmpty() &&
            suggestions.all { it.isNotBlank() }

    private fun motionPhaseFor(
        browsePhase: ChallengeCardBrowsePhase?,
    ): ChallengeCardMotionPhase? = when (browsePhase) {
        ChallengeCardBrowsePhase.IN_PROGRESS -> ChallengeCardMotionPhase.FREE_SCROLLING
        ChallengeCardBrowsePhase.ENDED -> ChallengeCardMotionPhase.SNAPPING
        null -> ChallengeCardMotionPhase.CENTERED
    }

    private fun snapStateFor(
        motionPhase: ChallengeCardMotionPhase?,
    ): ChallengeCardSnapState = when (motionPhase) {
        ChallengeCardMotionPhase.FREE_SCROLLING -> ChallengeCardSnapState.FREE_SCROLLING
        ChallengeCardMotionPhase.SNAPPING -> ChallengeCardSnapState.SNAPPING
        ChallengeCardMotionPhase.CENTERED,
        ChallengeCardMotionPhase.FACE_CHANGING,
        ChallengeCardMotionPhase.VERTICAL_READING,
        null,
        -> ChallengeCardSnapState.CENTERED
    }

    private fun failureResponse(
        reason: ChallengeCardRailFailureReason,
        message: String,
    ): ChallengeCardRailResponse {
        val response = ChallengeCardRailResponse(
            challengeCardRail = emptyList(),
            emptyCardRailState = null,
            activeCardState = null,
            adjacentCardPreviewState = null,
            cardVisualEmphasisState = null,
            motionState = null,
            failureResponse = CopFailureResponse(
                reason = reason,
                message = message,
            ),
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "present_card_rail_output",
            details = mapOf(
                "decision" to "rail_presentation_failed",
                "activeCardIdentifier" to null,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    companion object {
        private const val MODULE_NAME = "ChallengeCardRailPresentationModule"
    }
}
