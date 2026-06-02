package com.n8vd3v.lighttheworld.features.dailychallenge.reminder

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeProgress
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallenge
import java.time.LocalDate
import java.time.LocalTime

enum class ChallengeReminderFailureReason {
    NOTIFICATION_PERMISSION_REQUIRED,
    REMINDERS_DISABLED,
    CHALLENGE_CONTENT_UNAVAILABLE,
}

enum class ReminderScheduleStatus {
    SCHEDULED,
    NOT_SCHEDULED,
    SUPPRESSED,
}

data class ReminderPreference(
    val remindersEnabled: Boolean,
    val notificationPermissionGranted: Boolean,
)

data class ReminderDecision(
    val localTime: LocalTime,
    val status: ReminderScheduleStatus,
)

data class ReminderScheduleState(
    val challengeDate: LocalDate,
    val evaluatedAtLocalTime: LocalTime,
    val earlyReminder: ReminderDecision,
    val laterReminder: ReminderDecision,
)

data class ChallengeReminderResponse(
    val reminderScheduleState: ReminderScheduleState,
    val failureResponse: CopFailureResponse<ChallengeReminderFailureReason>? = null,
)

interface ChallengeReminderProtocol {
    fun evaluateReminderSchedule(
        reminderPreference: ReminderPreference,
        currentLocalDate: LocalDate,
        currentLocalTime: LocalTime,
        completionState: ChallengeProgress,
    ): ChallengeReminderResponse
}

data class ChallengeReminderEvaluationInput(
    val reminderPreference: ReminderPreference,
    val campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    val currentLocalDate: LocalDate,
    val currentLocalTime: LocalTime,
    val currentDayChallenge: DailyChallenge?,
    val completionState: ChallengeProgress,
)
