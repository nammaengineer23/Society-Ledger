package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DonationDao
import com.example.data.local.DonorDao
import com.example.data.local.ExpenseDao
import com.example.data.local.PledgeDao
import com.example.data.local.SocietyDatabase
import com.example.data.model.Donation
import com.example.data.model.Donor
import com.example.data.model.Expense
import com.example.data.model.Pledge
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDatabaseIntegrationTest {

    private lateinit var database: SocietyDatabase
    private lateinit var donationDao: DonationDao
    private lateinit var donorDao: DonorDao
    private lateinit var expenseDao: ExpenseDao
    private lateinit var pledgeDao: PledgeDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = SocietyDatabase.getInMemoryDatabase(context)
        donationDao = database.donationDao()
        donorDao = database.donorDao()
        expenseDao = database.expenseDao()
        pledgeDao = database.pledgeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDonationEntityAndDao() = runBlocking {
        val donation = Donation(
            id = 1L,
            donorName = "Vikram Sharma",
            phone = "9876543210",
            amount = 5000.0,
            status = "received",
            paymentMode = "UPI",
            paymentDate = System.currentTimeMillis(),
            receiptNo = "RCP-2026-001",
            notes = "Annual maintenance contribution"
        )

        val insertedId = donationDao.insertDonation(donation)
        assertEquals(1L, insertedId)

        val retrieved = donationDao.getDonationById(1L)
        assertNotNull(retrieved)
        assertEquals("Vikram Sharma", retrieved?.donorName)
        assertEquals(5000.0, retrieved?.amount ?: 0.0, 0.001)
        assertEquals("received", retrieved?.status)

        val allDonations = donationDao.getAllDonations().first()
        assertEquals(1, allDonations.size)

        val totalReceived = donationDao.getTotalReceivedFlow().first()
        assertEquals(5000.0, totalReceived ?: 0.0, 0.001)

        donationDao.deleteDonationById(1L)
        assertNull(donationDao.getDonationById(1L))
    }

    @Test
    fun donorDirectoryMatchesNameOrPhoneOnlyWithinItsSociety() = runBlocking {
        val firstId = donorDao.insert(
            Donor(name = "Ananya Rao", phone = "9876543210", societyId = "Temple A")
        )
        val otherSocietyId = donorDao.insert(
            Donor(name = "Ananya Rao", phone = "9876543210", societyId = "Temple B")
        )
        assertEquals(1L, firstId)
        assertEquals(2L, otherSocietyId)

        // A different phone number is a different donor even when the name matches.
        val sameNameDifferentPhone = donorDao.findExisting(
            societyId = "Temple A",
            name = "ananya rao",
            phone = "9000000000"
        )
        assertNull(sameNameDifferentPhone)

        // Phone is the primary identity when present, even if the entered name differs.
        val samePhoneDifferentName = donorDao.findExisting(
            societyId = "Temple A",
            name = "Another Name",
            phone = "9876543210"
        )
        assertEquals(firstId, samePhoneDifferentName?.id)

        // Name fallback is used only when the incoming contact has no phone.
        val sameNameWithoutPhone = donorDao.findExisting(
            societyId = "Temple A",
            name = "ananya rao",
            phone = ""
        )
        assertEquals(firstId, sameNameWithoutPhone?.id)

        val otherSocietyMatch = donorDao.findExisting(
            societyId = "Temple B",
            name = "ANANYA RAO",
            phone = "9876543210"
        )
        assertEquals(otherSocietyId, otherSocietyMatch?.id)

        assertEquals(1, donorDao.getForSociety("Temple A").size)
        assertEquals(1, donorDao.getForSociety("Temple B").size)
        assertNull(
            donorDao.findExisting(
                societyId = "Temple C",
                name = "Ananya Rao",
                phone = "9876543210"
            )
        )
    }

    @Test
    fun testExpenseEntityAndDao() = runBlocking {
        val expense = Expense(
            id = 1L,
            title = "Lift AMC Maintenance",
            category = "Maintenance",
            amount = 3500.0,
            expenseDate = System.currentTimeMillis(),
            paidTo = "Otis Elevator Service",
            paymentMode = "Bank Transfer",
            receiptUrl = "INV-7788"
        )

        val insertedId = expenseDao.insertExpense(expense)
        assertEquals(1L, insertedId)

        val retrieved = expenseDao.getExpenseById(1L)
        assertNotNull(retrieved)
        assertEquals("Lift AMC Maintenance", retrieved?.title)
        assertEquals("Maintenance", retrieved?.category)
        assertEquals(3500.0, retrieved?.amount ?: 0.0, 0.001)

        val categoryList = expenseDao.getExpensesByCategory("Maintenance").first()
        assertEquals(1, categoryList.size)
        assertEquals("Lift AMC Maintenance", categoryList[0].title)

        val totalExpenses = expenseDao.getTotalExpensesFlow().first()
        assertEquals(3500.0, totalExpenses ?: 0.0, 0.001)

        expenseDao.deleteExpenseById(1L)
        assertNull(expenseDao.getExpenseById(1L))
    }

    @Test
    fun testPledgeEntityAndDao() = runBlocking {
        val pledge = Pledge(
            id = 1L,
            donorName = "Ananya Roy",
            phone = "9123456780",
            flatNo = "B-402",
            amount = 10000.0,
            fulfilledAmount = 4000.0,
            status = "PENDING",
            purpose = "Clubhouse Renovation"
        )

        val insertedId = pledgeDao.insertPledge(pledge)
        assertEquals(1L, insertedId)

        val retrieved = pledgeDao.getPledgeById(1L)
        assertNotNull(retrieved)
        assertEquals("Ananya Roy", retrieved?.donorName)
        assertEquals("B-402", retrieved?.flatNo)
        assertEquals(10000.0, retrieved?.amount ?: 0.0, 0.001)
        assertEquals(4000.0, retrieved?.fulfilledAmount ?: 0.0, 0.001)
        assertEquals(6000.0, retrieved?.remainingAmount ?: 0.0, 0.001)

        val pendingPledges = pledgeDao.getPendingPledges().first()
        assertEquals(1, pendingPledges.size)

        val pendingAmount = pledgeDao.getTotalPendingPledgeAmountFlow().first()
        assertEquals(6000.0, pendingAmount ?: 0.0, 0.001)

        // Update to fulfilled
        val updated = retrieved!!.copy(fulfilledAmount = 10000.0, status = "FULFILLED")
        pledgeDao.updatePledge(updated)

        val fulfilledPledges = pledgeDao.getFulfilledPledges().first()
        assertEquals(1, fulfilledPledges.size)
        assertEquals(true, fulfilledPledges[0].isFullyPaid)

        pledgeDao.deletePledgeById(1L)
        assertNull(pledgeDao.getPledgeById(1L))
    }
}
