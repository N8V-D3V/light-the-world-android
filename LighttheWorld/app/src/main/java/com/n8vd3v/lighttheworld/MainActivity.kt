package com.n8vd3v.lighttheworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.n8vd3v.lighttheworld.features.dailychallenge.CampaignWindow
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeFlow
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.JsonChallengeContentSource
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.StubChallengeCalendar
import com.n8vd3v.lighttheworld.features.dailychallenge.cardpresentation.DailyChallengeCardScreen
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.StubChallengeCompletionTracker
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.StubChallengeReminderScheduler
import com.n8vd3v.lighttheworld.features.dailychallenge.share.StubChallengeShareComposer
import java.time.LocalDate

val RichRed = Color(0xFFBD2B34)
val DeepGold = Color(0xFFE6B325)
val CrispWhite = Color(0xFFFFFFFF)
val MidnightBlue = Color(0xFF191970)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentLocalDate = LocalDate.now()
        val contentSource = JsonChallengeContentSource(this)
        val campaignWindow = contentSource.resolveCampaignWindow(currentLocalDate.year)
            ?: CampaignWindow.LightTheWorldAnnual
        val challengeFlow = DailyServiceChallengeFlow(
            challengeCalendar = StubChallengeCalendar(contentSource = contentSource),
            challengeCompletionTracker = StubChallengeCompletionTracker(),
            challengeReminderScheduler = StubChallengeReminderScheduler(),
            challengeShareComposer = StubChallengeShareComposer(),
        )

        setContent {
            LightTheWorldTheme {
                Surface(color = CrispWhite) {
                    DailyChallengeCardScreen(
                        flow = challengeFlow,
                        currentLocalDate = currentLocalDate,
                        campaignWindow = campaignWindow,
                    )
                }
            }
        }
    }
}

@Composable
fun LightTheWorldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = RichRed,
            secondary = DeepGold,
            surface = CrispWhite,
            onSurface = MidnightBlue,
            background = CrispWhite,
        ),
        content = content,
    )
}
