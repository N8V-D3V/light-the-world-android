package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import java.time.LocalDate

data class PresentedChallengeCard(
    val cardIdentifier: String,
    val challengeDate: LocalDate,
    val shortSummary: String,
    val detailDescription: String,
    val suggestions: List<String>,
    val completionState: ChallengeCardCompletionPresentationState,
    val futureState: ChallengeCardFuturePresentationState,
)

enum class ChallengeCardCompletionPresentationState {
    INCOMPLETE,
    COMPLETED,
}

enum class ChallengeCardFuturePresentationState {
    NON_FUTURE,
    FUTURE,
}

enum class ChallengeCardEmphasisState {
    ACTIVE,
    ADJACENT,
    OFFSCREEN,
}

enum class ChallengeCardVisibleFace {
    FRONT,
    BACK,
}

enum class ChallengeCardSnapState {
    FREE_SCROLLING,
    SNAPPING,
    CENTERED,
}

data class ChallengeCardPresentationState(
    val cardIdentifier: String,
    val emphasisState: ChallengeCardEmphasisState,
    val visibleFace: ChallengeCardVisibleFace,
    val snapState: ChallengeCardSnapState,
)

data class ChallengeCardActiveState(
    val cardIdentifier: String,
    val challengeDate: LocalDate,
    val sequencePosition: ChallengeCardSequencePosition,
    val visibleFace: ChallengeCardVisibleFace,
)

data class ChallengeCardSequencePosition(
    val oneBasedIndex: Int,
    val totalCards: Int,
)

data class ChallengeCardAdjacentPreviewState(
    val previousCardIdentifier: String?,
    val nextCardIdentifier: String?,
)

data class ChallengeCardVisualEmphasisState(
    val cardStates: List<ChallengeCardPresentationState>,
)

enum class EmptyCardRailInteractionState {
    NON_INTERACTIVE,
}

data class EmptyCardRailState(
    val message: String,
    val interactionState: EmptyCardRailInteractionState = EmptyCardRailInteractionState.NON_INTERACTIVE,
)

enum class ChallengeCardBrowseDirection {
    PREVIOUS,
    NEXT,
}

enum class ChallengeCardBrowsePhase {
    IN_PROGRESS,
    ENDED,
}

data class ChallengeCardBrowseRequest(
    val direction: ChallengeCardBrowseDirection?,
    val phase: ChallengeCardBrowsePhase,
)

data class ChallengeCardActiveCardRequest(
    val cardIdentifier: String,
)

data class ChallengeCardFaceRequest(
    val requestedFace: ChallengeCardVisibleFace,
)

data class ChallengeCardFaceState(
    val cardIdentifier: String,
    val visibleFace: ChallengeCardVisibleFace,
)

enum class ChallengeCardMotionPhase {
    FREE_SCROLLING,
    SNAPPING,
    CENTERED,
    FACE_CHANGING,
    VERTICAL_READING,
}

data class ChallengeCardMotionState(
    val phase: ChallengeCardMotionPhase,
)

data class ChallengeCardBackFaceReadingRequest(
    val cardIdentifier: String,
)

enum class ChallengeCardAccessibilityAction {
    BROWSE_PREVIOUS,
    BROWSE_NEXT,
    SHOW_FRONT,
    SHOW_BACK,
}

data class ChallengeCardAccessibilityBrowseRequest(
    val direction: ChallengeCardBrowseDirection,
)

data class ChallengeCardAccessibilityFaceRequest(
    val requestedFace: ChallengeCardVisibleFace,
)

data class ChallengeCardAccessibilityPresentationContext(
    val activeCardIdentifier: String,
    val activeCardDate: LocalDate,
    val activeCardPosition: ChallengeCardSequencePosition,
    val activeCardFace: ChallengeCardVisibleFace,
    val availableActions: Set<ChallengeCardAccessibilityAction>,
)
