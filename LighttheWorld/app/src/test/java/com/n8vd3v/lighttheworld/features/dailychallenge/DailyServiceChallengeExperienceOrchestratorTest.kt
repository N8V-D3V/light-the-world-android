package com.n8vd3v.lighttheworld.features.dailychallenge

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarContentModule
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeDetailResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressModule
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressStateResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionEligibility
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalConfirmation
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalPromptResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderEvaluationInput
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderModule
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderPreference
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderScheduleState
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderScheduleStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareModule
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareProtocol
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyServiceChallengeExperienceOrchestratorTest {

    @Test
    fun browseChallengeCardsCombinesCalendarContentWithPerChallengeProgressState() {
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = ChallengeCalendarContentModule(),
            challengeProgressProtocol = ChallengeProgressModule(),
            challengeReminderProtocol = ChallengeReminderModule(),
            challengeShareProtocol = ChallengeShareModule(),
        )

        val response = orchestrator.browseChallengeCards(
            currentLocalDate = LocalDate.of(2026, 12, 10),
        )

        assertNull(response.calendarFailureResponse)
        assertEquals(25, response.challengeCards.size)
        assertEquals(LocalDate.of(2026, 12, 1), response.challengeCards.first().challenge.date)
        assertEquals(CompletionEligibility.ELIGIBLE, response.challengeCards.first().completionEligibility)
        assertEquals(LocalDate.of(2026, 12, 25), response.challengeCards.last().challenge.date)
        assertEquals(CompletionEligibility.NOT_ELIGIBLE, response.challengeCards.last().completionEligibility)
    }

    @Test
    fun markChallengeCompletedStopsAtCalendarFailureBeforeProgressMutation() {
        val progressProtocol = RecordingChallengeProgressProtocol()
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = ChallengeCalendarContentModule(
                contentSource = ChallengeCalendarContentModule.BundledChallengeContentSource(
                    templates = ChallengeCalendarContentModule.defaultTemplates().filterNot { it.dayOfMonth == 12 },
                ),
            ),
            challengeProgressProtocol = progressProtocol,
            challengeReminderProtocol = ChallengeReminderModule(),
            challengeShareProtocol = ChallengeShareModule(),
        )

        val response = orchestrator.markChallengeCompleted(
            currentLocalDate = LocalDate.of(2026, 12, 12),
            selectedChallengeDate = LocalDate.of(2026, 12, 12),
        )

        assertEquals(
            ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
            response.calendarFailureResponse?.reason,
        )
        assertEquals(0, progressProtocol.markCompletedCallCount)
    }

    @Test
    fun requestAndConfirmCompletionReversalStayOnProgressProtocolBoundary() {
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = ChallengeCalendarContentModule(),
            challengeProgressProtocol = ChallengeProgressModule(
                timestampProvider = { Instant.parse("2026-12-10T17:45:00Z") },
            ),
            challengeReminderProtocol = ChallengeReminderModule(),
            challengeShareProtocol = ChallengeShareModule(),
        )
        val challengeDate = LocalDate.of(2026, 12, 10)

        orchestrator.markChallengeCompleted(
            currentLocalDate = challengeDate,
            selectedChallengeDate = challengeDate,
        )
        val promptResponse = orchestrator.requestCompletionReversal(
            currentLocalDate = challengeDate,
            selectedChallengeDate = challengeDate,
        )
        val confirmResponse = orchestrator.confirmCompletionReversal(
            currentLocalDate = challengeDate,
            selectedChallengeDate = challengeDate,
            confirmation = CompletionReversalConfirmation.APPROVED,
        )

        assertNotNull(promptResponse.completionReversalPrompt)
        assertNull(promptResponse.progressFailureResponse)
        assertNull(confirmResponse.progressFailureResponse)
        assertEquals(ChallengeCompletionStatus.INCOMPLETE, confirmResponse.challengeViewState?.completionState?.status)
    }

    @Test
    fun evaluateReminderScheduleValidatesCurrentDayContentBeforeCallingReminderProtocolForInWindowDates() {
        val progressProtocol = RecordingChallengeProgressProtocol(
            stateResponse = ChallengeProgressStateResponse(
                completionState = ChallengeProgress(
                    challengeDate = LocalDate.of(2026, 12, 10),
                    status = ChallengeCompletionStatus.INCOMPLETE,
                    completedAt = null,
                ),
                completionEligibility = CompletionEligibility.ELIGIBLE,
            ),
        )
        val events = mutableListOf<String>()
        val challenge = DailyChallenge(
            date = LocalDate.of(2026, 12, 10),
            shortSummary = "Day 10 service invitation",
            detailDescription = "Detail",
            suggestions = listOf("Suggestion"),
        )
        val calendarProtocol = RecordingChallengeCalendarProtocol(
            challengeDetail = challenge,
            events = events,
        )
        val reminderProtocol = RecordingChallengeReminderProtocol(events = events)
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = calendarProtocol,
            challengeProgressProtocol = progressProtocol,
            challengeReminderProtocol = reminderProtocol,
            challengeShareProtocol = ChallengeShareModule(),
        )

        val response = orchestrator.evaluateReminderSchedule(
            reminderPreference = ReminderPreference(
                remindersEnabled = true,
                notificationPermissionGranted = true,
            ),
            currentLocalDate = LocalDate.of(2026, 12, 10),
            currentLocalTime = LocalTime.of(9, 15),
        )

        assertEquals(1, calendarProtocol.getChallengeDetailCallCount)
        assertEquals(LocalDate.of(2026, 12, 10), progressProtocol.lastChallengeDate)
        assertEquals(LocalDate.of(2026, 12, 10), progressProtocol.lastCurrentLocalDate)
        assertEquals(LocalDate.of(2026, 12, 10), calendarProtocol.lastSelectedChallengeDate)
        assertEquals(LocalDate.of(2026, 12, 10), reminderProtocol.lastCurrentLocalDate)
        assertEquals(LocalTime.of(9, 15), reminderProtocol.lastCurrentLocalTime)
        assertSame(challenge, reminderProtocol.lastInput?.currentDayChallenge)
        assertEquals(0, reminderProtocol.legacyShimCallCount)
        assertTrue(events.indexOf("calendar:getChallengeDetail") < events.indexOf("reminder:evaluateInput"))
        assertEquals(ReminderScheduleStatus.SCHEDULED, response.reminderScheduleState.earlyReminder.status)
    }

    @Test
    fun evaluateReminderScheduleSkipsChallengeContentLookupOutsideCampaignWindow() {
        val progressProtocol = RecordingChallengeProgressProtocol()
        val calendarProtocol = RecordingChallengeCalendarProtocol(
            challengeDetail = DailyChallenge(
                date = LocalDate.of(2026, 12, 10),
                shortSummary = "Day 10 service invitation",
                detailDescription = "Detail",
                suggestions = listOf("Suggestion"),
            ),
        )
        val reminderProtocol = RecordingChallengeReminderProtocol()
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = calendarProtocol,
            challengeProgressProtocol = progressProtocol,
            challengeReminderProtocol = reminderProtocol,
            challengeShareProtocol = ChallengeShareModule(),
        )

        val response = orchestrator.evaluateReminderSchedule(
            reminderPreference = ReminderPreference(
                remindersEnabled = true,
                notificationPermissionGranted = true,
            ),
            currentLocalDate = LocalDate.of(2026, 11, 30),
            currentLocalTime = LocalTime.of(9, 15),
        )

        assertEquals(0, calendarProtocol.getChallengeDetailCallCount)
        assertEquals(LocalDate.of(2026, 11, 30), reminderProtocol.lastInput?.currentLocalDate)
        assertNull(reminderProtocol.lastInput?.currentDayChallenge)
        assertEquals(0, reminderProtocol.legacyShimCallCount)
        assertNull(response.calendarFailureResponse)
        assertEquals(ReminderScheduleStatus.NOT_SCHEDULED, response.reminderScheduleState.earlyReminder.status)
        assertEquals(ReminderScheduleStatus.NOT_SCHEDULED, response.reminderScheduleState.laterReminder.status)
    }

    @Test
    fun evaluateReminderScheduleReturnsExplicitFailureWhenInWindowChallengeContentCannotBeValidated() {
        val progressProtocol = RecordingChallengeProgressProtocol()
        val calendarProtocol = RecordingChallengeCalendarProtocol(
            challengeDetail = null,
            failureResponse = CopFailureResponse(
                reason = ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
                message = "Missing challenge content.",
            ),
        )
        val reminderProtocol = RecordingChallengeReminderProtocol()
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = calendarProtocol,
            challengeProgressProtocol = progressProtocol,
            challengeReminderProtocol = reminderProtocol,
            challengeShareProtocol = ChallengeShareModule(),
        )

        val response = orchestrator.evaluateReminderSchedule(
            reminderPreference = ReminderPreference(
                remindersEnabled = true,
                notificationPermissionGranted = true,
            ),
            currentLocalDate = LocalDate.of(2026, 12, 10),
            currentLocalTime = LocalTime.of(9, 15),
        )

        assertEquals(1, calendarProtocol.getChallengeDetailCallCount)
        assertEquals(0, reminderProtocol.inputCallCount)
        assertEquals(
            ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
            response.calendarFailureResponse?.reason,
        )
        assertNull(response.reminderFailureResponse)
        assertEquals(ReminderScheduleStatus.NOT_SCHEDULED, response.reminderScheduleState.earlyReminder.status)
        assertEquals(ReminderScheduleStatus.NOT_SCHEDULED, response.reminderScheduleState.laterReminder.status)
    }

    @Test
    fun buildSharePayloadUsesCalendarSummaryAfterCompletionHasBeenRecorded() {
        val orchestrator = DailyServiceChallengeExperienceOrchestrator(
            challengeCalendarProtocol = ChallengeCalendarContentModule(),
            challengeProgressProtocol = ChallengeProgressModule(
                timestampProvider = { Instant.parse("2026-12-08T18:00:00Z") },
            ),
            challengeReminderProtocol = ChallengeReminderModule(),
            challengeShareProtocol = ChallengeShareModule(),
        )
        val challengeDate = LocalDate.of(2026, 12, 8)

        orchestrator.markChallengeCompleted(
            currentLocalDate = challengeDate,
            selectedChallengeDate = challengeDate,
        )
        val shareResponse = orchestrator.buildSharePayload(
            currentLocalDate = challengeDate,
            selectedChallengeDate = challengeDate,
            appLink = "https://example.com/app",
        )

        assertNull(shareResponse.calendarFailureResponse)
        assertNull(shareResponse.shareFailureResponse)
        assertTrue(
            shareResponse.sharePayload?.completionMessage?.contains("Day 8 service invitation") == true,
        )
    }

    private class RecordingChallengeProgressProtocol(
        private val stateResponse: ChallengeProgressStateResponse = ChallengeProgressStateResponse(
            completionState = ChallengeProgress(
                challengeDate = LocalDate.of(2026, 12, 10),
                status = ChallengeCompletionStatus.INCOMPLETE,
                completedAt = null,
            ),
            completionEligibility = CompletionEligibility.ELIGIBLE,
        ),
    ) : ChallengeProgressProtocol {
        var lastChallengeDate: LocalDate? = null
        var lastCurrentLocalDate: LocalDate? = null
        var markCompletedCallCount: Int = 0

        override fun getCompletionState(
            challengeDate: LocalDate,
            currentLocalDate: LocalDate,
        ): ChallengeProgressStateResponse {
            lastChallengeDate = challengeDate
            lastCurrentLocalDate = currentLocalDate
            return stateResponse.copy(
                completionState = stateResponse.completionState.copy(challengeDate = challengeDate),
            )
        }

        override fun markChallengeCompleted(
            challengeDate: LocalDate,
            currentLocalDate: LocalDate,
        ): ChallengeProgressStateResponse {
            markCompletedCallCount += 1
            return stateResponse.copy(
                completionState = stateResponse.completionState.copy(
                    challengeDate = challengeDate,
                    status = ChallengeCompletionStatus.COMPLETED,
                    completedAt = Instant.parse("2026-12-12T18:00:00Z"),
                ),
            )
        }

        override fun requestCompletionReversal(challengeDate: LocalDate): CompletionReversalPromptResponse {
            return CompletionReversalPromptResponse(
                completionReversalPrompt = null,
                failureResponse = CopFailureResponse(
                    reason = ChallengeProgressFailureReason.CHALLENGE_NOT_COMPLETED,
                    message = "Not completed.",
                ),
            )
        }

        override fun confirmCompletionReversal(
            challengeDate: LocalDate,
            confirmation: CompletionReversalConfirmation,
            currentLocalDate: LocalDate,
        ): ChallengeProgressStateResponse = stateResponse
    }

    private class RecordingChallengeCalendarProtocol(
        private val challengeDetail: DailyChallenge?,
        private val failureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
        private val events: MutableList<String>? = null,
    ) : ChallengeCalendarProtocol {
        var getChallengeDetailCallCount: Int = 0
        var lastSelectedChallengeDate: LocalDate? = null

        override fun getChallengeCardList(
            currentLocalDate: LocalDate,
            campaignWindow: CampaignWindow,
        ) = error("Card list is not used in this test.")

        override fun getChallengeDetail(
            currentLocalDate: LocalDate,
            campaignWindow: CampaignWindow,
            selectedChallengeDate: LocalDate,
        ): ChallengeDetailResponse {
            getChallengeDetailCallCount += 1
            lastSelectedChallengeDate = selectedChallengeDate
            events?.add("calendar:getChallengeDetail")
            return ChallengeDetailResponse(
                challengeDetail = challengeDetail,
                failureResponse = failureResponse,
            )
        }
    }

    private class RecordingChallengeReminderProtocol(
        private val events: MutableList<String>? = null,
    ) : ChallengeReminderProtocol {
        var inputCallCount: Int = 0
        var legacyShimCallCount: Int = 0
        var lastInput: ChallengeReminderEvaluationInput? = null
        var lastCurrentLocalDate: LocalDate? = null
        var lastCurrentLocalTime: LocalTime? = null

        override fun evaluateReminderSchedule(
            input: ChallengeReminderEvaluationInput,
        ): ChallengeReminderResponse {
            inputCallCount += 1
            lastInput = input
            lastCurrentLocalDate = input.currentLocalDate
            lastCurrentLocalTime = input.currentLocalTime
            events?.add("reminder:evaluateInput")
            return ChallengeReminderResponse(
                reminderScheduleState = ReminderScheduleState(
                    challengeDate = input.currentLocalDate,
                    evaluatedAtLocalTime = input.currentLocalTime,
                    earlyReminder = ReminderDecision(
                        localTime = LocalTime.of(10, 0),
                        status = if (!input.campaignWindow.includes(input.currentLocalDate)) {
                            ReminderScheduleStatus.NOT_SCHEDULED
                        } else if (input.currentLocalTime.isBefore(LocalTime.of(10, 0))) {
                            ReminderScheduleStatus.SCHEDULED
                        } else {
                            ReminderScheduleStatus.NOT_SCHEDULED
                        },
                    ),
                    laterReminder = ReminderDecision(
                        localTime = LocalTime.of(18, 0),
                        status = if (!input.campaignWindow.includes(input.currentLocalDate)) {
                            ReminderScheduleStatus.NOT_SCHEDULED
                        } else if (
                            input.currentLocalTime.isBefore(LocalTime.of(18, 0)) &&
                            input.completionState.status != ChallengeCompletionStatus.COMPLETED
                        ) {
                            ReminderScheduleStatus.SCHEDULED
                        } else {
                            ReminderScheduleStatus.NOT_SCHEDULED
                        },
                    ),
                ),
                failureResponse = null,
            )
        }

        @Deprecated("Test-only guard against shim usage.")
        @Suppress("DEPRECATION")
        override fun evaluateReminderSchedule(
            reminderPreference: ReminderPreference,
            currentLocalDate: LocalDate,
            currentLocalTime: LocalTime,
            completionState: ChallengeProgress,
        ): ChallengeReminderResponse {
            legacyShimCallCount += 1
            error("Deprecated reminder shim should not be used by the orchestrator.")
        }
    }
}
