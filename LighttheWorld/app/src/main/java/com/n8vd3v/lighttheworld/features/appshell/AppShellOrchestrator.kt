package com.n8vd3v.lighttheworld.features.appshell

import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.dailychallenge.DailyServiceChallengeExperienceOrchestrator
import com.n8vd3v.lighttheworld.features.donation.GivingMachineDonationExperienceOrchestrator

enum class AppShellExperience {
    DAILY_SERVICE_CHALLENGES,
    GIVING_MACHINE_DONATIONS,
}

data class AppShellHostState(
    val availableExperiences: List<AppShellExperience>,
    val activeExperience: AppShellExperience?,
)

class AppShellOrchestrator(
    private val dailyServiceChallengeExperienceOrchestrator: DailyServiceChallengeExperienceOrchestrator,
    private val givingMachineDonationExperienceOrchestrator: GivingMachineDonationExperienceOrchestrator,
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) {

    fun hostAppShell(activeExperience: AppShellExperience? = null): AppShellHostState {
        logger.logDecision(
            module = MODULE_NAME,
            action = "host_app_shell_input",
            details = mapOf("activeExperience" to activeExperience),
        )

        val hostState = AppShellHostState(
            availableExperiences = EXPERIENCES,
            activeExperience = activeExperience,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "host_app_shell_output",
            details = mapOf(
                "activeExperience" to hostState.activeExperience,
                "availableExperienceCount" to hostState.availableExperiences.size,
            ),
        )
        return hostState
    }

    fun openExperience(experience: AppShellExperience): AppShellHostState {
        logger.logDecision(
            module = MODULE_NAME,
            action = "open_experience_input",
            details = mapOf("experience" to experience),
        )
        return hostAppShell(activeExperience = experience)
    }

    companion object {
        private const val MODULE_NAME = "AppShellOrchestrator"
        private val EXPERIENCES = listOf(
            AppShellExperience.DAILY_SERVICE_CHALLENGES,
            AppShellExperience.GIVING_MACHINE_DONATIONS,
        )
    }
}
