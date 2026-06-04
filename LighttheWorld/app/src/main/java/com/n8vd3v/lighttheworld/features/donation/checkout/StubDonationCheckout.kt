package com.n8vd3v.lighttheworld.features.donation.checkout

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.DonationCart
import com.n8vd3v.lighttheworld.features.donation.DonationConfirmation
import com.n8vd3v.lighttheworld.features.donation.DonationPaymentResult
import com.n8vd3v.lighttheworld.features.donation.DonationPaymentStatus
import com.n8vd3v.lighttheworld.features.donation.DonorIdentity
import com.n8vd3v.lighttheworld.features.donation.PaymentDetails
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryContactInformation
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryMethod
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliverySelection
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryStatus
import java.math.BigDecimal
import java.time.Instant

enum class DonationCheckoutStubOutcome {
    SUCCEED,
    PAYMENT_FAILED,
    PAYMENT_CONFIRMATION_UNRESOLVED,
}

class StubDonationCheckout(
    private val stubOutcome: DonationCheckoutStubOutcome = DonationCheckoutStubOutcome.SUCCEED,
    private val timestampProvider: () -> Instant = { Instant.now() },
    private val confirmationIdProvider: () -> String = { "stub-confirmation" },
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : DonationCheckout {

    override fun submitDonationPayment(
        donationCart: DonationCart,
        donorIdentity: DonorIdentity,
        paymentDetails: PaymentDetails,
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): DonationCheckoutResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "submit_donation_payment_input",
            details = mapOf(
                "cartItemCount" to donationCart.items.size,
                "totalAmount" to donationCart.totalAmount,
                "donorNamePresent" to donorIdentity.donorName.isNotBlank(),
                "donorEmailPresent" to donorIdentity.donorEmail.isNotBlank(),
                "receiptDeliveryMethod" to receiptDeliverySelection.method,
                "stubOutcome" to stubOutcome,
            ),
        )

        val validationFailure = validateInputs(
            donationCart = donationCart,
            donorIdentity = donorIdentity,
            paymentDetails = paymentDetails,
            receiptDeliverySelection = receiptDeliverySelection,
            receiptDeliveryContactInformation = receiptDeliveryContactInformation,
        )
        if (validationFailure != null) {
            return buildFailedResponse(validationFailure, "checkout_rejected_validation")
        }

        return when (stubOutcome) {
            DonationCheckoutStubOutcome.PAYMENT_FAILED -> buildFailedResponse(
                failureResponse = CopFailureResponse(
                    reason = DonationCheckoutFailureReason.PAYMENT_FAILED,
                    message = "Stub payment processing failed.",
                ),
                decision = "checkout_payment_failed",
            )

            DonationCheckoutStubOutcome.PAYMENT_CONFIRMATION_UNRESOLVED -> buildFailedResponse(
                failureResponse = CopFailureResponse(
                    reason = DonationCheckoutFailureReason.PAYMENT_CONFIRMATION_UNRESOLVED,
                    message = "Stub payment confirmation could not be resolved.",
                ),
                decision = "checkout_confirmation_unresolved",
            )

            DonationCheckoutStubOutcome.SUCCEED -> buildSuccessResponse(
                donationCart = donationCart,
                paymentDetails = paymentDetails,
                receiptDeliverySelection = receiptDeliverySelection,
            )
        }
    }

    private fun validateInputs(
        donationCart: DonationCart,
        donorIdentity: DonorIdentity,
        paymentDetails: PaymentDetails,
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): CopFailureResponse<DonationCheckoutFailureReason>? {
        if (donationCart.items.isEmpty()) {
            return CopFailureResponse(
                reason = DonationCheckoutFailureReason.DONATION_CART_EMPTY,
                message = "Donation cart is empty.",
            )
        }
        if (donorIdentity.donorName.isBlank() || donorIdentity.donorEmail.isBlank()) {
            return CopFailureResponse(
                reason = DonationCheckoutFailureReason.DONOR_IDENTITY_REQUIRED,
                message = "Donor name and donor email are required before payment submission.",
            )
        }
        if (paymentDetails.paymentMethodToken.isBlank() || paymentDetails.paymentMethodSummary.isBlank()) {
            return CopFailureResponse(
                reason = DonationCheckoutFailureReason.PAYMENT_DETAILS_INVALID,
                message = "Payment details are missing or invalid.",
            )
        }

        return when (receiptDeliverySelection.method) {
            ReceiptDeliveryMethod.NONE -> null
            ReceiptDeliveryMethod.EMAIL ->
                if (receiptDeliveryContactInformation.emailAddress.isNullOrBlank()) {
                    CopFailureResponse(
                        reason = DonationCheckoutFailureReason.RECEIPT_CONTACT_INVALID,
                        message = "Receipt delivery by email requires an email address.",
                    )
                } else {
                    null
                }

            ReceiptDeliveryMethod.TEXT ->
                if (receiptDeliveryContactInformation.phoneNumber.isNullOrBlank()) {
                    CopFailureResponse(
                        reason = DonationCheckoutFailureReason.RECEIPT_CONTACT_INVALID,
                        message = "Receipt delivery by text requires a phone number.",
                    )
                } else {
                    null
                }

            ReceiptDeliveryMethod.BOTH ->
                if (receiptDeliveryContactInformation.emailAddress.isNullOrBlank() ||
                    receiptDeliveryContactInformation.phoneNumber.isNullOrBlank()
                ) {
                    CopFailureResponse(
                        reason = DonationCheckoutFailureReason.RECEIPT_CONTACT_INVALID,
                        message = "Receipt delivery by both email and text requires both contact methods.",
                    )
                } else {
                    null
                }
        }
    }

    private fun buildFailedResponse(
        failureResponse: CopFailureResponse<DonationCheckoutFailureReason>,
        decision: String,
    ): DonationCheckoutResponse {
        val paymentResult = DonationPaymentResult(
            status = DonationPaymentStatus.FAILED,
            confirmationId = null,
            failureReason = failureResponse.reason.name,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "submit_donation_payment_output",
            details = mapOf(
                "decision" to decision,
                "paymentStatus" to paymentResult.status,
                "failureReason" to failureResponse.reason,
            ),
        )
        return DonationCheckoutResponse(
            paymentResult = paymentResult,
            donationConfirmation = null,
            failureResponse = failureResponse,
        )
    }

    private fun buildSuccessResponse(
        donationCart: DonationCart,
        paymentDetails: PaymentDetails,
        receiptDeliverySelection: ReceiptDeliverySelection,
    ): DonationCheckoutResponse {
        val confirmationId = confirmationIdProvider()
        val receiptDeliveryStatus =
            if (receiptDeliverySelection.method == ReceiptDeliveryMethod.NONE) {
                ReceiptDeliveryStatus.NOT_REQUESTED
            } else {
                ReceiptDeliveryStatus.PENDING
            }
        val confirmation = DonationConfirmation(
            confirmationId = confirmationId,
            donationTimestamp = timestampProvider(),
            donatedItems = donationCart.items,
            lineItemAmounts = donationCart.items.map { it.amount },
            subtotal = donationCart.totalAmount,
            tax = ZERO,
            donorAppliedFees = ZERO,
            totalCharged = donationCart.totalAmount,
            donatedAmount = donationCart.totalAmount,
            paymentMethodSummary = paymentDetails.paymentMethodSummary,
            selectedOptionSummary = donationCart.items.joinToString(", ") { it.title },
            receiptDeliveryMethod = receiptDeliverySelection.method,
            receiptDeliveryStatus = receiptDeliveryStatus,
        )
        val paymentResult = DonationPaymentResult(
            status = DonationPaymentStatus.SUCCEEDED,
            confirmationId = confirmation.confirmationId,
            failureReason = null,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "non_refundable_policy_decision",
            details = mapOf(
                "decision" to "non_refundable_default_applied",
                "confirmationId" to confirmation.confirmationId,
            ),
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "submit_donation_payment_output",
            details = mapOf(
                "decision" to "checkout_succeeded",
                "paymentStatus" to paymentResult.status,
                "confirmationId" to confirmation.confirmationId,
                "receiptDeliveryStatus" to confirmation.receiptDeliveryStatus,
            ),
        )
        return DonationCheckoutResponse(
            paymentResult = paymentResult,
            donationConfirmation = confirmation,
        )
    }

    companion object {
        private const val MODULE_NAME = "StubDonationCheckout"
        private val ZERO = BigDecimal.ZERO
    }
}
