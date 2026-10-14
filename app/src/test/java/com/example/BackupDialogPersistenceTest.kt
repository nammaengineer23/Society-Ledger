package com.example

import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.components.ExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BackupDialogPersistenceTest {

    @Test
    fun `test backup JSON generation contains all persistent ledger data`() {
        val society = SocietyInfo(
            name = "Green Valley Resident Welfare Society",
            registrationNo = "REG-MH-2024-8891",
            financialYear = "FY 2026-2027",
            openingBalance = 50000.0,
            upiId = "greenvalley@upi"
        )

        val members = listOf(
            Member(id = 1, name = "Rajesh Sharma", role = "President", email = "rajesh@society.com", phone = "+919876543210"),
            Member(id = 2, name = "Priya Patel", role = "Treasurer", email = "priya@society.com", phone = "+919876543211")
        )

        val now = System.currentTimeMillis()
        val donations = listOf(
            Donation(
                id = 1,
                receiptNo = "REC-001",
                donorName = "Amit Verma",
                phone = "+919876500001",
                amount = 15000.0,
                status = "received",
                paymentMode = "UPI",
                paymentDate = now,
                recordedByName = "Priya Patel"
            ),
            Donation(
                id = 2,
                receiptNo = "REC-002",
                donorName = "Sunita Rao",
                phone = "+919876500002",
                amount = 5000.0,
                status = "pending",
                paymentMode = "Cash",
                paymentDate = now,
                recordedByName = "Rajesh Sharma"
            )
        )

        val expenses = listOf(
            Expense(
                id = 1,
                title = "Elevator Maintenance",
                category = "Repairs",
                amount = 8500.0,
                expenseDate = now,
                paidTo = "Otis Elevators",
                paymentMode = "Bank Transfer",
                recordedByName = "Rajesh Sharma"
            )
        )

        val jsonString = ExportHelper.generateJsonBackupString(society, donations, expenses, members)

        assertNotNull("Generated backup string must not be null", jsonString)
        assertTrue("Must contain schema version", jsonString.contains("\"schema_version\": \"1.0\""))
        assertTrue("Must contain society name", jsonString.contains("Green Valley Resident Welfare Society"))
        assertTrue("Must contain donation record", jsonString.contains("REC-001"))
        assertTrue("Must contain donor name", jsonString.contains("Amit Verma"))
        assertTrue("Must contain expense title", jsonString.contains("Elevator Maintenance"))
        assertTrue("Must contain committee member", jsonString.contains("Rajesh Sharma"))
    }

    @Test
    fun `test backup JSON restore recovers identical dataset accurately`() {
        val society = SocietyInfo(
            name = "Shanti Vihar Society",
            registrationNo = "REG-KA-2025-102",
            financialYear = "FY 2026-2027",
            openingBalance = 25000.0,
            upiId = "shantivihar@okhdfcbank"
        )

        val members = listOf(
            Member(id = 10, name = "Vikram Malhotra", role = "Secretary", email = "vikram@shanti.org", phone = "+919988776655")
        )

        val now = System.currentTimeMillis()
        val donations = listOf(
            Donation(
                id = 101,
                receiptNo = "SH-101",
                donorName = "Kavita Nair",
                phone = "+919876543219",
                amount = 7500.0,
                status = "received",
                paymentMode = "UPI",
                paymentDate = now
            )
        )

        val expenses = listOf(
            Expense(
                id = 201,
                title = "Security Guard Salary",
                category = "Security",
                amount = 12000.0,
                expenseDate = now,
                paidTo = "Shield Security Services",
                paymentMode = "Cheque"
            )
        )

        val jsonString = ExportHelper.generateJsonBackupString(society, donations, expenses, members)
        val restored = ExportHelper.parseJsonBackup(jsonString)

        assertTrue("Restore status must be success", restored.success)
        assertNotNull("Restored society info must exist", restored.societyInfo)
        assertEquals("Shanti Vihar Society", restored.societyInfo?.name)
        assertEquals(1, restored.donations.size)
        assertEquals("SH-101", restored.donations[0].receiptNo)
        assertEquals(7500.0, restored.donations[0].amount, 0.01)
        assertEquals(1, restored.expenses.size)
        assertEquals("Security Guard Salary", restored.expenses[0].title)
        assertEquals(12000.0, restored.expenses[0].amount, 0.01)
        assertEquals(1, restored.members.size)
        assertEquals("Vikram Malhotra", restored.members[0].name)
    }
}
