package com.n8vd3v.lighttheworld.features.dailychallenge.reminder

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeProgress
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallenge
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
    ): ChallengeReminderResponse =
        evaluateReminderSchedule(
            ChallengeReminderEvaluationInput(
                reminderPreference = reminderPreference,
                currentLocalDate = currentLocalDate,
                currentLocalTime = currentLocalTime,
                currentDayChallenge = null,
                completionState = completionState,
            ),
        )

    fun evaluateReminderSchedule(
        input: ChallengeReminderEvaluationInput,
    ): ChallengeReminderResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "evaluate_reminder_schedule_input",
            details = mapOf(
                "remindersEnabled" to input.reminderPreference.remindersEnabled,
                "notificationPermissionGranted" to input.reminderPreference.notificationPermissionGranted,
                "campaignWindow" to input.campaignWindow,
                "currentLocalDate" to input.currentLocalDate,
                "currentLocalTime" to input.currentLocalTime,
                "currentDayChallengeDate" to input.currentDayChallenge?.date,
                "completionStatus" to input.completionState.status,
            ),
        )

        if (!input.campaignWindow.includes(input.currentLocalDate)) {
            return buildResponse(
                challengeDate = input.currentLocalDate,
                currentLocalTime = input.currentLocalTime,
                earlyStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                laterStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                failureResponse = null,
                decision = "reminders_not_scheduled_out_of_window",
            )
        }

        if (!hasValidatedCurrentDayChallenge(
                currentLocalDate = input.currentLocalDate,
                currentDayChallenge = input.currentDayChallenge,
            )
        ) {
            return buildResponse(
                challengeDate = input.currentLocalDate,
                currentLocalTime = input.currentLocalTime,
                earlyStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                laterStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                failureResponse = CopFailureResponse(
                    reason = ChallengeReminderFailureReason.CHALLENGE_CONTENT_UNAVAILABLE,
                    message = "Current-day challenge content could not be validated for reminder scheduling.",
                ),
                decision = "reminders_not_scheduled_challenge_content_unavailable",
            )
        }

        if (!input.reminderPreference.remindersEnabled) {
            return buildResponse(
                challengeDate = input.currentLocalDate,
                currentLocalTime = input.currentLocalTime,
                earlyStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                laterStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                failureResponse = CopFailureResponse(
                    reason = ChallengeReminderFailureReason.REMINDERS_DISABLED,
                    message = "Challenge reminders are disabled.",
                ),
                decision = "reminders_not_scheduled_disabled",
            )
        }

        if (!input.reminderPreference.notificationPermissionGranted) {
            return buildResponse(
                challengeDate = input.currentLocalDate,
                currentLocalTime = input.currentLocalTime,
                earlyStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                laterStatus = ReminderScheduleStatus.NOT_SCHEDULED,
                failureResponse = CopFailureResponse(
                    reason = ChallengeReminderFailureReason.NOTIFICATION_PERMISSION_REQUIRED,
                    message = "Notification permission is required before reminders can be scheduled.",
                ),
                decision = "reminders_not_scheduled_permission_missing",
            )
        }

        val earlyStatus =
            if (input.currentLocalTime.isBefore(EARLY_REMINDER_TIME)) {
                ReminderScheduleStatus.SCHEDULED
            } else {
                ReminderScheduleStatus.NOT_SCHEDULED
            }

        val laterStatus =
            if (input.currentLocalTime.isBefore(LATER_REMINDER_TIME) &&
                input.completionState.status != ChallengeCompletionStatus.COMPLETED
            ) {
                ReminderScheduleStatus.SCHEDULED
            } else {
                ReminderScheduleStatus.NOT_SCHEDULED
            }

        return buildResponse(
            challengeDate = input.currentLocalDate,
            currentLocalTime = input.currentLocalTime,
            earlyStatus = earlyStatus,
            laterStatus = laterStatus,
            failureResponse = null,
            decision = if (laterStatus == ReminderScheduleStatus.SCHEDULED) {
                "reminder_schedule_incomplete_before_six_pm"
            } else {
                "reminder_schedule_not_scheduled_by_time_or_completion"
            },
        )
    }

    private fun hasValidatedCurrentDayChallenge(
        currentLocalDate: LocalDate,
        currentDayChallenge: DailyChallenge?,
    ): Boolean = currentDayChallenge?.date == currentLocalDate

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
