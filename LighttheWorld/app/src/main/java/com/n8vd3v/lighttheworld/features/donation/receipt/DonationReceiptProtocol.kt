package com.n8vd3v.lighttheworld.features.donation.receipt

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.features.donation.DonationConfirmation
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryContactInformation
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliverySelection
import com.n8vd3v.lighttheworld.features.donation.ReceiptState

enum class DonationReceiptFailureReason {
    RECEIPT_CONTACT_INVALID,
    RECEIPT_DELIVERY_FAILED,
    DONATION_CONFIRMATION_REQUIRED,
}

data class DonationReceiptResponse(
    val receiptState: ReceiptState?,
    val failureResponse: CopFailureResponse<DonationReceiptFailureReason>? = null,
)

interface DonationReceiptProtocol {
    fun deliverReceipt(
        donationConfirmation: DonationConfirmation?,
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): DonationReceiptResponse
}
