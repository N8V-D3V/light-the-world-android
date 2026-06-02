package com.n8vd3v.lighttheworld.features.appshell

import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeExperienceOrchestrator
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.ChallengeCalendarContentModule
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.ChallengeProgressModule
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.ChallengeReminderModule
import com.n8vd3v.lighttheworld.features.dailychallenge.share.ChallengeShareModule
import com.n8vd3v.lighttheworld.features.donation.GivingMachineDonationExperienceOrchestrator
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartModule
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogModule
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutModule
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShellOrchestratorTest {

    @Test
    fun hostAppShellKeepsBothFeatureExperiencesAvailableWithoutForcingAnActiveExperience() {
        val orchestrator = AppShellOrchestrator(
            dailyServiceChallengeExperienceOrchestrator = dailyExperienceOrchestrator(),
            givingMachineDonationExperienceOrchestrator = donationExperienceOrchestrator(),
        )

        val hostState = orchestrator.hostAppShell()

        assertEquals(
            listOf(
                AppShellExperience.DAILY_SERVICE_CHALLENGES,
                AppShellExperience.GIVING_MACHINE_DONATIONS,
            ),
            hostState.availableExperiences,
        )
        assertNull(hostState.activeExperience)
    }

    @Test
    fun openExperienceActivatesOnlyTheRequestedFeatureBoundary() {
        val orchestrator = AppShellOrchestrator(
            dailyServiceChallengeExperienceOrchestrator = dailyExperienceOrchestrator(),
            givingMachineDonationExperienceOrchestrator = donationExperienceOrchestrator(),
        )

        val hostState = orchestrator.openExperience(AppShellExperience.GIVING_MACHINE_DONATIONS)

        assertEquals(AppShellExperience.GIVING_MACHINE_DONATIONS, hostState.activeExperience)
        assertEquals(2, hostState.availableExperiences.size)
    }

    private fun dailyExperienceOrchestrator() = DailyServiceChallengeExperienceOrchestrator(
        challengeCalendarProtocol = ChallengeCalendarContentModule(),
        challengeProgressProtocol = ChallengeProgressModule(),
        challengeReminderProtocol = ChallengeReminderModule(),
        challengeShareProtocol = ChallengeShareModule(),
    )

    private fun donationExperienceOrchestrator() = GivingMachineDonationExperienceOrchestrator(
        donationCatalogProtocol = DonationCatalogModule(),
        donationCartProtocol = DonationCartModule(),
        donationCheckoutProtocol = DonationCheckoutModule(),
        donationReceiptProtocol = DonationReceiptModule(),
    )
}
