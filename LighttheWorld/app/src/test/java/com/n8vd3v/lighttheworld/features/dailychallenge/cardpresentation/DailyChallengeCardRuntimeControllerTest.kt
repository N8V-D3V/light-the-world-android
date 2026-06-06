package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeFlow
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.StubChallengeCalendar
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.StubChallengeCompletionTracker
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.StubChallengeReminderScheduler
import com.n8vd3v.lighttheworld.features.dailychallenge.share.SharePayload
import com.n8vd3v.lighttheworld.features.dailychallenge.share.StubChallengeShareComposer
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChallengeCardRuntimeControllerTest {

    @Test
    fun completionActionAvailabilityTracksEligibilityAndCompletionState() {
        val controller = controller(currentLocalDate = LocalDate.of(2026, 12, 10))

        val todayActions = controller.actionStateFor(LocalDate.of(2026, 12, 10))
        val futureActions = controller.actionStateFor(LocalDate.of(2026, 12, 25))

        assertTrue(todayActions.canMarkComplete)
        assertFalse(todayActions.canRequestReversal)
        assertFalse(todayActions.canShare)
        assertFalse(futureActions.canMarkComplete)
        assertFalse(futureActions.canRequestReversal)
        assertFalse(futureActions.canShare)
    }

    @Test
    fun confirmingReversalChangesCompletedChallengeBackToIncomplete() {
        val challengeDate = LocalDate.of(2026, 12, 10)
        val controller = controller(currentLocalDate = challengeDate)

        controller.markComplete(challengeDate)
        controller.requestCompletionReversal(challengeDate)

        assertNotNull(controller.uiState.pendingReversalPrompt)

        controller.confirmCompletionReversal()

        assertNull(controller.uiState.pendingReversalPrompt)
        assertTrue(controller.actionStateFor(challengeDate).canMarkComplete)
        assertFalse(controller.actionStateFor(challengeDate).canRequestReversal)
        assertFalse(controller.actionStateFor(challengeDate).canShare)
        assertEquals(
            ChallengeCompletionStatus.INCOMPLETE,
            controller.uiState.browseResponse.challengeCards
                .first { it.challenge.date == challengeDate }
                .completionState
                .status,
        )
    }

    @Test
    fun dismissingReversalPromptPreservesCompletedState() {
        val challengeDate = LocalDate.of(2026, 12, 10)
        val controller = controller(currentLocalDate = challengeDate)

        controller.markComplete(challengeDate)
        controller.requestCompletionReversal(challengeDate)
        controller.dismissCompletionReversalPrompt()

        assertNull(controller.uiState.pendingReversalPrompt)
        assertFalse(controller.actionStateFor(challengeDate).canMarkComplete)
        assertTrue(controller.actionStateFor(challengeDate).canRequestReversal)
        assertTrue(controller.actionStateFor(challengeDate).canShare)
    }

    @Test
    fun shareUsesExistingPayloadBuilderOnlyForCompletedChallenges() {
        val challengeDate = LocalDate.of(2026, 12, 10)
        val controller = controller(currentLocalDate = challengeDate)
        var launchedPayload: SharePayload? = null

        controller.shareCompletedChallenge(challengeDate) { launchedPayload = it }

        assertNull(launchedPayload)
        assertEquals(
            "Challenge 2026-12-10 must be completed before it can be shared.",
            controller.uiState.latestFailureMessage,
        )

        controller.markComplete(challengeDate)
        controller.shareCompletedChallenge(challengeDate) { launchedPayload = it }

        assertNotNull(launchedPayload)
        assertTrue(launchedPayload!!.completionMessage.contains(launchedPayload!!.challengeSummary))
        assertNull(controller.uiState.latestFailureMessage)
    }

    @Test
    fun completionFailureIsSurfacedForFutureChallenge() {
        val controller = controller(currentLocalDate = LocalDate.of(2026, 12, 10))

        controller.markComplete(LocalDate.of(2026, 12, 25))

        assertEquals(
            "Challenge completion is not allowed before 2026-12-25.",
            controller.uiState.latestFailureMessage,
        )
        assertFalse(controller.actionStateFor(LocalDate.of(2026, 12, 25)).canShare)
    }

    private fun controller(
        currentLocalDate: LocalDate,
    ): DailyChallengeCardRuntimeController = DailyChallengeCardRuntimeController(
        flow = DailyServiceChallengeFlow(
            challengeCalendar = StubChallengeCalendar(),
            challengeCompletionTracker = StubChallengeCompletionTracker(
                timestampProvider = { Instant.parse("2026-12-10T18:00:00Z") },
            ),
            challengeReminderScheduler = StubChallengeReminderScheduler(),
            challengeShareComposer = StubChallengeShareComposer(),
        ),
        currentLocalDate = currentLocalDate,
        campaignWindow = CampaignWindow.LightTheWorldAnnual,
        appLink = "https://www.lighttheworld.org/",
    )
}
