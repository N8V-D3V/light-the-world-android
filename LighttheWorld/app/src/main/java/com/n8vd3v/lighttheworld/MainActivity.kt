package com.n8vd3v.lighttheworld

import android.content.Intent
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
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.LTWGivingMachineExperienceScaffold
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.LTWGivingMachineSeedContent
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.LTWGivingMachineUiController
import com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation.LTWJsonGivingMachinePresentationContentSource
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

        val currentLocalDateProvider = { LocalDate.now() }
        val contentSource = JsonChallengeContentSource(this)
        val campaignWindow = contentSource.resolveCampaignWindow(currentLocalDateProvider().year)
            ?: CampaignWindow.LightTheWorldAnnual
        val challengeFlow = DailyServiceChallengeFlow(
            challengeCalendar = StubChallengeCalendar(contentSource = contentSource),
            challengeCompletionTracker = StubChallengeCompletionTracker(),
            challengeReminderScheduler = StubChallengeReminderScheduler(),
            challengeShareComposer = StubChallengeShareComposer(),
        )
        val givingMachineContent = LTWJsonGivingMachinePresentationContentSource(this).loadContent()
            ?: LTWGivingMachineSeedContent.defaultContent()
        val givingMachineController = LTWGivingMachineUiController(
            environment = LTWGivingMachineSeedContent.environmentFor(givingMachineContent),
        )

        setContent {
            LightTheWorldTheme {
                Surface(color = CrispWhite) {
                    LTWGivingMachineExperienceScaffold(
                        controller = givingMachineController,
                    ) {
                        DailyChallengeCardScreen(
                            flow = challengeFlow,
                            currentLocalDateProvider = currentLocalDateProvider,
                            campaignWindow = campaignWindow,
                            appLink = DAILY_CHALLENGE_SHARE_APP_LINK,
                            onSharePayload = { payload ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, payload.completionMessage)
                                }
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        getString(R.string.challenge_share_chooser_title),
                                    ),
                                )
                            },
                        )
                    }
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

private const val DAILY_CHALLENGE_SHARE_APP_LINK = "https://www.lighttheworld.org/"
