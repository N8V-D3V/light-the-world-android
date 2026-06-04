package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAdjacentPreviewState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBrowseRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardPresentationState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisualEmphasisState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.EmptyCardRailState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

enum class ChallengeCardRailFailureReason {
    CHALLENGE_CARD_LIST_UNAVAILABLE,
    REQUIRED_CARD_CONTENT_MISSING,
}

data class ChallengeCardRailInput(
    val challengeCardList: List<PresentedChallengeCard>?,
    val cardPresentationStates: List<ChallengeCardPresentationState>,
    val currentActiveCardIdentifier: String?,
    val browseRequest: ChallengeCardBrowseRequest?,
)

data class ChallengeCardRailResponse(
    val challengeCardRail: List<PresentedChallengeCard>,
    val emptyCardRailState: EmptyCardRailState?,
    val activeCardState: ChallengeCardActiveState?,
    val adjacentCardPreviewState: ChallengeCardAdjacentPreviewState?,
    val cardVisualEmphasisState: ChallengeCardVisualEmphasisState?,
    val motionState: ChallengeCardMotionState?,
    val failureResponse: CopFailureResponse<ChallengeCardRailFailureReason>? = null,
)

interface ChallengeCardRailPresenter {
    fun presentCardRail(
        input: ChallengeCardRailInput,
    ): ChallengeCardRailResponse
}
