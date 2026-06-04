package com.n8vd3v.lighttheworld.features.donation

import com.n8vd3v.lighttheworld.cop.InMemoryStubDecisionLogger
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogFailureReason
import com.n8vd3v.lighttheworld.features.donation.catalog.StubDonationCatalog
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartFailureReason
import com.n8vd3v.lighttheworld.features.donation.cart.StubDonationCartStore
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutFailureReason
import com.n8vd3v.lighttheworld.features.donation.checkout.StubDonationCheckout
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutStubOutcome
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptFailureReason
import com.n8vd3v.lighttheworld.features.donation.receipt.StubDonationReceiptSender
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptStubOutcome
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DonationStubsTest {

    @Test
    fun catalogModuleReturnsOnlyAvailableOptionsAndExplicitUnavailableFailures() {
        val logger = InMemoryStubDecisionLogger()
        val module = StubDonationCatalog(logger = logger)

        val listResponse = module.getDonationOptionList(CatalogAvailabilityState.AVAILABLE)
        val unavailableDetail = module.getDonationOptionDetail(
            currentCatalogState = CatalogAvailabilityState.AVAILABLE,
            selectedDonationOptionId = "outreach-pack",
        )

        assertEquals(2, listResponse.donationOptionList.size)
        assertNull(listResponse.failureResponse)
        assertEquals(
            DonationCatalogFailureReason.DONATION_OPTION_UNAVAILABLE,
            unavailableDetail.failureResponse?.reason,
        )
        assertTrue(logger.entries.isNotEmpty())
    }

    @Test
    fun cartModuleMaintainsMultipleSelectionsAndRejectsEmptyCheckout() {
        val module = StubDonationCartStore()
        val meals = selection("meals", "Provide meals", "15.00")
        val kit = selection("winter-kit", "Supply a winter kit", "20.00")

        module.addSelection(meals)
        val afterSecondAdd = module.addSelection(kit)
        assertNull(afterSecondAdd.failureResponse)
        assertEquals(2, afterSecondAdd.donationCart.items.size)
        assertMoneyEquals("35.00", afterSecondAdd.donationCart.totalAmount)

        val updated = module.updateSelection(kit.copy(amount = BigDecimal("25.00")))
        assertNull(updated.failureResponse)
        assertMoneyEquals("40.00", updated.donationCart.totalAmount)

        module.removeSelection("meals")
        val afterFinalRemoval = module.removeSelection("winter-kit")
        assertEquals(0, afterFinalRemoval.donationCart.items.size)

        val validateEmpty = module.validateCheckoutReady()
        assertEquals(
            DonationCartFailureReason.DONATION_CART_EMPTY,
            validateEmpty.failureResponse?.reason,
        )
    }

    @Test
    fun cartModuleReturnsExplicitFailureForMissingUpdateTarget() {
        val module = StubDonationCartStore()
        module.addSelection(selection("meals", "Provide meals", "15.00"))

        val updateMissing = module.updateSelection(
            selection("winter-kit", "Supply a winter kit", "20.00"),
        )

        assertEquals(
            DonationCartFailureReason.DONATION_SELECTION_INVALID,
            updateMissing.failureResponse?.reason,
        )
        assertEquals(1, updateMissing.donationCart.items.size)
        assertEquals("meals", updateMissing.donationCart.items.single().selectedOptionId)
    }

    @Test
    fun cartModuleReturnsExplicitFailureForMissingRemoveTarget() {
        val module = StubDonationCartStore()
        module.addSelection(selection("meals", "Provide meals", "15.00"))

        val removeMissing = module.removeSelection("winter-kit")
        assertEquals(
            DonationCartFailureReason.DONATION_SELECTION_INVALID,
            removeMissing.failureResponse?.reason,
        )
        assertEquals(1, removeMissing.donationCart.items.size)
        assertEquals("meals", removeMissing.donationCart.items.single().selectedOptionId)
    }

    @Test
    fun checkoutModuleSeparatesDonationSuccessFromLaterReceiptFailure() {
        val cart = DonationCart(
            items = listOf(
                selection("meals", "Provide meals", "15.00"),
                selection("winter-kit", "Supply a winter kit", "20.00"),
            ),
            totalAmount = BigDecimal("35.00"),
        )
        val checkoutModule = StubDonationCheckout(
            stubOutcome = DonationCheckoutStubOutcome.SUCCEED,
            timestampProvider = { Instant.parse("2026-12-12T20:15:30Z") },
            confirmationIdProvider = { "confirm-123" },
        )
        val checkoutResponse = checkoutModule.submitDonationPayment(
            donationCart = cart,
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

        assertNull(checkoutResponse.failureResponse)
        assertEquals("confirm-123", checkoutResponse.donationConfirmation?.confirmationId)
        assertEquals(ReceiptDeliveryStatus.PENDING, checkoutResponse.donationConfirmation?.receiptDeliveryStatus)
        assertEquals(DonationPaymentStatus.SUCCEEDED, checkoutResponse.paymentResult?.status)

        val receiptResponse = StubDonationReceiptSender(
            stubOutcome = DonationReceiptStubOutcome.DELIVERY_FAILED,
        ).deliverReceipt(
            donationConfirmation = checkoutResponse.donationConfirmation,
            receiptDeliverySelection = ReceiptDeliverySelection(ReceiptDeliveryMethod.BOTH),
            receiptDeliveryContactInformation = ReceiptDeliveryContactInformation(
                emailAddress = "jane@example.com",
                phoneNumber = "555-111-2222",
            ),
        )

        assertEquals(
            DonationReceiptFailureReason.RECEIPT_DELIVERY_FAILED,
            receiptResponse.failureResponse?.reason,
        )
        assertEquals(ReceiptDeliveryStatus.FAILED, receiptResponse.receiptState?.receiptDeliveryStatus)
        assertEquals(DonationPaymentStatus.SUCCEEDED, checkoutResponse.paymentResult?.status)
    }

    @Test
    fun checkoutModuleRequiresPhoneNumberForTextReceiptDelivery() {
        val response = StubDonationCheckout(
            stubOutcome = DonationCheckoutStubOutcome.SUCCEED,
        ).submitDonationPayment(
            donationCart = DonationCart(
                items = listOf(selection("meals", "Provide meals", "15.00")),
                totalAmount = BigDecimal("15.00"),
            ),
            donorIdentity = DonorIdentity(
                donorName = "Jane Doe",
                donorEmail = "jane@example.com",
            ),
            paymentDetails = PaymentDetails(
                paymentMethodToken = "stub-card-token",
                paymentMethodSummary = "Visa ending in 4242",
            ),
            receiptDeliverySelection = ReceiptDeliverySelection(ReceiptDeliveryMethod.TEXT),
            receiptDeliveryContactInformation = ReceiptDeliveryContactInformation(
                emailAddress = "jane@example.com",
                phoneNumber = null,
            ),
        )

        assertEquals(
            DonationCheckoutFailureReason.RECEIPT_CONTACT_INVALID,
            response.failureResponse?.reason,
        )
        assertEquals(DonationPaymentStatus.FAILED, response.paymentResult?.status)
    }

    @Test
    fun checkoutModuleReturnsExplicitFailureForUnresolvedPaymentConfirmation() {
        val response = StubDonationCheckout(
            stubOutcome = DonationCheckoutStubOutcome.PAYMENT_CONFIRMATION_UNRESOLVED,
        ).submitDonationPayment(
            donationCart = DonationCart(
                items = listOf(selection("meals", "Provide meals", "15.00")),
                totalAmount = BigDecimal("15.00"),
            ),
            donorIdentity = DonorIdentity(
                donorName = "Jane Doe",
                donorEmail = "jane@example.com",
            ),
            paymentDetails = PaymentDetails(
                paymentMethodToken = "stub-card-token",
                paymentMethodSummary = "Visa ending in 4242",
            ),
            receiptDeliverySelection = ReceiptDeliverySelection(ReceiptDeliveryMethod.EMAIL),
            receiptDeliveryContactInformation = ReceiptDeliveryContactInformation(
                emailAddress = "jane@example.com",
            ),
        )

        assertEquals(
            DonationCheckoutFailureReason.PAYMENT_CONFIRMATION_UNRESOLVED,
            response.failureResponse?.reason,
        )
        assertEquals(DonationPaymentStatus.FAILED, response.paymentResult?.status)
        assertNull(response.donationConfirmation)
    }

    private fun selection(
        id: String,
        title: String,
        amount: String,
    ) = DonationSelection(
        selectedOptionId = id,
        title = title,
        amount = BigDecimal(amount),
        quantity = null,
        availabilityStatus = DonationOptionAvailabilityStatus.AVAILABLE,
    )

    private fun assertMoneyEquals(expected: String, actual: BigDecimal) {
        assertEquals(0, actual.compareTo(BigDecimal(expected)))
    }
}
