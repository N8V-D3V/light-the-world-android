package com.n8vd3v.lighttheworld.features.donation.cart

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.NoOpStubDecisionLogger
import com.n8vd3v.lighttheworld.cop.StubDecisionLogger
import com.n8vd3v.lighttheworld.cop.logDecision
import com.n8vd3v.lighttheworld.features.donation.DonationCart
import com.n8vd3v.lighttheworld.features.donation.DonationOptionAvailabilityStatus
import com.n8vd3v.lighttheworld.features.donation.DonationSelection
import com.n8vd3v.lighttheworld.features.donation.DonationSelectionSummary
import com.n8vd3v.lighttheworld.features.donation.DonationSelectionSummaryItem
import java.math.BigDecimal

class StubDonationCartStore(
    private val logger: StubDecisionLogger = NoOpStubDecisionLogger,
) : DonationCartStore {

    private val cartItems = mutableListOf<DonationSelection>()

    override fun addSelection(selection: DonationSelection): DonationCartResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "add_selection_input",
            details = mapOf("selection" to selection),
        )
        val failure = validateSelection(selection)
        if (failure != null) {
            return buildResponse(failure, "add_selection_rejected")
        }

        cartItems.removeAll { it.selectedOptionId == selection.selectedOptionId }
        cartItems += selection
        return buildResponse(null, "selection_added")
    }

    override fun updateSelection(selection: DonationSelection): DonationCartResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "update_selection_input",
            details = mapOf("selection" to selection),
        )
        val failure = validateSelection(selection) ?: validateExistingSelection(selection.selectedOptionId)
        if (failure != null) {
            return buildResponse(failure, "update_selection_rejected")
        }

        cartItems.replaceAll { current ->
            if (current.selectedOptionId == selection.selectedOptionId) selection else current
        }
        return buildResponse(null, "selection_updated")
    }

    override fun removeSelection(selectedOptionId: String): DonationCartResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "remove_selection_input",
            details = mapOf("selectedOptionId" to selectedOptionId),
        )

        val failure =
            if (selectedOptionId.isBlank()) {
                CopFailureResponse(
                    reason = DonationCartFailureReason.DONATION_SELECTION_INVALID,
                    message = "Donation selection identifier is required.",
                )
            } else {
                validateExistingSelection(selectedOptionId)
            }

        if (failure != null) {
            return buildResponse(failure, "remove_selection_rejected")
        }

        cartItems.removeAll { it.selectedOptionId == selectedOptionId }
        return buildResponse(null, "selection_removed")
    }

    override fun getCurrentCart(): DonationCartResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "get_current_cart_input",
            details = mapOf("itemCount" to cartItems.size),
        )
        return buildResponse(null, "cart_returned")
    }

    override fun validateCheckoutReady(): DonationCartResponse {
        logger.logDecision(
            module = MODULE_NAME,
            action = "validate_checkout_ready_input",
            details = mapOf("itemCount" to cartItems.size),
        )
        val failure =
            if (cartItems.isEmpty()) {
                CopFailureResponse(
                    reason = DonationCartFailureReason.DONATION_CART_EMPTY,
                    message = "Donation cart is empty.",
                )
            } else {
                null
            }
        return buildResponse(
            failureResponse = failure,
            decision = if (failure == null) "checkout_ready" else "checkout_rejected_empty_cart",
        )
    }

    private fun validateSelection(selection: DonationSelection): CopFailureResponse<DonationCartFailureReason>? {
        if (selection.availabilityStatus != DonationOptionAvailabilityStatus.AVAILABLE) {
            return CopFailureResponse(
                reason = DonationCartFailureReason.DONATION_OPTION_UNAVAILABLE,
                message = "Donation option ${selection.selectedOptionId} is unavailable.",
            )
        }
        if (selection.selectedOptionId.isBlank() || selection.title.isBlank()) {
            return CopFailureResponse(
                reason = DonationCartFailureReason.DONATION_SELECTION_INVALID,
                message = "Donation selection is missing a required option identifier or title.",
            )
        }
        if (selection.amount <= ZERO) {
            return CopFailureResponse(
                reason = DonationCartFailureReason.DONATION_SELECTION_INVALID,
                message = "Donation amount must be greater than zero.",
            )
        }
        if (selection.quantity != null && selection.quantity <= 0) {
            return CopFailureResponse(
                reason = DonationCartFailureReason.DONATION_SELECTION_INVALID,
                message = "Donation quantity must be greater than zero when quantity is provided.",
            )
        }
        return null
    }

    private fun validateExistingSelection(selectedOptionId: String): CopFailureResponse<DonationCartFailureReason>? =
        if (cartItems.any { it.selectedOptionId == selectedOptionId }) {
            null
        } else {
            CopFailureResponse(
                reason = DonationCartFailureReason.DONATION_SELECTION_INVALID,
                message = "Donation selection $selectedOptionId is not present in the cart.",
            )
        }

    private fun buildResponse(
        failureResponse: CopFailureResponse<DonationCartFailureReason>?,
        decision: String,
    ): DonationCartResponse {
        val items = cartItems.toList()
        val totalAmount = items.fold(ZERO) { total, item -> total + item.amount }
        val cart = DonationCart(items = items, totalAmount = totalAmount)
        val summary = DonationSelectionSummary(
            items = items.map { item ->
                DonationSelectionSummaryItem(
                    selectedOptionId = item.selectedOptionId,
                    title = item.title,
                    amount = item.amount,
                    quantity = item.quantity,
                )
            },
            totalAmount = totalAmount,
        )
        logger.logDecision(
            module = MODULE_NAME,
            action = "donation_cart_output",
            details = mapOf(
                "decision" to decision,
                "itemCount" to items.size,
                "totalAmount" to totalAmount,
                "failureReason" to failureResponse?.reason,
            ),
        )
        return DonationCartResponse(
            donationCart = cart,
            donationSelectionSummary = summary,
            failureResponse = failureResponse,
        )
    }

    companion object {
        private const val MODULE_NAME = "StubDonationCartStore"
        private val ZERO = BigDecimal.ZERO
    }
}
