package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import com.n8vd3v.lighttheworld.cop.InMemoryStubDecisionLogger
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityAction.BROWSE_NEXT
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityAction.BROWSE_PREVIOUS
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityAction.SHOW_BACK
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.ChallengeCardAccessibilityAction.SHOW_FRONT
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.ChallengeCardAccessibilityFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.ChallengeCardAccessibilityInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.accessibility.LTWChallengeCardAccessibilityPresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.ChallengeCardFaceFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.ChallengeCardFaceInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.face.LTWChallengeCardFacePresenter
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.ChallengeCardFocusFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.ChallengeCardFocusInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.focus.LTWChallengeCardFocusResolver
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.ChallengeCardRailFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.ChallengeCardRailInput
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.rail.LTWChallengeCardRailPresenter
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeCardPresentationImplementationsTest {

    @Test
    fun railModuleReturnsNonInteractiveEmptyStateWithoutActiveOrAdjacentCards() {
        val logger = InMemoryStubDecisionLogger()
        val module = LTWChallengeCardRailPresenter(logger = logger)

        val response = module.presentCardRail(
            ChallengeCardRailInput(
                challengeCardList = emptyList(),
                cardPresentationStates = emptyList(),
                currentActiveCardIdentifier = null,
                browseRequest = null,
            ),
        )

        assertNull(response.failureResponse)
        assertEquals(0, response.challengeCardRail.size)
        assertEquals("No challenges are available.", response.emptyCardRailState?.message)
        assertEquals(EmptyCardRailInteractionState.NON_INTERACTIVE, response.emptyCardRailState?.interactionState)
        assertNull(response.activeCardState)
        assertNull(response.adjacentCardPreviewState)
        assertNotNull(response.cardVisualEmphasisState)
        assertTrue(response.cardVisualEmphasisState!!.cardStates.isEmpty())
        assertTrue(logger.entries.isNotEmpty())
    }

    @Test
    fun focusModuleUsesTodaysCardAsInitialActiveWhenPresent() {
        val module = LTWChallengeCardFocusResolver()
        val cards = listOf(card(10), card(11), card(12))

        val response = module.resolveActiveCardFocus(
            ChallengeCardFocusInput(
                challengeCardList = cards,
                currentDate = LocalDate.of(2026, 12, 11),
                currentActiveCardIdentifier = null,
                activeCardRequest = null,
                browseRequest = null,
            ),
        )

        assertNull(response.failureResponse)
        assertEquals("card-11", response.activeCardState?.cardIdentifier)
        assertEquals(2, response.activeCardState?.sequencePosition?.oneBasedIndex)
        assertEquals("card-10", response.adjacentCardPreviewState?.previousCardIdentifier)
        assertEquals("card-12", response.adjacentCardPreviewState?.nextCardIdentifier)
    }

    @Test
    fun focusModuleFallsBackToFirstCardWhenTodaysCardIsAbsent() {
        val module = LTWChallengeCardFocusResolver()
        val cards = listOf(card(2), card(5), card(9))

        val response = module.resolveActiveCardFocus(
            ChallengeCardFocusInput(
                challengeCardList = cards,
                currentDate = LocalDate.of(2026, 12, 11),
                currentActiveCardIdentifier = null,
                activeCardRequest = null,
                browseRequest = null,
            ),
        )

        assertNull(response.failureResponse)
        assertEquals("card-2", response.activeCardState?.cardIdentifier)
        assertEquals(1, response.activeCardState?.sequencePosition?.oneBasedIndex)
    }

    @Test
    fun focusModuleFailsWhenInitialActiveCardCannotBeDeterministicallyResolved() {
        val module = LTWChallengeCardFocusResolver()
        val cards = listOf(
            card(day = 10, identifier = "card-a"),
            card(day = 10, identifier = "card-b"),
            card(day = 11, identifier = "card-c"),
        )

        val response = module.resolveActiveCardFocus(
            ChallengeCardFocusInput(
                challengeCardList = cards,
                currentDate = LocalDate.of(2026, 12, 10),
                currentActiveCardIdentifier = null,
                activeCardRequest = null,
                browseRequest = null,
            ),
        )

        assertNull(response.activeCardState)
        assertEquals(
            ChallengeCardFocusFailureReason.INITIAL_ACTIVE_CARD_UNDETERMINED,
            response.failureResponse?.reason,
        )
    }

    @Test
    fun focusAndRailModulesPreserveSingleCenteredCardAndSnapAfterBrowseEnds() {
        val focusModule = LTWChallengeCardFocusResolver()
        val railModule = LTWChallengeCardRailPresenter()
        val cards = listOf(card(10), card(11), card(12))

        val focusResponse = focusModule.resolveActiveCardFocus(
            ChallengeCardFocusInput(
                challengeCardList = cards,
                currentDate = LocalDate.of(2026, 12, 11),
                currentActiveCardIdentifier = "card-11",
                activeCardRequest = null,
                browseRequest = ChallengeCardBrowseRequest(
                    direction = ChallengeCardBrowseDirection.NEXT,
                    phase = ChallengeCardBrowsePhase.ENDED,
                ),
            ),
        )

        assertNull(focusResponse.failureResponse)
        assertEquals("card-12", focusResponse.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardMotionPhase.SNAPPING, focusResponse.motionState?.phase)

        val railResponse = railModule.presentCardRail(
            ChallengeCardRailInput(
                challengeCardList = cards,
                cardPresentationStates = focusResponse.cardVisualEmphasisState!!.cardStates,
                currentActiveCardIdentifier = focusResponse.activeCardState!!.cardIdentifier,
                browseRequest = ChallengeCardBrowseRequest(
                    direction = ChallengeCardBrowseDirection.NEXT,
                    phase = ChallengeCardBrowsePhase.ENDED,
                ),
            ),
        )

        assertNull(railResponse.failureResponse)
        assertEquals("card-12", railResponse.activeCardState?.cardIdentifier)
        assertEquals("card-11", railResponse.adjacentCardPreviewState?.previousCardIdentifier)
        assertNull(railResponse.adjacentCardPreviewState?.nextCardIdentifier)
        assertEquals(ChallengeCardMotionPhase.SNAPPING, railResponse.motionState?.phase)
        assertEquals(
            1,
            railResponse.cardVisualEmphasisState!!.cardStates.count {
                it.emphasisState == ChallengeCardEmphasisState.ACTIVE
            },
        )
        assertEquals(
            listOf(ChallengeCardVisibleFace.FRONT),
            railResponse.cardVisualEmphasisState!!.cardStates
                .filter { it.emphasisState == ChallengeCardEmphasisState.ADJACENT }
                .map { it.visibleFace }
                .distinct(),
        )
    }

    @Test
    fun railModuleFailsWhenCardContentIsMissing() {
        val module = LTWChallengeCardRailPresenter()
        val invalidCard = card(day = 10, detailDescription = "", suggestions = emptyList())

        val response = module.presentCardRail(
            ChallengeCardRailInput(
                challengeCardList = listOf(invalidCard),
                cardPresentationStates = emptyList(),
                currentActiveCardIdentifier = invalidCard.cardIdentifier,
                browseRequest = null,
            ),
        )

        assertEquals(
            ChallengeCardRailFailureReason.REQUIRED_CARD_CONTENT_MISSING,
            response.failureResponse?.reason,
        )
        assertTrue(response.challengeCardRail.isEmpty())
    }

    @Test
    fun faceModuleAllowsOnlyActiveCardToFlipAndKeepsBackFaceReadingInPlace() {
        val module = LTWChallengeCardFacePresenter()
        val cards = listOf(card(10), card(11))

        val faceResponse = module.resolveCardFace(
            ChallengeCardFaceInput(
                challengeCardList = cards,
                currentActiveCardIdentifier = "card-10",
                cardFaceRequest = ChallengeCardFaceRequest(requestedFace = ChallengeCardVisibleFace.BACK),
                activeCardRequest = null,
                backFaceReadingRequest = null,
            ),
        )

        assertNull(faceResponse.failureResponse)
        assertEquals("card-10", faceResponse.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardVisibleFace.BACK, faceResponse.cardFaceState?.visibleFace)
        assertEquals(ChallengeCardMotionPhase.FACE_CHANGING, faceResponse.motionState?.phase)

        val readingResponse = module.resolveCardFace(
            ChallengeCardFaceInput(
                challengeCardList = cards,
                currentActiveCardIdentifier = "card-10",
                cardFaceRequest = null,
                activeCardRequest = null,
                backFaceReadingRequest = ChallengeCardBackFaceReadingRequest(cardIdentifier = "card-10"),
            ),
        )

        assertNull(readingResponse.failureResponse)
        assertEquals("card-10", readingResponse.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardVisibleFace.BACK, readingResponse.cardFaceState?.visibleFace)
        assertEquals(ChallengeCardMotionPhase.VERTICAL_READING, readingResponse.motionState?.phase)
    }

    @Test
    fun faceModuleReturnsFrontWhenDifferentCardBecomesActive() {
        val module = LTWChallengeCardFacePresenter()
        val cards = listOf(card(10), card(11))

        val response = module.resolveCardFace(
            ChallengeCardFaceInput(
                challengeCardList = cards,
                currentActiveCardIdentifier = "card-10",
                cardFaceRequest = null,
                activeCardRequest = ChallengeCardActiveCardRequest(cardIdentifier = "card-11"),
                backFaceReadingRequest = null,
            ),
        )

        assertNull(response.failureResponse)
        assertEquals("card-11", response.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardVisibleFace.FRONT, response.cardFaceState?.visibleFace)
        assertEquals(ChallengeCardMotionPhase.CENTERED, response.motionState?.phase)
    }

    @Test
    fun faceModuleFailsWhenRequiredActiveCardContentIsMissing() {
        val module = LTWChallengeCardFacePresenter()
        val invalidCard = card(day = 10, detailDescription = "", suggestions = emptyList())

        val response = module.resolveCardFace(
            ChallengeCardFaceInput(
                challengeCardList = listOf(invalidCard),
                currentActiveCardIdentifier = invalidCard.cardIdentifier,
                cardFaceRequest = ChallengeCardFaceRequest(requestedFace = ChallengeCardVisibleFace.BACK),
                activeCardRequest = null,
                backFaceReadingRequest = null,
            ),
        )

        assertEquals(
            ChallengeCardFaceFailureReason.REQUIRED_CARD_CONTENT_MISSING,
            response.failureResponse?.reason,
        )
    }

    @Test
    fun faceModuleFailsWhenNoActiveCardExistsForFaceChange() {
        val module = LTWChallengeCardFacePresenter()

        val response = module.resolveCardFace(
            ChallengeCardFaceInput(
                challengeCardList = listOf(card(10)),
                currentActiveCardIdentifier = null,
                cardFaceRequest = ChallengeCardFaceRequest(requestedFace = ChallengeCardVisibleFace.BACK),
                activeCardRequest = null,
                backFaceReadingRequest = null,
            ),
        )

        assertEquals(ChallengeCardFaceFailureReason.NO_ACTIVE_CARD, response.failureResponse?.reason)
    }

    @Test
    fun accessibilityModuleSupportsBrowseFaceChangeAndLongContentReading() {
        val module = LTWChallengeCardAccessibilityPresenter()
        val cards = listOf(card(10), card(11), card(12))

        val browseResponse = module.resolveAccessibilityPresentation(
            ChallengeCardAccessibilityInput(
                challengeCardList = cards,
                currentActiveCardIdentifier = "card-11",
                cardFaceState = ChallengeCardFaceState(
                    cardIdentifier = "card-11",
                    visibleFace = ChallengeCardVisibleFace.FRONT,
                ),
                accessibilityBrowseRequest = ChallengeCardAccessibilityBrowseRequest(
                    direction = ChallengeCardBrowseDirection.NEXT,
                ),
                accessibilityFaceRequest = null,
                backFaceReadingRequest = null,
            ),
        )

        assertNull(browseResponse.failureResponse)
        assertEquals("card-12", browseResponse.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardVisibleFace.FRONT, browseResponse.cardFaceState?.visibleFace)
        assertTrue(browseResponse.accessibilityPresentationState!!.availableActions.contains(BROWSE_PREVIOUS))
        assertFalse(browseResponse.accessibilityPresentationState!!.availableActions.contains(BROWSE_NEXT))
        assertTrue(browseResponse.accessibilityPresentationState!!.availableActions.contains(SHOW_BACK))

        val faceResponse = module.resolveAccessibilityPresentation(
            ChallengeCardAccessibilityInput(
                challengeCardList = cards,
                currentActiveCardIdentifier = "card-12",
                cardFaceState = ChallengeCardFaceState(
                    cardIdentifier = "card-12",
                    visibleFace = ChallengeCardVisibleFace.FRONT,
                ),
                accessibilityBrowseRequest = null,
                accessibilityFaceRequest = ChallengeCardAccessibilityFaceRequest(
                    requestedFace = ChallengeCardVisibleFace.BACK,
                ),
                backFaceReadingRequest = null,
            ),
        )

        assertNull(faceResponse.failureResponse)
        assertEquals(ChallengeCardVisibleFace.BACK, faceResponse.cardFaceState?.visibleFace)
        assertTrue(faceResponse.accessibilityPresentationState!!.availableActions.contains(SHOW_FRONT))
        assertFalse(faceResponse.accessibilityPresentationState!!.availableActions.contains(SHOW_BACK))

        val readingResponse = module.resolveAccessibilityPresentation(
            ChallengeCardAccessibilityInput(
                challengeCardList = cards,
                currentActiveCardIdentifier = "card-12",
                cardFaceState = ChallengeCardFaceState(
                    cardIdentifier = "card-12",
                    visibleFace = ChallengeCardVisibleFace.BACK,
                ),
                accessibilityBrowseRequest = null,
                accessibilityFaceRequest = null,
                backFaceReadingRequest = ChallengeCardBackFaceReadingRequest(cardIdentifier = "card-12"),
            ),
        )

        assertNull(readingResponse.failureResponse)
        assertEquals("card-12", readingResponse.activeCardState?.cardIdentifier)
        assertEquals(ChallengeCardVisibleFace.BACK, readingResponse.cardFaceState?.visibleFace)
    }

    @Test
    fun accessibilityModuleFailsWhenAccessibleStateCannotBeExposed() {
        val module = LTWChallengeCardAccessibilityPresenter()

        val response = module.resolveAccessibilityPresentation(
            ChallengeCardAccessibilityInput(
                challengeCardList = listOf(card(10)),
                currentActiveCardIdentifier = "card-10",
                cardFaceState = null,
                accessibilityBrowseRequest = null,
                accessibilityFaceRequest = null,
                backFaceReadingRequest = null,
            ),
        )

        assertEquals(
            ChallengeCardAccessibilityFailureReason.ACCESSIBILITY_STATE_UNAVAILABLE,
            response.failureResponse?.reason,
        )
    }

    private fun card(
        day: Int,
        identifier: String = "card-$day",
        shortSummary: String = "Summary $day",
        detailDescription: String = "Detail description for challenge $day.",
        suggestions: List<String> = listOf("Suggestion $day"),
        completionState: ChallengeCardCompletionPresentationState = ChallengeCardCompletionPresentationState.INCOMPLETE,
        futureState: ChallengeCardFuturePresentationState = ChallengeCardFuturePresentationState.NON_FUTURE,
    ): PresentedChallengeCard = PresentedChallengeCard(
        cardIdentifier = identifier,
        challengeDate = LocalDate.of(2026, 12, day),
        shortSummary = shortSummary,
        detailDescription = detailDescription,
        suggestions = suggestions,
        completionState = completionState,
        futureState = futureState,
    )
}
