package com.n8vd3v.lighttheworld.features.dailychallenge.reminder

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeProgress
import java.time.LocalDate
import java.time.LocalTime

class ChallengeReminderModule(
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) : ChallengeReminderProtocol {

    override fun evaluateReminderSchedule(
        reminderPreference: ReminderPreference,
        currentLocalDate: LocalDate,
        currentLocalTime: LocalTime,
        completionState: ChallengeProgress,
    ): ChallengeReminderResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "evaluate_reminder_schedule_input",
            details = mapOf(
                "remindersEnabled" to reminderPreference.remindersEnabled,
                "notificationPermissionGranted" to reminderPreference.notificationPermissionGranted,
                "currentLocalDate" to currentLocalDate,
                "currentLocalTime" to currentLocalTime,
                "completionStatus" to completionState.status,
            ),
        )

        if (!reminderPreference.remindersEnabled) {
            return buildResponse(
                challengeDate = currentLocalDate,
                currentLocalTime = currentLocalTime,
                earlyStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                laterStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                failureResponse = CopFailureResponse(
                    reason = ChallengeReminderFailureReason.REMINDERS_DISABLED,
                    message = "Challenge reminders are disabled.",
                ),
                decision = "reminders_not_scheduled_disabled",
            )
        }

        if (!reminderPreference.notificationPermissionGranted) {
            return buildResponse(
                challengeDate = currentLocalDate,
                currentLocalTime = currentLocalTime,
                earlyStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                laterStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                failureResponse = CopFailureResponse(
                    reason = ChallengeReminderFailureReason.NOTIFICATION_PERMISSION_REQUIRED,
                    message = "Notification permission is required before reminders can be scheduled.",
                ),
                decision = "reminders_not_scheduled_permission_missing",
            )
        }

        val laterStatus =
            if (completionState.status == ChallengeCompletionStatus.COMPLETED) {
                ReminderScheduleStatus.SUPPRESSED
            } else {
                ReminderScheduleStatus.SCHEDULED
            }

        return buildResponse(
            challengeDate = currentLocalDate,
            currentLocalTime = currentLocalTime,
            earlyStatus = ReminderScheduleStatus.SCHEDULED,
            laterStatus = laterStatus,
            failureResponse = null,
            decision = if (laterStatus == ReminderScheduleStatus.SUPPRESSED) {
                "later_reminder_suppressed_completed"
            } else {
                "both_reminders_scheduled"
            },
        )
    }

    private fun buildResponse(
        challengeDate: LocalDate,
        currentLocalTime: LocalTime,
        earlyStatus: ReminderScheduleStatus,
        laterStatus: ReminderScheduleStatus,
        failureResponse: CopFailureResponse<ChallengeReminderFailureReason>?,
        decision: String,
    ): ChallengeReminderResponse {
        val response = ChallengeReminderResponse(
            reminderScheduleState = ReminderScheduleState(
                challengeDate = challengeDate,
                evaluatedAtLocalTime = currentLocalTime,
                earlyReminder = ReminderDecision(
                    localTime = EARLY_REMINDER_TIME,
                    status = earlyStatus,
                ),
                laterReminder = ReminderDecision(
                    localTime = LATER_REMINDER_TIME,
                    status = laterStatus,
                ),
            ),
            failureResponse = failureResponse,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "evaluate_reminder_schedule_output",
            details = mapOf(
                "decision" to decision,
                "earlyReminderStatus" to earlyStatus,
                "laterReminderStatus" to laterStatus,
                "failureReason" to failureResponse?.reason,
            ),
        )
        return response
    }

    companion object {
        private const val MODULE_NAME = "ChallengeReminderModule"
        private val EARLY_REMINDER_TIME: LocalTime = LocalTime.of(10, 0)
        private val LATER_REMINDER_TIME: LocalTime = LocalTime.of(18, 0)
    }
}
