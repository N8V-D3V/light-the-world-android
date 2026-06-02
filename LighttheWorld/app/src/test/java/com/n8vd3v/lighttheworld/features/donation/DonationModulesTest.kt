package com.n8vd3v.lighttheworld.features.donation

import com.n8vd3v.lighttheworld.cop.InMemoryStubModuleLogger
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogFailureReason
import com.n8vd3v.lighttheworld.features.donation.catalog.DonationCatalogModule
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartFailureReason
import com.n8vd3v.lighttheworld.features.donation.cart.DonationCartModule
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutFailureReason
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutModule
import com.n8vd3v.lighttheworld.features.donation.checkout.DonationCheckoutStubOutcome
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptFailureReason
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptModule
import com.n8vd3v.lighttheworld.features.donation.receipt.DonationReceiptStubOutcome
import java.math.BigDecimal
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DonationModulesTest {

    @Test
    fun catalogModuleReturnsOnlyAvailableOptionsAndExplicitUnavailableFailures() {
        val logger = InMemoryStubModuleLogger()
        val module = DonationCatalogModule(logger = logger)

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
        val module = DonationCartModule()
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
    fun cartModuleTreatsMissingUpdateAndRemoveTargetsAsNoOpStubBehavior() {
        val logger = InMemoryStubModuleLogger()
        val module = DonationCartModule(logger = logger)
        module.addSelection(selection("meals", "Provide meals", "15.00"))

        val updateMissing = module.updateSelection(
            selection("winter-kit", "Supply a winter kit", "20.00"),
        )
        assertNull(updateMissing.failureResponse)
        assertEquals(1, updateMissing.donationCart.items.size)
        assertEquals("meals", updateMissing.donationCart.items.single().selectedOptionId)

        val removeMissing = module.removeSelection("winter-kit")
        assertNull(removeMissing.failureResponse)
        assertEquals(1, removeMissing.donationCart.items.size)
        assertEquals("meals", removeMissing.donationCart.items.single().selectedOptionId)
        assertTrue(
            logger.entries.any { entry ->
                entry.action == "update_selection_missing_target_decision" &&
                    entry.details["decision"] == "selection_update_no_op_target_missing"
            },
        )
        assertTrue(
            logger.entries.any { entry ->
                entry.action == "remove_selection_missing_target_decision" &&
                    entry.details["decision"] == "selection_remove_no_op_target_missing"
            },
        )
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
        val checkoutModule = DonationCheckoutModule(
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

        val receiptResponse = DonationReceiptModule(
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
        val response = DonationCheckoutModule(
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
