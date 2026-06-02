package com.n8vd3v.lighttheworld.features.dailychallenge.calendar

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallenge
import java.time.LocalDate

class ChallengeCalendarContentModule(
    private val contentSource: ChallengeContentSource = BundledChallengeContentSource(),
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) : ChallengeCalendarProtocol {

    override fun getChallengeCardList(
        currentLocalDate: LocalDate,
        campaignWindow: CampaignWindow,
    ): ChallengeCardListResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_challenge_card_list_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "campaignWindow" to campaignWindow,
            ),
        )

        val loadResult = loadValidatedChallenges(currentLocalDate.year, campaignWindow)
        if (loadResult.failureResponse != null) {
            return ChallengeCardListResponse(
                challengeCardList = emptyList(),
                failureResponse = loadResult.failureResponse,
            )
        }

        val orderedChallenges = loadResult.challenges.orEmpty()
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_challenge_card_list_output",
            details = mapOf(
                "decision" to "content_returned",
                "challengeCount" to orderedChallenges.size,
                "firstChallengeDate" to orderedChallenges.firstOrNull()?.date,
                "lastChallengeDate" to orderedChallenges.lastOrNull()?.date,
            ),
        )
        return ChallengeCardListResponse(challengeCardList = orderedChallenges)
    }

    override fun getChallengeDetail(
        currentLocalDate: LocalDate,
        campaignWindow: CampaignWindow,
        selectedChallengeDate: LocalDate,
    ): ChallengeDetailResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_challenge_detail_input",
            details = mapOf(
                "currentLocalDate" to currentLocalDate,
                "campaignWindow" to campaignWindow,
                "selectedChallengeDate" to selectedChallengeDate,
            ),
        )

        val loadResult = loadValidatedChallenges(selectedChallengeDate.year, campaignWindow)
        if (loadResult.failureResponse != null) {
            return ChallengeDetailResponse(
                challengeDetail = null,
                failureResponse = loadResult.failureResponse,
            )
        }

        val challenge = loadResult.challenges.orEmpty().firstOrNull { it.date == selectedChallengeDate }
        if (challenge == null) {
            val failure = CopFailureResponse(
                reason = ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
                message = "Challenge content is missing for $selectedChallengeDate.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "get_challenge_detail_output",
                details = mapOf(
                    "decision" to "date_missing",
                    "failureReason" to failure.reason,
                ),
            )
            return ChallengeDetailResponse(
                challengeDetail = null,
                failureResponse = failure,
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "get_challenge_detail_output",
            details = mapOf(
                "decision" to "detail_returned",
                "challengeDate" to challenge.date,
                "summary" to challenge.shortSummary,
            ),
        )
        return ChallengeDetailResponse(challengeDetail = challenge)
    }

    private fun loadValidatedChallenges(
        year: Int,
        campaignWindow: CampaignWindow,
    ): ChallengeLoadResult {
        val challenges = contentSource.loadCampaignChallenges(year, campaignWindow)
        if (challenges == null) {
            val failure = CopFailureResponse(
                reason = ChallengeCalendarFailureReason.CHALLENGE_CONTENT_UNAVAILABLE,
                message = "Challenge calendar data is unavailable.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "challenge_content_validation_output",
                details = mapOf(
                    "decision" to "content_unavailable",
                    "failureReason" to failure.reason,
                ),
            )
            return ChallengeLoadResult(
                challenges = null,
                failureResponse = failure,
            )
        }

        val orderedChallenges = challenges.sortedBy { it.date }
        val expectedDates = campaignWindow.datesForYear(year)
        val actualDates = orderedChallenges.map { it.date }
        val missingDates = expectedDates.filterNot(actualDates::contains)
        if (missingDates.isNotEmpty()) {
            val failure = CopFailureResponse(
                reason = ChallengeCalendarFailureReason.CHALLENGE_DATE_MISSING,
                message = "Challenge content is missing for ${missingDates.joinToString()}.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "challenge_content_validation_output",
                details = mapOf(
                    "decision" to "campaign_window_incomplete",
                    "failureReason" to failure.reason,
                    "missingDates" to missingDates.joinToString(),
                ),
            )
            return ChallengeLoadResult(
                challenges = null,
                failureResponse = failure,
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "challenge_content_validation_output",
            details = mapOf(
                "decision" to "campaign_window_complete",
                "challengeCount" to orderedChallenges.size,
            ),
        )
        return ChallengeLoadResult(
            challenges = orderedChallenges,
            failureResponse = null,
        )
    }

    interface ChallengeContentSource {
        fun loadCampaignChallenges(year: Int, campaignWindow: CampaignWindow): List<DailyChallenge>?
    }

    class BundledChallengeContentSource(
        private val templates: List<ChallengeTemplate> = defaultTemplates(),
        private val contentAvailable: Boolean = true,
    ) : ChallengeContentSource {
        override fun loadCampaignChallenges(
            year: Int,
            campaignWindow: CampaignWindow,
        ): List<DailyChallenge>? {
            if (!contentAvailable) {
                return null
            }

            val allowedDates = campaignWindow.datesForYear(year).toSet()
            return templates
                .map { template ->
                    DailyChallenge(
                        date = LocalDate.of(year, 12, template.dayOfMonth),
                        shortSummary = template.shortSummary,
                        detailDescription = template.detailDescription,
                        suggestions = template.suggestions,
                    )
                }
                .filter { challenge -> challenge.date in allowedDates }
                .sortedBy { it.date }
        }
    }

    data class ChallengeTemplate(
        val dayOfMonth: Int,
        val shortSummary: String,
        val detailDescription: String,
        val suggestions: List<String>,
    )

    private data class ChallengeLoadResult(
        val challenges: List<DailyChallenge>?,
        val failureResponse: CopFailureResponse<ChallengeCalendarFailureReason>?,
    )

    companion object {
        private const val MODULE_NAME = "ChallengeCalendarContentModule"

        fun defaultTemplates(): List<ChallengeTemplate> =
            (1..25).map { day ->
                ChallengeTemplate(
                    dayOfMonth = day,
                    shortSummary = "Day $day service invitation",
                    detailDescription = "Stub challenge detail for December $day in the Light the World campaign.",
                    suggestions = listOf(
                        "Choose one person to serve on December $day.",
                        "Record one way you shared light after completing this day's challenge.",
                    ),
                )
            }
    }
}
