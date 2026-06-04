package com.n8vd3v.lighttheworld.features.donation

import com.n8vd3v.lighttheworld.cop.CopFailureResponse
import com.n8vd3v.lighttheworld.cop.InMemoryStubDecisionLogger
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartFailureReason
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartStore
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartResponse
import com.n8vd3v.lighttheworld.features.donation.cart.StubDonationCartStore
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogFailureReason
import com.n8vd3v.lighttheworld.features.donation.catalog.StubDonationCatalog
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalog
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationOptionDetailResponse
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationOptionListResponse
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutFailureReason
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckout
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutResponse
import com.n8vd3v.lighttheworld.features.donation.checkout.StubDonationCheckout
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutStubOutcome
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptFailureReason
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptSender
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptResponse
import com.n8vd3v.lighttheworld.features.donation.receipt.StubDonationReceiptSender
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptStubOutcome
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GivingMachineDonationFlowTest {

    @Test
    fun browseDonationOptionsAndViewDetailStayOnCatalogBoundary() {
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = StubDonationCatalog(),
            donationCartStore = StubDonationCartStore(),
            donationCheckout = StubDonationCheckout(),
            donationReceiptSender = StubDonationReceiptSender(),
        )

        val browseResponse = orchestrator.browseDonationOptions(CatalogAvailabilityState.AVAILABLE)
        val detailResponse = orchestrator.viewDonationOptionDetail(
            currentCatalogState = CatalogAvailabilityState.AVAILABLE,
            selectedDonationOptionId = "meals",
        )

        assertNull(browseResponse.catalogFailureResponse)
        assertEquals(2, browseResponse.donationOptions.size)
        assertNull(detailResponse.catalogFailureResponse)
        assertEquals("Provide meals", detailResponse.donationOption?.title)
    }

    @Test
    fun addDonationSelectionRejectsCatalogFailureBeforeCartMutation() {
        val catalogProtocol = FailingDonationCatalog()
        val cartProtocol = RecordingDonationCartStore()
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = catalogProtocol,
            donationCartStore = cartProtocol,
            donationCheckout = UnusedDonationCheckout(),
            donationReceiptSender = UnusedDonationReceiptSender(),
        )

        val response = orchestrator.addDonationSelection(
            currentCatalogState = CatalogAvailabilityState.AVAILABLE,
            selectionRequest = DonationSelectionRequest(
                selectedOptionId = "missing-option",
                amount = BigDecimal("15.00"),
                quantity = null,
            ),
        )

        assertEquals(
            DonationCatalogFailureReason.DONATION_OPTION_MISSING,
            response.catalogFailureResponse?.reason,
        )
        assertEquals(0, cartProtocol.addSelectionCallCount)
        assertEquals(1, cartProtocol.getCurrentCartCallCount)
    }

    @Test
    fun updateDonationSelectionSurfacesExplicitFailureForMissingCartTarget() {
        val logger = InMemoryStubDecisionLogger()
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = StubDonationCatalog(),
            donationCartStore = StubDonationCartStore(),
            donationCheckout = StubDonationCheckout(),
            donationReceiptSender = StubDonationReceiptSender(),
            logger = logger,
        )

        val response = orchestrator.updateDonationSelection(
            currentCatalogState = CatalogAvailabilityState.AVAILABLE,
            selectionRequest = DonationSelectionRequest(
                selectedOptionId = "meals",
                amount = BigDecimal("15.00"),
                quantity = null,
            ),
        )

        assertEquals(
            DonationCartFailureReason.DONATION_SELECTION_INVALID,
            response.cartFailureResponse?.reason,
        )
        assertTrue(
            logger.entries.any { entry ->
                entry.action == "mutate_donation_cart_output" &&
                    entry.details["decision"] == "selection_update_rejected_cart_failure" &&
                    entry.details["failureReason"] == DonationCartFailureReason.DONATION_SELECTION_INVALID.name
            },
        )
    }

    @Test
    fun removeDonationSelectionSurfacesExplicitFailureForMissingCartTarget() {
        val logger = InMemoryStubDecisionLogger()
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = StubDonationCatalog(),
            donationCartStore = StubDonationCartStore(),
            donationCheckout = StubDonationCheckout(),
            donationReceiptSender = StubDonationReceiptSender(),
            logger = logger,
        )

        val response = orchestrator.removeDonationSelection("meals")

        assertEquals(
            DonationCartFailureReason.DONATION_SELECTION_INVALID,
            response.cartFailureResponse?.reason,
        )
        assertTrue(
            logger.entries.any { entry ->
                entry.action == "remove_donation_selection_output" &&
                    entry.details["decision"] == "selection_remove_rejected_cart_failure" &&
                    entry.details["failureReason"] == DonationCartFailureReason.DONATION_SELECTION_INVALID.name
            },
        )
    }

    @Test
    fun submitDonationCheckoutInvokesReceiptOnlyAfterSuccessfulCheckout() {
        val events = mutableListOf<String>()
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = FailingDonationCatalog(),
            donationCartStore = SuccessfulCheckoutCartStore(events),
            donationCheckout = SuccessfulDonationCheckout(events),
            donationReceiptSender = SuccessfulDonationReceiptSender(events),
        )

        val response = orchestrator.submitDonationCheckout(
            donorIdentity = DonorIdentity(
                donorName = "Jane Doe",
                donorEmail = "jane@example.com",
            ),
            paymentDetails = PaymentDetails(
                paymentMethodToken = "token",
                paymentMethodSummary = "Visa ending in 4242",
            ),
            receiptDeliverySelection = ReceiptDeliverySelection(ReceiptDeliveryMethod.EMAIL),
            receiptDeliveryContactInformation = ReceiptDeliveryContactInformation(
                emailAddress = "jane@example.com",
            ),
        )

        assertEquals(
            listOf("validateCheckoutReady", "submitDonationPayment", "deliverReceipt"),
            events,
        )
        assertEquals(DonationPaymentStatus.SUCCEEDED, response.paymentResult?.status)
        assertEquals(ReceiptDeliveryStatus.SENT, response.donationConfirmation?.receiptDeliveryStatus)
    }

    @Test
    fun submitDonationCheckoutDoesNotInvokeReceiptWhenCheckoutFails() {
        val receiptProtocol = RecordingDonationReceiptSender()
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = FailingDonationCatalog(),
            donationCartStore = SuccessfulCheckoutCartStore(mutableListOf()),
            donationCheckout = FailedDonationCheckout(),
            donationReceiptSender = receiptProtocol,
        )

        val response = orchestrator.submitDonationCheckout(
            donorIdentity = DonorIdentity(
                donorName = "Jane Doe",
                donorEmail = "jane@example.com",
            ),
            paymentDetails = PaymentDetails(
                paymentMethodToken = "token",
                paymentMethodSummary = "Visa ending in 4242",
            ),
            receiptDeliverySelection = ReceiptDeliverySelection(ReceiptDeliveryMethod.TEXT),
            receiptDeliveryContactInformation = ReceiptDeliveryContactInformation(
                phoneNumber = "555-000-1111",
            ),
        )

        assertEquals(0, receiptProtocol.callCount)
        assertEquals(DonationCheckoutFailureReason.PAYMENT_FAILED, response.checkoutFailureResponse?.reason)
        assertEquals(DonationPaymentStatus.FAILED, response.paymentResult?.status)
        assertNull(response.receiptState)
    }

    @Test
    fun submitDonationCheckoutPreservesDonationSuccessWhenReceiptDeliveryFails() {
        val orchestrator = GivingMachineDonationFlow(
            donationCatalog = StubDonationCatalog(),
            donationCartStore = StubDonationCartStore(),
            donationCheckout = StubDonationCheckout(
                stubOutcome = DonationCheckoutStubOutcome.SUCCEED,
                timestampProvider = { Instant.parse("2026-12-12T20:15:30Z") },
                confirmationIdProvider = { "confirm-123" },
            ),
            donationReceiptSender = StubDonationReceiptSender(
                stubOutcome = DonationReceiptStubOutcome.DELIVERY_FAILED,
            ),
        )

        orchestrator.addDonationSelection(
            currentCatalogState = CatalogAvailabilityState.AVAILABLE,
            selectionRequest = DonationSelectionRequest(
                selectedOptionId = "meals",
                amount = BigDecimal("15.00"),
                quantity = null,
            ),
        )

        val response = orchestrator.submitDonationCheckout(
            donorIdentity = DonorIdentity(
                donorName = "Jane Doe",
                donorEmail = "jane@example.com",
            ),
            paymentDetails = PaymentDetails(
                paymentMethodToken = "stub-card-token",
                paymentMethodSummary = "Visa ending in 4242",
            ),
            receiptDeliverySelection = ReceiptDeliverySelection(ReceiptDeliveryMethod.BOTH),
            receiptDeliveryContactInformation = ReceiptDeliveryContactInformation(
                emailAddress = "jane@example.com",
                phoneNumber = "555-111-2222",
            ),
        )

        assertEquals(DonationPaymentStatus.SUCCEEDED, response.paymentResult?.status)
        assertEquals("confirm-123", response.donationConfirmation?.confirmationId)
        assertEquals(ReceiptDeliveryStatus.FAILED, response.donationConfirmation?.receiptDeliveryStatus)
        assertEquals(
            DonationReceiptFailureReason.RECEIPT_DELIVERY_FAILED,
            response.receiptFailureResponse?.reason,
        )
        assertTrue(response.receiptState?.receiptDeliveryStatus == ReceiptDeliveryStatus.FAILED)
    }

    private class FailingDonationCatalog : DonationCatalog {
        override fun getDonationOptionList(currentCatalogState: CatalogAvailabilityState): DonationOptionListResponse =
            DonationOptionListResponse(
                donationOptionList = emptyList(),
                failureResponse = CopFailureResponse(
                    reason = DonationCatalogFailureReason.DONATION_CATALOG_UNAVAILABLE,
                    message = "Catalog unavailable.",
                ),
            )

        override fun getDonationOptionDetail(
            currentCatalogState: CatalogAvailabilityState,
            selectedDonationOptionId: String,
        ): DonationOptionDetailResponse = DonationOptionDetailResponse(
            donationOptionDetail = null,
            failureResponse = CopFailureResponse(
                reason = DonationCatalogFailureReason.DONATION_OPTION_MISSING,
                message = "Option missing.",
            ),
        )
    }

    private class RecordingDonationCartStore : DonationCartStore {
        var addSelectionCallCount: Int = 0
        var getCurrentCartCallCount: Int = 0

        override fun addSelection(selection: DonationSelection): DonationCartResponse {
            addSelectionCallCount += 1
            return emptyCartResponse()
        }

        override fun updateSelection(selection: DonationSelection): DonationCartResponse = emptyCartResponse()

        override fun removeSelection(selectedOptionId: String): DonationCartResponse = emptyCartResponse()

        override fun getCurrentCart(): DonationCartResponse {
            getCurrentCartCallCount += 1
            return emptyCartResponse()
        }

        override fun validateCheckoutReady(): DonationCartResponse = emptyCartResponse()

        private fun emptyCartResponse() = DonationCartResponse(
            donationCart = DonationCart(emptyList(), BigDecimal.ZERO),
            donationSelectionSummary = DonationSelectionSummary(emptyList(), BigDecimal.ZERO),
        )
    }

    private class SuccessfulCheckoutCartStore(
        private val events: MutableList<String>,
    ) : DonationCartStore {
        override fun addSelection(selection: DonationSelection): DonationCartResponse = checkoutReadyResponse()

        override fun updateSelection(selection: DonationSelection): DonationCartResponse = checkoutReadyResponse()

        override fun removeSelection(selectedOptionId: String): DonationCartResponse = checkoutReadyResponse()

        override fun getCurrentCart(): DonationCartResponse = checkoutReadyResponse()

        override fun validateCheckoutReady(): DonationCartResponse {
            events += "validateCheckoutReady"
            return checkoutReadyResponse()
        }

        private fun checkoutReadyResponse(): DonationCartResponse {
            val item = DonationSelection(
                selectedOptionId = "meals",
                title = "Provide meals",
                amount = BigDecimal("15.00"),
                quantity = null,
                availabilityStatus = DonationOptionAvailabilityStatus.AVAILABLE,
            )
            return DonationCartResponse(
                donationCart = DonationCart(
                    items = listOf(item),
                    totalAmount = BigDecimal("15.00"),
                ),
                donationSelectionSummary = DonationSelectionSummary(
                    items = listOf(
                        DonationSelectionSummaryItem(
                            selectedOptionId = item.selectedOptionId,
                            title = item.title,
                            amount = item.amount,
                            quantity = item.quantity,
                        ),
                    ),
                    totalAmount = BigDecimal("15.00"),
                ),
            )
        }
    }

    private class SuccessfulDonationCheckout(
        private val events: MutableList<String>,
    ) : DonationCheckout {
        override fun submitDonationPayment(
            donationCart: DonationCart,
            donorIdentity: DonorIdentity,
            paymentDetails: PaymentDetails,
            receiptDeliverySelection: ReceiptDeliverySelection,
            receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
        ): DonationCheckoutResponse {
            events += "submitDonationPayment"
            val confirmation = DonationConfirmation(
                confirmationId = "confirm-sequence",
                donationTimestamp = Instant.parse("2026-12-12T20:15:30Z"),
                donatedItems = donationCart.items,
                lineItemAmounts = donationCart.items.map { it.amount },
                subtotal = donationCart.totalAmount,
                tax = BigDecimal.ZERO,
                donorAppliedFees = BigDecimal.ZERO,
                totalCharged = donationCart.totalAmount,
                donatedAmount = donationCart.totalAmount,
                paymentMethodSummary = paymentDetails.paymentMethodSummary,
                selectedOptionSummary = donationCart.items.joinToString { it.title },
                receiptDeliveryMethod = receiptDeliverySelection.method,
                receiptDeliveryStatus = ReceiptDeliveryStatus.PENDING,
            )
            return DonationCheckoutResponse(
                paymentResult = DonationPaymentResult(
                    status = DonationPaymentStatus.SUCCEEDED,
                    confirmationId = confirmation.confirmationId,
                    failureReason = null,
                ),
                donationConfirmation = confirmation,
            )
        }
    }

    private class SuccessfulDonationReceiptSender(
        private val events: MutableList<String>,
    ) : DonationReceiptSender {
        override fun deliverReceipt(
            donationConfirmation: DonationConfirmation?,
            receiptDeliverySelection: ReceiptDeliverySelection,
            receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
        ): DonationReceiptResponse {
            events += "deliverReceipt"
            return DonationReceiptResponse(
                receiptState = ReceiptState(
                    receiptDeliveryMethod = receiptDeliverySelection.method,
                    receiptDeliveryStatus = ReceiptDeliveryStatus.SENT,
                    emailAddress = receiptDeliveryContactInformation.emailAddress,
                    phoneNumber = receiptDeliveryContactInformation.phoneNumber,
                ),
            )
        }
    }

    private class FailedDonationCheckout : DonationCheckout {
        override fun submitDonationPayment(
            donationCart: DonationCart,
            donorIdentity: DonorIdentity,
            paymentDetails: PaymentDetails,
            receiptDeliverySelection: ReceiptDeliverySelection,
            receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
        ): DonationCheckoutResponse = DonationCheckoutResponse(
            paymentResult = DonationPaymentResult(
                status = DonationPaymentStatus.FAILED,
                confirmationId = null,
                failureReason = DonationCheckoutFailureReason.PAYMENT_FAILED.name,
            ),
            donationConfirmation = null,
            failureResponse = CopFailureResponse(
                reason = DonationCheckoutFailureReason.PAYMENT_FAILED,
                message = "Payment failed.",
            ),
        )
    }

    private class RecordingDonationReceiptSender : DonationReceiptSender {
        var callCount: Int = 0

        override fun deliverReceipt(
            donationConfirmation: DonationConfirmation?,
            receiptDeliverySelection: ReceiptDeliverySelection,
            receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
        ): DonationReceiptResponse {
            callCount += 1
            return DonationReceiptResponse(
                receiptState = null,
                failureResponse = null,
            )
        }
    }

    private class UnusedDonationCheckout : DonationCheckout {
        override fun submitDonationPayment(
            donationCart: DonationCart,
            donorIdentity: DonorIdentity,
            paymentDetails: PaymentDetails,
            receiptDeliverySelection: ReceiptDeliverySelection,
            receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
        ): DonationCheckoutResponse {
            error("Checkout protocol should not be used in this test.")
        }
    }

    private class UnusedDonationReceiptSender : DonationReceiptSender {
        override fun deliverReceipt(
            donationConfirmation: DonationConfirmation?,
            receiptDeliverySelection: ReceiptDeliverySelection,
            receiptDeliveryContactInformation: ReceiptDeliveryContactInformation,
        ): DonationReceiptResponse {
            error("Receipt protocol should not be used in this test.")
        }
    }
}
