package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeExperienceState
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallengeBrowseExperienceResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.ChallengeCardAccessibilityInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.ChallengeCardAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.ChallengeCardAccessibilityResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.LTWChallengeCardAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.ChallengeCardFaceInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.ChallengeCardFacePresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.ChallengeCardFaceResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.LTWChallengeCardFacePresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.ChallengeCardFocusInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.ChallengeCardFocusResolver
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.ChallengeCardFocusResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.LTWChallengeCardFocusResolver
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.ChallengeCardRailInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.ChallengeCardRailPresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.ChallengeCardRailResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.LTWChallengeCardRailPresenter
import java.time.LocalDate

enum class ChallengeCardPresentationFailureStage {
    CALENDAR_CONTENT,
    INITIAL_FOCUS,
    FOCUS,
    RAIL,
    FACE,
    ACCESSIBILITY,
    BACK_FACE_READING,
}

data class ChallengeCardPresentationFailure(
    val stage: ChallengeCardPresentationFailureStage,
    val message: String,
)

sealed interface ChallengeCardPresentationLoadState {
    data class Failure(
        val failure: ChallengeCardPresentationFailure,
    ) : ChallengeCardPresentationLoadState

    data class Empty(
        val message: String,
    ) : ChallengeCardPresentationLoadState

    data class Content(
        val cards: List<PresentedChallengeCard>,
    ) : ChallengeCardPresentationLoadState
}

sealed interface ChallengeCardPresentationRenderState {
    data class Failure(
        val failure: ChallengeCardPresentationFailure,
    ) : ChallengeCardPresentationRenderState

    data class Content(
        val cards: List<PresentedChallengeCard>,
        val focusResponse: ChallengeCardFocusResponse,
        val railResponse: ChallengeCardRailResponse,
        val accessibilityResponse: ChallengeCardAccessibilityResponse,
        val activeCardIdentifier: String?,
    ) : ChallengeCardPresentationRenderState
}

class ChallengeCardPresentationCoordinator(
    private val focusResolver: ChallengeCardFocusResolver = LTWChallengeCardFocusResolver(),
    private val railPresenter: ChallengeCardRailPresenter = LTWChallengeCardRailPresenter(),
    private val facePresenter: ChallengeCardFacePresenter = LTWChallengeCardFacePresenter(),
    private val accessibilityPresenter: ChallengeCardAccessibilityPresenter = LTWChallengeCardAccessibilityPresenter(),
) {

    fun resolveLoadState(
        browseResponse: DailyChallengeBrowseExperienceResponse,
        currentLocalDate: LocalDate,
        emptyMessage: String,
    ): ChallengeCardPresentationLoadState {
        val calendarFailure = browseResponse.calendarFailureResponse
        if (calendarFailure != null) {
            return ChallengeCardPresentationLoadState.Failure(
                ChallengeCardPresentationFailure(
                    stage = ChallengeCardPresentationFailureStage.CALENDAR_CONTENT,
                    message = calendarFailure.message,
                ),
            )
        }

        val cards = mapToPresentedCards(
            challengeStates = browseResponse.challengeCards,
            currentLocalDate = currentLocalDate,
        )
        return if (cards.isEmpty()) {
            ChallengeCardPresentationLoadState.Empty(message = emptyMessage)
        } else {
            ChallengeCardPresentationLoadState.Content(cards = cards)
        }
    }

    fun mapToPresentedCards(
        challengeStates: List<ChallengeExperienceState>,
        currentLocalDate: LocalDate,
    ): List<PresentedChallengeCard> = challengeStates.map { state ->
        PresentedChallengeCard(
            cardIdentifier = state.challenge.date.toString(),
            challengeDate = state.challenge.date,
            shortSummary = state.challenge.shortSummary,
            detailDescription = state.challenge.detailDescription,
            suggestions = state.challenge.suggestions,
            completionState = if (state.completionState.status == ChallengeCompletionStatus.COMPLETED) {
                ChallengeCardCompletionPresentationState.COMPLETED
            } else {
                ChallengeCardCompletionPresentationState.INCOMPLETE
            },
            futureState = if (state.challenge.date.isAfter(currentLocalDate)) {
                ChallengeCardFuturePresentationState.FUTURE
            } else {
                ChallengeCardFuturePresentationState.NON_FUTURE
            },
        )
    }

    fun resolveInitialFocus(
        cards: List<PresentedChallengeCard>,
        currentDate: LocalDate,
    ): ChallengeCardFocusResponse = focusResolver.resolveActiveCardFocus(
        ChallengeCardFocusInput(
            challengeCardList = cards,
            currentDate = currentDate,
            currentActiveCardIdentifier = null,
            activeCardRequest = null,
            browseRequest = null,
        ),
    )

    fun resolveFocus(
        cards: List<PresentedChallengeCard>,
        currentDate: LocalDate,
        currentActiveCardIdentifier: String?,
        requestedActiveCardIdentifier: String?,
        browseDirection: ChallengeCardBrowseDirection?,
        browsePhase: ChallengeCardBrowsePhase,
    ): ChallengeCardFocusResponse = focusResolver.resolveActiveCardFocus(
        ChallengeCardFocusInput(
            challengeCardList = cards,
            currentDate = currentDate,
            currentActiveCardIdentifier = currentActiveCardIdentifier,
            activeCardRequest = requestedActiveCardIdentifier?.let(::ChallengeCardActiveCardRequest),
            browseRequest = ChallengeCardBrowseRequest(
                direction = browseDirection,
                phase = browsePhase,
            ),
        ),
    )

    fun resolveFaceToggle(
        cards: List<PresentedChallengeCard>,
        activeCardIdentifier: String?,
        currentFaceState: ChallengeCardFaceState?,
    ): ChallengeCardFaceResponse {
        val requestedFace = if (currentFaceState?.visibleFace == ChallengeCardVisibleFace.BACK) {
            ChallengeCardVisibleFace.FRONT
        } else {
            ChallengeCardVisibleFace.BACK
        }
        return resolveFaceRequest(
            cards = cards,
            activeCardIdentifier = activeCardIdentifier,
            requestedFace = requestedFace,
        )
    }

    fun resolveFaceRequest(
        cards: List<PresentedChallengeCard>,
        activeCardIdentifier: String?,
        requestedFace: ChallengeCardVisibleFace,
    ): ChallengeCardFaceResponse = facePresenter.resolveCardFace(
        ChallengeCardFaceInput(
            challengeCardList = cards,
            currentActiveCardIdentifier = activeCardIdentifier,
            cardFaceRequest = ChallengeCardFaceRequest(requestedFace = requestedFace),
            activeCardRequest = null,
            backFaceReadingRequest = null,
        ),
    )

    fun resolveActiveCardChange(
        cards: List<PresentedChallengeCard>,
        previousActiveCardIdentifier: String?,
        requestedActiveCardIdentifier: String?,
    ): ChallengeCardFaceResponse = facePresenter.resolveCardFace(
        ChallengeCardFaceInput(
            challengeCardList = cards,
            currentActiveCardIdentifier = previousActiveCardIdentifier,
            cardFaceRequest = null,
            activeCardRequest = requestedActiveCardIdentifier?.let(::ChallengeCardActiveCardRequest),
            backFaceReadingRequest = null,
        ),
    )

    fun resolveBackFaceReading(
        cards: List<PresentedChallengeCard>,
        activeCardIdentifier: String?,
    ): ChallengeCardFaceResponse = facePresenter.resolveCardFace(
        ChallengeCardFaceInput(
            challengeCardList = cards,
            currentActiveCardIdentifier = activeCardIdentifier,
            cardFaceRequest = null,
            activeCardRequest = null,
            backFaceReadingRequest = activeCardIdentifier?.let(::ChallengeCardBackFaceReadingRequest),
        ),
    )

    fun resolveRail(
        cards: List<PresentedChallengeCard>,
        focusResponse: ChallengeCardFocusResponse,
        faceState: ChallengeCardFaceState?,
        browseDirection: ChallengeCardBrowseDirection?,
        browsePhase: ChallengeCardBrowsePhase,
    ): ChallengeCardRailResponse {
        val cardStates = focusResponse.cardVisualEmphasisState?.cardStates.orEmpty().map { state ->
            if (state.cardIdentifier == faceState?.cardIdentifier) {
                state.copy(visibleFace = faceState.visibleFace)
            } else {
                state.copy(visibleFace = ChallengeCardVisibleFace.FRONT)
            }
        }
        return railPresenter.presentCardRail(
            ChallengeCardRailInput(
                challengeCardList = cards,
                cardPresentationStates = cardStates,
                currentActiveCardIdentifier = focusResponse.activeCardState?.cardIdentifier,
                browseRequest = ChallengeCardBrowseRequest(
                    direction = browseDirection,
                    phase = browsePhase,
                ),
            ),
        )
    }

    fun resolveAccessibility(
        cards: List<PresentedChallengeCard>,
        activeCardIdentifier: String?,
        faceState: ChallengeCardFaceState?,
    ): ChallengeCardAccessibilityResponse = accessibilityPresenter.resolveAccessibilityPresentation(
        ChallengeCardAccessibilityInput(
            challengeCardList = cards,
            currentActiveCardIdentifier = activeCardIdentifier,
            cardFaceState = faceState,
            accessibilityBrowseRequest = null,
            accessibilityFaceRequest = null,
            backFaceReadingRequest = null,
        ),
    )

    fun resolveRenderState(
        cards: List<PresentedChallengeCard>,
        currentDate: LocalDate,
        currentActiveCardIdentifier: String?,
        requestedActiveCardIdentifier: String?,
        browseDirection: ChallengeCardBrowseDirection?,
        browsePhase: ChallengeCardBrowsePhase,
        faceState: ChallengeCardFaceState?,
    ): ChallengeCardPresentationRenderState {
        val isInitialResolution = currentActiveCardIdentifier == null && requestedActiveCardIdentifier == null
        val focusResponse = resolveFocus(
            cards = cards,
            currentDate = currentDate,
            currentActiveCardIdentifier = currentActiveCardIdentifier,
            requestedActiveCardIdentifier = requestedActiveCardIdentifier,
            browseDirection = browseDirection,
            browsePhase = browsePhase,
        )
        if (focusResponse.failureResponse != null) {
            return ChallengeCardPresentationRenderState.Failure(
                ChallengeCardPresentationFailure(
                    stage = if (isInitialResolution) {
                        ChallengeCardPresentationFailureStage.INITIAL_FOCUS
                    } else {
                        ChallengeCardPresentationFailureStage.FOCUS
                    },
                    message = focusResponse.failureResponse.message,
                ),
            )
        }

        val activeCardIdentifier = focusResponse.activeCardState?.cardIdentifier
        val normalizedFaceState = normalizeFaceState(
            activeCardIdentifier = activeCardIdentifier,
            faceState = faceState,
        )
        val railResponse = resolveRail(
            cards = cards,
            focusResponse = focusResponse,
            faceState = normalizedFaceState,
            browseDirection = browseDirection,
            browsePhase = browsePhase,
        )
        if (railResponse.failureResponse != null) {
            return ChallengeCardPresentationRenderState.Failure(
                ChallengeCardPresentationFailure(
                    stage = ChallengeCardPresentationFailureStage.RAIL,
                    message = railResponse.failureResponse.message,
                ),
            )
        }

        val accessibilityResponse = resolveAccessibility(
            cards = cards,
            activeCardIdentifier = activeCardIdentifier,
            faceState = normalizedFaceState,
        )
        if (accessibilityResponse.failureResponse != null) {
            return ChallengeCardPresentationRenderState.Failure(
                ChallengeCardPresentationFailure(
                    stage = ChallengeCardPresentationFailureStage.ACCESSIBILITY,
                    message = accessibilityResponse.failureResponse.message,
                ),
            )
        }

        return ChallengeCardPresentationRenderState.Content(
            cards = cards,
            focusResponse = focusResponse,
            railResponse = railResponse,
            accessibilityResponse = accessibilityResponse,
            activeCardIdentifier = activeCardIdentifier,
        )
    }

    private fun normalizeFaceState(
        activeCardIdentifier: String?,
        faceState: ChallengeCardFaceState?,
    ): ChallengeCardFaceState? = activeCardIdentifier?.let { identifier ->
        if (faceState?.cardIdentifier == identifier) {
            faceState
        } else {
            ChallengeCardFaceState(
                cardIdentifier = identifier,
                visibleFace = ChallengeCardVisibleFace.FRONT,
            )
        }
    }

    fun toFaceFailure(
        response: ChallengeCardFaceResponse,
        stage: ChallengeCardPresentationFailureStage,
    ): ChallengeCardPresentationFailure? = response.failureResponse?.let { failure ->
        ChallengeCardPresentationFailure(
            stage = stage,
            message = failure.message,
        )
    }
}
