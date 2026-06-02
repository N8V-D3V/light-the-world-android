package com.n8vd3v.lighttheworld.features.dailychallenge

import com.n8vd3v.lighttheworld.cop.InMemoryStubModuleLogger
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarContentModule
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressModule
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalConfirmation
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderModule
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderPreference
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderScheduleStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareModule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyChallengeModulesTest {

    @Test
    fun calendarModuleReturnsOrderedCardsAndAllowsFutureDetailBrowsing() {
        val logger = InMemoryStubModuleLogger()
        val module = ChallengeCalendarContentModule(logger = logger)

        val cardsResponse = module.getChallengeCardList(currentLocalDate = LocalDate.of(2026, 12, 5))
        val detailResponse = module.getChallengeDetail(
            currentLocalDate = LocalDate.of(2026, 12, 5),
            selectedChallengeDate = LocalDate.of(2026, 12, 24),
        )

        assertNull(cardsResponse.failureResponse)
        assertEquals(25, cardsResponse.challengeCardList.size)
        assertEquals(LocalDate.of(2026, 12, 1), cardsResponse.challengeCardList.first().date)
        assertEquals(LocalDate.of(2026, 12, 25), cardsResponse.challengeCardList.last().date)
        assertNull(detailResponse.failureResponse)
        assertEquals(LocalDate.of(2026, 12, 24), detailResponse.challengeDetail?.date)
        assertTrue(logger.entries.isNotEmpty())
    }

    @Test
    fun calendarModuleReturnsExplicitFailureWhenRequestedDateIsMissing() {
        val templatesWithoutDayTwelve = ChallengeCalendarContentModule
            .defaultTemplates()
            .filterNot { it.dayOfMonth == 12 }
        val module = ChallengeCalendarContentModule(
            contentSource = ChallengeCalendarContentModule.BundledChallengeContentSource(
                templates = templatesWithoutDayTwelve,
            ),
        )

        val response = module.getChallengeDetail(
            currentLocalDate = LocalDate.of(2026, 12, 5),
            selectedChallengeDate = LocalDate.of(2026, 12, 12),
        )

        assertNull(response.challengeDetail)
        assertEquals(
            ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
            response.failureResponse?.reason,
        )
    }

    @Test
    fun calendarModuleFailsCardListWhenCampaignWindowContentIsIncomplete() {
        val module = ChallengeCalendarContentModule(
            contentSource = ChallengeCalendarContentModule.BundledChallengeContentSource(
                templates = ChallengeCalendarContentModule.defaultTemplates().dropLast(1),
            ),
        )

        val response = module.getChallengeCardList(currentLocalDate = LocalDate.of(2026, 12, 5))

        assertEquals(0, response.challengeCardList.size)
        assertEquals(
            ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
            response.failureResponse?.reason,
        )
    }

    @Test
    fun progressModuleUsesLocalDateForEligibilityAndRequiresConfirmedReversal() {
        val module = ChallengeProgressModule(
            timestampProvider = { Instant.parse("2026-12-10T18:00:00Z") },
        )
        val challengeDate = LocalDate.of(2026, 12, 10)

        val earlyAttempt = module.markChallengeCompleted(
            challengeDate = challengeDate,
            currentLocalDate = LocalDate.of(2026, 12, 9),
        )
        assertEquals(
            ChallengeProgressFailureReason.COMPLETION_NOT_YET_ALLOWED,
            earlyAttempt.failureResponse?.reason,
        )
        assertEquals(ChallengeCompletionStatus.INCOMPLETE, earlyAttempt.completionState.status)

        val completed = module.markChallengeCompleted(
            challengeDate = challengeDate,
            currentLocalDate = challengeDate,
        )
        assertNull(completed.failureResponse)
        assertEquals(ChallengeCompletionStatus.COMPLETED, completed.completionState.status)
        assertNotNull(module.requestCompletionReversal(challengeDate).completionReversalPrompt)

        val declinedReversal = module.confirmCompletionReversal(
            challengeDate = challengeDate,
            confirmation = CompletionReversalConfirmation.DECLINED,
            currentLocalDate = challengeDate,
        )
        assertEquals(
            ChallengeProgressFailureReason.REVERSAL_NOT_CONFIRMED,
            declinedReversal.failureResponse?.reason,
        )
        assertEquals(ChallengeCompletionStatus.COMPLETED, declinedReversal.completionState.status)

        val approvedReversal = module.confirmCompletionReversal(
            challengeDate = challengeDate,
            confirmation = CompletionReversalConfirmation.APPROVED,
            currentLocalDate = challengeDate,
        )
        assertNull(approvedReversal.failureResponse)
        assertEquals(ChallengeCompletionStatus.INCOMPLETE, approvedReversal.completionState.status)
    }

    @Test
    fun reminderModuleSchedulesTenAmAndConditionallySuppressesSixPm() {
        val module = ChallengeReminderModule()
        val reminderPreference = ReminderPreference(
            remindersEnabled = true,
            notificationPermissionGranted = true,
        )
        val incompleteProgress = ChallengeProgress(
            challengeDate = LocalDate.of(2026, 12, 10),
            status = ChallengeCompletionStatus.INCOMPLETE,
            completedAt = null,
        )
        val completedProgress = incompleteProgress.copy(
            status = ChallengeCompletionStatus.COMPLETED,
            completedAt = Instant.parse("2026-12-10T17:00:00Z"),
        )

        val incompleteResponse = module.evaluateReminderSchedule(
            reminderPreference = reminderPreference,
            currentLocalDate = LocalDate.of(2026, 12, 10),
            currentLocalTime = LocalTime.of(9, 30),
            completionState = incompleteProgress,
        )
        assertNull(incompleteResponse.failureResponse)
        assertEquals(
            ReminderScheduleStatus.SCHEDULED,
            incompleteResponse.reminderScheduleState.earlyReminder.status,
        )
        assertEquals(
            ReminderScheduleStatus.SCHEDULED,
            incompleteResponse.reminderScheduleState.laterReminder.status,
        )

        val completedResponse = module.evaluateReminderSchedule(
            reminderPreference = reminderPreference,
            currentLocalDate = LocalDate.of(2026, 12, 10),
            currentLocalTime = LocalTime.of(15, 0),
            completionState = completedProgress,
        )
        assertEquals(
            ReminderScheduleStatus.SUPPRESSED,
            completedResponse.reminderScheduleState.laterReminder.status,
        )

        val noPermissionResponse = module.evaluateReminderSchedule(
            reminderPreference = reminderPreference.copy(notificationPermissionGranted = false),
            currentLocalDate = LocalDate.of(2026, 12, 10),
            currentLocalTime = LocalTime.of(8, 0),
            completionState = incompleteProgress,
        )
        assertEquals(
            ChallengeReminderFailureReason.NOTIFICATION_PERMISSION_REQUIRED,
            noPermissionResponse.failureResponse?.reason,
        )
        assertEquals(
            ReminderScheduleStatus.NOT_SCHEDULED,
            noPermissionResponse.reminderScheduleState.earlyReminder.status,
        )
    }

    @Test
    fun shareModuleOnlyBuildsPayloadForCompletedChallenges() {
        val module = ChallengeShareModule()

        val rejected = module.buildSharePayload(
            challengeDate = LocalDate.of(2026, 12, 8),
            challengeSummary = "Visit a neighbor",
            completionState = ChallengeCompletionStatus.INCOMPLETE,
            appLink = "https://example.com/app",
        )
        assertEquals(
            ChallengeShareFailureReason.CHALLENGE_NOT_COMPLETED,
            rejected.failureResponse?.reason,
        )

        val accepted = module.buildSharePayload(
            challengeDate = LocalDate.of(2026, 12, 8),
            challengeSummary = "Visit a neighbor",
            completionState = ChallengeCompletionStatus.COMPLETED,
            appLink = "https://example.com/app",
        )
        assertNull(accepted.failureResponse)
        assertEquals(
            """I completed "Visit a neighbor" in Light the World today. Join me in sharing some light this Christmas: https://example.com/app""",
            accepted.sharePayload?.completionMessage,
        )
    }
}
