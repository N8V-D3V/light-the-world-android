package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAdjacentPreviewState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBrowseDirection
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBrowsePhase
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardEmphasisState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionPhase
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardPresentationState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardSequencePosition
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardSnapState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisibleFace
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisualEmphasisState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

class LTWChallengeCardFocusResolver(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : ChallengeCardFocusResolver {

    override fun resolveActiveCardFocus(
        input: ChallengeCardFocusInput,
    ): ChallengeCardFocusResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_active_card_focus_input",
            details = mapOf(
                "challengeCardCount" to input.challengeCardList?.size,
                "currentDate" to input.currentDate,
                "currentActiveCardIdentifier" to input.currentActiveCardIdentifier,
                "activeCardRequest" to input.activeCardRequest?.cardIdentifier,
                "browseDirection" to input.browseRequest?.direction,
                "browsePhase" to input.browseRequest?.phase,
            ),
        )

        val cards = input.challengeCardList
            ?: return failureResponse(
                reason = ChallengeCardFocusFailureReason.CHALLENGE_CARD_LIST_UNAVAILABLE,
                message = "Challenge card list is unavailable.",
            )

        if (cards.isEmpty()) {
            return successResponse(
                cards = cards,
                activeCard = null,
                motionPhase = null,
                decision = "empty_card_list_no_active_card",
            )
        }

        val currentActiveCard = cards.firstOrNull { it.cardIdentifier == input.currentActiveCardIdentifier }
        val explicitTarget = input.activeCardRequest?.cardIdentifier
        if (explicitTarget != null) {
            val requestedCard = cards.firstOrNull { it.cardIdentifier == explicitTarget }
            if (requestedCard == null) {
                return failureResponse(
                    reason = ChallengeCardFocusFailureReason.ACTIVE_CARD_NOT_FOUND,
                    message = "Requested active card is not present in the current challenge card list.",
                    cards = cards,
                    preservedActiveCard = currentActiveCard,
                )
            }

            return successResponse(
                cards = cards,
                activeCard = requestedCard,
                motionPhase = ChallengeCardMotionPhase.CENTERED,
                decision = "explicit_active_card_selected",
            )
        }

        val activeCardFromCurrent = currentActiveCard ?: determineInitialActiveCard(
            cards = cards,
            currentDate = input.currentDate,
        ) ?: return failureResponse(
                reason = ChallengeCardFocusFailureReason.INITIAL_ACTIVE_CARD_UNDETERMINED,
                message = "The contract-defined initial active card could not be determined.",
            )

        if (input.browseRequest?.direction != null) {
            val nextActiveCard = moveOneCard(
                cards = cards,
                currentActiveCard = activeCardFromCurrent,
                direction = input.browseRequest.direction,
            )
            return successResponse(
                cards = cards,
                activeCard = nextActiveCard,
                motionPhase = motionPhaseFor(input.browseRequest.phase),
                decision = "browse_request_resolved_to_single_centered_card",
            )
        }

        return successResponse(
            cards = cards,
            activeCard = activeCardFromCurrent,
            motionPhase = ChallengeCardMotionPhase.CENTERED,
            decision = if (currentActiveCard == null) {
                "deterministic_initial_active_card_selected"
            } else {
                "existing_active_card_preserved"
            },
        )
    }

    private fun determineInitialActiveCard(
        cards: List<PresentedChallengeCard>,
        currentDate: java.time.LocalDate?,
    ): PresentedChallengeCard? {
        if (cards.isEmpty()) {
            return null
        }

        if (currentDate != null) {
            val currentDateMatches = cards.filter { it.challengeDate == currentDate }
            if (currentDateMatches.size > 1) {
                return null
            }
            if (currentDateMatches.size == 1) {
                return currentDateMatches.single()
            }
        }

        return cards.firstOrNull()
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

    private fun successResponse(
        cards: List<PresentedChallengeCard>,
        activeCard: PresentedChallengeCard?,
        motionPhase: ChallengeCardMotionPhase?,
        decision: String,
    ): ChallengeCardFocusResponse {
        val response = ChallengeCardFocusResponse(
            activeCardState = activeCard?.toActiveState(cards),
            adjacentCardPreviewState = activeCard?.toAdjacentPreviewState(cards),
            cardVisualEmphasisState = ChallengeCardVisualEmphasisState(
                cardStates = cards.map { card ->
                    val emphasisState = when {
                        activeCard == null -> ChallengeCardEmphasisState.OFFSCREEN
                        card.cardIdentifier == activeCard.cardIdentifier -> ChallengeCardEmphasisState.ACTIVE
                        activeCard.toAdjacentIdentifiers(cards).contains(card.cardIdentifier) -> ChallengeCardEmphasisState.ADJACENT
                        else -> ChallengeCardEmphasisState.OFFSCREEN
                    }
                    ChallengeCardPresentationState(
                        cardIdentifier = card.cardIdentifier,
                        emphasisState = emphasisState,
                        visibleFace = ChallengeCardVisibleFace.FRONT,
                        snapState = snapStateFor(motionPhase),
                    )
                },
            ),
            motionState = motionPhase?.let(::ChallengeCardMotionState),
            failureResponse = null,
        )

        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_active_card_focus_output",
            details = mapOf(
                "decision" to decision,
                "activeCardIdentifier" to response.activeCardState?.cardIdentifier,
                "motionPhase" to response.motionState?.phase,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    private fun failureResponse(
        reason: ChallengeCardFocusFailureReason,
        message: String,
        cards: List<PresentedChallengeCard> = emptyList(),
        preservedActiveCard: PresentedChallengeCard? = null,
    ): ChallengeCardFocusResponse {
        val response = ChallengeCardFocusResponse(
            activeCardState = preservedActiveCard?.toActiveState(cards),
            adjacentCardPreviewState = preservedActiveCard?.toAdjacentPreviewState(cards),
            cardVisualEmphasisState = if (cards.isEmpty()) {
                null
            } else {
                ChallengeCardVisualEmphasisState(
                    cardStates = cards.map { card ->
                        val emphasisState = when {
                            preservedActiveCard == null -> ChallengeCardEmphasisState.OFFSCREEN
                            card.cardIdentifier == preservedActiveCard.cardIdentifier -> ChallengeCardEmphasisState.ACTIVE
                            preservedActiveCard.toAdjacentIdentifiers(cards).contains(card.cardIdentifier) -> ChallengeCardEmphasisState.ADJACENT
                            else -> ChallengeCardEmphasisState.OFFSCREEN
                        }
                        ChallengeCardPresentationState(
                            cardIdentifier = card.cardIdentifier,
                            emphasisState = emphasisState,
                            visibleFace = ChallengeCardVisibleFace.FRONT,
                            snapState = ChallengeCardSnapState.CENTERED,
                        )
                    },
                )
            },
            motionState = preservedActiveCard?.let { ChallengeCardMotionState(ChallengeCardMotionPhase.CENTERED) },
            failureResponse = CopFailureResponse(
                reason = reason,
                message = message,
            ),
        )

        logger.logDecision(
            module = MODULE_NAME,
            action = "resolve_active_card_focus_output",
            details = mapOf(
                "decision" to "focus_resolution_failed",
                "activeCardIdentifier" to response.activeCardState?.cardIdentifier,
                "motionPhase" to response.motionState?.phase,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return response
    }

    private fun PresentedChallengeCard.toActiveState(
        cards: List<PresentedChallengeCard>,
    ): ChallengeCardActiveState {
        val index = cards.indexOfFirst { it.cardIdentifier == cardIdentifier }
        return ChallengeCardActiveState(
            cardIdentifier = cardIdentifier,
            challengeDate = challengeDate,
            sequencePosition = ChallengeCardSequencePosition(
                oneBasedIndex = index + 1,
                totalCards = cards.size,
            ),
            visibleFace = ChallengeCardVisibleFace.FRONT,
        )
    }

    private fun PresentedChallengeCard.toAdjacentPreviewState(
        cards: List<PresentedChallengeCard>,
    ): ChallengeCardAdjacentPreviewState {
        val index = cards.indexOfFirst { it.cardIdentifier == cardIdentifier }
        val previousCardIdentifier = cards.getOrNull(index - 1)?.cardIdentifier
        val nextCardIdentifier = cards.getOrNull(index + 1)?.cardIdentifier
        return ChallengeCardAdjacentPreviewState(
            previousCardIdentifier = previousCardIdentifier,
            nextCardIdentifier = nextCardIdentifier,
        )
    }

    private fun PresentedChallengeCard.toAdjacentIdentifiers(
        cards: List<PresentedChallengeCard>,
    ): Set<String> {
        val adjacent = toAdjacentPreviewState(cards)
        return setOfNotNull(adjacent.previousCardIdentifier, adjacent.nextCardIdentifier)
    }

    private fun motionPhaseFor(
        browsePhase: ChallengeCardBrowsePhase?,
    ): ChallengeCardMotionPhase = when (browsePhase) {
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

    companion object {
        private const val MODULE_NAME = "LTWChallengeCardFocusResolver"
    }
}
