package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityBrowseRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityFaceRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityPresentationContext
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardActiveState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardBackFaceReadingRequest
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardFaceState
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.PresentedChallengeCard

enum class ChallengeCardAccessibilityFailureReason {
    ACCESSIBILITY_STATE_UNAVAILABLE,
    NO_ACTIVE_CARD,
    ACTIVE_CARD_NOT_FOUND,
    BACK_FACE_READING_UNAVAILABLE,
}

data class ChallengeCardAccessibilityInput(
    val challengeCardList: List<PresentedChallengeCard>?,
    val currentActiveCardIdentifier: String?,
    val cardFaceState: ChallengeCardFaceState?,
    val accessibilityBrowseRequest: ChallengeCardAccessibilityBrowseRequest?,
    val accessibilityFaceRequest: ChallengeCardAccessibilityFaceRequest?,
    val backFaceReadingRequest: ChallengeCardBackFaceReadingRequest?,
)

data class ChallengeCardAccessibilityResponse(
    val accessibilityPresentationState: ChallengeCardAccessibilityPresentationContext?,
    val activeCardState: ChallengeCardActiveState?,
    val cardFaceState: ChallengeCardFaceState?,
    val failureResponse: CopFailureResponse<ChallengeCardAccessibilityFailureReason>? = null,
)

interface ChallengeCardAccessibilityProtocol {
    fun resolveAccessibilityPresentation(
        input: ChallengeCardAccessibilityInput,
    ): ChallengeCardAccessibilityResponse
}
