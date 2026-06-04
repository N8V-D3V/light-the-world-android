package com.n8vd3v.lighttheworld.features.dailychallenge.calendar

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallenge
import java.time.LocalDate

enum class ChallengeCalendarFailureReason {
    CHALLENGE_CONTENT_UNAVAILABLE,
    CHALLENGE_DATE_MISSING,
}

data class ChallengeCardListResponse(
    val challengeCardList: List<DailyChallenge>,
    val failureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
)

data class ChallengeDetailResponse(
    val challengeDetail: DailyChallenge?,
    val failureResponse: CopFailureResponse<ChallengeCalendarFailureReason>? = null,
)

interface ChallengeCalendar {
    fun getChallengeCardList(
        currentLocalDate: LocalDate,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
    ): ChallengeCardListResponse

    fun getChallengeDetail(
        currentLocalDate: LocalDate,
        campaignWindow: CampaignWindow = CampaignWindow.LightTheWorldAnnual,
        selectedChallengeDate: LocalDate,
    ): ChallengeDetailResponse
}
