package com.n8vd3v.lighttheworld.features.appshell

import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeFlow
import com.n8vd3v.lighttheworld.features.dailychallenge.calendar.StubChallengeCalendar
import com.n8vd3v.lighttheworld.features.dailychallenge.progress.StubChallengeCompletionTracker
import com.n8vd3v.lighttheworld.features.dailychallenge.reminder.StubChallengeReminderScheduler
import com.n8vd3v.lighttheworld.features.dailychallenge.share.StubChallengeShareComposer
import com.n8vd3v.lighttheworld.features.donation.GivingMachineDonationFlow
import com.n8vd3v.lighttheworld.features.donation.cart.StubDonationCartStore
import com.n8vd3v.lighttheworld.features.donation.catalog.StubDonationCatalog
import com.n8vd3v.lighttheworld.features.donation.checkout.StubDonationCheckout
import com.n8vd3v.lighttheworld.features.donation.receipt.StubDonationReceiptSender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShellFlowTest {

    @Test
    fun hostAppShellKeepsBothFeatureExperiencesAvailableWithoutForcingAnActiveExperience() {
        val orchestrator = AppShellFlow(
            dailyServiceChallengeFlow = dailyExperienceOrchestrator(),
            givingMachineDonationFlow = donationExperienceOrchestrator(),
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
        val orchestrator = AppShellFlow(
            dailyServiceChallengeFlow = dailyExperienceOrchestrator(),
            givingMachineDonationFlow = donationExperienceOrchestrator(),
        )

        val hostState = orchestrator.openExperience(AppShellExperience.GIVING_MACHINE_DONATIONS)

        assertEquals(AppShellExperience.GIVING_MACHINE_DONATIONS, hostState.activeExperience)
        assertEquals(2, hostState.availableExperiences.size)
    }

    private fun dailyExperienceOrchestrator() = DailyServiceChallengeFlow(
        challengeCalendar = StubChallengeCalendar(),
        challengeCompletionTracker = StubChallengeCompletionTracker(),
        challengeReminderScheduler = StubChallengeReminderScheduler(),
        challengeShareComposer = StubChallengeShareComposer(),
    )

    private fun donationExperienceOrchestrator() = GivingMachineDonationFlow(
        donationCatalog = StubDonationCatalog(),
        donationCartStore = StubDonationCartStore(),
        donationCheckout = StubDonationCheckout(),
        donationReceiptSender = StubDonationReceiptSender(),
    )
}
