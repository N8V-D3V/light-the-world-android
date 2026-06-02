package com.n8vd3v.lighttheworld.features.donation

import java.math.BigDecimal
import java.time.Instant

enum class CatalogAvailabilityState {
    AVAILABLE,
    UNAVAILABLE,
}

enum class DonationOptionAvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
}

data class DonationOption(
    val optionId: String,
    val title: String,
    val description: String,
    val availabilityStatus: DonationOptionAvailabilityStatus,
)

data class DonationSelection(
    val selectedOptionId: String,
    val title: String,
    val amount: BigDecimal,
    val quantity: Int?,
    val availabilityStatus: DonationOptionAvailabilityStatus,
)

data class DonationCart(
    val items: List<DonationSelection>,
    val totalAmount: BigDecimal,
)

data class DonationSelectionSummaryItem(
    val selectedOptionId: String,
    val title: String,
    val amount: BigDecimal,
    val quantity: Int?,
)

data class DonationSelectionSummary(
    val items: List<DonationSelectionSummaryItem>,
    val totalAmount: BigDecimal,
)

data class DonorIdentity(
    val donorName: String,
    val donorEmail: String,
)

data class PaymentDetails(
    val paymentMethodToken: String,
    val paymentMethodSummary: String,
)

enum class ReceiptDeliveryMethod {
    EMAIL,
    TEXT,
    BOTH,
    NONE,
}

data class ReceiptDeliverySelection(
    val method: ReceiptDeliveryMethod,
)

data class ReceiptDeliveryContactInformation(
    val emailAddress: String? = null,
    val phoneNumber: String? = null,
)

enum class DonationPaymentStatus {
    SUCCEEDED,
    FAILED,
}

data class DonationPaymentResult(
    val status: DonationPaymentStatus,
    val confirmationId: String?,
    val failureReason: String?,
)

enum class ReceiptDeliveryStatus {
    NOT_REQUESTED,
    PENDING,
    SENT,
    FAILED,
}

data class DonationConfirmation(
    val confirmationId: String,
    val donationTimestamp: Instant,
    val donatedItems: List<DonationSelection>,
    val lineItemAmounts: List<BigDecimal>,
    val subtotal: BigDecimal,
    val tax: BigDecimal,
    val donorAppliedFees: BigDecimal,
    val totalCharged: BigDecimal,
    val donatedAmount: BigDecimal,
    val paymentMethodSummary: String,
    val selectedOptionSummary: String,
    val receiptDeliveryMethod: ReceiptDeliveryMethod,
    val receiptDeliveryStatus: ReceiptDeliveryStatus,
)

data class ReceiptState(
    val receiptDeliveryMethod: ReceiptDeliveryMethod,
    val receiptDeliveryStatus: ReceiptDeliveryStatus,
    val emailAddress: String?,
    val phoneNumber: String?,
)
