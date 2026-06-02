package com.n8vd3v.lighttheworld.features.donation.catalog

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.CatalogAvailabilityState
import com.n8vd3v.lighttheworld.features.donation.DonationOption

enum class DonationCatalogFailureReason {
    DONATION_CATALOG_UNAVAILABLE,
    DONATION_OPTION_UNAVAILABLE,
    DONATION_OPTION_MISSING,
}

data class DonationOptionListResponse(
    val donationOptionList: List<DonationOption>,
    val failureResponse: CopFailureResponse<DonationCatalogFailureReason>? = null,
)

data class DonationOptionDetailResponse(
    val donationOptionDetail: DonationOption?,
    val failureResponse: CopFailureResponse<DonationCatalogFailureReason>? = null,
)

interface DonationCatalogProtocol {
    fun getDonationOptionList(currentCatalogState: CatalogAvailabilityState): DonationOptionListResponse

    fun getDonationOptionDetail(
        currentCatalogState: CatalogAvailabilityState,
        selectedDonationOptionId: String,
    ): DonationOptionDetailResponse
}
