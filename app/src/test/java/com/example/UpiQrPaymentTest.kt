package com.example

import com.example.ui.components.buildUpiPaymentUri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiQrPaymentTest {

    @Test
    fun `test buildUpiPaymentUri generates standard UPI payment URI with amount`() {
        val upiId = "greenvalleysociety@upi"
        val societyName = "Green Valley RWS"
        val amount = 5000.0
        val note = "Donation"

        val uri = buildUpiPaymentUri(upiId, societyName, amount, note)

        assertTrue("URI must start with upi://pay?", uri.startsWith("upi://pay?"))
        assertTrue("URI must contain payee UPI ID", uri.contains("pa=greenvalleysociety@upi"))
        assertTrue("URI must contain payee name", uri.contains("pn=Green%20Valley%20RWS"))
        assertTrue("URI must contain formatted amount", uri.contains("am=5000.00"))
        assertTrue("URI must contain INR currency", uri.contains("cu=INR"))
        assertTrue("URI must contain transaction note", uri.contains("tn=Donation"))
    }

    @Test
    fun `test buildUpiPaymentUri with custom society UPI ID and open amount`() {
        val upiId = "royalpalmsaoa@icici"
        val societyName = "Royal Palms Association"
        val amount = null // Open donation amount
        val note = "Diwali_Fund"

        val uri = buildUpiPaymentUri(upiId, societyName, amount, note)

        assertTrue(uri.contains("pa=royalpalmsaoa@icici"))
        assertFalse("Open amount URI should not restrict amount parameter", uri.contains("&am="))
        assertTrue(uri.contains("cu=INR"))
    }

    @Test
    fun `test buildUpiPaymentUri falls back to default society UPI when blank`() {
        val blankUpiId = "   "
        val societyName = "Sunrise Cooperative"
        val amount = 1000.0

        val uri = buildUpiPaymentUri(blankUpiId, societyName, amount)

        assertTrue("Should fallback to default society UPI", uri.contains("pa=greenvalleysociety@upi"))
        assertTrue(uri.contains("am=1000.00"))
    }
}
