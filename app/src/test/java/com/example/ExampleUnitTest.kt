package com.example

import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Pledge
import com.example.data.model.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun transactionModel_firestoreSerializationAndConversion() {
        val transaction = Transaction(
            id = 42L,
            transactionType = "INCOME",
            title = "Maintenance Q1 - Flat 204",
            category = "Maintenance Fee",
            amount = 4500.0,
            partyName = "Sunil Mehta",
            partyPhone = "9876543210",
            paymentMode = "UPI/Online",
            receiptNo = "REC-2026-42",
            referenceNo = "UPI-REF-998877",
            status = "COMPLETED"
        )

        // Verify Firestore map
        val map = transaction.toFirestoreMap()
        assertEquals(42L, map["localId"])
        assertEquals("INCOME", map["transactionType"])
        assertEquals(4500.0, map["amount"])
        assertEquals("Sunil Mehta", map["partyName"])
        assertEquals("UPI-REF-998877", map["referenceNo"])

        // Verify roundtrip from Firestore map
        val reconstructed = Transaction.fromFirestoreDoc("doc_tx_42", map)
        assertEquals(42L, reconstructed.id)
        assertEquals("doc_tx_42", reconstructed.firestoreId)
        assertEquals("Maintenance Q1 - Flat 204", reconstructed.title)
        assertEquals(4500.0, reconstructed.amount, 0.001)

        // Verify conversion to Donation
        val donation = transaction.toDonation()
        assertEquals("Sunil Mehta", donation.donorName)
        assertEquals(4500.0, donation.amount, 0.001)
        assertEquals("received", donation.status)
    }

    @Test
    fun pledgeModel_remainingCalculationsAndConversion() {
        val pledge = Pledge(
            id = 10L,
            donorName = "Vikas Singhal",
            phone = "9812345678",
            flatNo = "C-402",
            amount = 10000.0,
            fulfilledAmount = 4000.0,
            status = "PENDING",
            purpose = "Clubhouse Renovation"
        )

        assertEquals(6000.0, pledge.remainingAmount, 0.001)
        assertFalse(pledge.isFullyPaid)

        // When completed
        val completedPledge = pledge.copy(fulfilledAmount = 10000.0, status = "FULFILLED")
        assertEquals(0.0, completedPledge.remainingAmount, 0.001)
        assertTrue(completedPledge.isFullyPaid)

        // Firestore Map test
        val map = pledge.toFirestoreMap()
        assertEquals(10000.0, map["amount"])
        assertEquals("Clubhouse Renovation", map["purpose"])

        val reconstructed = Pledge.fromFirestoreDoc("doc_plg_10", map)
        assertEquals("Vikas Singhal", reconstructed.donorName)
        assertEquals("doc_plg_10", reconstructed.firestoreId)
    }

    @Test
    fun committeeMemberModel_rolesAndPermissions() {
        val member = CommitteeMember(
            id = 1L,
            name = "Rajesh Sharma",
            role = "President",
            phone = "9811234567",
            email = "rajesh@society.org",
            pin = "1111",
            isSuperAdmin = true,
            status = "ACTIVE"
        )

        assertTrue(member.isSuperAdmin)
        assertEquals("President", member.role)

        // Firestore Map test
        val map = member.toFirestoreMap()
        assertEquals("Rajesh Sharma", map["name"])
        assertEquals("1111", map["pin"])
        assertEquals(true, map["isSuperAdmin"])

        val reconstructed = CommitteeMember.fromFirestoreDoc("doc_cm_1", map)
        assertEquals("Rajesh Sharma", reconstructed.name)
        assertEquals("doc_cm_1", reconstructed.firestoreId)
    }

    @Test
    fun donationSearch_matchesMemberNameAmountAndReceiptId() {
        val donations = listOf(
            Donation(id = 1, donorName = "Ramesh Kumar", phone = "9811111111", amount = 5000.0, status = "received", paymentMode = "UPI", paymentDate = 1700000000000L, receiptNo = "REC-101", recordedByName = "Secretary Rajesh"),
            Donation(id = 2, donorName = "Priya Sharma", phone = "9822222222", amount = 1500.0, status = "received", paymentMode = "Cash", paymentDate = 1700000000000L, receiptNo = "REC-102", recordedByName = "Treasurer Suresh"),
            Donation(id = 3, donorName = "Amit Patel", phone = "9833333333", amount = 500.0, status = "received", paymentMode = "Bank Transfer", paymentDate = 1700000000000L, receiptNo = "REC-103", recordedByName = "Secretary Rajesh")
        )

        // 1. Search by Member Name (recordedByName)
        val memberQuery = "Rajesh"
        val memberMatches = filterDonations(donations, memberQuery)
        assertEquals(2, memberMatches.size)
        assertTrue(memberMatches.any { it.id == 1L })
        assertTrue(memberMatches.any { it.id == 3L })

        // 2. Search by Amount (numeric / with rupee symbol)
        val amountQuery = "₹1,500"
        val amountMatches = filterDonations(donations, amountQuery)
        assertEquals(1, amountMatches.size)
        assertEquals(2L, amountMatches.first().id)

        // 3. Search by Receipt ID
        val receiptQuery = "rec-101"
        val receiptMatches = filterDonations(donations, receiptQuery)
        assertEquals(1, receiptMatches.size)
        assertEquals(1L, receiptMatches.first().id)
    }

    @Test
    fun expenseSearch_matchesMemberNameAmountAndReceiptId() {
        val expenses = listOf(
            Expense(id = 1, title = "Catering for Annual Gathering", category = "Events & Festivals", amount = 12000.0, expenseDate = 1700000000000L, paidTo = "Sharma Sweets", paymentMode = "UPI", receiptUrl = "VOUCHER-501", recordedByName = "President Sharma"),
            Expense(id = 2, title = "Plumbing pipe repair", category = "Repairs", amount = 850.0, expenseDate = 1700000000000L, paidTo = "National Hardware", paymentMode = "Cash", receiptUrl = "BILL-882", recordedByName = "Treasurer Suresh")
        )

        // 1. Search by Member Name (recordedByName)
        val memberMatches = filterExpenses(expenses, "Suresh")
        assertEquals(1, memberMatches.size)
        assertEquals(2L, memberMatches.first().id)

        // 2. Search by Amount (₹850)
        val amountMatches = filterExpenses(expenses, "850")
        assertEquals(1, amountMatches.size)
        assertEquals(2L, amountMatches.first().id)

        // 3. Search by Voucher / Bill ID
        val billMatches = filterExpenses(expenses, "voucher-501")
        assertEquals(1, billMatches.size)
        assertEquals(1L, billMatches.first().id)
    }

    private fun filterDonations(list: List<Donation>, query: String): List<Donation> {
        val q = query.trim().lowercase().removePrefix("₹").removePrefix("rs.").removePrefix("rs").trim()
        val numericAmountQuery = q.replace(",", "").toDoubleOrNull()
        return list.filter { donation ->
            donation.donorName.lowercase().contains(q) ||
            donation.recordedByName.lowercase().contains(q) ||
            donation.receiptNo.lowercase().contains(q) ||
            donation.id.toString() == q ||
            (numericAmountQuery != null && (donation.amount == numericAmountQuery || donation.amount.toInt() == numericAmountQuery.toInt())) ||
            donation.amount.toString().contains(q) ||
            String.format(Locale.US, "%.0f", donation.amount).contains(q) ||
            String.format(Locale.US, "%,.0f", donation.amount).lowercase().contains(q)
        }
    }

    private fun filterExpenses(list: List<Expense>, query: String): List<Expense> {
        val q = query.trim().lowercase().removePrefix("₹").removePrefix("rs.").removePrefix("rs").trim()
        val numericAmountQuery = q.replace(",", "").toDoubleOrNull()
        return list.filter { expense ->
            expense.title.lowercase().contains(q) ||
            expense.paidTo.lowercase().contains(q) ||
            expense.recordedByName.lowercase().contains(q) ||
            expense.receiptUrl.lowercase().contains(q) ||
            expense.id.toString() == q ||
            (numericAmountQuery != null && (expense.amount == numericAmountQuery || expense.amount.toInt() == numericAmountQuery.toInt())) ||
            expense.amount.toString().contains(q) ||
            String.format(Locale.US, "%.0f", expense.amount).contains(q) ||
            String.format(Locale.US, "%,.0f", expense.amount).lowercase().contains(q)
        }
    }
}

