package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveCardRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBackFaceReadingRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardFaceRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardFaceState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardMotionState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

enum class ChallengeCardFaceFailureReason {
    NO_ACTIVE_CARD,
    REQUIRED_CARD_CONTENT_MISSING,
    BACK_FACE_READING_UNAVAILABLE,
}

data class ChallengeCardFaceInput(
    val challengeCardList: List<PresentedChallengeCard>?,
    val currentActiveCardIdentifier: String?,
    val cardFaceRequest: ChallengeCardFaceRequest?,
    val activeCardRequest: ChallengeCardActiveCardRequest?,
    val backFaceReadingRequest: ChallengeCardBackFaceReadingRequest?,
)

data class ChallengeCardFaceResponse(
    val cardFaceState: ChallengeCardFaceState?,
    val motionState: ChallengeCardMotionState?,
    val activeCardState: ChallengeCardActiveState?,
    val failureResponse: CopFailureResponse<ChallengeCardFaceFailureReason>? = null,
)

interface ChallengeCardFaceProtocol {
    fun resolveCardFace(
        input: ChallengeCardFaceInput,
    ): ChallengeCardFaceResponse
}
