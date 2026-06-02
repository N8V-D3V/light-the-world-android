package com.n8vd3v.lighttheworld.features.donation

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubModuleLogger
import com.n8vd3v.lighttheworld.cop.StubModuleLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartFailureReason
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartProtocol
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogFailureReason
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogProtocol
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutFailureReason
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutProtocol
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptFailureReason
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptProtocol
import java.math.BigDecimal

data class DonationSelectionRequest(
    val selectedOptionId: String,
    val amount: BigDecimal,
    val quantity: Int?,
)

data class DonationCatalogExperienceResponse(
    val donationOptions: List<DonationOption>,
    val catalogFailureResponse: CopFailureResponse<DonationCatalogFailureReason>? = null,
)

data class DonationOptionDetailExperienceResponse(
    val donationOption: DonationOption?,
    val catalogFailureResponse: CopFailureResponse<DonationCatalogFailureReason>? = null,
)

data class DonationCartExperienceResponse(
    val donationCart: DonationCart,
    val donationSelectionSummary: DonationSelectionSummary,
    val catalogFailureResponse: CopFailureResponse<DonationCatalogFailureReason>? = null,
    val cartFailureResponse: CopFailureResponse<DonationCartFailureReason>? = null,
)

data class DonationCheckoutExperienceResponse(
    val donationCart: DonationCart,
    val donationSelectionSummary: DonationSelectionSummary,
    val paymentResult: DonationPaymentResult?,
    val donationConfirmation: DonationConfirmation?,
    val receiptState: ReceiptState?,
    val cartFailureResponse: CopFailureResponse<DonationCartFailureReason>? = null,
    val checkoutFailureResponse: CopFailureResponse<DonationCheckoutFailureReason>? = null,
    val receiptFailureResponse: CopFailureResponse<DonationReceiptFailureReason>? = null,
)

class GivingMachineDonationExperienceOrchestrator(
    private val donationCatalogProtocol: DonationCatalogProtocol,
    private val donationCartProtocol: DonationCartProtocol,
    private val donationCheckoutProtocol: DonationCheckoutProtocol,
    private val donationReceiptProtocol: DonationReceiptProtocol,
    private val logger: StubModuleLogger = NoOpStubModuleLogger,
) {

    fun browseDonationOptions(
        currentCatalogState: CatalogAvailabilityState,
    ): DonationCatalogExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "browse_donation_options_input",
            details = mapOf("currentCatalogState" to currentCatalogState),
        )
        val response = donationCatalogProtocol.getDonationOptionList(currentCatalogState)
        logger.logDecision(
            module = MODULE_NAME,
            action = "browse_donation_options_output",
            details = mapOf(
                "decision" to if (response.failureResponse == null) {
                    "catalog_browse_completed"
                } else {
                    "catalog_browse_rejected"
                },
                "optionCount" to response.donationOptionList.size,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return DonationCatalogExperienceResponse(
            donationOptions = response.donationOptionList,
            catalogFailureResponse = response.failureResponse,
        )
    }

    fun viewDonationOptionDetail(
        currentCatalogState: CatalogAvailabilityState,
        selectedDonationOptionId: String,
    ): DonationOptionDetailExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "view_donation_option_detail_input",
            details = mapOf(
                "currentCatalogState" to currentCatalogState,
                "selectedDonationOptionId" to selectedDonationOptionId,
            ),
        )
        val response = donationCatalogProtocol.getDonationOptionDetail(
            currentCatalogState = currentCatalogState,
            selectedDonationOptionId = selectedDonationOptionId,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "view_donation_option_detail_output",
            details = mapOf(
                "decision" to if (response.failureResponse == null) {
                    "detail_view_completed"
                } else {
                    "detail_view_rejected"
                },
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return DonationOptionDetailExperienceResponse(
            donationOption = response.donationOptionDetail,
            catalogFailureResponse = response.failureResponse,
        )
    }

    fun addDonationSelection(
        currentCatalogState: CatalogAvailabilityState,
        selectionRequest: DonationSelectionRequest,
    ): DonationCartExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "add_donation_selection_input",
            details = mapOf(
                "currentCatalogState" to currentCatalogState,
                "selectedDonationOptionId" to selectionRequest.selectedOptionId,
                "amount" to selectionRequest.amount,
                "quantity" to selectionRequest.quantity,
            ),
        )
        return resolveSelectionAndMutateCart(
            currentCatalogState = currentCatalogState,
            selectionRequest = selectionRequest,
            mutation = { selection -> donationCartProtocol.addSelection(selection) },
            successDecision = "selection_added_to_cart",
            catalogFailureDecision = "selection_add_rejected_catalog_failure",
            cartFailureDecision = "selection_add_rejected_cart_failure",
        )
    }

    fun updateDonationSelection(
        currentCatalogState: CatalogAvailabilityState,
        selectionRequest: DonationSelectionRequest,
    ): DonationCartExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "update_donation_selection_input",
            details = mapOf(
                "currentCatalogState" to currentCatalogState,
                "selectedDonationOptionId" to selectionRequest.selectedOptionId,
                "amount" to selectionRequest.amount,
                "quantity" to selectionRequest.quantity,
            ),
        )
        return resolveSelectionAndMutateCart(
            currentCatalogState = currentCatalogState,
            selectionRequest = selectionRequest,
            mutation = { selection -> donationCartProtocol.updateSelection(selection) },
            successDecision = "selection_updated_in_cart",
            catalogFailureDecision = "selection_update_rejected_catalog_failure",
            cartFailureDecision = "selection_update_rejected_cart_failure",
        )
    }

    fun removeDonationSelection(selectedDonationOptionId: String): DonationCartExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "remove_donation_selection_input",
            details = mapOf("selectedDonationOptionId" to selectedDonationOptionId),
        )
        val response = donationCartProtocol.removeSelection(selectedDonationOptionId)
        logger.logDecision(
            module = MODULE_NAME,
            action = "remove_donation_selection_output",
            details = mapOf(
                "decision" to if (response.failureResponse == null) {
                    "selection_removed"
                } else {
                    "selection_remove_rejected_cart_failure"
                },
                "itemCount" to response.donationCart.items.size,
                "failureReason" to response.failureResponse?.reason,
            ),
        )
        return DonationCartExperienceResponse(
            donationCart = response.donationCart,
            donationSelectionSummary = response.donationSelectionSummary,
            cartFailureResponse = response.failureResponse,
        )
    }

    fun reviewDonationCart(): DonationCartExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "review_donation_cart_input",
            details = emptyMap<String, Any?>(),
        )
        val response = donationCartProtocol.getCurrentCart()
        logger.logDecision(
            module = MODULE_NAME,
            action = "review_donation_cart_output",
            details = mapOf(
                "itemCount" to response.donationCart.items.size,
                "totalAmount" to response.donationCart.totalAmount,
            ),
        )
        return DonationCartExperienceResponse(
            donationCart = response.donationCart,
            donationSelectionSummary = response.donationSelectionSummary,
            cartFailureResponse = response.failureResponse,
        )
    }

    fun submitDonationCheckout(
        donorIdentity: DonorIdentity,
        paymentDetails: PaymentDetails,
        receiptDeliverySelection: ReceiptDeliverySelection,
        receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
    ): DonationCheckoutExperienceResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "submit_donation_checkout_input",
            details = mapOf(
                "donorNamePresent" to donorIdentity.donorName.isNotBlank(),
                "donorEmailPresent" to donorIdentity.donorEmail.isNotBlank(),
                "receiptDeliveryMethod" to receiptDeliverySelection.method,
            ),
        )

        val cartResponse = donationCartProtocol.validateCheckoutReady()
        if (cartResponse.failureResponse != null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "submit_donation_checkout_output",
                details = mapOf(
                    "decision" to "checkout_rejected_cart_failure",
                    "failureReason" to cartResponse.failureResponse.reason,
                ),
            )
            return DonationCheckoutExperienceResponse(
                donationCart = cartResponse.donationCart,
                donationSelectionSummary = cartResponse.donationSelectionSummary,
                paymentResult = null,
                donationConfirmation = null,
                receiptState = null,
                cartFailureResponse = cartResponse.failureResponse,
            )
        }

        val checkoutResponse = donationCheckoutProtocol.submitDonationPayment(
            donationCart = cartResponse.donationCart,
            donorIdentity = donorIdentity,
            paymentDetails = paymentDetails,
            receiptDeliverySelection = receiptDeliverySelection,
            receiptDeliveryContactInformation = receiptDeliveryContactInformation,
        )
        if (checkoutResponse.failureResponse != null || checkoutResponse.donationConfirmation == null) {
            logger.logDecision(
                module = MODULE_NAME,
                action = "submit_donation_checkout_output",
                details = mapOf(
                    "decision" to "checkout_failed_before_receipt",
                    "failureReason" to checkoutResponse.failureResponse?.reason,
                    "paymentStatus" to checkoutResponse.paymentResult?.status,
                ),
            )
            return DonationCheckoutExperienceResponse(
                donationCart = cartResponse.donationCart,
                donationSelectionSummary = cartResponse.donationSelectionSummary,
                paymentResult = checkoutResponse.paymentResult,
                donationConfirmation = checkoutResponse.donationConfirmation,
                receiptState = null,
                checkoutFailureResponse = checkoutResponse.failureResponse,
            )
        }

        val receiptResponse = donationReceiptProtocol.deliverReceipt(
            donationConfirmation = checkoutResponse.donationConfirmation,
            receiptDeliverySelection = receiptDeliverySelection,
            receiptDeliveryContactInformation = receiptDeliveryContactInformation,
        )
        val coordinatedConfirmation = mergeReceiptState(
            donationConfirmation = checkoutResponse.donationConfirmation,
            receiptState = receiptResponse.receiptState,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "submit_donation_checkout_output",
            details = mapOf(
                "decision" to if (receiptResponse.failureResponse == null) {
                    "checkout_succeeded_with_receipt_handled"
                } else {
                    "checkout_succeeded_receipt_failed"
                },
                "paymentStatus" to checkoutResponse.paymentResult?.status,
                "confirmationId" to coordinatedConfirmation?.confirmationId,
                "receiptStatus" to coordinatedConfirmation?.receiptDeliveryStatus,
                "receiptFailureReason" to receiptResponse.failureResponse?.reason,
            ),
        )
        return DonationCheckoutExperienceResponse(
            donationCart = cartResponse.donationCart,
            donationSelectionSummary = cartResponse.donationSelectionSummary,
            paymentResult = checkoutResponse.paymentResult,
            donationConfirmation = coordinatedConfirmation,
            receiptState = receiptResponse.receiptState,
            receiptFailureResponse = receiptResponse.failureResponse,
        )
    }

    private fun resolveSelectionAndMutateCart(
        currentCatalogState: CatalogAvailabilityState,
        selectionRequest: DonationSelectionRequest,
        mutation: (DonationSelection) -> com.n8vd3v.lighttheworld.features.donation.cart.DonationCartResponse,
        successDecision: String,
        catalogFailureDecision: String,
        cartFailureDecision: String,
    ): DonationCartExperienceResponse {
        val optionDetailResponse = donationCatalogProtocol.getDonationOptionDetail(
            currentCatalogState = currentCatalogState,
            selectedDonationOptionId = selectionRequest.selectedOptionId,
        )
        if (optionDetailResponse.failureResponse != null || optionDetailResponse.donationOptionDetail == null) {
            val currentCartResponse = donationCartProtocol.getCurrentCart()
            logger.logDecision(
                module = MODULE_NAME,
                action = "mutate_donation_cart_output",
                details = mapOf(
                    "decision" to catalogFailureDecision,
                    "failureReason" to optionDetailResponse.failureResponse?.reason,
                    "itemCount" to currentCartResponse.donationCart.items.size,
                ),
            )
            return DonationCartExperienceResponse(
                donationCart = currentCartResponse.donationCart,
                donationSelectionSummary = currentCartResponse.donationSelectionSummary,
                catalogFailureResponse = optionDetailResponse.failureResponse,
                cartFailureResponse = currentCartResponse.failureResponse,
            )
        }

        val selection = DonationSelection(
            selectedOptionId = optionDetailResponse.donationOptionDetail.optionId,
            title = optionDetailResponse.donationOptionDetail.title,
            amount = selectionRequest.amount,
            quantity = selectionRequest.quantity,
            availabilityStatus = optionDetailResponse.donationOptionDetail.availabilityStatus,
        )
        val cartResponse = mutation(selection)
        logger.logDecision(
            module = MODULE_NAME,
            action = "mutate_donation_cart_output",
            details = mapOf(
                "decision" to if (cartResponse.failureResponse == null) {
                    successDecision
                } else {
                    cartFailureDecision
                },
                "selectedDonationOptionId" to selection.selectedOptionId,
                "itemCount" to cartResponse.donationCart.items.size,
                "failureReason" to cartResponse.failureResponse?.reason,
            ),
        )
        return DonationCartExperienceResponse(
            donationCart = cartResponse.donationCart,
            donationSelectionSummary = cartResponse.donationSelectionSummary,
            cartFailureResponse = cartResponse.failureResponse,
        )
    }

    private fun mergeReceiptState(
        donationConfirmation: DonationConfirmation?,
        receiptState: ReceiptState?,
    ): DonationConfirmation? {
        if (donationConfirmation == null || receiptState == null) {
            return donationConfirmation
        }
        return donationConfirmation.copy(
            receiptDeliveryMethod = receiptState.receiptDeliveryMethod,
            receiptDeliveryStatus = receiptState.receiptDeliveryStatus,
        )
    }

    companion object {
        private const val MODULE_NAME = "GivingMachineDonationExperienceOrchestrator"
    }
}
