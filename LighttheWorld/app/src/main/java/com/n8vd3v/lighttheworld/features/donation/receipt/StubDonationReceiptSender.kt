package com.n8vd3v.lighttheworld.features.donation.receipt

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.DonationConfirmation
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryContactInformation
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryMethod
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliverySelection
import com.n8vd3v.lighttheworld.features.donation.ReceiptDeliveryStatus
import com.n8vd3v.lighttheworld.features.donation.ReceiptState

enum class DonationReceiptStubOutcome {
    SUCCEED,
    DELIVERY_FAILED,
}

class StubDonationReceiptSender(
    private val stubOutcome: DonationReceiptStubOutcome = DonationReceiptStubOutcome.SUCCEED,
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : DonationReceiptSender {

    override fun deliverReceipt(
        donationConfirmation: DonationConfirmation?,
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): DonationReceiptResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "deliver_receipt_input",
            details = mapOf(
                "hasDonationConfirmation" to (donationConfirmation != null),
                "receiptDeliveryMethod" to receiptDeliverySelection.method,
                "stubOutcome" to stubOutcome,
            ),
        )

        if (donationConfirmation == null) {
            val failure = CopFailureResponse(
                reason = DonationReceiptFailureReason.DONATION_CONFIRMATION_REQUIRED,
                message = "A successful donation confirmation is required before receipt delivery.",
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "deliver_receipt_output",
                details = mapOf(
                    "decision" to "receipt_rejected_missing_confirmation",
                    "failureReason" to failure.reason,
                ),
            )
            return DonationReceiptResponse(
                receiptState = null,
                failureResponse = failure,
            )
        }

        if (receiptDeliverySelection.method == ReceiptDeliveryMethod.NONE) {
            val receiptState = ReceiptState(
                receiptDeliveryMethod = ReceiptDeliveryMethod.NONE,
                receiptDeliveryStatus = ReceiptDeliveryStatus.NOT_REQUESTED,
                emailAddress = null,
                phoneNumber = null,
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "deliver_receipt_output",
                details = mapOf(
                    "decision" to "receipt_not_requested",
                    "confirmationId" to donationConfirmation.confirmationId,
                ),
            )
            return DonationReceiptResponse(receiptState = receiptState)
        }

        val contactFailure = validateContact(receiptDeliverySelection, receiptDeliveryContactInformation)
        if (contactFailure != null) {
            return DonationReceiptResponse(
                receiptState = null,
                failureResponse = contactFailure,
            ).also {
                logger.logDecision(
                    module = MODULE_NAME,
                    action = "deliver_receipt_output",
                    details = mapOf(
                        "decision" to "receipt_rejected_invalid_contact",
                        "failureReason" to contactFailure.reason,
                    ),
                )
            }
        }

        return if (stubOutcome == DonationReceiptStubOutcome.DELIVERY_FAILED) {
            val failure = CopFailureResponse(
                reason = DonationReceiptFailureReason.RECEIPT_DELIVERY_FAILED,
                message = "Stub receipt delivery failed after donation success.",
            )
            val receiptState = ReceiptState(
                receiptDeliveryMethod = receiptDeliverySelection.method,
                receiptDeliveryStatus = ReceiptDeliveryStatus.FAILED,
                emailAddress = receiptDeliveryContactInformation.emailAddress,
                phoneNumber = receiptDeliveryContactInformation.phoneNumber,
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "deliver_receipt_output",
                details = mapOf(
                    "decision" to "receipt_delivery_failed",
                    "confirmationId" to donationConfirmation.confirmationId,
                    "failureReason" to failure.reason,
                ),
            )
            DonationReceiptResponse(
                receiptState = receiptState,
                failureResponse = failure,
            )
        } else {
            val receiptState = ReceiptState(
                receiptDeliveryMethod = receiptDeliverySelection.method,
                receiptDeliveryStatus = ReceiptDeliveryStatus.SENT,
                emailAddress = receiptDeliveryContactInformation.emailAddress,
                phoneNumber = receiptDeliveryContactInformation.phoneNumber,
            )
            logger.logDecision(
                module = MODULE_NAME,
                action = "deliver_receipt_output",
                details = mapOf(
                    "decision" to "receipt_sent",
                    "confirmationId" to donationConfirmation.confirmationId,
                ),
            )
            DonationReceiptResponse(receiptState = receiptState)
        }
    }

    private fun validateContact(
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): CopFailureResponse<DonationReceiptFailureReason>? =
        when (receiptDeliverySelection.method) {
            ReceiptDeliveryMethod.NONE -> null
            ReceiptDeliveryMethod.EMAIL ->
                if (receiptDeliveryContactInformation.emailAddress.isNullOrBlank()) {
                    CopFailureResponse(
                        reason = DonationReceiptFailureReason.RECEIPT_CONTACT_INVALID,
                        message = "Receipt delivery by email requires an email address.",
                    )
                } else {
                    null
                }

            ReceiptDeliveryMethod.TEXT ->
                if (receiptDeliveryContactInformation.phoneNumber.isNullOrBlank()) {
                    CopFailureResponse(
                        reason = DonationReceiptFailureReason.RECEIPT_CONTACT_INVALID,
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
                        reason = DonationReceiptFailureReason.RECEIPT_CONTACT_INVALID,
                        message = "Receipt delivery by both email and text requires both contact methods.",
                    )
                } else {
                    null
                }
        }

    companion object {
        private const val MODULE_NAME = "StubDonationReceiptSender"
    }
}
