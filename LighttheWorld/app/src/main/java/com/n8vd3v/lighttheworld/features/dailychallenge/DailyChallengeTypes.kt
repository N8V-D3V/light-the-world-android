package com.n8vd3v.lighttheworld.features.dailychallenge

import java.time.Instant
import java.time.LocalDate

data class CampaignWindow(
    val startMonth: Int = 12,
    val startDay: Int = 1,
    val endMonth: Int = 12,
    val endDay: Int = 25,
) {
    fun datesForYear(year: Int): List<LocalDate> {
        val start = LocalDate.of(year, startMonth, startDay)
        val end = LocalDate.of(year, endMonth, endDay)
        return generateSequence(start) { date ->
            if (date.isBefore(end)) {
                date.plusDays(1)
            } else {
                null
            }
        }.toList()
    }

    fun includes(date: LocalDate): Boolean = date in datesForYear(date.year)

    companion object {
        val LightTheWorldAnnual = CampaignWindow()
    }
}

data class DailyChallenge(
    val date: LocalDate,
    val shortSummary: String,
    val detailDescription: String,
    val suggestions: List<String>,
)

enum class ChallengeCompletionStatus {
    INCOMPLETE,
    COMPLETED,
}

data class ChallengeProgress(
    val challengeDate: LocalDate,
    val status: ChallengeCompletionStatus,
    val completedAt: Instant?,
)
