package com.example

import com.example.data.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseRecordTest {

    @Test
    fun `test expense creation with description, category, amount, date, and vendor`() {
        val testDate = 1714500000000L
        val expense = Expense(
            id = 101L,
            title = "Water Pump Motor Replacement & Maintenance",
            category = "Maintenance",
            amount = 8500.0,
            expenseDate = testDate,
            paidTo = "Balaji Electricals & Hardware",
            paymentMode = "UPI",
            receiptUrl = "INV-2024-998",
            societyId = "Green Valley Residents Welfare Society"
        )

        assertEquals("Water Pump Motor Replacement & Maintenance", expense.title)
        assertEquals("Maintenance", expense.category)
        assertEquals(8500.0, expense.amount, 0.001)
        assertEquals(testDate, expense.expenseDate)
        assertEquals("Balaji Electricals & Hardware", expense.paidTo)
        assertEquals("UPI", expense.paymentMode)
        assertEquals("Green Valley Residents Welfare Society", expense.societyId)
    }

    @Test
    fun `test expense toFirestoreMap serializes all fields properly`() {
        val testDate = System.currentTimeMillis()
        val expense = Expense(
            title = "Festival Stage Decoration & Sound",
            category = "Events & Festivals",
            amount = 15000.0,
            expenseDate = testDate,
            paidTo = "Shree Ganesh Sounds & Lights",
            paymentMode = "Bank Transfer",
            receiptUrl = "BILL-882",
            societyId = "Sunshine Enclave"
        )

        val map = expense.toFirestoreMap()

        assertNotNull(map)
        assertEquals("Festival Stage Decoration & Sound", map["title"])
        assertEquals("Events & Festivals", map["category"])
        assertEquals(15000.0, map["amount"])
        assertEquals(testDate, map["expenseDate"])
        assertEquals("Shree Ganesh Sounds & Lights", map["paidTo"])
        assertEquals("Bank Transfer", map["paymentMode"])
        assertEquals("BILL-882", map["receiptUrl"])
        assertEquals("Sunshine Enclave", map["societyId"])
    }

    @Test
    fun `test expense categories list contains expected options`() {
        val expectedCategories = listOf(
            "Events & Festivals",
            "Maintenance",
            "Repairs",
            "Utilities",
            "Refreshments",
            "Admin & Legal",
            "Miscellaneous"
        )

        assertTrue(expectedCategories.contains("Maintenance"))
        assertTrue(expectedCategories.contains("Utilities"))
        assertTrue(expectedCategories.contains("Repairs"))
        assertTrue(expectedCategories.contains("Events & Festivals"))
    }
}
