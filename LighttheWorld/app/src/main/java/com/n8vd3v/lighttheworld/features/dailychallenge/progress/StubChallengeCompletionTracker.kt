package com.n8vd3v.lighttheworld.features.dailychallenge.progress

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeProgress
import java.time.Instant
import java.time.LocalDate

class StubChallengeCompletionTracker(
    private val timestampProvider: () -> Instant = { Instant.now() },
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : ChallengeCompletionTracker {

    private val progressByDate = mutableMapOf<LocalDate, ChallengeProgress>()

    override fun getCompletionState(
        challengeDate: LocalDate,
        currentLocalDate: LocalDate,
    ): ChallengeProgressStateResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_completion_state_input",
            details = mapOf(
                "challengeDate" to challengeDate,
                "currentLocalDate" to currentLocalDate,
            ),
        )
        return buildStateResponse(
            challengeDate = challengeDate,
            currentLocalDate = currentLocalDate,
            failureResponse = null,
            decision = "state_returned",
        )
    }

    override fun markChallengeCompleted(
        challengeDate: LocalDate,
        currentLocalDate: LocalDate,
    ): ChallengeProgressStateResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "mark_challenge_completed_input",
            details = mapOf(
                "challengeDate" to challengeDate,
                "currentLocalDate" to currentLocalDate,
            ),
        )

        if (currentLocalDate.isBefore(challengeDate)) {
            val failure = CopFailureResponse(
                reason = ChallengeProgressFailureReason.COMPLETION_NOT_YET_ALLOWED,
                message = "Challenge completion is not allowed before $challengeDate.",
            )
            return buildStateResponse(
                challengeDate = challengeDate,
                currentLocalDate = currentLocalDate,
                failureResponse = failure,
                decision = "completion_rejected_not_yet_allowed",
            )
        }

        progressByDate[challengeDate] = ChallengeProgress(
            challengeDate = challengeDate,
            status = ChallengeCompletionStatus.COMPLETED,
            completedAt = timestampProvider(),
        )
        return buildStateResponse(
            challengeDate = challengeDate,
            currentLocalDate = currentLocalDate,
            failureResponse = null,
            decision = "completion_recorded",
        )
    }

    override fun requestCompletionReversal(challengeDate: LocalDate): CompletionReversalPromptResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "request_completion_reversal_input",
            details = mapOf("challengeDate" to challengeDate),
        )

        val currentProgress = currentProgressFor(challengeDate)
        if (currentProgress.status != ChallengeCompletionStatus.COMPLETED) {
            val failure = CopFailureResponse(
                reason = ChallengeProgressFailureReason.CHALLENGE_NOT_COMPLETED,
                message = "Challenge $challengeDate is not completed.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "request_completion_reversal_output",
                details = mapOf(
                    "decision" to "reversal_rejected_not_completed",
                    "failureReason" to failure.reason,
                ),
            )
            return CompletionReversalPromptResponse(
                completionReversalPrompt = null,
                failureResponse = failure,
            )
        }

        val prompt = CompletionReversalPrompt(
            challengeDate = challengeDate,
            message = "Confirm returning the completed challenge on $challengeDate to incomplete.",
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "request_completion_reversal_output",
            details = mapOf(
                "decision" to "reversal_prompt_returned",
                "challengeDate" to challengeDate,
            ),
        )
        return CompletionReversalPromptResponse(completionReversalPrompt = prompt)
    }

    override fun confirmCompletionReversal(
        challengeDate: LocalDate,
        confirmation: CompletionReversalConfirmation,
        currentLocalDate: LocalDate,
    ): ChallengeProgressStateResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "confirm_completion_reversal_input",
            details = mapOf(
                "challengeDate" to challengeDate,
                "confirmation" to confirmation,
                "currentLocalDate" to currentLocalDate,
            ),
        )

        val currentProgress = currentProgressFor(challengeDate)
        if (currentProgress.status != ChallengeCompletionStatus.COMPLETED) {
            val failure = CopFailureResponse(
                reason = ChallengeProgressFailureReason.CHALLENGE_NOT_COMPLETED,
                message = "Challenge $challengeDate is not completed.",
            )
            return buildStateResponse(
                challengeDate = challengeDate,
                currentLocalDate = currentLocalDate,
                failureResponse = failure,
                decision = "reversal_rejected_not_completed",
            )
        }

        if (confirmation != CompletionReversalConfirmation.APPROVED) {
            val failure = CopFailureResponse(
                reason = ChallengeProgressFailureReason.REVERSAL_NOT_CONFIRMED,
                message = "Completion reversal for $challengeDate was not confirmed.",
            )
            return buildStateResponse(
                challengeDate = challengeDate,
                currentLocalDate = currentLocalDate,
                failureResponse = failure,
                decision = "reversal_preserved_completed",
            )
        }

        progressByDate[challengeDate] = ChallengeProgress(
            challengeDate = challengeDate,
            status = ChallengeCompletionStatus.INCOMPLETE,
            completedAt = null,
        )
        return buildStateResponse(
            challengeDate = challengeDate,
            currentLocalDate = currentLocalDate,
            failureResponse = null,
            decision = "reversal_completed",
        )
    }

    private fun buildStateResponse(
        challengeDate: LocalDate,
        currentLocalDate: LocalDate,
        failureResponse: CopFailureResponse<ChallengeProgressFailureReason>?,
        decision: String,
    ): ChallengeProgressStateResponse {
        val progress = currentProgressFor(challengeDate)
        val eligibility =
            if (currentLocalDate.isBefore(challengeDate)) CompletionEligibility.NOT_ELIGIBLE else CompletionEligibility.ELIGIBLE
        logger.logDecision(
            module = MODULE_NAME,
            action = "challenge_progress_output",
            details = mapOf(
                "decision" to decision,
                "challengeDate" to challengeDate,
                "status" to progress.status,
                "completedAt" to progress.completedAt,
                "eligibility" to eligibility,
                "failureReason" to failureResponse?.reason,
            ),
        )
        return ChallengeProgressStateResponse(
            completionState = progress,
            completionEligibility = eligibility,
            failureResponse = failureResponse,
        )
    }

    private fun currentProgressFor(challengeDate: LocalDate): ChallengeProgress =
        progressByDate.getOrPut(challengeDate) {
            ChallengeProgress(
                challengeDate = challengeDate,
                status = ChallengeCompletionStatus.INCOMPLETE,
                completedAt = null,
            )
        }

    companion object {
        private const val MODULE_NAME = "StubChallengeCompletionTracker"
    }
}
