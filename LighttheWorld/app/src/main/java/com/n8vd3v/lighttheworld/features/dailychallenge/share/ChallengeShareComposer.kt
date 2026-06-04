package com.n8vd3v.lighttheworld.features.dailychallenge.share

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.ChallengeCompletionStatus
import java.time.LocalDate

enum class ChallengeShareFailureReason {
    CHALLENGE_NOT_COMPLETED,
    APP_LINK_UNAVAILABLE,
}

data class SharePayload(
    val challengeDate: LocalDate,
    val challengeSummary: String,
    val completionMessage: String,
)

data class ChallengeShareResponse(
    val sharePayload: SharePayload?,
    val failureResponse: CopFailureResponse<ChallengeShareFailureReason>? = null,
)

interface ChallengeShareComposer {
    fun buildSharePayload(
        challengeDate: LocalDate,
        challengeSummary: String,
        completionState: ChallengeCompletionStatus,
        appLink: String,
    ): ChallengeShareResponse
}
