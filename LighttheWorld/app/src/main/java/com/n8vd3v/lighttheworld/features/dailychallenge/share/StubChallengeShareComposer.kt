package com.n8vd3v.lighttheworld.features.dailychallenge.share

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import java.time.LocalDate

class StubChallengeShareComposer(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : ChallengeShareComposer {

    override fun buildSharePayload(
        challengeDate: LocalDate,
        challengeSummary: String,
        completionState: ChallengeCompletionStatus,
        appLink: String,
    ): ChallengeShareResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "build_share_payload_input",
            details = mapOf(
                "challengeDate" to challengeDate,
                "challengeSummary" to challengeSummary,
                "completionState" to completionState,
                "appLink" to appLink,
            ),
        )

        if (completionState != ChallengeCompletionStatus.COMPLETED) {
            val failure = CopFailureResponse(
                reason = ChallengeShareFailureReason.CHALLENGE_NOT_COMPLETED,
                message = "Challenge $challengeDate must be completed before it can be shared.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "build_share_payload_output",
                details = mapOf(
                    "decision" to "share_rejected_not_completed",
                    "failureReason" to failure.reason,
                ),
            )
            return ChallengeShareResponse(
                sharePayload = null,
                failureResponse = failure,
            )
        }

        if (appLink.isBlank()) {
            val failure = CopFailureResponse(
                reason = ChallengeShareFailureReason.APP_LINK_UNAVAILABLE,
                message = "Share content requires an app link.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "build_share_payload_output",
                details = mapOf(
                    "decision" to "share_rejected_missing_app_link",
                    "failureReason" to failure.reason,
                ),
            )
            return ChallengeShareResponse(
                sharePayload = null,
                failureResponse = failure,
            )
        }

        val payload = SharePayload(
            challengeDate = challengeDate,
            challengeSummary = challengeSummary,
            completionMessage = """I completed "$challengeSummary" in Light the World today. Join me in sharing some light this Christmas: $appLink""",
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "build_share_payload_output",
            details = mapOf(
                "decision" to "share_payload_created",
                "challengeDate" to challengeDate,
            ),
        )
        return ChallengeShareResponse(sharePayload = payload)
    }

    companion object {
        private const val MODULE_NAME = "StubChallengeShareComposer"
    }
}
