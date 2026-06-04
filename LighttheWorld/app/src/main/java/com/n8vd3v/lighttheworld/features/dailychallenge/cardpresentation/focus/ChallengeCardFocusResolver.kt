package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveCardRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAdjacentPreviewState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBrowseRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardVisualEmphasisState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard
import java.time.LocalDate

enum class ChallengeCardFocusFailureReason {
    ACTIVE_CARD_NOT_FOUND,
    CHALLENGE_CARD_LIST_UNAVAILABLE,
    INITIAL_ACTIVE_CARD_UNDETERMINED,
}

data class ChallengeCardFocusInput(
    val challengeCardList: List<PresentedChallengeCard>?,
    val currentDate: LocalDate?,
    val currentActiveCardIdentifier: String?,
    val activeCardRequest: ChallengeCardActiveCardRequest?,
    val browseRequest: ChallengeCardBrowseRequest?,
)

data class ChallengeCardFocusResponse(
    val activeCardState: ChallengeCardActiveState?,
    val adjacentCardPreviewState: ChallengeCardAdjacentPreviewState?,
    val cardVisualEmphasisState: ChallengeCardVisualEmphasisState?,
    val motionState: ChallengeCardMotionState?,
    val failureResponse: CopFailureResponse<ChallengeCardFocusFailureReason>? = null,
)

interface ChallengeCardFocusResolver {
    fun resolveActiveCardFocus(
        input: ChallengeCardFocusInput,
    ): ChallengeCardFocusResponse
}
