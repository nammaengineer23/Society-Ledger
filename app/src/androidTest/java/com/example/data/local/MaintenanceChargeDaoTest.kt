package com.example.data.local

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.MaintenanceCharge
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaintenanceChargeDaoTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val database = Room.inMemoryDatabaseBuilder(
        context,
        SocietyDatabase::class.java
    ).allowMainThreadQueries().build()

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertsAndObservesChargeForFlatAndPeriod() = runBlocking {
        val id = database.maintenanceChargeDao().insert(
            MaintenanceCharge(
                flatNo = "B-204",
                memberId = 7,
                memberName = "Test Resident",
                billingPeriod = "2026-10",
                amount = 2500.0,
                dueDate = 1_792_339_200_000,
                status = "OPEN"
            )
        )

        val saved = database.maintenanceChargeDao().findById(id)
        assertNotNull(saved)
        assertEquals("B-204", saved?.flatNo)
        assertEquals("2026-10", saved?.billingPeriod)
        assertEquals(2500.0, saved?.outstandingAmount ?: 0.0, 0.001)
        assertEquals(1, database.maintenanceChargeDao().observeForFlat("B-204").first().size)
        assertEquals(1, database.maintenanceChargeDao().observeForPeriod("2026-10").first().size)
    }

    @Test
    fun calculatesOutstandingAfterPartialExternalPayment() = runBlocking {
        database.maintenanceChargeDao().insert(
            MaintenanceCharge(
                flatNo = "C-301",
                billingPeriod = "2026-10",
                amount = 1800.0,
                amountPaid = 500.0,
                dueDate = 1_792_339_200_000,
                status = "PARTIALLY_PAID",
                paymentMode = "UPI",
                paymentReference = "UPI-TEST-001"
            )
        )

        assertEquals(1300.0, database.maintenanceChargeDao().totalOutstanding(), 0.001)
    }

    @Test
    fun rejectsDuplicateFlatPeriodAtDatabaseLevel() = runBlocking {
        val dao = database.maintenanceChargeDao()
        val charge = MaintenanceCharge(
            flatNo = "A-101",
            billingPeriod = "2026-10",
            amount = 1000.0,
            dueDate = 1_792_339_200_000
        )
        dao.insert(charge)
        var threw = false
        try {
            dao.insert(charge)
        } catch (_: Exception) {
            threw = true
        }
        assertEquals(true, threw)
    }
}
