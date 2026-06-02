package com.n8vd3v.lighttheworld.features.donation.catalog

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.CatalogAvailabilityState
import com.n8vd3v.lighttheworld.features.donation.DonationOption
import com.n8vd3v.lighttheworld.features.donation.DonationOptionAvailabilityStatus

class DonationCatalogModule(
    private val catalogSource: DonationCatalogSource = StubDonationCatalogSource(),
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) : DonationCatalogProtocol {

    override fun getDonationOptionList(currentCatalogState: CatalogAvailabilityState): DonationOptionListResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_donation_option_list_input",
            details = mapOf("currentCatalogState" to currentCatalogState),
        )

        val options = loadOptions(currentCatalogState) ?: return catalogUnavailableListResponse()
        val availableOptions = options.filter { it.availabilityStatus == DonationOptionAvailabilityStatus.AVAILABLE }
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_donation_option_list_output",
            details = mapOf(
                "decision" to "available_options_returned",
                "optionCount" to availableOptions.size,
            ),
        )
        return DonationOptionListResponse(donationOptionList = availableOptions)
    }

    override fun getDonationOptionDetail(
        currentCatalogState: CatalogAvailabilityState,
        selectedDonationOptionId: String,
    ): DonationOptionDetailResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_donation_option_detail_input",
            details = mapOf(
                "currentCatalogState" to currentCatalogState,
                "selectedDonationOptionId" to selectedDonationOptionId,
            ),
        )

        val options = loadOptions(currentCatalogState) ?: return DonationOptionDetailResponse(
            donationOptionDetail = null,
            failureResponse = catalogUnavailableFailure(),
        )

        val option = options.firstOrNull { it.optionId == selectedDonationOptionId }
        if (option == null) {
            val failure = CopFailureResponse(
                reason = DonationCatalogFailureReason.DONATION_OPTION_MISSING,
                message = "Donation option $selectedDonationOptionId is missing.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "get_donation_option_detail_output",
                details = mapOf(
                    "decision" to "option_missing",
                    "failureReason" to failure.reason,
                ),
            )
            return DonationOptionDetailResponse(
                donationOptionDetail = null,
                failureResponse = failure,
            )
        }

        if (option.availabilityStatus != DonationOptionAvailabilityStatus.AVAILABLE) {
            val failure = CopFailureResponse(
                reason = DonationCatalogFailureReason.DONATION_OPTION_UNAVAILABLE,
                message = "Donation option $selectedDonationOptionId is unavailable.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "get_donation_option_detail_output",
                details = mapOf(
                    "decision" to "option_unavailable",
                    "failureReason" to failure.reason,
                ),
            )
            return DonationOptionDetailResponse(
                donationOptionDetail = null,
                failureResponse = failure,
            )
        }

        logger.logDecision(
            module = MODULE_NAME,
            action = "get_donation_option_detail_output",
            details = mapOf(
                "decision" to "option_returned",
                "selectedDonationOptionId" to selectedDonationOptionId,
            ),
        )
        return DonationOptionDetailResponse(donationOptionDetail = option)
    }

    private fun loadOptions(currentCatalogState: CatalogAvailabilityState): List<DonationOption>? {
        if (currentCatalogState == CatalogAvailabilityState.UNAVAILABLE) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "catalog_availability_decision",
                details = mapOf("decision" to "catalog_state_unavailable"),
            )
            return null
        }
        return catalogSource.loadOptions()
    }

    private fun catalogUnavailableListResponse(): DonationOptionListResponse {
        val failure = catalogUnavailableFailure()
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_donation_option_list_output",
            details = mapOf(
                "decision" to "catalog_unavailable",
                "failureReason" to failure.reason,
            ),
        )
        return DonationOptionListResponse(
            donationOptionList = emptyList(),
            failureResponse = failure,
        )
    }

    private fun catalogUnavailableFailure() = CopFailureResponse(
        reason = DonationCatalogFailureReason.DONATION_CATALOG_UNAVAILABLE,
        message = "Donation catalog content is unavailable.",
    )

    interface DonationCatalogSource {
        fun loadOptions(): List<DonationOption>?
    }

    class StubDonationCatalogSource(
        private val options: List<DonationOption> = defaultOptions(),
        private val catalogAvailable: Boolean = true,
    ) : DonationCatalogSource {
        override fun loadOptions(): List<DonationOption>? = if (catalogAvailable) options else null
    }

    companion object {
        private const val MODULE_NAME = "DonationCatalogModule"

        fun defaultOptions(): List<DonationOption> = listOf(
            DonationOption(
                optionId = "meals",
                title = "Provide meals",
                description = "Stub donation option that represents meal support.",
                availabilityStatus = DonationOptionAvailabilityStatus.AVAILABLE,
            ),
            DonationOption(
                optionId = "winter-kit",
                title = "Supply a winter kit",
                description = "Stub donation option that represents winter supplies.",
                availabilityStatus = DonationOptionAvailabilityStatus.AVAILABLE,
            ),
            DonationOption(
                optionId = "outreach-pack",
                title = "Fund an outreach pack",
                description = "Stub donation option that is intentionally unavailable for failure-path coverage.",
                availabilityStatus = DonationOptionAvailabilityStatus.UNAVAILABLE,
            ),
        )
    }
}
