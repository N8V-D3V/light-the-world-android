package com.n8vd3v.lighttheworld.features.dailychallenge

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionEligibility
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalConfirmation
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalPrompt
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderPreference
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ReminderScheduleState
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareFailureReason
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareProtocol
import com.n8vd3v.lighttheworld.features.dailychallenge.share.SharePayload
import java.time.LocalDate
import java.time.LocalTime

data class ChallengeExperienceState(
    val challenge: DailyChallenge,
    val completionState: ChallengeProgress,
    val completionEligibility: CompletionEligibility,
)

data class DailyChallengeBrowseExperienceResponse(
    val challengeCards: List<ChallengeExperienceState>,
    val calendarFailureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
)

data class DailyChallengeDetailExperienceResponse(
    val challengeViewState: ChallengeExperienceState?,
    val calendarFailureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
)

data class DailyChallengeProgressUpdateResponse(
    val challengeViewState: ChallengeExperienceState?,
    val calendarFailureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
    val progressFailureResponse: CopFailureResponse<ChallengeProgressFailureReason>? = null,
)

data class DailyChallengeReversalRequestResponse(
    val challengeViewState: ChallengeExperienceState?,
    val completionReversalPrompt: CompletionReversalPrompt?,
    val calendarFailureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
    val progressFailureResponse: CopFailureResponse<ChallengeProgressFailureReason>? = null,
)

data class DailyChallengeReminderEvaluationResponse(
    val completionState: ChallengeProgress,
    val completionEligibility: CompletionEligibility,
    val reminderScheduleState: ReminderScheduleState,
    val progressFailureResponse: CopFailureResponse<ChallengeProgressFailureReason>? = null,
    val reminderFailureResponse: CopFailureResponse<ChallengeReminderFailureReason>? = null,
)

data class DailyChallengeShareExperienceResponse(
    val challengeViewState: ChallengeExperienceState?,
    val sharePayload: SharePayload?,
    val calendarFailureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
    val shareFailureResponse: CopFailureResponse<ChallengeShareFailureReason>? = null,
)

class DailyServiceChallengeExperienceOrchestrator(
    private val challengeCalendarProtocol: ChallengeCalendarProtocol,
    private val challengeProgressProtocol: ChallengeProgressProtocol,
    private val challengeReminderProtocol: ChallengeReminderProtocol,
    private val challengeShareProtocol: ChallengeShareProtocol,
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) {

    fun browseChallengeCards(
        currentLocalDate: LocalDate,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): DailyChallengeBrowseExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "browse_challenge_cards_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "campaignWindow" to campaignWindow,
            ),
        )

        val cardListResponse = challengeCalendarProtocol.getChallengeCardList(
            currentLocalDate = currentLocalDate,
            campaignWindow = campaignWindow,
        )
        if (cardListResponse.failureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "browse_challenge_cards_output",
                details = mapOf(
                    "decision" to "card_browse_rejected_calendar_failure",
                    "failureReason" to cardListResponse.failureResponse.reason,
                ),
            )
            return DailyChallengeBrowseExperienceResponse(
                challengeCards = emptyList(),
                calendarFailureResponse = cardListResponse.failureResponse,
            )
        }

        val challengeCards = cardListResponse.challengeCardList.map { challenge ->
            loadChallengeExperienceState(challenge = challenge, currentLocalDate = currentLocalDate)
        }
        logger.logDecision(
            module = MODULE_NAME,
            action = "browse_challenge_cards_output",
            details = mapOf(
                "decision" to "card_browse_completed",
                "challengeCount" to challengeCards.size,
            ),
        )
        return DailyChallengeBrowseExperienceResponse(challengeCards = challengeCards)
    }

    fun viewChallengeDetail(
        currentLocalDate: LocalDate,
        selectedChallengeDate: LocalDate,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): DailyChallengeDetailExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "view_challenge_detail_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "selectedChallengeDate" to selectedChallengeDate,
                "campaignWindow" to campaignWindow,
            ),
        )

        val challengeDetailResult = loadChallengeDetailState(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = selectedChallengeDate,
            campaignWindow = campaignWindow,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "view_challenge_detail_output",
            details = mapOf(
                "decision" to if (challengeDetailResult.calendarFailureResponse == null) {
                    "detail_view_completed"
                } else {
                    "detail_view_rejected_calendar_failure"
                },
                "failureReason" to challengeDetailResult.calendarFailureResponse?.reason,
            ),
        )
        return DailyChallengeDetailExperienceResponse(
            challengeViewState = challengeDetailResult.challengeViewState,
            calendarFailureResponse = challengeDetailResult.calendarFailureResponse,
        )
    }

    fun markChallengeCompleted(
        currentLocalDate: LocalDate,
        selectedChallengeDate: LocalDate,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): DailyChallengeProgressUpdateResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "mark_challenge_completed_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "selectedChallengeDate" to selectedChallengeDate,
                "campaignWindow" to campaignWindow,
            ),
        )

        val challengeDetailResult = loadChallengeDetailState(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = selectedChallengeDate,
            campaignWindow = campaignWindow,
        )
        if (challengeDetailResult.calendarFailureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "mark_challenge_completed_output",
                details = mapOf(
                    "decision" to "completion_rejected_calendar_failure",
                    "failureReason" to challengeDetailResult.calendarFailureResponse.reason,
                ),
            )
            return DailyChallengeProgressUpdateResponse(
                challengeViewState = null,
                calendarFailureResponse = challengeDetailResult.calendarFailureResponse,
            )
        }

        val progressResponse = challengeProgressProtocol.markChallengeCompleted(
            challengeDate = selectedChallengeDate,
            currentLocalDate = currentLocalDate,
        )
        val challengeViewState = challengeDetailResult.challenge?.let { challenge ->
            challenge.toExperienceState(progressResponse)
        }
        logger.logDecision(
            module = MODULE_NAME,
            action = "mark_challenge_completed_output",
            details = mapOf(
                "decision" to if (progressResponse.failureResponse == null) {
                    "completion_recorded"
                } else {
                    "completion_rejected_progress_failure"
                },
                "failureReason" to progressResponse.failureResponse?.reason,
                "completionStatus" to progressResponse.completionState.status,
            ),
        )
        return DailyChallengeProgressUpdateResponse(
            challengeViewState = challengeViewState,
            progressFailureResponse = progressResponse.failureResponse,
        )
    }

    fun requestCompletionReversal(
        currentLocalDate: LocalDate,
        selectedChallengeDate: LocalDate,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): DailyChallengeReversalRequestResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "request_completion_reversal_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "selectedChallengeDate" to selectedChallengeDate,
                "campaignWindow" to campaignWindow,
            ),
        )

        val challengeDetailResult = loadChallengeDetailState(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = selectedChallengeDate,
            campaignWindow = campaignWindow,
        )
        if (challengeDetailResult.calendarFailureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "request_completion_reversal_output",
                details = mapOf(
                    "decision" to "reversal_request_rejected_calendar_failure",
                    "failureReason" to challengeDetailResult.calendarFailureResponse.reason,
                ),
            )
            return DailyChallengeReversalRequestResponse(
                challengeViewState = null,
                completionReversalPrompt = null,
                calendarFailureResponse = challengeDetailResult.calendarFailureResponse,
            )
        }

        val promptResponse = challengeProgressProtocol.requestCompletionReversal(selectedChallengeDate)
        logger.logDecision(
            module = MODULE_NAME,
            action = "request_completion_reversal_output",
            details = mapOf(
                "decision" to if (promptResponse.failureResponse == null) {
                    "reversal_prompt_returned"
                } else {
                    "reversal_request_rejected_progress_failure"
                },
                "failureReason" to promptResponse.failureResponse?.reason,
                "promptReturned" to (promptResponse.completionReversalPrompt != null),
            ),
        )
        return DailyChallengeReversalRequestResponse(
            challengeViewState = challengeDetailResult.challengeViewState,
            completionReversalPrompt = promptResponse.completionReversalPrompt,
            progressFailureResponse = promptResponse.failureResponse,
        )
    }

    fun confirmCompletionReversal(
        currentLocalDate: LocalDate,
        selectedChallengeDate: LocalDate,
        confirmation: CompletionReversalConfirmation,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): DailyChallengeProgressUpdateResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "confirm_completion_reversal_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "selectedChallengeDate" to selectedChallengeDate,
                "confirmation" to confirmation,
                "campaignWindow" to campaignWindow,
            ),
        )

        val challengeDetailResult = loadChallengeDetailState(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = selectedChallengeDate,
            campaignWindow = campaignWindow,
        )
        if (challengeDetailResult.calendarFailureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "confirm_completion_reversal_output",
                details = mapOf(
                    "decision" to "reversal_confirmation_rejected_calendar_failure",
                    "failureReason" to challengeDetailResult.calendarFailureResponse.reason,
                ),
            )
            return DailyChallengeProgressUpdateResponse(
                challengeViewState = null,
                calendarFailureResponse = challengeDetailResult.calendarFailureResponse,
            )
        }

        val progressResponse = challengeProgressProtocol.confirmCompletionReversal(
            challengeDate = selectedChallengeDate,
            confirmation = confirmation,
            currentLocalDate = currentLocalDate,
        )
        val challengeViewState = challengeDetailResult.challenge?.let { challenge ->
            challenge.toExperienceState(progressResponse)
        }
        logger.logDecision(
            module = MODULE_NAME,
            action = "confirm_completion_reversal_output",
            details = mapOf(
                "decision" to if (progressResponse.failureResponse == null) {
                    "reversal_completed"
                } else {
                    "reversal_preserved_completed"
                },
                "failureReason" to progressResponse.failureResponse?.reason,
                "completionStatus" to progressResponse.completionState.status,
            ),
        )
        return DailyChallengeProgressUpdateResponse(
            challengeViewState = challengeViewState,
            progressFailureResponse = progressResponse.failureResponse,
        )
    }

    fun evaluateReminderSchedule(
        reminderPreference: ReminderPreference,
        currentLocalDate: LocalDate,
        currentLocalTime: LocalTime,
    ): DailyChallengeReminderEvaluationResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "evaluate_reminder_schedule_input",
            details = mapOf(
                "remindersEnabled" to reminderPreference.remindersEnabled,
                "notificationPermissionGranted" to reminderPreference.notificationPermissionGranted,
                "currentLocalDate" to currentLocalDate,
                "currentLocalTime" to currentLocalTime,
            ),
        )

        val progressResponse = challengeProgressProtocol.getCompletionState(
            challengeDate = currentLocalDate,
            currentLocalDate = currentLocalDate,
        )
        val reminderResponse = challengeReminderProtocol.evaluateReminderSchedule(
            reminderPreference = reminderPreference,
            currentLocalDate = currentLocalDate,
            currentLocalTime = currentLocalTime,
            completionState = progressResponse.completionState,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "evaluate_reminder_schedule_output",
            details = mapOf(
                "decision" to if (reminderResponse.failureResponse == null) {
                    "reminder_evaluation_completed"
                } else {
                    "reminder_evaluation_returned_failure"
                },
                "progressStatus" to progressResponse.completionState.status,
                "laterReminderStatus" to reminderResponse.reminderScheduleState.laterReminder.status,
                "failureReason" to reminderResponse.failureResponse?.reason,
            ),
        )
        return DailyChallengeReminderEvaluationResponse(
            completionState = progressResponse.completionState,
            completionEligibility = progressResponse.completionEligibility,
            reminderScheduleState = reminderResponse.reminderScheduleState,
            progressFailureResponse = progressResponse.failureResponse,
            reminderFailureResponse = reminderResponse.failureResponse,
        )
    }

    fun buildSharePayload(
        currentLocalDate: LocalDate,
        selectedChallengeDate: LocalDate,
        appLink: String,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): DailyChallengeShareExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "build_share_payload_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "selectedChallengeDate" to selectedChallengeDate,
                "campaignWindow" to campaignWindow,
                "appLinkPresent" to appLink.isNotBlank(),
            ),
        )

        val challengeDetailResult = loadChallengeDetailState(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = selectedChallengeDate,
            campaignWindow = campaignWindow,
        )
        if (challengeDetailResult.calendarFailureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "build_share_payload_output",
                details = mapOf(
                    "decision" to "share_rejected_calendar_failure",
                    "failureReason" to challengeDetailResult.calendarFailureResponse.reason,
                ),
            )
            return DailyChallengeShareExperienceResponse(
                challengeViewState = null,
                sharePayload = null,
                calendarFailureResponse = challengeDetailResult.calendarFailureResponse,
            )
        }

        val challengeViewState = requireNotNull(challengeDetailResult.challengeViewState)
        val shareResponse = challengeShareProtocol.buildSharePayload(
            challengeDate = selectedChallengeDate,
            challengeSummary = challengeViewState.challenge.shortSummary,
            completionState = challengeViewState.completionState.status,
            appLink = appLink,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "build_share_payload_output",
            details = mapOf(
                "decision" to if (shareResponse.failureResponse == null) {
                    "share_payload_created"
                } else {
                    "share_rejected_share_failure"
                },
                "failureReason" to shareResponse.failureResponse?.reason,
            ),
        )
        return DailyChallengeShareExperienceResponse(
            challengeViewState = challengeViewState,
            sharePayload = shareResponse.sharePayload,
            shareFailureResponse = shareResponse.failureResponse,
        )
    }

    private fun loadChallengeDetailState(
        currentLocalDate: LocalDate,
        selectedChallengeDate: LocalDate,
        campaignWindow: CampaignWindow,
    ): ChallengeDetailLoadResult {
        val detailResponse = challengeCalendarProtocol.getChallengeDetail(
            currentLocalDate = currentLocalDate,
            campaignWindow = campaignWindow,
            selectedChallengeDate = selectedChallengeDate,
        )
        if (detailResponse.failureResponse != null || detailResponse.challengeDetail == null) {
            return ChallengeDetailLoadResult(
                challenge = null,
                challengeViewState = null,
                calendarFailureResponse = detailResponse.failureResponse,
            )
        }

        val challengeViewState = loadChallengeExperienceState(
            challenge = detailResponse.challengeDetail,
            currentLocalDate = currentLocalDate,
        )
        return ChallengeDetailLoadResult(
            challenge = detailResponse.challengeDetail,
            challengeViewState = challengeViewState,
            calendarFailureResponse = null,
        )
    }

    private fun loadChallengeExperienceState(
        challenge: DailyChallenge,
        currentLocalDate: LocalDate,
    ): ChallengeExperienceState {
        val progressResponse = challengeProgressProtocol.getCompletionState(
            challengeDate = challenge.date,
            currentLocalDate = currentLocalDate,
        )
        return challenge.toExperienceState(progressResponse)
    }

    private fun DailyChallenge.toExperienceState(
        progressResponse: com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressStateResponse,
    ) = ChallengeExperienceState(
        challenge = this,
        completionState = progressResponse.completionState,
        completionEligibility = progressResponse.completionEligibility,
    )

    private data class ChallengeDetailLoadResult(
        val challenge: DailyChallenge?,
        val challengeViewState: ChallengeExperienceState?,
        val calendarFailureResponse: CopFailureResponse<ChallengeCalendarFailureReason>?,
    )

    companion object {
        private const val MODULE_NAME = "DailyServiceChallengeExperienceOrchestrator"
    }
}
