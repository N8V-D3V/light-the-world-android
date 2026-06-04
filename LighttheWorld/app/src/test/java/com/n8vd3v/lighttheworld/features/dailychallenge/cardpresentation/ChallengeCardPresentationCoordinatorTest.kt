package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallengeBrowseExperienceResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeExperienceState
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeProgress
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallenge
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionEligibility
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.ChallengeCardFaceFailureReason
import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ChallengeCardPresentationCoordinatorTest {

    private val coordinator = ChallengeCardPresentationCoordinator()

    @Test
    fun mapToPresentedCardsCalculatesFutureStateRelativeToCurrentDate() {
        val currentLocalDate = LocalDate.of(2026, 6, 4)
        val cards = coordinator.mapToPresentedCards(
            challengeStates = listOf(
                challengeState(day = 3),
                challengeState(day = 4),
                challengeState(day = 5),
            ),
            currentLocalDate = currentLocalDate,
        )

        assertEquals(ChallengeCardFuturePresentationState.NON_FUTURE, cards[0].futureState)
        assertEquals(ChallengeCardFuturePresentationState.NON_FUTURE, cards[1].futureState)
        assertEquals(ChallengeCardFuturePresentationState.FUTURE, cards[2].futureState)
    }

    @Test
    fun resolveInitialFocusUsesCurrentDateMatchWhenPresent() {
        val cards = listOf(card(3), card(4), card(5))

        val response = coordinator.resolveInitialFocus(
            cards = cards,
            currentDate = LocalDate.of(2026, 6, 4),
        )

        assertEquals("2026-06-04", response.activeCardState?.cardIdentifier)
        assertEquals(2, response.activeCardState?.sequencePosition?.oneBasedIndex)
    }

    @Test
    fun resolveActiveCardChangeReturnsNewActiveCardToFront() {
        val cards = listOf(card(3), card(4))

        val response = coordinator.resolveActiveCardChange(
            cards = cards,
            previousActiveCardIdentifier = "2026-06-03",
            requestedActiveCardIdentifier = "2026-06-04",
        )

        assertNull(response.failureResponse)
        assertEquals("2026-06-04", response.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardVisibleFace.FRONT, response.cardFaceState?.visibleFace)
    }

    @Test
    fun resolveRailKeepsAdjacentCardsOnFrontFaceOnly() {
        val cards = listOf(card(3), card(4), card(5))
        val focusResponse = coordinator.resolveFocus(
            cards = cards,
            currentDate = LocalDate.of(2026, 6, 4),
            currentActiveCardIdentifier = "2026-06-04",
            requestedActiveCardIdentifier = "2026-06-04",
            browseDirection = ChallengeCardBrowseDirection.NEXT,
            browsePhase = ChallengeCardBrowsePhase.IN_PROGRESS,
        )

        val railResponse = coordinator.resolveRail(
            cards = cards,
            focusResponse = focusResponse,
            faceState = ChallengeCardFaceState(
                cardIdentifier = "2026-06-04",
                visibleFace = ChallengeCardVisibleFace.BACK,
            ),
            browseDirection = ChallengeCardBrowseDirection.NEXT,
            browsePhase = ChallengeCardBrowsePhase.IN_PROGRESS,
        )

        val adjacentFaces = railResponse.cardVisualEmphasisState!!.cardStates
            .filter { it.emphasisState == ChallengeCardEmphasisState.ADJACENT }
            .map { it.visibleFace }

        assertTrue(adjacentFaces.all { it == ChallengeCardVisibleFace.FRONT })
        assertEquals(
            ChallengeCardVisibleFace.BACK,
            railResponse.cardVisualEmphasisState!!.cardStates
                .first { it.cardIdentifier == "2026-06-04" }
                .visibleFace,
        )
    }

    @Test
    fun resolveLoadStateDistinguishesCalendarFailureFromValidEmptyList() {
        val failureState = coordinator.resolveLoadState(
            browseResponse = DailyChallengeBrowseExperienceResponse(
                challengeCards = emptyList(),
                calendarFailureResponse = CopFailureResponse(
                    reason = ChallengeCalendarFailureReason.CHALLENGE_CONTENT_UNAVAILABLE,
                    message = "Challenge calendar data is unavailable.",
                ),
            ),
            currentLocalDate = LocalDate.of(2026, 6, 4),
            emptyMessage = "No challenges are available at this time.",
        )
        val emptyState = coordinator.resolveLoadState(
            browseResponse = DailyChallengeBrowseExperienceResponse(
                challengeCards = emptyList(),
                calendarFailureResponse = null,
            ),
            currentLocalDate = LocalDate.of(2026, 6, 4),
            emptyMessage = "No challenges are available at this time.",
        )

        assertTrue(failureState is ChallengeCardPresentationLoadState.Failure)
        assertEquals(
            ChallengeCardPresentationFailureStage.CALENDAR_CONTENT,
            (failureState as ChallengeCardPresentationLoadState.Failure).failure.stage,
        )
        assertTrue(emptyState is ChallengeCardPresentationLoadState.Empty)
    }

    @Test
    fun resolveRenderStateSurfacesInitialFocusFailure() {
        val renderState = coordinator.resolveRenderState(
            cards = listOf(
                card(day = 4, identifier = "a"),
                card(day = 4, identifier = "b"),
            ),
            currentDate = LocalDate.of(2026, 6, 4),
            currentActiveCardIdentifier = null,
            requestedActiveCardIdentifier = null,
            browseDirection = null,
            browsePhase = ChallengeCardBrowsePhase.ENDED,
            faceState = null,
        )

        assertTrue(renderState is ChallengeCardPresentationRenderState.Failure)
        assertEquals(
            ChallengeCardPresentationFailureStage.INITIAL_FOCUS,
            (renderState as ChallengeCardPresentationRenderState.Failure).failure.stage,
        )
    }

    @Test
    fun resolveRenderStateNormalizesStaleFaceStateWhenActiveCardChanges() {
        val renderState = coordinator.resolveRenderState(
            cards = listOf(card(3), card(4), card(5)),
            currentDate = LocalDate.of(2026, 6, 4),
            currentActiveCardIdentifier = "2026-06-04",
            requestedActiveCardIdentifier = "2026-06-05",
            browseDirection = null,
            browsePhase = ChallengeCardBrowsePhase.ENDED,
            faceState = ChallengeCardFaceState(
                cardIdentifier = "2026-06-04",
                visibleFace = ChallengeCardVisibleFace.BACK,
            ),
        )

        assertTrue(renderState is ChallengeCardPresentationRenderState.Content)
        assertEquals(
            "2026-06-05",
            (renderState as ChallengeCardPresentationRenderState.Content).activeCardIdentifier,
        )
        assertEquals(
            ChallengeCardVisibleFace.FRONT,
            renderState.accessibilityResponse.cardFaceState?.visibleFace,
        )
    }

    @Test
    fun faceAndBackFaceReadingFailuresRemainExplicit() {
        val faceFailure = coordinator.resolveFaceRequest(
            cards = listOf(card(3)),
            activeCardIdentifier = null,
            requestedFace = ChallengeCardVisibleFace.BACK,
        )
        val readingFailure = coordinator.resolveBackFaceReading(
            cards = listOf(card(3)),
            activeCardIdentifier = null,
        )

        assertEquals(ChallengeCardFaceFailureReason.NO_ACTIVE_CARD, faceFailure.failureResponse?.reason)
        assertFalse(
            coordinator.toFaceFailure(
                response = faceFailure,
                stage = ChallengeCardPresentationFailureStage.FACE,
            ) == null,
        )
        assertEquals(ChallengeCardFaceFailureReason.NO_ACTIVE_CARD, readingFailure.failureResponse?.reason)
        assertFalse(
            coordinator.toFaceFailure(
                response = readingFailure,
                stage = ChallengeCardPresentationFailureStage.BACK_FACE_READING,
            ) == null,
        )
    }

    private fun challengeState(
        day: Int,
        status: ChallengeCompletionStatus = ChallengeCompletionStatus.INCOMPLETE,
    ): ChallengeExperienceState = ChallengeExperienceState(
        challenge = DailyChallenge(
            date = LocalDate.of(2026, 6, day),
            shortSummary = "Summary $day",
            detailDescription = "Details $day",
            suggestions = listOf("Suggestion $day"),
        ),
        completionState = ChallengeProgress(
            challengeDate = LocalDate.of(2026, 6, day),
            status = status,
            completedAt = null,
        ),
        completionEligibility = CompletionEligibility.ELIGIBLE,
    )

    private fun card(day: Int): PresentedChallengeCard =
        coordinator.mapToPresentedCards(
            challengeStates = listOf(challengeState(day)),
            currentLocalDate = LocalDate.of(2026, 6, 4),
        ).single()

    private fun card(
        day: Int,
        identifier: String,
    ): PresentedChallengeCard =
        coordinator.mapToPresentedCards(
            challengeStates = listOf(challengeState(day)),
            currentLocalDate = LocalDate.of(2026, 6, 4),
        ).single().copy(cardIdentifier = identifier)
}
