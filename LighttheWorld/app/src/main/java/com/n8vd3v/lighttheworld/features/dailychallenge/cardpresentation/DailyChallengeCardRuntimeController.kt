package com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallengeBrowseExperienceResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeFlow
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionEligibility
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalConfirmation
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.CompletionReversalPrompt
import com.n8vd3v.lighttheworld.features.dailychallenge.share.SharePayload
import java.time.LocalDate

data class DailyChallengeCardActionState(
    val canMarkComplete: Boolean = false,
    val canRequestReversal: Boolean = false,
    val canShare: Boolean = false,
)

data class DailyChallengeCardRuntimeUiState(
    val currentLocalDate: LocalDate,
    val browseResponse: DailyChallengeBrowseExperienceResponse,
    val pendingReversalPrompt: CompletionReversalPrompt? = null,
    val latestFailureMessage: String? = null,
)

class DailyChallengeCardRuntimeController(
    private val flow: DailyServiceChallengeFlow,
    private val currentLocalDateProvider: () -> LocalDate,
    private val campaignWindow: CampaignWindow,
    private val appLink: String,
) {

    var uiState by mutableStateOf(
        initialUiState(),
    )
        private set

    fun refresh() {
        val currentLocalDate = currentLocalDate()
        uiState = uiState.copy(
            currentLocalDate = currentLocalDate,
            browseResponse = browseChallenges(currentLocalDate),
        )
    }

    fun actionStateFor(challengeDate: LocalDate?): DailyChallengeCardActionState {
        if (challengeDate == null) {
            return DailyChallengeCardActionState()
        }

        val challengeState = uiState.browseResponse.challengeCards.firstOrNull {
            it.challenge.date == challengeDate
        } ?: return DailyChallengeCardActionState()

        val isCompleted = challengeState.completionState.status == ChallengeCompletionStatus.COMPLETED
        val canMarkComplete = challengeState.completionEligibility == CompletionEligibility.ELIGIBLE &&
            challengeState.completionState.status == ChallengeCompletionStatus.INCOMPLETE
        return DailyChallengeCardActionState(
            canMarkComplete = canMarkComplete,
            canRequestReversal = isCompleted,
            canShare = isCompleted,
        )
    }

    fun markComplete(challengeDate: LocalDate?) {
        if (challengeDate == null) {
            return
        }

        val currentLocalDate = currentLocalDate()
        val response = flow.markChallengeCompleted(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = challengeDate,
            campaignWindow = campaignWindow,
        )
        val failureMessage = response.calendarFailureResponse?.message ?: response.progressFailureResponse?.message
        uiState = if (failureMessage == null) {
            uiState.copy(
                currentLocalDate = currentLocalDate,
                browseResponse = browseChallenges(currentLocalDate),
                pendingReversalPrompt = null,
                latestFailureMessage = null,
            )
        } else {
            uiState.copy(
                currentLocalDate = currentLocalDate,
                pendingReversalPrompt = null,
                latestFailureMessage = failureMessage,
            )
        }
    }

    fun requestCompletionReversal(challengeDate: LocalDate?) {
        if (challengeDate == null) {
            return
        }

        val currentLocalDate = currentLocalDate()
        val response = flow.requestCompletionReversal(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = challengeDate,
            campaignWindow = campaignWindow,
        )
        val failureMessage = response.calendarFailureResponse?.message ?: response.progressFailureResponse?.message
        uiState = if (response.completionReversalPrompt != null && failureMessage == null) {
            uiState.copy(
                currentLocalDate = currentLocalDate,
                pendingReversalPrompt = response.completionReversalPrompt,
                latestFailureMessage = null,
            )
        } else {
            uiState.copy(
                currentLocalDate = currentLocalDate,
                pendingReversalPrompt = null,
                latestFailureMessage = failureMessage,
            )
        }
    }

    fun dismissCompletionReversalPrompt() {
        uiState = uiState.copy(pendingReversalPrompt = null)
    }

    fun confirmCompletionReversal() {
        val prompt = uiState.pendingReversalPrompt ?: return
        val currentLocalDate = currentLocalDate()
        val response = flow.confirmCompletionReversal(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = prompt.challengeDate,
            confirmation = CompletionReversalConfirmation.APPROVED,
            campaignWindow = campaignWindow,
        )
        val failureMessage = response.calendarFailureResponse?.message ?: response.progressFailureResponse?.message
        uiState = if (failureMessage == null) {
            uiState.copy(
                currentLocalDate = currentLocalDate,
                browseResponse = browseChallenges(currentLocalDate),
                pendingReversalPrompt = null,
                latestFailureMessage = null,
            )
        } else {
            uiState.copy(
                currentLocalDate = currentLocalDate,
                pendingReversalPrompt = null,
                latestFailureMessage = failureMessage,
            )
        }
    }

    fun shareCompletedChallenge(
        challengeDate: LocalDate?,
        onSharePayloadReady: (SharePayload) -> Unit,
    ) {
        if (challengeDate == null) {
            return
        }

        val currentLocalDate = currentLocalDate()
        val response = flow.buildSharePayload(
            currentLocalDate = currentLocalDate,
            selectedChallengeDate = challengeDate,
            appLink = appLink,
            campaignWindow = campaignWindow,
        )
        val failureMessage = response.calendarFailureResponse?.message ?: response.shareFailureResponse?.message
        if (response.sharePayload != null && failureMessage == null) {
            uiState = uiState.copy(
                currentLocalDate = currentLocalDate,
                latestFailureMessage = null,
            )
            onSharePayloadReady(response.sharePayload)
        } else {
            uiState = uiState.copy(
                currentLocalDate = currentLocalDate,
                latestFailureMessage = failureMessage,
            )
        }
    }

    private fun initialUiState(): DailyChallengeCardRuntimeUiState {
        val currentLocalDate = currentLocalDate()
        return DailyChallengeCardRuntimeUiState(
            currentLocalDate = currentLocalDate,
            browseResponse = browseChallenges(currentLocalDate),
        )
    }

    private fun currentLocalDate(): LocalDate = currentLocalDateProvider()

    private fun browseChallenges(currentLocalDate: LocalDate): DailyChallengeBrowseExperienceResponse =
        flow.browseChallengeCards(
            currentLocalDate = currentLocalDate,
            campaignWindow = campaignWindow,
        )
}
