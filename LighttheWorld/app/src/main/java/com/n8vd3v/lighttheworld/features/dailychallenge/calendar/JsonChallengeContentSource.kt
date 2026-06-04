package com.n8vd3v.lighttheworld.features.dailychallenge.calendar

import android.content.Context
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyChallenge
import java.time.LocalDate
import org.json.JSONArray

class JsonChallengeContentSource(private val context: Context) : StubChallengeCalendar.ChallengeContentSource {
    private val cachedChallenges: List<DailyChallenge>? by lazy {
        loadAllChallenges()
    }

    override fun loadCampaignChallenges(year: Int, campaignWindow: CampaignWindow): List<DailyChallenge>? {
        val allowedDates = campaignWindow.datesForYear(year).toSet()
        return cachedChallenges
            ?.filter { challenge -> challenge.date.year == year && challenge.date in allowedDates }
            ?.sortedBy(DailyChallenge::date)
    }

    fun resolveCampaignWindow(referenceYear: Int): CampaignWindow? {
        val datesForYear = cachedChallenges
            ?.map(DailyChallenge::date)
            ?.filter { it.year == referenceYear }
            .orEmpty()
        if (datesForYear.isEmpty()) {
            return null
        }

        val startDate = datesForYear.minOrNull() ?: return null
        val endDate = datesForYear.maxOrNull() ?: return null
        return CampaignWindow(
            startMonth = startDate.monthValue,
            startDay = startDate.dayOfMonth,
            endMonth = endDate.monthValue,
            endDay = endDate.dayOfMonth,
        )
    }

    private fun loadAllChallenges(): List<DailyChallenge>? {
        return try {
            val jsonString = context.assets.open("challenges.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            buildList {
                for (index in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(index)
                    val suggestionsArray = obj.getJSONArray("suggestions")
                    add(
                        DailyChallenge(
                            date = LocalDate.parse(obj.getString("date")),
                            shortSummary = obj.getString("shortSummary"),
                            detailDescription = obj.getString("detailDescription"),
                            suggestions = List(suggestionsArray.length()) { suggestionIndex ->
                                suggestionsArray.getString(suggestionIndex)
                            },
                        ),
                    )
                }
            }.sortedBy(DailyChallenge::date)
        } catch (_: Exception) {
            null
        }
    }
}
