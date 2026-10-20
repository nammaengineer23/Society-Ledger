package com.example.data.repository

import android.util.Log
import com.example.data.local.AuditLogDao
import com.example.data.local.CommitteeMemberDao
import com.example.data.local.DonationDao
import com.example.data.local.DonorDao
import com.example.data.local.ExpenseDao
import com.example.data.local.MemberDao
import com.example.data.local.MaintenanceChargeDao
import com.example.data.local.PledgeDao
import com.example.data.local.PreloadedData
import com.example.data.local.SocietyPreferences
import com.example.data.local.TransactionDao
import com.example.data.model.AuditLog
import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Donor
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.MaintenanceCharge
import com.example.data.model.Pledge
import com.example.data.model.Society
import com.example.data.model.SocietyInfo
import com.example.data.model.Transaction
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Calendar

class SocietyRepository(
    private val memberDao: MemberDao,
    private val donationDao: DonationDao,
    private val expenseDao: ExpenseDao,
    private val auditLogDao: AuditLogDao? = null,
    private val societyPreferences: SocietyPreferences? = null,
    private val firestoreService: FirestoreService = FirestoreService(),
    private val transactionDao: TransactionDao? = null,
    private val pledgeDao: PledgeDao? = null,
    private val committeeMemberDao: CommitteeMemberDao? = null,
    private val maintenanceChargeDao: MaintenanceChargeDao? = null,
    private val donorDao: DonorDao? = null
) {
    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()
    val allDonations: Flow<List<Donation>> = donationDao.getAllDonations()
    val allDonors: Flow<List<Donor>> = donorDao?.observeForSociety(getSocietyInfo().name) ?: flowOf(emptyList())
    val pendingDonations: Flow<List<Donation>> = donationDao.getPendingDonations()
    val receivedDonations: Flow<List<Donation>> = donationDao.getReceivedDonations()
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allMaintenanceCharges: Flow<List<MaintenanceCharge>> = maintenanceChargeDao?.observeAll() ?: flowOf(emptyList())
    val allAuditLogs: Flow<List<AuditLog>> = auditLogDao?.getAllAuditLogs() ?: flowOf(emptyList())

    val allTransactions: Flow<List<Transaction>> = transactionDao?.getAllTransactions() ?: flowOf(emptyList())
    val allPledges: Flow<List<Pledge>> = pledgeDao?.getAllPledges() ?: flowOf(emptyList())
    val pendingPledgesList: Flow<List<Pledge>> = pledgeDao?.getPendingPledges() ?: flowOf(emptyList())
    val allCommitteeMembers: Flow<List<CommitteeMember>> = committeeMemberDao?.getAllCommitteeMembers() ?: flowOf(emptyList())

    /**
     * Records a maintenance assessment. This is a ledger entry only; it never
     * starts or collects an online payment.
     */
    suspend fun addMaintenanceCharge(charge: MaintenanceCharge): Long {
        val dao = maintenanceChargeDao ?: error("Maintenance ledger is not configured")
        require(charge.flatNo.isNotBlank()) { "Flat number is required" }
        require(charge.billingPeriod.matches(Regex("\\d{4}-\\d{2}"))) { "Billing period must use YYYY-MM format" }
        require(charge.amount > 0.0 && charge.amount.isFinite()) { "Charge amount must be greater than zero" }
        require(charge.amountPaid >= 0.0 && charge.amountPaid <= charge.amount) { "Paid amount must be between zero and the charge amount" }
        require(dao.findForFlatAndPeriod(charge.flatNo.trim(), charge.billingPeriod) == null) {
            "A maintenance charge already exists for this flat and billing period"
        }
        val normalized = charge.copy(
            flatNo = charge.flatNo.trim(),
            amountPaid = charge.amountPaid,
            status = when {
                charge.status == "WAIVED" -> "WAIVED"
                charge.amountPaid >= charge.amount -> "PAID"
                charge.amountPaid > 0.0 -> "PARTIALLY_PAID"
                else -> "OPEN"
            },
            updatedAt = System.currentTimeMillis()
        )
        val id = dao.insert(normalized)
        auditLogDao?.insertAuditLog(
            AuditLog(
                transactionType = "MAINTENANCE",
                transactionId = id,
                transactionRef = "${normalized.flatNo}/${normalized.billingPeriod}",
                action = "CREATED",
                reason = "Maintenance charge assessed",
                performedById = normalized.recordedById,
                performedByName = normalized.recordedByName.ifBlank { "Committee" },
                performedByRole = "Treasurer",
                newValueSummary = "amount=${normalized.amount}; paid=${normalized.amountPaid}; status=${normalized.status}"
            )
        )
        return id
    }

    /** Updates a charge/payment record without processing money or contacting a payment gateway. */
    suspend fun updateMaintenanceCharge(charge: MaintenanceCharge) {
        val dao = maintenanceChargeDao ?: error("Maintenance ledger is not configured")
        require(charge.id > 0) { "A saved maintenance charge is required" }
        require(charge.flatNo.isNotBlank()) { "Flat number is required" }
        require(charge.amount > 0.0 && charge.amount.isFinite()) { "Charge amount must be greater than zero" }
        require(charge.amountPaid >= 0.0 && charge.amountPaid <= charge.amount) { "Paid amount must be between zero and the charge amount" }
        val duplicate = dao.findForFlatAndPeriod(charge.flatNo.trim(), charge.billingPeriod)
        require(duplicate == null || duplicate.id == charge.id) {
            "A maintenance charge already exists for this flat and billing period"
        }
        val normalized = charge.copy(
            flatNo = charge.flatNo.trim(),
            status = when {
                charge.status == "WAIVED" -> "WAIVED"
                charge.amountPaid >= charge.amount -> "PAID"
                charge.amountPaid > 0.0 -> "PARTIALLY_PAID"
                else -> "OPEN"
            },
            updatedAt = System.currentTimeMillis()
        )
        val before = dao.findById(charge.id)
        dao.update(normalized)
        auditLogDao?.insertAuditLog(
            AuditLog(
                transactionType = "MAINTENANCE",
                transactionId = normalized.id,
                transactionRef = "${normalized.flatNo}/${normalized.billingPeriod}",
                action = "UPDATED",
                reason = "Maintenance charge or external payment record updated",
                performedById = normalized.recordedById,
                performedByName = normalized.recordedByName.ifBlank { "Committee" },
                performedByRole = "Treasurer",
                oldValueSummary = before?.let { "amount=${it.amount}; paid=${it.amountPaid}; status=${it.status}" }.orEmpty(),
                newValueSummary = "amount=${normalized.amount}; paid=${normalized.amountPaid}; status=${normalized.status}"
            )
        )
    }

    init {
        try {
            val info = getSocietyInfo()
            if (info.name.isNotBlank()) {
                firestoreService.activeSocietyName = info.name
            }
        } catch (_: Exception) {}
    }

    fun getSocietyInfo(): SocietyInfo {
        return societyPreferences?.getSocietyInfo() ?: SocietyInfo()
    }

    suspend fun saveSocietyInfo(info: SocietyInfo) {
        societyPreferences?.saveSocietyInfo(info)
        firestoreService.activeSocietyName = info.name
        firestoreService.saveSocietyInfo(info)
    }

    suspend fun saveSociety(society: Society) {
        val currentSaved = (societyPreferences?.getSavedDirectorySocieties() ?: emptyList()).toMutableList()
        val index = currentSaved.indexOfFirst {
            it.name.equals(society.name, ignoreCase = true) ||
            (it.registrationNo.isNotBlank() && it.registrationNo.equals(society.registrationNo, ignoreCase = true)) ||
            (it.id.isNotBlank() && it.id == society.id)
        }
        if (index >= 0) {
            currentSaved[index] = society
        } else {
            currentSaved.add(0, society)
        }
        societyPreferences?.saveDirectorySocieties(currentSaved)
        firestoreService.activeSocietyName = society.name
        firestoreService.saveSociety(society)
        firestoreService.saveSocietyInfo(society.toSocietyInfo())
    }

    suspend fun ensureInitialData() {
        if (societyPreferences?.hasCompletedInitialDemoSeed() == true) {
            return
        }
        val currentSoc = getSocietyInfo()
        // Initialize clean committee members for demo society if empty: only Admin
        if (committeeMemberDao?.getCommitteeMemberCount() == 0) {
            val president = CommitteeMember(
                id = 1L,
                name = currentSoc.presidentName.ifBlank { "Admin" },
                role = "President & Admin",
                phone = currentSoc.contactPhone,
                email = currentSoc.adminEmail,
                flatNo = "Office",
                pin = "1111",
                isSuperAdmin = true,
                status = "ACTIVE",
                responsibilities = "Executive leadership, statutory compliance",
                societyId = currentSoc.name
            )
            val initialCommittee = listOf(president)
            committeeMemberDao?.insertAll(initialCommittee)
            insertMember(president.toMember())
        }
        if (auditLogDao?.getAuditLogCount() == 0) {
            val initLog = AuditLog(
                transactionType = "SOCIETY",
                transactionRef = currentSoc.name,
                action = "INITIALIZED",
                reason = "Society initialized. All financial accounts set to zero.",
                performedByName = "President",
                performedByRole = "Admin",
                oldValueSummary = "New Society",
                newValueSummary = "Name: ${currentSoc.name} | Opening Balance: ₹0.0",
                timestamp = System.currentTimeMillis()
            )
            insertAuditLog(initLog)
        }
        societyPreferences?.markInitialDemoSeedCompleted()
    }

    /**
     * Initializes the Firestore schema with nested collections under the active society:
     * - /societies/{societyName} (Society Details)
     * - /societies/{societyName}/committee_members (1) Committee members)
     * - /societies/{societyName}/donations (2) Donations)
     * - /societies/{societyName}/expenses (3) Expenses)
     * - /societies/{societyName}/members (4) All donated members)
     * - /societies/{societyName}/audit_logs (5) Audit logs)
     */
    suspend fun initializeNestedFirestoreSchema(): Pair<Boolean, String> {
        val society = getSocietyInfo()
        val members = memberDao.getAllMembersList()
        val donations = donationDao.getAllDonationsList()
        val expenses = expenseDao.getAllExpensesList()
        val transactions = (transactionDao?.getAllTransactionsList() ?: emptyList())
        val pledges = (pledgeDao?.getAllPledgesList() ?: emptyList())
        val committeeMembers = (committeeMemberDao?.getAllCommitteeMembersList() ?: emptyList())
        val auditLogs = (auditLogDao?.getAllAuditLogsList() ?: emptyList())

        return firestoreService.initializeNestedFirestoreSchema(
            society = society,
            initialMembers = members,
            initialDonations = donations,
            initialExpenses = expenses,
            initialTransactions = transactions,
            initialPledges = pledges,
            initialCommitteeMembers = committeeMembers,
            initialAuditLogs = auditLogs
        )
    }

    suspend fun initializeNewSocietyCleanLedger(
        society: Society,
        adminPin: String
    ): Boolean {
        // 1. Clear all transaction accounts so all balances reset to 0
        donationDao.deleteAllDonations()
        expenseDao.deleteAllExpenses()
        transactionDao?.deleteAllTransactions()
        pledgeDao?.deleteAllPledges()
        memberDao.deleteAllMembers()
        committeeMemberDao?.deleteAllCommitteeMembers()
        auditLogDao?.deleteAllLogs()

        // 2. Persist Society Details
        saveSociety(society)
        val info = society.toSocietyInfo()
        saveSocietyInfo(info)
        societyPreferences?.saveSocietyPin(society.id, adminPin)
        if (society.registrationNo.isNotBlank()) {
            societyPreferences?.saveSocietyPin(society.registrationNo, adminPin)
        }
        societyPreferences?.markInitialDemoSeedCompleted()

        // 3. 1) Committee members: Initialize new society with ONLY Admin.
        // All subsequent committee members are created, allotted PINs, and modified later by the Admin.
        val president = CommitteeMember(
            id = 1,
            name = society.presidentName.ifBlank { "Admin" },
            role = "President & Admin",
            phone = society.contactPhone,
            email = society.adminEmail,
            pin = adminPin,
            isSuperAdmin = true,
            flatNo = "Office",
            status = "ACTIVE",
            responsibilities = "Executive leadership, bank cheque counter-signing, statutory compliance",
            societyId = society.name
        )
        val committeeMembersList = listOf(president)
        insertCommitteeMember(president)
        insertMember(president.toMember())

        // 4. 5) Audit logs: Insert initial audit record
        val initLog = AuditLog(
            transactionType = "SOCIETY",
            transactionRef = society.name,
            action = "CREATED",
            reason = "New society created with Admin PIN authentication. All financial accounts initialized to zero.",
            performedByName = president.name,
            performedByRole = "Admin",
            oldValueSummary = "New Society",
            newValueSummary = "Name: ${society.name} | Opening Balance: ₹${society.openingBalance}",
            timestamp = System.currentTimeMillis()
        )
        insertAuditLog(initLog)

        // 5. Initialize Firestore schema with clean zero accounts
        val result = firestoreService.initializeNestedFirestoreSchema(
            society = info,
            initialMembers = emptyList(), // 4) All donated members starts empty at 0
            initialDonations = emptyList(), // 2) Donations starts empty at 0
            initialExpenses = emptyList(), // 3) Expenses starts empty at 0
            initialTransactions = emptyList(),
            initialPledges = emptyList(),
            initialCommitteeMembers = committeeMembersList, // 1) committee members
            initialAuditLogs = listOf(initLog) // 5) audit logs
        )
        return result.first
    }

    suspend fun switchActiveSociety(society: Society) {
        val current = getSocietyInfo()
        val isDifferent = !current.name.equals(society.name, ignoreCase = true) ||
                (society.registrationNo.isNotBlank() && !current.registrationNo.equals(society.registrationNo, ignoreCase = true))
        val info = society.toSocietyInfo()
        saveSocietyInfo(info)
        firestoreService.activeSocietyName = society.name

        if (isDifferent) {
            // Clear local cache of transactions/donations/expenses/pledges so another society's data never leaks
            donationDao.deleteAllDonations()
            expenseDao.deleteAllExpenses()
            transactionDao?.deleteAllTransactions()
            pledgeDao?.deleteAllPledges()
            memberDao.deleteAllMembers()
            committeeMemberDao?.deleteAllCommitteeMembers()
            auditLogDao?.deleteAllLogs()

            // Fetch remote records belonging exclusively to this society
            try {
                val remoteMembers = firestoreService.fetchAllMembers(society.name)
                if (remoteMembers.isNotEmpty()) {
                    memberDao.insertAll(remoteMembers)
                }
                val remoteCommittee = firestoreService.fetchAllCommitteeMembers(society.name)
                if (remoteCommittee.isNotEmpty()) {
                    committeeMemberDao?.insertAll(remoteCommittee)
                } else {
                    val pin = societyPreferences?.getSocietyPin(society.id)
                        ?: societyPreferences?.getSocietyPin(society.registrationNo)
                        ?: "1111"
                    val president = CommitteeMember(
                        id = 1L,
                        name = society.presidentName.ifBlank { "Admin" },
                        role = "President & Admin",
                        phone = society.contactPhone,
                        email = society.adminEmail,
                        flatNo = "Office",
                        pin = pin,
                        isSuperAdmin = true,
                        status = "ACTIVE",
                        responsibilities = "Executive leadership, statutory compliance",
                        societyId = society.name
                    )
                    committeeMemberDao?.insertAll(listOf(president))
                    if (remoteMembers.isEmpty()) {
                        memberDao.insertMember(president.toMember())
                    }
                }

                val remoteDonations = firestoreService.fetchAllDonations(society.name)
                if (remoteDonations.isNotEmpty()) {
                    donationDao.insertAll(remoteDonations)
                }

                val remoteExpenses = firestoreService.fetchAllExpenses(society.name)
                if (remoteExpenses.isNotEmpty()) {
                    expenseDao.insertAll(remoteExpenses)
                }

                val remoteTransactions = firestoreService.fetchAllTransactions(society.name)
                if (remoteTransactions.isNotEmpty()) {
                    transactionDao?.insertAll(remoteTransactions)
                }

                val remotePledges = firestoreService.fetchAllPledges(society.name)
                if (remotePledges.isNotEmpty()) {
                    pledgeDao?.insertAll(remotePledges)
                }

                val remoteAuditLogs = firestoreService.fetchAuditLogs(society.name)
                if (remoteAuditLogs.isNotEmpty()) {
                    auditLogDao?.insertAll(remoteAuditLogs)
                }
            } catch (e: Exception) {
                Log.e("SocietyRepository", "Notice: Error fetching remote data when switching society: ${e.message}")
            }
        }
    }

    suspend fun resetToSampleData() {
        memberDao.insertAll(PreloadedData.COMMITTEE_MEMBERS)
        donationDao.insertAll(PreloadedData.INITIAL_DONATIONS)
        expenseDao.insertAll(PreloadedData.INITIAL_EXPENSES)
        transactionDao?.insertAll(PreloadedData.INITIAL_TRANSACTIONS)
        pledgeDao?.insertAll(PreloadedData.INITIAL_PLEDGES)
        committeeMemberDao?.insertAll(PreloadedData.INITIAL_COMMITTEE_MEMBERS)
        auditLogDao?.insertAll(PreloadedData.INITIAL_AUDIT_LOGS)
    }

    /**
     * Completely recreates all collections and subcollections in Firestore with the clean new schema.
     */
    suspend fun recreateAllDataWithNewStructure(): Pair<Boolean, String> {
        resetToSampleData()
        val society = getSocietyInfo()
        return firestoreService.recreateAllDataWithNewStructure(
            society = society,
            members = PreloadedData.COMMITTEE_MEMBERS,
            donations = PreloadedData.INITIAL_DONATIONS,
            expenses = PreloadedData.INITIAL_EXPENSES,
            transactions = PreloadedData.INITIAL_TRANSACTIONS,
            pledges = PreloadedData.INITIAL_PLEDGES,
            committeeMembers = PreloadedData.INITIAL_COMMITTEE_MEMBERS,
            auditLogs = PreloadedData.INITIAL_AUDIT_LOGS
        )
    }

    suspend fun clearTransactions() {
        donationDao.deleteAllDonations()
        expenseDao.deleteAllExpenses()
        auditLogDao?.deleteAllLogs()
    }

    suspend fun insertAuditLog(log: AuditLog) {
        val societyName = getSocietyInfo().name
        val localId = auditLogDao?.insertAuditLog(log.copy(societyId = societyName)) ?: 0L
        val withId = log.copy(id = if (log.id == 0L) localId else log.id, societyId = societyName)
        try {
            val firestoreId = firestoreService.saveAuditLog(withId, societyName = societyName)
            if (firestoreId != null && firestoreId.isNotBlank()) {
                val withFirestore = withId.copy(firestoreId = firestoreId)
                auditLogDao?.insertAuditLog(withFirestore)
            }
        } catch (e: Exception) {
            // Offline/fallback
        }
    }

    suspend fun fetchAuditLogsFromFirestore(): List<AuditLog> {
        return try {
            val societyName = getSocietyInfo().name
            val remoteLogs = firestoreService.fetchAuditLogs(societyName)
            if (remoteLogs.isNotEmpty()) {
                auditLogDao?.insertAll(remoteLogs)
            }
            remoteLogs
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAuditedTransactionsFromFirestore(): Pair<List<Donation>, List<Expense>> {
        return try {
            firestoreService.fetchModifiedAndDeletedTransactions(getSocietyInfo().name)
        } catch (e: Exception) {
            Pair(emptyList(), emptyList())
        }
    }

    suspend fun syncAllFromFirestore(): Boolean {
        firestoreService.resetCloudAccessStatus()
        return try {
            val currentInfo = getSocietyInfo()
            firestoreService.activeSocietyName = currentInfo.name

            // Ensure any local directory societies are stored in Firestore
            val localDirSocieties = societyPreferences?.getSavedDirectorySocieties() ?: emptyList()
            localDirSocieties.forEach { localSoc ->
                try {
                    firestoreService.saveSociety(localSoc)
                } catch (_: Exception) {}
            }

            val remoteDirSocieties = firestoreService.fetchAllDirectorySocieties()
            if (remoteDirSocieties.isNotEmpty()) {
                val currentSaved = (societyPreferences?.getSavedDirectorySocieties() ?: emptyList()).toMutableList()
                remoteDirSocieties.forEach { remote ->
                    val index = currentSaved.indexOfFirst {
                        it.name.equals(remote.name, ignoreCase = true) ||
                        (it.registrationNo.isNotBlank() && it.registrationNo.equals(remote.registrationNo, ignoreCase = true)) ||
                        (it.id.isNotBlank() && it.id == remote.id)
                    }
                    if (index >= 0) {
                        currentSaved[index] = remote
                    } else {
                        currentSaved.add(remote)
                    }
                }
                societyPreferences?.saveDirectorySocieties(currentSaved)
            }

            val remoteSocieties = firestoreService.fetchAllSocieties()
            if (remoteSocieties.isNotEmpty()) {
                val matched = remoteSocieties.find {
                    (it.registrationNo.isNotBlank() && it.registrationNo.equals(currentInfo.registrationNo, ignoreCase = true)) ||
                    it.name.equals(currentInfo.name, ignoreCase = true)
                }
                if (matched != null) {
                    societyPreferences?.saveSocietyInfo(matched)
                }
                val allSocietiesList = (societyPreferences?.getAllSocieties() ?: emptyList()).toMutableList()
                remoteSocieties.forEach { remote ->
                    if (allSocietiesList.none { it.registrationNo.equals(remote.registrationNo, ignoreCase = true) && it.name.equals(remote.name, ignoreCase = true) }) {
                        allSocietiesList.add(remote)
                    }
                }
                societyPreferences?.saveAllSocieties(allSocietiesList)
            }

            // Remove any duplicates before syncing cloud data
            deduplicateMembers()

            val remoteMembers = firestoreService.fetchAllMembers(currentInfo.name)
            if (remoteMembers.isNotEmpty()) {
                val currentLocalMembers = memberDao.getAllMembersList()
                remoteMembers.forEach { remote ->
                    // Legacy releases stored donors in the generic members collection.
                    // Keep them in the donor directory, never as committee/member accounts.
                    if (remote.role.equals("Resident Member", ignoreCase = true) ||
                        remote.role.equals("Donor", ignoreCase = true)) {
                        val donors = donorDao
                        if (donors != null && remote.name.isNotBlank()) {
                            val donorName = remote.name.trim()
                            val donorPhone = remote.phone.trim()
                            if (donors.findExisting(currentInfo.name, donorName, donorPhone) == null) {
                                donors.insert(Donor(name = donorName, phone = donorPhone, email = remote.email.trim(), societyId = currentInfo.name))
                            }
                        }
                        return@forEach
                    }
                    val existing = (if (remote.id > 0) memberDao.getMemberById(remote.id) else null)
                        ?: (if (remote.email.isNotBlank()) memberDao.getMemberByEmail(remote.email) else null)
                        ?: (if (remote.phone.isNotBlank()) memberDao.getMemberByPhone(remote.phone) else null)
                        ?: currentLocalMembers.find { it.name.trim().equals(remote.name.trim(), ignoreCase = true) }
                    if (existing != null) {
                        memberDao.updateMember(remote.copy(id = existing.id))
                    } else {
                        memberDao.insertMember(remote.copy(id = 0L))
                    }
                }
            }

            val isFirstCloudSync = !(societyPreferences?.hasSyncedCloudData() ?: false)

            val remoteDonations = firestoreService.fetchAllDonations(currentInfo.name)
            if (remoteDonations.isNotEmpty()) {
                if (isFirstCloudSync) {
                    val sampleReceipts = setOf("RCP-2026-001", "RCP-2026-002", "RCP-2026-003", "RCP-2026-004", "RCP-2026-005")
                    sampleReceipts.forEach { rcp ->
                        val sample = donationDao.getDonationByReceiptNo(rcp)
                        if (sample != null && sample.firestoreId.isBlank()) {
                            donationDao.deleteDonation(sample)
                        }
                    }
                }

                remoteDonations.forEach { remote ->
                    val existing = if (remote.firestoreId.isNotBlank()) {
                        donationDao.getDonationByFirestoreId(remote.firestoreId)
                    } else null ?: if (remote.receiptNo.isNotBlank()) {
                        donationDao.getDonationByReceiptNo(remote.receiptNo)
                    } else null

                    if (existing != null) {
                        donationDao.updateDonation(remote.copy(id = existing.id, firestoreId = remote.firestoreId.ifBlank { existing.firestoreId }))
                    } else {
                        donationDao.insertDonation(remote.copy(id = 0L))
                    }
                }
            }

            val remoteExpenses = firestoreService.fetchAllExpenses(currentInfo.name)
            if (remoteExpenses.isNotEmpty()) {
                remoteExpenses.forEach { remote ->
                    val existing = if (remote.firestoreId.isNotBlank()) {
                        expenseDao.getExpenseByFirestoreId(remote.firestoreId)
                    } else null ?: expenseDao.getExpenseByDetails(remote.title, remote.expenseDate, remote.amount)

                    if (existing != null) {
                        expenseDao.updateExpense(remote.copy(id = existing.id, firestoreId = remote.firestoreId.ifBlank { existing.firestoreId }))
                    } else {
                        expenseDao.insertExpense(remote.copy(id = 0L))
                    }
                }
            }

            val remoteTransactions = firestoreService.fetchAllTransactions(currentInfo.name)
            if (remoteTransactions.isNotEmpty() && transactionDao != null) {
                remoteTransactions.forEach { remote ->
                    val existing = if (remote.firestoreId.isNotBlank()) {
                        transactionDao.getTransactionByFirestoreId(remote.firestoreId)
                    } else null ?: if (remote.referenceNo.isNotBlank()) {
                        transactionDao.getTransactionByReferenceNo(remote.referenceNo)
                    } else null

                    if (existing != null) {
                        transactionDao.updateTransaction(remote.copy(id = existing.id, firestoreId = remote.firestoreId.ifBlank { existing.firestoreId }))
                    } else {
                        transactionDao.insertTransaction(remote.copy(id = 0L))
                    }
                }
            }

            val remoteAuditLogs = firestoreService.fetchAuditLogs(currentInfo.name)
            if (remoteAuditLogs.isNotEmpty() && auditLogDao != null) {
                remoteAuditLogs.forEach { remote ->
                    val existing = if (remote.firestoreId.isNotBlank()) {
                        auditLogDao.getAuditLogByFirestoreId(remote.firestoreId)
                    } else null

                    if (existing != null) {
                        auditLogDao.updateAuditLog(remote.copy(id = existing.id))
                    } else {
                        auditLogDao.insertAuditLog(remote.copy(id = 0L))
                    }
                }
            }

            // If Firestore is completely fresh/empty, seed the local data to Firestore only if local transactions actually exist
            if (remoteSocieties.isEmpty() && remoteMembers.isEmpty() && remoteDonations.isEmpty() && remoteExpenses.isEmpty()) {
                if (donationDao.getAllDonationsList().isNotEmpty() || expenseDao.getAllExpensesList().isNotEmpty()) {
                    uploadAllLocalDataToFirestore()
                }
            }

            societyPreferences?.setHasSyncedCloudData(true)
            true
        } catch (e: Exception) {
            Log.e("SocietyRepository", "Error during syncAllFromFirestore: ${e.message}", e)
            false
        }
    }

    suspend fun testCloudConnection(): Pair<Boolean, String> {
        return firestoreService.testCloudConnection()
    }

    suspend fun uploadAllLocalDataToFirestore(): Pair<Boolean, String> {
        firestoreService.resetCloudAccessStatus()
        firestoreService.clearLastError()
        return try {
            val society = getSocietyInfo()
            firestoreService.activeSocietyName = society.name
            val societySaved = firestoreService.saveSocietyInfo(society)

            val members = memberDao.getAllMembersList()
            var memberCount = 0
            members.forEach { member ->
                if (firestoreService.saveMember(member, societyName = society.name)) {
                    memberCount++
                }
            }

            val donations = donationDao.getAllDonationsList()
            var donationCount = 0
            donations.forEach { donation ->
                val cloudId = firestoreService.saveDonation(donation, societyName = society.name)
                if (!cloudId.isNullOrBlank()) {
                    donationCount++
                    donationDao.updateDonation(donation.copy(firestoreId = cloudId, societyId = society.name))
                }
            }

            val expenses = expenseDao.getAllExpensesList()
            var expenseCount = 0
            expenses.forEach { expense ->
                val cloudId = firestoreService.saveExpense(expense, societyName = society.name)
                if (!cloudId.isNullOrBlank()) {
                    expenseCount++
                    expenseDao.updateExpense(expense.copy(firestoreId = cloudId, societyId = society.name))
                }
            }

            val transactions = transactionDao?.getAllTransactionsList() ?: emptyList()
            var transactionCount = 0
            transactions.forEach { txn ->
                val cloudId = firestoreService.saveTransaction(txn, societyName = society.name)
                if (!cloudId.isNullOrBlank()) {
                    transactionCount++
                    transactionDao?.updateTransaction(txn.copy(firestoreId = cloudId, societyId = society.name))
                }
            }

            if (!societySaved && memberCount == 0 && donationCount == 0 && expenseCount == 0 && transactionCount == 0) {
                val err = firestoreService.lastCloudError ?: "Permission denied or network unreachable"
                Pair(false, "Cloud upload blocked: $err")
            } else {
                societyPreferences?.setHasSyncedCloudData(true)
                Pair(true, "Successfully uploaded to '${society.name}': Society profile, $memberCount members, $donationCount donations, $expenseCount expenses, and $transactionCount transactions to Cloud!")
            }
        } catch (e: Exception) {
            Log.e("SocietyRepository", "Error uploading to Firestore: ${e.message}", e)
            Pair(false, "Upload failed: ${e.message}")
        }
    }

    suspend fun insertAllDonations(donations: List<Donation>) {
        if (donations.isNotEmpty()) {
            donationDao.insertAll(donations)
        }
    }

    suspend fun insertAllExpenses(expenses: List<Expense>) {
        if (expenses.isNotEmpty()) {
            expenseDao.insertAll(expenses)
        }
    }

    suspend fun deduplicateMembers() {
        try {
            val all = memberDao.getAllMembersList()
            val seenNames = mutableSetOf<String>()
            val duplicates = mutableListOf<Member>()
            for (m in all) {
                val key = m.name.trim().lowercase()
                if (key.isNotEmpty() && !seenNames.add(key)) {
                    duplicates.add(m)
                }
            }
            duplicates.forEach { dup ->
                memberDao.deleteMember(dup)
                committeeMemberDao?.deleteCommitteeMemberById(dup.id)
            }
        } catch (_: Exception) {}
    }

    suspend fun insertMember(member: Member): Long {
        val societyName = getSocietyInfo().name
        val id = memberDao.insertMember(member)
        val saved = member.copy(id = id)
        firestoreService.saveMember(saved, societyName = societyName)
        try {
            val cm = CommitteeMember.fromMember(saved).copy(societyId = societyName)
            committeeMemberDao?.insertCommitteeMember(cm)
            firestoreService.saveCommitteeMember(cm, societyName = societyName)
        } catch (_: Exception) {}
        return id
    }

    suspend fun updateMember(member: Member) {
        val societyName = getSocietyInfo().name
        memberDao.updateMember(member)
        firestoreService.saveMember(member, societyName = societyName)
        try {
            val cm = CommitteeMember.fromMember(member).copy(societyId = societyName)
            committeeMemberDao?.updateCommitteeMember(cm)
            firestoreService.saveCommitteeMember(cm, societyName = societyName)
        } catch (_: Exception) {}
    }

    suspend fun updateMemberPin(memberId: Long, newPin: String): Boolean {
        val societyName = getSocietyInfo().name
        val member = memberDao.getMemberById(memberId) ?: return false
        val updated = member.copy(pin = newPin)
        memberDao.updateMember(updated)
        firestoreService.saveMember(updated, societyName = societyName)
        try {
            val cm = committeeMemberDao?.getCommitteeMemberById(memberId)
            if (cm != null) {
                val updatedCm = cm.copy(pin = newPin)
                committeeMemberDao?.updateCommitteeMember(updatedCm)
                firestoreService.saveCommitteeMember(updatedCm, societyName = societyName)
            }
        } catch (_: Exception) {}
        return true
    }

    suspend fun resetMemberPin(memberId: Long): Boolean {
        val societyName = getSocietyInfo().name
        val member = memberDao.getMemberById(memberId) ?: return false
        val updated = member.copy(pin = "1234")
        memberDao.updateMember(updated)
        firestoreService.saveMember(updated, societyName = societyName)
        try {
            val cm = committeeMemberDao?.getCommitteeMemberById(memberId)
            if (cm != null) {
                val updatedCm = cm.copy(pin = "1234")
                committeeMemberDao?.updateCommitteeMember(updatedCm)
                firestoreService.saveCommitteeMember(updatedCm, societyName = societyName)
            }
        } catch (_: Exception) {}
        return true
    }

    suspend fun deleteMember(member: Member) {
        val societyName = getSocietyInfo().name
        memberDao.deleteMember(member)
        firestoreService.deleteMember(member.id, member.email, societyName = societyName)
        try {
            val cm = committeeMemberDao?.getCommitteeMemberById(member.id)
            if (cm != null) {
                deleteCommitteeMember(cm)
            } else {
                committeeMemberDao?.deleteCommitteeMemberById(member.id)
            }
        } catch (_: Exception) {}
    }
    suspend fun getMemberById(id: Long): Member? = memberDao.getMemberById(id)
    suspend fun getMemberByEmail(email: String): Member? = memberDao.getMemberByEmail(email)
    suspend fun getMemberByPhone(phone: String): Member? = memberDao.getMemberByPhone(phone)

    suspend fun insertDonation(
        donation: Donation,
        userId: String = "",
        userEmail: String = ""
    ): Long {
        val societyName = getSocietyInfo().name
        val localId = donationDao.insertDonation(donation.copy(societyId = societyName))
        val withLocalId = donation.copy(id = localId, societyId = societyName)
        val firestoreDocId = firestoreService.saveDonation(withLocalId, userId, userEmail, societyName = societyName)
        if (firestoreDocId != null) {
            val withFirestore = withLocalId.copy(
                firestoreId = firestoreDocId,
                userId = userId,
                userEmail = userEmail
            )
            donationDao.updateDonation(withFirestore)
        }

        // Donors are maintained in their own list. Never add a donor to Member
        // or CommitteeMember: a donation does not grant committee membership.
        val donors = donorDao
        if (donors != null && donation.donorName.isNotBlank()) {
            try {
                val donorName = donation.donorName.trim()
                val donorPhone = donation.phone.trim()
                val donorEmail = userEmail.ifBlank { donation.userEmail }.trim()
                val existing = donors.findExisting(societyName, donorName, donorPhone)
                if (existing == null) {
                    donors.insert(
                        Donor(
                            name = donorName,
                            phone = donorPhone,
                            email = donorEmail,
                            societyId = societyName
                        )
                    )
                } else if (existing.phone != donorPhone || existing.email != donorEmail) {
                    donors.updateContact(
                        id = existing.id,
                        phone = donorPhone.ifBlank { existing.phone },
                        email = donorEmail.ifBlank { existing.email },
                        updatedAt = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.w("SocietyRepository", "Donation saved, but donor directory update failed: ${e.message}")
            }
        }

        return localId
    }

    /**
     * Standard update
     */
    suspend fun updateDonation(donation: Donation) {
        val societyName = getSocietyInfo().name
        donationDao.updateDonation(donation)
        firestoreService.updateDonation(donation, societyName = societyName)
    }

    /**
     * Super Admin modification of Donation with compulsory reason and audit log
     */
    suspend fun superAdminModifyDonation(
        originalDonation: Donation,
        updatedDonation: Donation,
        reason: String,
        superAdmin: Member
    ) {
        val now = System.currentTimeMillis()
        val audited = updatedDonation.copy(
            isModified = true,
            modificationReason = reason.trim(),
            modifiedAt = now,
            modifiedByName = "${superAdmin.name} (${superAdmin.role})",
            updatedAt = now
        )
        updateDonation(audited)

        // Log into AuditLog
        val oldSummary = "Amount: ₹${originalDonation.amount} | Donor: ${originalDonation.donorName} | Mode: ${originalDonation.paymentMode} | Status: ${originalDonation.status}"
        val newSummary = "Amount: ₹${updatedDonation.amount} | Donor: ${updatedDonation.donorName} | Mode: ${updatedDonation.paymentMode} | Status: ${updatedDonation.status}"
        insertAuditLog(
            AuditLog(
                transactionType = "DONATION",
                transactionId = audited.id,
                transactionRef = audited.receiptNo.ifBlank { "ID: ${audited.id}" },
                action = "MODIFIED",
                reason = reason.trim(),
                performedById = superAdmin.id,
                performedByName = superAdmin.name,
                performedByRole = superAdmin.role,
                oldValueSummary = oldSummary,
                newValueSummary = newSummary,
                timestamp = now
            )
        )
    }

    /**
     * Super Admin deletion / voiding of Donation with compulsory reason and audit log.
     * Marks as deleted (soft delete) so it is excluded from totals, shown in red, and visible in reports.
     */
    suspend fun superAdminDeleteDonation(
        donation: Donation,
        reason: String,
        superAdmin: Member
    ) {
        val societyName = getSocietyInfo().name
        val now = System.currentTimeMillis()
        val voided = donation.copy(
            isDeleted = true,
            deletionReason = reason.trim(),
            deletedAt = now,
            deletedByName = "${superAdmin.name} (${superAdmin.role})",
            updatedAt = now
        )
        donationDao.updateDonation(voided)
        firestoreService.updateDonation(voided, societyName = societyName)

        insertAuditLog(
            AuditLog(
                transactionType = "DONATION",
                transactionId = donation.id,
                transactionRef = donation.receiptNo.ifBlank { "ID: ${donation.id}" },
                action = "DELETED",
                reason = reason.trim(),
                performedById = superAdmin.id,
                performedByName = superAdmin.name,
                performedByRole = superAdmin.role,
                oldValueSummary = "Amount: ₹${donation.amount} | Donor: ${donation.donorName} | Receipt: ${donation.receiptNo}",
                newValueSummary = "TRANSACTION VOIDED & EXCLUDED FROM TOTALS",
                timestamp = now
            )
        )
    }

    suspend fun deleteDonation(donation: Donation) {
        val societyName = getSocietyInfo().name
        donationDao.deleteDonation(donation)
        if (donation.firestoreId.isNotBlank()) {
            firestoreService.deleteDonation(donation.firestoreId, societyName = societyName)
        }
    }

    suspend fun deleteDonationById(id: Long) {
        val existing = donationDao.getDonationById(id)
        if (existing != null) {
            deleteDonation(existing)
        } else {
            donationDao.deleteDonationById(id)
        }
    }

    suspend fun markDonationAsPaid(
        donationId: Long,
        paymentMode: String,
        paymentDate: Long,
        notes: String,
        recordedById: Long,
        recordedByName: String
    ) {
        val existing = donationDao.getDonationById(donationId) ?: return
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val newReceiptNo = "RCP-$year-${String.format("%03d", donationId)}"
        val updated = existing.copy(
            status = "received",
            paymentMode = paymentMode,
            paymentDate = paymentDate,
            receiptNo = newReceiptNo,
            notes = if (notes.isNotBlank()) notes else existing.notes,
            recordedById = recordedById,
            recordedByName = recordedByName,
            updatedAt = System.currentTimeMillis()
        )
        updateDonation(updated)
    }

    suspend fun insertExpense(
        expense: Expense,
        userId: String = "",
        userEmail: String = ""
    ): Long {
        val societyName = getSocietyInfo().name
        val localId = expenseDao.insertExpense(expense.copy(societyId = societyName))
        val withLocalId = expense.copy(id = localId, societyId = societyName)
        val firestoreDocId = firestoreService.saveExpense(withLocalId, userId, userEmail, societyName = societyName)
        if (firestoreDocId != null) {
            val withFirestore = withLocalId.copy(
                firestoreId = firestoreDocId,
                userId = userId,
                userEmail = userEmail
            )
            expenseDao.updateExpense(withFirestore)
        }
        return localId
    }

    suspend fun updateExpense(expense: Expense) {
        val societyName = getSocietyInfo().name
        expenseDao.updateExpense(expense)
        firestoreService.updateExpense(expense, societyName = societyName)
    }

    /**
     * Super Admin modification of Expense with compulsory reason and audit log
     */
    suspend fun superAdminModifyExpense(
        originalExpense: Expense,
        updatedExpense: Expense,
        reason: String,
        superAdmin: Member
    ) {
        val now = System.currentTimeMillis()
        val audited = updatedExpense.copy(
            isModified = true,
            modificationReason = reason.trim(),
            modifiedAt = now,
            modifiedByName = "${superAdmin.name} (${superAdmin.role})",
            updatedAt = now
        )
        updateExpense(audited)

        val oldSummary = "Amount: ₹${originalExpense.amount} | Title: ${originalExpense.title} | Category: ${originalExpense.category} | PaidTo: ${originalExpense.paidTo}"
        val newSummary = "Amount: ₹${updatedExpense.amount} | Title: ${updatedExpense.title} | Category: ${updatedExpense.category} | PaidTo: ${updatedExpense.paidTo}"
        insertAuditLog(
            AuditLog(
                transactionType = "EXPENSE",
                transactionId = audited.id,
                transactionRef = audited.title,
                action = "MODIFIED",
                reason = reason.trim(),
                performedById = superAdmin.id,
                performedByName = superAdmin.name,
                performedByRole = superAdmin.role,
                oldValueSummary = oldSummary,
                newValueSummary = newSummary,
                timestamp = now
            )
        )
    }

    /**
     * Super Admin deletion / voiding of Expense with compulsory reason and audit log
     */
    suspend fun superAdminDeleteExpense(
        expense: Expense,
        reason: String,
        superAdmin: Member
    ) {
        val societyName = getSocietyInfo().name
        val now = System.currentTimeMillis()
        val voided = expense.copy(
            isDeleted = true,
            deletionReason = reason.trim(),
            deletedAt = now,
            deletedByName = "${superAdmin.name} (${superAdmin.role})",
            updatedAt = now
        )
        expenseDao.updateExpense(voided)
        firestoreService.updateExpense(voided, societyName = societyName)

        insertAuditLog(
            AuditLog(
                transactionType = "EXPENSE",
                transactionId = expense.id,
                transactionRef = expense.title,
                action = "DELETED",
                reason = reason.trim(),
                performedById = superAdmin.id,
                performedByName = superAdmin.name,
                performedByRole = superAdmin.role,
                oldValueSummary = "Amount: ₹${expense.amount} | Title: ${expense.title} | Paid To: ${expense.paidTo}",
                newValueSummary = "TRANSACTION VOIDED & EXCLUDED FROM TOTALS",
                timestamp = now
            )
        )
    }

    suspend fun deleteExpense(expense: Expense) {
        val societyName = getSocietyInfo().name
        expenseDao.deleteExpense(expense)
        if (expense.firestoreId.isNotBlank()) {
            firestoreService.deleteExpense(expense.firestoreId, societyName = societyName)
        }
    }

    suspend fun deleteExpenseById(id: Long) {
        val existing = expenseDao.getExpenseById(id)
        if (existing != null) {
            deleteExpense(existing)
        } else {
            expenseDao.deleteExpenseById(id)
        }
    }

    suspend fun deleteEntireSociety(societyId: String = "") {
        clearTransactions()
        societyPreferences?.clearAllSocietyData()
        if (societyId.isNotBlank()) {
            firestoreService.deleteSocietyInfo(societyId)
        }
    }

    suspend fun restoreAllData(
        societyInfo: SocietyInfo?,
        donations: List<Donation>,
        expenses: List<Expense>,
        members: List<Member>,
        mergeMode: Boolean
    ) {
        if (!mergeMode) {
            donationDao.deleteAllDonations()
            expenseDao.deleteAllExpenses()
            if (members.isNotEmpty()) {
                memberDao.deleteAllMembers()
            }
        }
        if (societyInfo != null) {
            saveSocietyInfo(societyInfo)
        }
        if (members.isNotEmpty()) {
            memberDao.insertAll(members)
        }
        if (donations.isNotEmpty()) {
            donationDao.insertAll(donations)
        }
        if (expenses.isNotEmpty()) {
            expenseDao.insertAll(expenses)
        }
        // Background sync to Firestore
        try {
            val socName = getSocietyInfo().name
            donations.forEach { firestoreService.saveDonation(it, societyName = socName) }
            expenses.forEach { firestoreService.saveExpense(it, societyName = socName) }
            members.forEach { firestoreService.saveMember(it, societyName = socName) }
        } catch (e: Exception) {
            // offline/fallback
        }
    }

    // ==========================================
    // Unified Transactions Tracking Operations
    // ==========================================

    suspend fun insertTransaction(transaction: Transaction): Long {
        val societyName = getSocietyInfo().name
        val localId = transactionDao?.insertTransaction(transaction.copy(societyId = societyName)) ?: 0L
        val withId = transaction.copy(id = if (transaction.id == 0L) localId else transaction.id, societyId = societyName)
        try {
            val firestoreId = firestoreService.saveTransaction(withId, societyName = societyName)
            if (firestoreId != null && firestoreId.isNotBlank()) {
                val withRemoteId = withId.copy(firestoreId = firestoreId)
                transactionDao?.insertTransaction(withRemoteId)
            }
        } catch (e: Exception) {
            // Offline fallback
        }
        return localId
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val societyName = getSocietyInfo().name
        transactionDao?.updateTransaction(transaction)
        try {
            firestoreService.updateTransaction(transaction, societyName = societyName)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        val societyName = getSocietyInfo().name
        transactionDao?.deleteTransaction(transaction)
        try {
            if (transaction.firestoreId.isNotBlank()) {
                firestoreService.deleteTransaction(transaction.firestoreId, societyName = societyName)
            }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun syncTransactionsFromFirestore(): List<Transaction> {
        return try {
            val remoteTransactions = firestoreService.fetchAllTransactions(getSocietyInfo().name)
            if (remoteTransactions.isNotEmpty()) {
                transactionDao?.insertAll(remoteTransactions)
            }
            remoteTransactions
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==========================================
    // Pledges Tracking Operations
    // ==========================================

    suspend fun insertPledge(pledge: Pledge): Long {
        val societyName = getSocietyInfo().name
        val localId = pledgeDao?.insertPledge(pledge.copy(societyId = societyName)) ?: 0L
        val withId = pledge.copy(id = if (pledge.id == 0L) localId else pledge.id, societyId = societyName)
        try {
            val firestoreId = firestoreService.savePledge(withId, societyName = societyName)
            if (firestoreId != null && firestoreId.isNotBlank()) {
                val withRemoteId = withId.copy(firestoreId = firestoreId)
                pledgeDao?.insertPledge(withRemoteId)
            }
        } catch (e: Exception) {
            // Offline fallback
        }
        return localId
    }

    suspend fun updatePledge(pledge: Pledge) {
        val societyName = getSocietyInfo().name
        pledgeDao?.updatePledge(pledge)
        try {
            firestoreService.updatePledge(pledge, societyName = societyName)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deletePledge(pledge: Pledge) {
        val societyName = getSocietyInfo().name
        pledgeDao?.deletePledge(pledge)
        try {
            if (pledge.firestoreId.isNotBlank()) {
                firestoreService.deletePledge(pledge.firestoreId, societyName = societyName)
            }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun syncPledgesFromFirestore(): List<Pledge> {
        return try {
            val remotePledges = firestoreService.fetchAllPledges(getSocietyInfo().name)
            if (remotePledges.isNotEmpty()) {
                pledgeDao?.insertAll(remotePledges)
            }
            remotePledges
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==========================================
    // Committee Members Operations
    // ==========================================

    suspend fun insertCommitteeMember(member: CommitteeMember): Long {
        val societyName = getSocietyInfo().name
        val localId = committeeMemberDao?.insertCommitteeMember(member) ?: 0L
        val withId = member.copy(id = if (member.id == 0L) localId else member.id)
        try {
            val firestoreId = firestoreService.saveCommitteeMember(withId, societyName = societyName)
            if (firestoreId != null && firestoreId.isNotBlank()) {
                val withRemoteId = withId.copy(firestoreId = firestoreId)
                committeeMemberDao?.insertCommitteeMember(withRemoteId)
            }
        } catch (e: Exception) {
            // Offline fallback
        }
        return localId
    }

    suspend fun updateCommitteeMember(member: CommitteeMember) {
        val societyName = getSocietyInfo().name
        committeeMemberDao?.updateCommitteeMember(member)
        try {
            firestoreService.updateCommitteeMember(member, societyName = societyName)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteCommitteeMember(member: CommitteeMember) {
        val societyName = getSocietyInfo().name
        committeeMemberDao?.deleteCommitteeMember(member)
        try {
            if (member.firestoreId.isNotBlank()) {
                firestoreService.deleteCommitteeMember(member.firestoreId, societyName = societyName)
            }
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun syncCommitteeMembersFromFirestore(): List<CommitteeMember> {
        return try {
            val remoteMembers = firestoreService.fetchAllCommitteeMembers(getSocietyInfo().name)
            if (remoteMembers.isNotEmpty()) {
                committeeMemberDao?.insertAll(remoteMembers)
            }
            remoteMembers
        } catch (e: Exception) {
            emptyList()
        }
    }
}
