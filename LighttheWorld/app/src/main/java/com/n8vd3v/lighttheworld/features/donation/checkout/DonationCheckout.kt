package com.n8vd3v.lighttheworld.features.donation.checkout

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.DonationCart
import com.n8vd3v.lighttheworld.features.donation.DonationConfirmation
import com.n8vd3v.lighttheworld.features.donation.DonationPaymentResult
import com.n8vd3v.lighttheworld.features.donation.DonorIdentity
import com.n8vd3v.lighttheworld.features.donation.PaymentDetails
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryContactInformation
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliverySelection

enum class DonationCheckoutFailureReason {
    DONATION_CART_EMPTY,
    DONOR_IDENTITY_REQUIRED,
    PAYMENT_DETAILS_INVALID,
    RECEIPT_CONTACT_INVALID,
    PAYMENT_FAILED,
    PAYMENT_CONFIRMATION_UNRESOLVED,
}

data class DonationCheckoutResponse(
    val paymentResult: DonationPaymentResult?,
    val donationConfirmation: DonationConfirmation?,
    val failureResponse: CopFailureResponse<DonationCheckoutFailureReason>? = null,
)

interface DonationCheckout {
    fun submitDonationPayment(
        donationCart: DonationCart,
        donorIdentity: DonorIdentity,
        paymentDetails: PaymentDetails,
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): DonationCheckoutResponse
}
