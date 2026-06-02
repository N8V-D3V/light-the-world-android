package com.n8vd3v.lighttheworld.features.donation.cart

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.DonationCart
import com.n8vd3v.lighttheworld.features.donation.DonationSelection
import com.n8vd3v.lighttheworld.features.donation.DonationSelectionSummary

enum class DonationCartFailureReason {
    DONATION_OPTION_UNAVAILABLE,
    DONATION_CART_EMPTY,
    DONATION_SELECTION_INVALID,
}

data class DonationCartResponse(
    val donationCart: DonationCart,
    val donationSelectionSummary: DonationSelectionSummary,
    val failureResponse: CopFailureResponse<DonationCartFailureReason>? = null,
)

interface DonationCartProtocol {
    fun addSelection(selection: DonationSelection): DonationCartResponse

    fun updateSelection(selection: DonationSelection): DonationCartResponse

    fun removeSelection(selectedOptionId: String): DonationCartResponse

    fun getCurrentCart(): DonationCartResponse

    fun validateCheckoutReady(): DonationCartResponse
}
