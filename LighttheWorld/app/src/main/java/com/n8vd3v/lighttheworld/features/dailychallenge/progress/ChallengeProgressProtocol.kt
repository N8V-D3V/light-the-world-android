package com.n8vd3v.lighttheworld.features.dailychallenge.progress

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeProgress
import java.time.LocalDate

enum class ChallengeProgressFailureReason {
    COMPLETION_NOT_YET_ALLOWED,
    CHALLENGE_NOT_COMPLETED,
    REVERSAL_NOT_CONFIRMED,
}

enum class CompletionEligibility {
    ELIGIBLE,
    NOT_ELIGIBLE,
}

enum class CompletionReversalConfirmation {
    APPROVED,
    DECLINED,
    DISMISSED,
}

data class CompletionReversalPrompt(
    val challengeDate: LocalDate,
    val message: String,
)

data class ChallengeProgressStateResponse(
    val completionState: ChallengeProgress,
    val completionEligibility: CompletionEligibility,
    val failureResponse: CopFailureResponse<ChallengeProgressFailureReason>? = null,
)

data class CompletionReversalPromptResponse(
    val completionReversalPrompt: CompletionReversalPrompt?,
    val failureResponse: CopFailureResponse<ChallengeProgressFailureReason>? = null,
)

interface ChallengeProgressProtocol {
    fun getCompletionState(
        challengeDate: LocalDate,
        currentLocalDate: LocalDate,
    ): ChallengeProgressStateResponse

    fun markChallengeCompleted(
        challengeDate: LocalDate,
        currentLocalDate: LocalDate,
    ): ChallengeProgressStateResponse

    fun requestCompletionReversal(challengeDate: LocalDate): CompletionReversalPromptResponse

    fun confirmCompletionReversal(
        challengeDate: LocalDate,
        confirmation: CompletionReversalConfirmation,
        currentLocalDate: LocalDate,
    ): ChallengeProgressStateResponse
}
