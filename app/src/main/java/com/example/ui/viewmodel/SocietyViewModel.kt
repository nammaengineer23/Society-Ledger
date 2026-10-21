package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreloadedData
import com.example.data.local.SocietyDatabase
import com.example.data.local.SocietyPreferences
import com.example.data.model.AuditLog
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Donor
import com.example.data.model.Expense
import com.example.data.model.FinancialKPIs
import com.example.data.model.Member
import com.example.data.model.MaintenanceCharge
import com.example.data.model.MonthlyFlowSummary
import com.example.data.model.PaymentModeSummary
import com.example.data.model.Pledge
import com.example.data.model.Society
import com.example.data.model.SocietyInfo
import com.example.data.model.Transaction
import com.example.data.model.toSociety
import com.example.data.remote.FirestoreService
import com.example.data.repository.SocietyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SocietyViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences: SocietyPreferences = SocietyPreferences(application)
    private val firestoreService = FirestoreService()
    private val database = SocietyDatabase.getDatabase(application, viewModelScope)
    private val repository: SocietyRepository = SocietyRepository(
        memberDao = database.memberDao(),
        donationDao = database.donationDao(),
        expenseDao = database.expenseDao(),
        auditLogDao = database.auditLogDao(),
        societyPreferences = preferences,
        firestoreService = firestoreService,
        transactionDao = database.transactionDao(),
        pledgeDao = database.pledgeDao(),
        committeeMemberDao = database.committeeMemberDao(),
        maintenanceChargeDao = database.maintenanceChargeDao(),
        donorDao = database.donorDao()
    )

    private val _societyInfo = MutableStateFlow(preferences.getSocietyInfo())
    val societyInfo: StateFlow<SocietyInfo> = _societyInfo.asStateFlow()

    private val _appLanguage = MutableStateFlow(preferences.getAppLanguage())
    val appLanguage: StateFlow<com.example.data.model.AppLanguage> = _appLanguage.asStateFlow()

    // Material 3 Dark Theme State for Late-Night Ledger Accessibility & Comfort
    private val _isDarkTheme = MutableStateFlow(preferences.isDarkTheme())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        preferences.setDarkTheme(next)
    }

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        preferences.setDarkTheme(isDark)
    }

    // Subscription & Plan Management State
    private val _trialStartTime = MutableStateFlow(preferences.getTrialStartTime())
    val trialStartTime: StateFlow<Long> = _trialStartTime.asStateFlow()

    private val _isProSubscriber = MutableStateFlow(preferences.isProSubscriber())
    val isProSubscriber: StateFlow<Boolean> = _isProSubscriber.asStateFlow()

    private val _subscriptionExpiry = MutableStateFlow(preferences.getSubscriptionExpiryTime())
    val subscriptionExpiry: StateFlow<Long> = _subscriptionExpiry.asStateFlow()

    private val _razorpayKeyId = MutableStateFlow(preferences.getRazorpayKeyId())
    val razorpayKeyId: StateFlow<String> = _razorpayKeyId.asStateFlow()

    private val _lastPaymentTxnId = MutableStateFlow(preferences.getLastPaymentTransactionId())
    val lastPaymentTxnId: StateFlow<String> = _lastPaymentTxnId.asStateFlow()

    // Global Announcement & App Broadcast
    private val _globalAnnouncement = MutableStateFlow(preferences.getGlobalAnnouncement())
    val globalAnnouncement: StateFlow<String> = _globalAnnouncement.asStateFlow()

    private val _isAnnouncementEnabled = MutableStateFlow(preferences.isGlobalAnnouncementEnabled())
    val isAnnouncementEnabled: StateFlow<Boolean> = _isAnnouncementEnabled.asStateFlow()

    // Service Complaints & Support Tickets Desk
    private val _complaints = MutableStateFlow<List<com.example.data.model.AppComplaint>>(PreloadedData.INITIAL_COMPLAINTS)
    val complaints: StateFlow<List<com.example.data.model.AppComplaint>> = _complaints.asStateFlow()

    // App PIN & Security Lock
    private val _isAppLockEnabled = MutableStateFlow(preferences.isAppLockEnabled())
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _appLockPin = MutableStateFlow(preferences.getAppLockPin())
    val appLockPin: StateFlow<String> = _appLockPin.asStateFlow()

    fun setAppLockEnabled(enabled: Boolean) {
        preferences.setAppLockEnabled(enabled)
        _isAppLockEnabled.value = enabled
    }

    fun setAppLockPin(pin: String) {
        preferences.setAppLockPin(pin)
        _appLockPin.value = pin
    }

    // Category-wise Monthly Budget Caps
    val standardExpenseCategories = listOf(
        "Events & Festivals",
        "Maintenance",
        "Repairs",
        "Utilities",
        "Refreshments",
        "Admin & Legal",
        "Miscellaneous"
    )

    private val _categoryBudgets = MutableStateFlow(preferences.getAllCategoryBudgets(standardExpenseCategories))
    val categoryBudgets: StateFlow<Map<String, Double>> = _categoryBudgets.asStateFlow()

    fun setCategoryBudget(category: String, amount: Double) {
        preferences.setCategoryBudget(category, amount)
        _categoryBudgets.value = preferences.getAllCategoryBudgets(standardExpenseCategories)
    }

    fun setAppLanguage(language: com.example.data.model.AppLanguage) {
        preferences.saveAppLanguage(language)
        _appLanguage.value = language
    }

    val allMembers: StateFlow<List<Member>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val members: StateFlow<List<Member>> = allMembers

    val maintenanceCharges: StateFlow<List<MaintenanceCharge>> = repository.allMaintenanceCharges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMaintenanceCharge(charge: MaintenanceCharge, onComplete: (Result<Long>) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(runCatching { repository.addMaintenanceCharge(charge) })
        }
    }

    fun updateMaintenanceCharge(charge: MaintenanceCharge, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            onComplete(runCatching { repository.updateMaintenanceCharge(charge) })
        }
    }

    val allCommitteeMembers: StateFlow<List<CommitteeMember>> = repository.allCommitteeMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDonations: StateFlow<List<Donation>> = repository.allDonations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val donations: StateFlow<List<Donation>> = allDonations

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<Expense>> = allExpenses

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPledges: StateFlow<List<Pledge>> = repository.allPledges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPledgesList: StateFlow<List<Pledge>> = repository.pendingPledgesList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<com.example.data.model.AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit Log Filters & Firestore Sync State
    private val _auditSearchQuery = MutableStateFlow("")
    val auditSearchQuery: StateFlow<String> = _auditSearchQuery.asStateFlow()

    private val _auditActionFilter = MutableStateFlow("ALL") // "ALL", "DELETED", "MODIFIED", "STATUS_CHANGED"
    val auditActionFilter: StateFlow<String> = _auditActionFilter.asStateFlow()

    private val _auditTypeFilter = MutableStateFlow("ALL") // "ALL", "DONATION", "EXPENSE"
    val auditTypeFilter: StateFlow<String> = _auditTypeFilter.asStateFlow()

    private val _isAuditSyncing = MutableStateFlow(false)
    val isAuditSyncing: StateFlow<Boolean> = _isAuditSyncing.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastAuditSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastAuditSyncTime: StateFlow<Long> = _lastAuditSyncTime.asStateFlow()

    fun setAuditSearchQuery(query: String) {
        _auditSearchQuery.value = query
    }

    fun setAuditActionFilter(filter: String) {
        _auditActionFilter.value = filter
    }

    fun setAuditTypeFilter(filter: String) {
        _auditTypeFilter.value = filter
    }

    fun refreshAuditLogsFromFirestore() {
        viewModelScope.launch {
            _isAuditSyncing.value = true
            try {
                repository.fetchAuditLogsFromFirestore()
                _lastAuditSyncTime.value = System.currentTimeMillis()
            } catch (e: Exception) {
                // Safety fallback
            } finally {
                _isAuditSyncing.value = false
            }
        }
    }

    // Filtered Audit Logs stream sorted by timestamp descending
    val filteredAuditLogs: StateFlow<List<com.example.data.model.AuditLog>> = combine(
        allAuditLogs,
        _auditSearchQuery,
        _auditActionFilter,
        _auditTypeFilter
    ) { logs, query, actionFilter, typeFilter ->
        logs.filter { log ->
            val matchesQuery = query.isBlank() ||
                log.reason.contains(query, ignoreCase = true) ||
                log.transactionRef.contains(query, ignoreCase = true) ||
                log.performedByName.contains(query, ignoreCase = true) ||
                log.oldValueSummary.contains(query, ignoreCase = true) ||
                log.newValueSummary.contains(query, ignoreCase = true)

            val matchesAction = when (actionFilter) {
                "ALL" -> true
                "DELETED" -> log.action.contains("DELETE", ignoreCase = true) || log.action.contains("VOID", ignoreCase = true)
                "MODIFIED" -> log.action.contains("MODIFY", ignoreCase = true) || log.action.contains("EDIT", ignoreCase = true)
                "STATUS_CHANGED" -> log.action.contains("STATUS", ignoreCase = true)
                else -> log.action.equals(actionFilter, ignoreCase = true)
            }

            val matchesType = when (typeFilter) {
                "ALL" -> true
                else -> log.transactionType.equals(typeFilter, ignoreCase = true)
            }

            matchesQuery && matchesAction && matchesType
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Modified & Deleted records combined for dedicated auditing
    val auditedTransactions: StateFlow<List<com.example.data.model.AuditedTransactionItem>> = combine(
        allDonations,
        allExpenses
    ) { donations, expenses ->
        val donationAudits = donations.filter { it.isModified || it.isDeleted }.map { donation ->
            com.example.data.model.AuditedTransactionItem(
                id = donation.id,
                firestoreId = donation.firestoreId,
                type = "DONATION",
                reference = donation.receiptNo.ifBlank { "Donation #${donation.id}" },
                entityName = donation.donorName,
                amount = donation.amount,
                isDeleted = donation.isDeleted,
                isModified = donation.isModified,
                reason = if (donation.isDeleted) donation.deletionReason else donation.modificationReason,
                timestamp = if (donation.isDeleted) donation.deletedAt else donation.modifiedAt,
                performedBy = if (donation.isDeleted) donation.deletedByName else donation.modifiedByName,
                details = "Mode: ${donation.paymentMode} | Phone: ${donation.phone}"
            )
        }

        val expenseAudits = expenses.filter { it.isModified || it.isDeleted }.map { expense ->
            com.example.data.model.AuditedTransactionItem(
                id = expense.id,
                firestoreId = expense.firestoreId,
                type = "EXPENSE",
                reference = expense.title,
                entityName = expense.paidTo,
                amount = expense.amount,
                isDeleted = expense.isDeleted,
                isModified = expense.isModified,
                reason = if (expense.isDeleted) expense.deletionReason else expense.modificationReason,
                timestamp = if (expense.isDeleted) expense.deletedAt else expense.modifiedAt,
                performedBy = if (expense.isDeleted) expense.deletedByName else expense.modifiedByName,
                details = "Category: ${expense.category} | Mode: ${expense.paymentMode}"
            )
        }

        (donationAudits + expenseAudits).sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active authenticated committee member (Defaulted to Super Admin)
    private val _currentMember = MutableStateFlow<Member?>(PreloadedData.COMMITTEE_MEMBERS.firstOrNull { it.isSuperAdmin } ?: PreloadedData.COMMITTEE_MEMBERS.firstOrNull())
    val currentMember: StateFlow<Member?> = _currentMember.asStateFlow()
    val activeMember: StateFlow<Member?> = currentMember

    // Flag whether the app is currently in Admin Mode or Public Read-Only Transparency mode (Default is PUBLIC)
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Selective Financial Year filter for Audits and Reports
    private val _selectedFinancialYear = MutableStateFlow("ALL")
    val selectedFinancialYear: StateFlow<String> = _selectedFinancialYear.asStateFlow()

    // Filters for Donations
    private val _donationSearchQuery = MutableStateFlow("")
    val donationSearchQuery: StateFlow<String> = _donationSearchQuery.asStateFlow()

    // Filters for Expenses
    private val _expenseSearchQuery = MutableStateFlow("")
    val expenseSearchQuery: StateFlow<String> = _expenseSearchQuery.asStateFlow()

    private val _expenseCategoryFilter = MutableStateFlow("ALL")
    val expenseCategoryFilter: StateFlow<String> = _expenseCategoryFilter.asStateFlow()

    private val _donationStatusFilter = MutableStateFlow("ALL") // "ALL", "received", "pending"
    val donationStatusFilter: StateFlow<String> = _donationStatusFilter.asStateFlow()

    private val _donationModeFilter = MutableStateFlow("ALL")
    val donationModeFilter: StateFlow<String> = _donationModeFilter.asStateFlow()

    // Filter by Committee Member (null means ALL)
    private val _donationMemberFilter = MutableStateFlow<Long?>(null)
    val donationMemberFilter: StateFlow<Long?> = _donationMemberFilter.asStateFlow()

    private val _expenseMemberFilter = MutableStateFlow<Long?>(null)
    val expenseMemberFilter: StateFlow<Long?> = _expenseMemberFilter.asStateFlow()

    // Filter by Time Period
    private val _timePeriodFilter = MutableStateFlow("ALL") // "ALL", "THIS_MONTH", "LAST_MONTH", "FY"
    val timePeriodFilter: StateFlow<String> = _timePeriodFilter.asStateFlow()
    val donationTimePeriod: StateFlow<String> = _timePeriodFilter.asStateFlow()
    val expenseTimePeriod: StateFlow<String> = _timePeriodFilter.asStateFlow()

    // Computed KPIs including opening reserve / starting balance (Excluding Deleted Transactions)
    val financialKPIs: StateFlow<FinancialKPIs> = combine(
        allDonations,
        allExpenses,
        _societyInfo
    ) { donations, expenses, society ->
        val received = donations.filter { it.status == "received" && !it.isDeleted }
        val pending = donations.filter { it.status == "pending" && !it.isDeleted }
        val validExpenses = expenses.filter { !it.isDeleted }

        val totalCollected = received.sumOf { it.amount }
        val totalPending = pending.sumOf { it.amount }
        val totalExpenses = validExpenses.sumOf { it.amount }
        val netBalance = society.openingBalance + totalCollected - totalExpenses

        FinancialKPIs(
            openingBalance = society.openingBalance,
            totalCollected = totalCollected,
            totalPending = totalPending,
            totalExpenses = totalExpenses,
            netBalance = netBalance,
            receivedCount = received.size,
            pendingCount = pending.size,
            expenseCount = validExpenses.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialKPIs())

    // Category Breakdown for Expenses (Excluding Deleted)
    val categoryExpenses: StateFlow<List<CategoryExpenseSummary>> = allExpenses.combine(
        _timePeriodFilter
    ) { expenses, period ->
        val validExpenses = expenses.filter { !it.isDeleted }
        val filtered = filterExpensesByPeriod(validExpenses, period)
        val total = filtered.sumOf { it.amount }
        if (total == 0.0) return@combine emptyList()

        val grouped = filtered.groupBy { it.category }
        grouped.map { (category, list) ->
            val sum = list.sumOf { it.amount }
            CategoryExpenseSummary(
                category = category,
                amount = sum,
                percentage = (sum / total * 100).toFloat()
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Inflow vs Outflow (Excluding Deleted)
    val monthlyFlowSummary: StateFlow<List<MonthlyFlowSummary>> = combine(
        allDonations,
        allExpenses
    ) { donations, expenses ->
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val months = mutableListOf<String>()

        // Last 5 months
        for (i in 4 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            months.add(monthFormat.format(c.time))
        }

        val received = donations.filter { it.status == "received" && !it.isDeleted }
        val validExpenses = expenses.filter { !it.isDeleted }

        months.map { monthStr ->
            val inflow = received.filter {
                monthFormat.format(Date(it.paymentDate)) == monthStr
            }.sumOf { it.amount }

            val outflow = validExpenses.filter {
                monthFormat.format(Date(it.expenseDate)) == monthStr
            }.sumOf { it.amount }

            MonthlyFlowSummary(
                monthName = monthStr.split(" ")[0],
                inflow = inflow,
                outflow = outflow
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val monthlyFlow: StateFlow<List<MonthlyFlowSummary>> = monthlyFlowSummary

    // Payment Mode Breakdown for Donations (Excluding Deleted)
    val paymentModeSummary: StateFlow<List<PaymentModeSummary>> = allDonations.combine(
        _timePeriodFilter
    ) { donations, period ->
        val received = filterDonationsByPeriod(donations.filter { it.status == "received" && !it.isDeleted }, period)
        val grouped = received.groupBy { it.paymentMode }
        grouped.map { (mode, list) ->
            PaymentModeSummary(
                mode = mode,
                amount = list.sumOf { it.amount },
                count = list.size
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val paymentModeBreakdown: StateFlow<List<PaymentModeSummary>> = paymentModeSummary

    // Filtered Donations Stream
    val filteredDonations: StateFlow<List<Donation>> = combine(
        allDonations,
        _donationSearchQuery,
        _donationStatusFilter,
        _donationModeFilter,
        _timePeriodFilter,
        _donationMemberFilter,
        allMembers
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val donations = args[0] as List<Donation>
        val query = args[1] as String
        val status = args[2] as String
        val mode = args[3] as String
        val period = args[4] as String
        val memberId = args[5] as? Long
        @Suppress("UNCHECKED_CAST")
        val membersList = args[6] as List<Member>

        var list = filterDonationsByPeriod(donations, period)

        if (status != "ALL") {
            if (status.equals("modified", ignoreCase = true)) {
                list = list.filter { it.isModified || it.modifiedAt > 0L }
            } else {
                list = list.filter { it.status.equals(status, ignoreCase = true) }
            }
        }

        if (mode != "ALL") {
            list = list.filter { it.paymentMode.equals(mode, ignoreCase = true) }
        }

        if (memberId != null && memberId > 0L) {
            val selectedMember = membersList.firstOrNull { it.id == memberId }
            list = list.filter {
                it.recordedById == memberId ||
                (selectedMember != null && (
                    it.recordedByName.contains(selectedMember.name, ignoreCase = true) ||
                    (it.userEmail.isNotBlank() && it.userEmail.equals(selectedMember.email, ignoreCase = true))
                ))
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase().removePrefix("₹").removePrefix("rs.").removePrefix("rs").trim()
            val numericAmountQuery = q.replace(",", "").toDoubleOrNull()
            list = list.filter { donation ->
                donation.donorName.lowercase().contains(q) ||
                donation.recordedByName.lowercase().contains(q) ||
                donation.userEmail.lowercase().contains(q) ||
                donation.phone.contains(q) ||
                donation.receiptNo.lowercase().contains(q) ||
                donation.id.toString() == q ||
                "rec-${donation.id}".contains(q) ||
                "rec#${donation.id}".contains(q) ||
                donation.firestoreId.lowercase().contains(q) ||
                donation.notes.lowercase().contains(q) ||
                donation.paymentMode.lowercase().contains(q) ||
                (numericAmountQuery != null && (donation.amount == numericAmountQuery || donation.amount.toInt() == numericAmountQuery.toInt())) ||
                donation.amount.toString().contains(q) ||
                String.format(java.util.Locale.US, "%.0f", donation.amount).contains(q) ||
                String.format(java.util.Locale.US, "%,.0f", donation.amount).lowercase().contains(q)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Pending Donations Stream
    val filteredPendingDonations: StateFlow<List<Donation>> = combine(
        allDonations,
        _donationSearchQuery,
        _donationMemberFilter,
        allMembers
    ) { donations, query, memberId, membersList ->
        var list = donations.filter { it.status == "pending" }

        if (memberId != null && memberId > 0L) {
            val selectedMember = membersList.firstOrNull { it.id == memberId }
            list = list.filter {
                it.recordedById == memberId ||
                (selectedMember != null && (
                    it.recordedByName.contains(selectedMember.name, ignoreCase = true) ||
                    (it.userEmail.isNotBlank() && it.userEmail.equals(selectedMember.email, ignoreCase = true))
                ))
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase().removePrefix("₹").removePrefix("rs.").removePrefix("rs").trim()
            val numericAmountQuery = q.replace(",", "").toDoubleOrNull()
            list = list.filter { donation ->
                donation.donorName.lowercase().contains(q) ||
                donation.recordedByName.lowercase().contains(q) ||
                donation.userEmail.lowercase().contains(q) ||
                donation.phone.contains(q) ||
                donation.receiptNo.lowercase().contains(q) ||
                donation.id.toString() == q ||
                "rec-${donation.id}".contains(q) ||
                "rec#${donation.id}".contains(q) ||
                donation.notes.lowercase().contains(q) ||
                donation.paymentMode.lowercase().contains(q) ||
                (numericAmountQuery != null && (donation.amount == numericAmountQuery || donation.amount.toInt() == numericAmountQuery.toInt())) ||
                donation.amount.toString().contains(q) ||
                String.format(java.util.Locale.US, "%.0f", donation.amount).contains(q) ||
                String.format(java.util.Locale.US, "%,.0f", donation.amount).lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingDonations: StateFlow<List<Donation>> = filteredPendingDonations

    // Filtered Expenses Stream
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        allExpenses,
        _expenseSearchQuery,
        _expenseCategoryFilter,
        _timePeriodFilter,
        _expenseMemberFilter,
        allMembers
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val expenses = args[0] as List<Expense>
        val query = args[1] as String
        val category = args[2] as String
        val period = args[3] as String
        val memberId = args[4] as? Long
        @Suppress("UNCHECKED_CAST")
        val membersList = args[5] as List<Member>

        var list = filterExpensesByPeriod(expenses, period)

        if (category != "ALL") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (memberId != null && memberId > 0L) {
            val selectedMember = membersList.firstOrNull { it.id == memberId }
            list = list.filter {
                it.recordedById == memberId ||
                (selectedMember != null && (
                    it.recordedByName.contains(selectedMember.name, ignoreCase = true) ||
                    (it.userEmail.isNotBlank() && it.userEmail.equals(selectedMember.email, ignoreCase = true))
                ))
            }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase().removePrefix("₹").removePrefix("rs.").removePrefix("rs").trim()
            val numericAmountQuery = q.replace(",", "").toDoubleOrNull()
            list = list.filter { expense ->
                expense.title.lowercase().contains(q) ||
                expense.paidTo.lowercase().contains(q) ||
                expense.recordedByName.lowercase().contains(q) ||
                expense.userEmail.lowercase().contains(q) ||
                expense.category.lowercase().contains(q) ||
                expense.receiptUrl.lowercase().contains(q) ||
                expense.id.toString() == q ||
                "exp-${expense.id}".contains(q) ||
                "exp#${expense.id}".contains(q) ||
                expense.firestoreId.lowercase().contains(q) ||
                expense.paymentMode.lowercase().contains(q) ||
                (numericAmountQuery != null && (expense.amount == numericAmountQuery || expense.amount.toInt() == numericAmountQuery.toInt())) ||
                expense.amount.toString().contains(q) ||
                String.format(java.util.Locale.US, "%.0f", expense.amount).contains(q) ||
                String.format(java.util.Locale.US, "%,.0f", expense.amount).lowercase().contains(q)
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun filterDonationsByPeriod(list: List<Donation>, period: String): List<Donation> {
        val cal = Calendar.getInstance()
        return when (period) {
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfMonth = cal.timeInMillis
                list.filter { it.paymentDate >= startOfMonth }
            }
            "LAST_MONTH" -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfLast = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val endOfLast = cal.timeInMillis
                list.filter { it.paymentDate in startOfLast until endOfLast }
            }
            "QUARTERLY" -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfQuarter = cal.timeInMillis
                list.filter { it.paymentDate >= startOfQuarter }
            }
            "YEARLY", "FY" -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val fyStartYear = if (currentMonth >= Calendar.APRIL) cal.get(Calendar.YEAR) else cal.get(Calendar.YEAR) - 1
                cal.set(Calendar.YEAR, fyStartYear)
                cal.set(Calendar.MONTH, Calendar.APRIL)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfFY = cal.timeInMillis
                list.filter { it.paymentDate >= startOfFY }
            }
            else -> list
        }
    }

    private fun filterExpensesByPeriod(list: List<Expense>, period: String): List<Expense> {
        val cal = Calendar.getInstance()
        return when (period) {
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfMonth = cal.timeInMillis
                list.filter { it.expenseDate >= startOfMonth }
            }
            "LAST_MONTH" -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfLast = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val endOfLast = cal.timeInMillis
                list.filter { it.expenseDate in startOfLast until endOfLast }
            }
            "QUARTERLY" -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfQuarter = cal.timeInMillis
                list.filter { it.expenseDate >= startOfQuarter }
            }
            "YEARLY", "FY" -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val fyStartYear = if (currentMonth >= Calendar.APRIL) cal.get(Calendar.YEAR) else cal.get(Calendar.YEAR) - 1
                cal.set(Calendar.YEAR, fyStartYear)
                cal.set(Calendar.MONTH, Calendar.APRIL)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfFY = cal.timeInMillis
                list.filter { it.expenseDate >= startOfFY }
            }
            else -> list
        }
    }

    // Auth & Committee Switcher Actions
    fun loginCommitteeMember(member: Member, pin: String): Boolean {
        if (member.pin.isNotBlank() && member.pin == pin.trim()) {
            _currentMember.value = member
            _isAdminMode.value = true
            return true
        }
        return false
    }

    fun switchMember(member: Member?) {
        _currentMember.value = member
        val isCommittee = member != null && (member.isSuperAdmin || member.role.contains("Admin", ignoreCase = true) || member.role.contains("President", ignoreCase = true) || member.role.contains("Secretary", ignoreCase = true) || member.role.contains("Treasurer", ignoreCase = true)) && !member.role.contains("Public", ignoreCase = true)
        _isAdminMode.value = isCommittee
    }

    fun loginWithFirebaseAuth(member: Member) {
        _currentMember.value = member
        _isAdminMode.value = true
        firestoreService.resetCloudAccessStatus()
        viewModelScope.launch {
            val existing = if (member.email.isNotBlank()) {
                repository.getMemberByEmail(member.email)
            } else if (member.phone.isNotBlank()) {
                repository.getMemberByPhone(member.phone)
            } else if (member.id > 0) {
                repository.getMemberById(member.id)
            } else null

            val activeMember = if (existing != null) {
                val updated = member.copy(id = existing.id)
                repository.updateMember(updated)
                updated
            } else {
                val newId = repository.insertMember(member)
                member.copy(id = if (newId > 0) newId else member.id)
            }
            _currentMember.value = activeMember

            try {
                repository.insertAuditLog(
                    com.example.data.model.AuditLog(
                        transactionType = "MEMBER",
                        action = "LOGIN_SUCCESS",
                        reason = "Firebase Authenticated Committee Login: ${activeMember.name} (${activeMember.role})",
                        performedById = activeMember.id,
                        performedByName = activeMember.name,
                        performedByRole = activeMember.role,
                        societyId = _societyInfo.value.registrationNo.ifBlank { "REG-SOCIETY" },
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                // Ignore audit logging errors during offline or initial mode
            }

            // Fetch and sync all latest data from Firestore for this user
            repository.syncAllFromFirestore()
        }
    }

    fun setActiveMember(member: Member?) = switchMember(member)

    fun setPublicReadOnlyMode(enabled: Boolean) {
        _isAdminMode.value = !enabled
    }

    fun setPublicMode(publicMode: Boolean) = setPublicReadOnlyMode(publicMode)

    fun logout() {
        _currentMember.value = null
        _isAdminMode.value = false
    }

    // Donors Directory & WhatsApp Broadcast State
    private val _customDonors = MutableStateFlow<List<com.example.data.model.Donor>>(emptyList())
    val customDonors: StateFlow<List<com.example.data.model.Donor>> = _customDonors.asStateFlow()

    private val _donorSearchQuery = MutableStateFlow("")
    val donorSearchQuery: StateFlow<String> = _donorSearchQuery.asStateFlow()

    private val _donorFilterType = MutableStateFlow("ALL") // "ALL", "RECEIVED", "PENDING", "HIGH_CONTRIBUTOR"
    val donorFilterType: StateFlow<String> = _donorFilterType.asStateFlow()

    val allDonors: StateFlow<List<com.example.data.model.DonorSummary>> = combine(
        allDonations,
        _customDonors,
        _societyInfo
    ) { donations, customList, society ->
        val donorMap = mutableMapOf<String, com.example.data.model.DonorSummary>()

        // 1. Process all recorded donations in this society
        donations.filter { !it.isDeleted }.forEach { donation ->
            val key = if (donation.phone.isNotBlank()) donation.phone.trim() else donation.donorName.trim().lowercase()
            val existing = donorMap[key]
            val receivedAmt = if (donation.status == "received") donation.amount else 0.0
            val pendingAmt = if (donation.status == "pending") donation.amount else 0.0

            if (existing == null) {
                donorMap[key] = com.example.data.model.DonorSummary(
                    name = donation.donorName,
                    phone = donation.phone,
                    email = donation.userEmail,
                    flatOrAddress = "",
                    totalDonated = donation.amount,
                    totalReceived = receivedAmt,
                    totalPending = pendingAmt,
                    donationCount = 1,
                    lastDonationDate = donation.paymentDate,
                    paymentModes = listOf(donation.paymentMode).filter { it.isNotBlank() },
                    notes = donation.notes,
                    societyId = society.registrationNo
                )
            } else {
                val updatedModes = (existing.paymentModes + donation.paymentMode).distinct().filter { it.isNotBlank() }
                val updatedLastDate = maxOf(existing.lastDonationDate, donation.paymentDate)
                donorMap[key] = existing.copy(
                    totalDonated = existing.totalDonated + donation.amount,
                    totalReceived = existing.totalReceived + receivedAmt,
                    totalPending = existing.totalPending + pendingAmt,
                    donationCount = existing.donationCount + 1,
                    lastDonationDate = updatedLastDate,
                    paymentModes = updatedModes,
                    notes = if (donation.notes.isNotBlank()) "${existing.notes} | ${donation.notes}".trim(' ', '|') else existing.notes
                )
            }
        }

        // 2. Add any custom pre-registered donors who haven't made a donation yet
        customList.forEach { custom ->
            val key = if (custom.phone.isNotBlank()) custom.phone.trim() else custom.name.trim().lowercase()
            if (!donorMap.containsKey(key)) {
                donorMap[key] = com.example.data.model.DonorSummary(
                    name = custom.name,
                    phone = custom.phone,
                    email = custom.email,
                    flatOrAddress = custom.flatOrAddress,
                    totalDonated = 0.0,
                    totalReceived = 0.0,
                    totalPending = 0.0,
                    donationCount = 0,
                    lastDonationDate = custom.createdAt,
                    paymentModes = emptyList(),
                    notes = custom.notes,
                    societyId = society.registrationNo
                )
            }
        }

        donorMap.values.sortedByDescending { it.totalDonated }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDonors: StateFlow<List<com.example.data.model.DonorSummary>> = combine(
        allDonors,
        _donorSearchQuery,
        _donorFilterType
    ) { donors, query, filterType ->
        var list = donors
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.email.lowercase().contains(q) ||
                it.flatOrAddress.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }
        when (filterType) {
            "RECEIVED" -> list.filter { it.totalReceived > 0 }
            "PENDING" -> list.filter { it.totalPending > 0 }
            "HIGH_CONTRIBUTOR" -> list.filter { it.totalDonated >= 5000 }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDonorSearchQuery(query: String) { _donorSearchQuery.value = query }
    fun setDonorFilterType(filter: String) { _donorFilterType.value = filter }

    fun addCustomDonor(name: String, phone: String, email: String, flatOrAddress: String, notes: String) {
        val cleanPhone = phone.trim().filter { it.isDigit() }
        if (name.isBlank() || cleanPhone.length != 10) return
        val newDonor = com.example.data.model.Donor(
            name = name.trim(),
            phone = cleanPhone,
            email = email.trim(),
            flatOrAddress = flatOrAddress.trim(),
            notes = notes.trim(),
            societyId = _societyInfo.value.registrationNo,
            isCustomAdded = true
        )
        _customDonors.value = (_customDonors.value + newDonor)
    }

    // Filter Setters
    fun setDonationSearchQuery(query: String) { _donationSearchQuery.value = query }
    fun setDonationStatusFilter(status: String) { _donationStatusFilter.value = status }
    fun setDonationModeFilter(mode: String) { _donationModeFilter.value = mode }
    fun setDonationMemberFilter(memberId: Long?) { _donationMemberFilter.value = memberId }
    fun setExpenseSearchQuery(query: String) { _expenseSearchQuery.value = query }
    fun setExpenseCategoryFilter(category: String) { _expenseCategoryFilter.value = category }
    fun setExpenseMemberFilter(memberId: Long?) { _expenseMemberFilter.value = memberId }
    fun setTimePeriodFilter(period: String) { _timePeriodFilter.value = period }
    fun setDonationTimePeriod(period: String) = setTimePeriodFilter(period)
    fun setExpenseTimePeriod(period: String) = setTimePeriodFilter(period)

    // CRUD for Donations
    fun addDonation(
        donorName: String,
        phone: String,
        amount: Double,
        status: String,
        paymentMode: String,
        paymentDate: Long,
        notes: String,
        onSuccess: ((Donation) -> Unit)? = null
    ) {
        val cleanDigits = phone.trim().filter { it.isDigit() }
        val cleanPhone = if (cleanDigits.length >= 10) cleanDigits.takeLast(10) else cleanDigits
        if (amount <= 0 || donorName.isBlank()) {
            return
        }
        viewModelScope.launch {
            val member = _currentMember.value
            val recordedId = member?.id ?: 1
            val recordedName = member?.let { "${it.name} (${it.role})" } ?: "Admin"
            val count = allDonations.value.size + 1
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val prefix = if (status == "received") "RCP" else "PND"
            val receiptNo = "$prefix-$year-${String.format("%03d", count)}"

            val userEmail = member?.email ?: "committee@society.org"
            val userId = member?.id?.toString() ?: "admin_1"

            val donation = Donation(
                donorName = donorName.trim(),
                phone = cleanPhone,
                amount = amount,
                status = status,
                paymentMode = if (status == "received") paymentMode else "Pending",
                paymentDate = paymentDate,
                receiptNo = receiptNo,
                notes = notes.trim(),
                recordedById = recordedId,
                recordedByName = recordedName,
                userId = userId,
                userEmail = userEmail
            )
            val insertedId = repository.insertDonation(donation, userId = userId, userEmail = userEmail)
            val finalDonation = if (insertedId > 0) donation.copy(id = insertedId) else donation
            onSuccess?.invoke(finalDonation)
        }
    }

    fun updateDonation(donation: Donation) {
        val cleanDigits = donation.phone.trim().filter { it.isDigit() }
        val cleanPhone = if (cleanDigits.length >= 10) cleanDigits.takeLast(10) else cleanDigits
        if (donation.amount <= 0 || donation.donorName.isBlank()) {
            return
        }
        viewModelScope.launch {
            repository.updateDonation(donation.copy(phone = cleanPhone, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteDonation(donation: Donation) {
        viewModelScope.launch {
            repository.deleteDonation(donation)
        }
    }

    fun markDonationAsPaid(
        donationId: Long,
        paymentMode: String,
        paymentDate: Long,
        notes: String,
        onSuccess: ((Donation) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val member = _currentMember.value
            val recordedId = member?.id ?: 1
            val recordedName = member?.name ?: "Admin"
            repository.markDonationAsPaid(
                donationId = donationId,
                paymentMode = paymentMode,
                paymentDate = paymentDate,
                notes = notes,
                recordedById = recordedId,
                recordedByName = recordedName
            )
            val existing = allDonations.value.firstOrNull { it.id == donationId }
            val updated = existing?.copy(
                status = "received",
                paymentMode = paymentMode,
                paymentDate = paymentDate,
                notes = notes,
                recordedById = recordedId,
                recordedByName = recordedName
            )
            if (updated != null) {
                onSuccess?.invoke(updated)
            }
        }
    }

    fun recordCommitteeMemberSettlement(
        member: Member,
        amount: Double,
        trustPaysMember: Boolean,
        paymentMode: String,
        referenceNo: String,
        notes: String,
        onResult: (Result<Long>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = runCatching {
                val committeeMember = CommitteeMember.fromMember(member).copy(
                    id = member.id,
                    societyId = repository.getSocietyInfo().name
                )
                repository.recordCommitteeMemberSettlement(
                    member = committeeMember,
                    amount = amount,
                    trustPaysMember = trustPaysMember,
                    paymentMode = paymentMode,
                    referenceNo = referenceNo,
                    notes = notes,
                    recordedByName = _currentMember.value?.name ?: "Admin"
                )
            }
            onResult(result)
        }
    }

    // CRUD for Expenses
    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        expenseDate: Long,
        paidTo: String,
        paymentMode: String,
        receiptUrl: String,
        paidByCommitteeMemberId: Long = 0L,
        onSuccess: ((Expense) -> Unit)? = null
    ) {
        if (amount <= 0 || title.isBlank() || paidTo.isBlank()) {
            return
        }
        viewModelScope.launch {
            val member = _currentMember.value
            val recordedId = member?.id ?: 1
            val recordedName = member?.let { "${it.name} (${it.role})" } ?: "Admin"
            val userEmail = member?.email ?: "committee@society.org"
            val userId = member?.id?.toString() ?: "admin_1"

            val expense = Expense(
                title = title.trim(),
                category = category,
                amount = amount,
                expenseDate = expenseDate,
                paidTo = paidTo.trim(),
                paymentMode = paymentMode,
                receiptUrl = receiptUrl.trim(),
                recordedById = recordedId,
                recordedByName = recordedName,
                userId = userId,
                userEmail = userEmail
            )
            val id = repository.insertExpense(expense, userId = userId, userEmail = userEmail)
            val savedExpense = expense.copy(id = id)
            onSuccess?.invoke(savedExpense)
        }
    }

    fun updateExpense(expense: Expense) {
        if (expense.amount <= 0 || expense.title.isBlank() || expense.paidTo.isBlank()) {
            return
        }
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Flag whether current user is Admin (strictly requires active admin login and admin authority)
    val isCurrentSuperAdmin: StateFlow<Boolean> = combine(
        _currentMember,
        _isAdminMode
    ) { member, isAdmin ->
        isAdmin && member != null && (
            member.isSuperAdmin ||
            member.role.equals("Admin", ignoreCase = true) ||
            member.role.equals("Super Admin", ignoreCase = true) ||
            member.role.contains("Admin", ignoreCase = true)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updateSocietyInfo(newInfo: SocietyInfo) {
        viewModelScope.launch {
            repository.saveSocietyInfo(newInfo)
            _societyInfo.value = newInfo

            // Keep directory list on Home screen in sync
            val currentList = _directorySocieties.value.toMutableList()
            val index = currentList.indexOfFirst {
                (it.registrationNo.isNotBlank() && it.registrationNo.equals(newInfo.registrationNo, ignoreCase = true)) ||
                it.name.equals(newInfo.name, ignoreCase = true)
            }
            val updatedSoc = newInfo.toSociety()
            if (index >= 0) {
                currentList[index] = updatedSoc.copy(id = currentList[index].id)
            } else {
                currentList.add(0, updatedSoc)
            }
            _directorySocieties.value = currentList
            preferences.saveDirectorySocieties(currentList)
            repository.saveSociety(updatedSoc)
        }
    }

    /**
     * Master Bulk Setup for Super Admin:
     * Updates Society Info, Opening Balances, and optionally replaces or adds batch Present Donations & Expenses all at once.
     */
    fun bulkSetupPresentData(
        newSocietyInfo: SocietyInfo,
        presentDonations: List<Donation> = emptyList(),
        presentExpenses: List<Expense> = emptyList(),
        replaceExistingData: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            // 1. Save Society Information
            repository.saveSocietyInfo(newSocietyInfo)
            _societyInfo.value = newSocietyInfo

            val currentList = _directorySocieties.value.toMutableList()
            val index = currentList.indexOfFirst {
                (it.registrationNo.isNotBlank() && it.registrationNo.equals(newSocietyInfo.registrationNo, ignoreCase = true)) ||
                it.name.equals(newSocietyInfo.name, ignoreCase = true)
            }
            val updatedSoc = newSocietyInfo.toSociety()
            if (index >= 0) {
                currentList[index] = updatedSoc.copy(id = currentList[index].id)
            } else {
                currentList.add(0, updatedSoc)
            }
            _directorySocieties.value = currentList
            preferences.saveDirectorySocieties(currentList)

            // 2. Handle Data records
            if (replaceExistingData) {
                repository.clearTransactions()
            }

            if (presentDonations.isNotEmpty()) {
                repository.insertAllDonations(presentDonations)
            }

            if (presentExpenses.isNotEmpty()) {
                repository.insertAllExpenses(presentExpenses)
            }

            onComplete()
        }
    }

    // CRUD for Members (Super Admin Access)
    fun addMember(
        name: String,
        email: String,
        phone: String,
        role: String,
        pin: String = "",
        isSuperAdmin: Boolean = false,
        openingBalance: Double = 0.0,
        openingBalanceNotes: String = ""
    ) {
        if (!openingBalance.isFinite() || openingBalance < 0.0) return
        viewModelScope.launch {
            val member = Member(
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim(),
                role = role.trim(),
                pin = pin.trim(),
                isSuperAdmin = isSuperAdmin
            )
            repository.insertMember(member, openingBalance, openingBalanceNotes)
        }
    }

    fun addMember(member: Member) {
        viewModelScope.launch {
            repository.insertMember(member)
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            repository.updateMember(member)
            if (_currentMember.value?.id == member.id) {
                _currentMember.value = member
            }
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
            if (_currentMember.value?.id == member.id) {
                val remaining = allMembers.value.filter { it.id != member.id }
                _currentMember.value = remaining.firstOrNull()
            }
        }
    }

    // PIN Management
    fun changeMemberPin(memberId: Long, newPin: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.updateMemberPin(memberId, newPin)
            if (success && _currentMember.value?.id == memberId) {
                _currentMember.value = _currentMember.value?.copy(pin = newPin)
            }
            onResult(success)
        }
    }

    fun resetMemberPin(memberId: Long, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.resetMemberPin(memberId)
            if (success && _currentMember.value?.id == memberId) {
                _currentMember.value = _currentMember.value?.copy(pin = "1234")
            }
            onResult(success)
        }
    }

    // Selective Financial Year Management
    fun setSelectedFinancialYear(financialYear: String) {
        _selectedFinancialYear.value = financialYear
    }

    /**
     * Rolls over to the next financial year:
     * Takes current closing balance (Opening Reserve + Received - Expenses)
     * Sets it as the new opening reserve of the next financial year.
     */
    fun startNextFinancialYear(
        nextFinancialYearName: String,
        archiveCurrentTransactions: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentInfo = _societyInfo.value
            val totalReceived = allDonations.value.filter { it.status == "received" }.sumOf { it.amount }
            val totalExpenses = allExpenses.value.sumOf { it.amount }
            val closingBalance = currentInfo.openingBalance + totalReceived - totalExpenses

            val updatedInfo = currentInfo.copy(
                financialYear = nextFinancialYearName.trim(),
                openingBalance = closingBalance
            )

            repository.saveSocietyInfo(updatedInfo)
            _societyInfo.value = updatedInfo

            if (archiveCurrentTransactions) {
                repository.clearTransactions()
            }
            onComplete()
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }

    /**
     * Super Admin modification of Donation with COMPULSORY reason.
     * Committee members cannot modify; Super Admin only.
     */
    fun superAdminModifyDonation(
        original: Donation,
        updated: Donation,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (reason.trim().length < 5) {
            onResult(false, "Compulsory reason (min 5 characters) is required for audit trail.")
            return
        }
        val admin = _currentMember.value
        if (admin == null || (!admin.isSuperAdmin && !admin.role.contains("Admin", ignoreCase = true))) {
            onResult(false, "Unauthorized: Only Admin can modify entries.")
            return
        }
        viewModelScope.launch {
            try {
                repository.superAdminModifyDonation(original, updated, reason.trim(), admin)
                onResult(true, "Transaction modified & logged into audit trail.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to modify transaction.")
            }
        }
    }

    /**
     * Admin deletion of Donation with COMPULSORY reason.
     * Marks as deleted with red badge and excludes from accounts.
     */
    fun superAdminDeleteDonation(
        donation: Donation,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (reason.trim().length < 5) {
            onResult(false, "Compulsory reason (min 5 characters) is required to delete/void transaction.")
            return
        }
        val admin = _currentMember.value
        if (admin == null || (!admin.isSuperAdmin && !admin.role.contains("Admin", ignoreCase = true))) {
            onResult(false, "Unauthorized: Only Admin can delete entries.")
            return
        }
        viewModelScope.launch {
            try {
                repository.superAdminDeleteDonation(donation, reason.trim(), admin)
                onResult(true, "Transaction voided, highlighted in red, and excluded from accounts.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete transaction.")
            }
        }
    }

    /**
     * Admin modification of Expense with COMPULSORY reason.
     */
    fun superAdminModifyExpense(
        original: Expense,
        updated: Expense,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (reason.trim().length < 5) {
            onResult(false, "Compulsory reason (min 5 characters) is required for audit trail.")
            return
        }
        val admin = _currentMember.value
        if (admin == null || (!admin.isSuperAdmin && !admin.role.contains("Admin", ignoreCase = true))) {
            onResult(false, "Unauthorized: Only Admin can modify expense entries.")
            return
        }
        viewModelScope.launch {
            try {
                repository.superAdminModifyExpense(original, updated, reason.trim(), admin)
                onResult(true, "Expense modified & logged into audit trail.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to modify expense.")
            }
        }
    }

    /**
     * Admin deletion of Expense with COMPULSORY reason.
     */
    fun superAdminDeleteExpense(
        expense: Expense,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (reason.trim().length < 5) {
            onResult(false, "Compulsory reason (min 5 characters) is required to delete/void expense.")
            return
        }
        val admin = _currentMember.value
        if (admin == null || (!admin.isSuperAdmin && !admin.role.contains("Admin", ignoreCase = true))) {
            onResult(false, "Unauthorized: Only Admin can delete expense entries.")
            return
        }
        viewModelScope.launch {
            try {
                repository.superAdminDeleteExpense(expense, reason.trim(), admin)
                onResult(true, "Expense voided, highlighted in red, and excluded from accounts.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete expense.")
            }
        }
    }

    // Societies Search & Switch State
    private val _societySearchQuery = MutableStateFlow("")
    val societySearchQuery: StateFlow<String> = _societySearchQuery.asStateFlow()

    fun setSocietySearchQuery(query: String) {
        _societySearchQuery.value = query
    }

    // Pre-registered directory of societies for search & pin
    private fun loadInitialDirectorySocieties(): List<com.example.data.model.Society> {
        val saved = preferences.getSavedDirectorySocieties()
        val activeSoc = preferences.getSocietyInfo().toSociety("active_soc")

        val baseList = mutableListOf<com.example.data.model.Society>()
        if (saved.isNotEmpty()) {
            baseList.addAll(saved)
        }

        // Ensure active society is present if non-empty
        if (activeSoc.name.isNotBlank()) {
            val exists = baseList.any {
                it.name.equals(activeSoc.name, ignoreCase = true) ||
                        (it.registrationNo.isNotBlank() && it.registrationNo.equals(activeSoc.registrationNo, ignoreCase = true))
            }
            if (!exists) {
                baseList.add(0, activeSoc)
            }
        }

        return baseList
    }

    private val _directorySocieties = MutableStateFlow<List<com.example.data.model.Society>>(loadInitialDirectorySocieties())
    val directorySocieties: StateFlow<List<com.example.data.model.Society>> = _directorySocieties.asStateFlow()

    val searchableSocieties: StateFlow<List<com.example.data.model.Society>> = combine(
        _directorySocieties,
        _societySearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.registrationNo.contains(query, ignoreCase = true) ||
            it.city.contains(query, ignoreCase = true) ||
            it.address.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Auto-select Super Admin once database members are loaded
        viewModelScope.launch {
            allMembers.collect { members ->
                if (members.isNotEmpty()) {
                    val superAdmin = members.find { it.isSuperAdmin }
                        ?: members.find { it.role.contains("Super Admin", ignoreCase = true) }
                        ?: members.first()
                    if (_currentMember.value == null || members.none { it.id == _currentMember.value?.id }) {
                        _currentMember.value = superAdmin
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.ensureInitialData()
            syncAllFromFirestore()
            // Default to continuous cross-device auto-sync in the background
            while (isActive) {
                delay(30_000L)
                try {
                    repository.syncAllFromFirestore()
                } catch (_: Exception) {
                    // Silent background retry on next interval
                }
            }
        }

        viewModelScope.launch {
            try {
                val remoteConfig = firestoreService.getMasterAdminConfig()
                val remotePin = (remoteConfig?.get("masterPin") as? String)?.trim()
                if (!remotePin.isNullOrBlank()) {
                    preferences.saveAppAdminPin(remotePin)
                    preferences.setAppAdminPinConfigured(true)
                }
            } catch (_: Exception) {}
        }

        firestoreService.observeMasterAdminConfig { config ->
            val remotePin = (config?.get("masterPin") as? String)?.trim()
            if (!remotePin.isNullOrBlank()) {
                preferences.saveAppAdminPin(remotePin)
                preferences.setAppAdminPinConfigured(true)
            }
        }
    }

    /**
     * Retrieves the committee members roster for any given society.
     * If it is the active society, returns the live members list from the database.
     * Otherwise, provides the registered committee members with their specific roles and allotted PINs.
     */
    fun getMembersForSociety(society: com.example.data.model.Society): List<Member> {
        val currentInfo = _societyInfo.value
        val liveMembers = allMembers.value
        val isCurrentSoc = society.registrationNo == currentInfo.registrationNo ||
                society.id == currentInfo.registrationNo ||
                society.name.equals(currentInfo.name, ignoreCase = true)

        if (isCurrentSoc && liveMembers.isNotEmpty()) {
            return liveMembers
        }

        val socPin = preferences.getSocietyPin(society.id) ?: preferences.getSocietyPin(society.registrationNo) ?: ""

        return listOf(
            Member(
                id = 1,
                name = society.presidentName.ifBlank { "Admin" },
                role = "President & Admin",
                phone = society.contactPhone,
                email = society.adminEmail,
                pin = socPin,
                isSuperAdmin = true
            )
        )
    }

    /**
     * Authenticates a specific committee member using their allotted PIN and switches to the society.
     * Enters into the member's controlled access page (non-super-admin for regular members).
     */
    fun enterSocietyAsMember(
        society: com.example.data.model.Society,
        selectedMember: Member,
        enteredPin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanPin = enteredPin.trim()
        val currentSuperAdminPin = preferences.getSuperAdminPin()
        val societyCustomPin = preferences.getSocietyPin(society.id) ?: preferences.getSocietyPin(society.registrationNo)
        val isSuperAdminMember = selectedMember.isSuperAdmin ||
                selectedMember.role.contains("President", ignoreCase = true) ||
                selectedMember.role.contains("Super Admin", ignoreCase = true)

        // Verify PIN: Super Admin matches the PIN provided on creation or super admin pin; regular members only match their assigned PIN if set
        val isPinValid = if (isSuperAdminMember) {
            cleanPin.isNotBlank() && (
                cleanPin == selectedMember.pin ||
                (currentSuperAdminPin.isNotBlank() && cleanPin == currentSuperAdminPin) ||
                (societyCustomPin != null && cleanPin == societyCustomPin)
            )
        } else {
            selectedMember.pin.isNotBlank() && cleanPin == selectedMember.pin
        }

        if (!isPinValid) {
            if (!isSuperAdminMember && selectedMember.pin.isBlank()) {
                onResult(false, "No PIN has been allotted for ${selectedMember.name}. Only Admin can assign a PIN in the Committee Roster.")
            } else {
                onResult(false, "Incorrect PIN for ${selectedMember.name}.")
            }
            return
        }

        val newInfo = SocietyInfo(
            name = society.name,
            registrationNo = society.registrationNo,
            upiId = society.upiId,
            bankName = society.bankName,
            accountNumber = society.accountNumber,
            ifscCode = society.ifscCode,
            branchName = society.branchName,
            bankAccount = society.bankAccount,
            address = society.address,
            city = society.city,
            state = society.state,
            pinCode = society.pinCode,
            contactEmail = society.contactEmail,
            adminEmail = society.adminEmail,
            contactPhone = society.contactPhone,
            financialYear = society.financialYear,
            openingBalance = society.openingBalance,
            presidentName = society.presidentName,
            secretaryName = society.secretaryName,
            treasurerName = society.treasurerName
        )

        viewModelScope.launch {
            repository.switchActiveSociety(society)
            _societyInfo.value = newInfo

            // Set the authenticated member and admin mode
            _currentMember.value = selectedMember
            _isAdminMode.value = true

            val accessLevel = if (selectedMember.isSuperAdmin) "Admin Workspace" else "${selectedMember.role} Member Workspace"
            onResult(true, "Unlocked as ${selectedMember.name} • $accessLevel for ${society.name}")
        }
    }

    /**
     * User search and PIN to enter society:
     * If user enters with valid Admin PIN or allotted member PIN, enters in Committee Admin Mode.
     * If user enters without PIN or as resident, enters in Public Read-Only Transparency mode.
     */
    fun enterSociety(
        society: com.example.data.model.Society,
        enteredPin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanPin = enteredPin.trim()
        val currentSuperAdminPin = preferences.getSuperAdminPin()
        val societyCustomPin = preferences.getSocietyPin(society.id) ?: preferences.getSocietyPin(society.registrationNo)
        val membersList = getMembersForSociety(society)
        val matchedMember = if (cleanPin.isNotBlank()) membersList.find { it.pin.isNotBlank() && it.pin == cleanPin } else null

        val newInfo = SocietyInfo(
            name = society.name,
            registrationNo = society.registrationNo,
            upiId = society.upiId,
            bankName = society.bankName,
            accountNumber = society.accountNumber,
            ifscCode = society.ifscCode,
            branchName = society.branchName,
            bankAccount = society.bankAccount,
            address = society.address,
            city = society.city,
            state = society.state,
            pinCode = society.pinCode,
            contactEmail = society.contactEmail,
            adminEmail = society.adminEmail,
            contactPhone = society.contactPhone,
            financialYear = society.financialYear,
            openingBalance = society.openingBalance,
            presidentName = society.presidentName,
            secretaryName = society.secretaryName,
            treasurerName = society.treasurerName
        )

        viewModelScope.launch {
            repository.switchActiveSociety(society)
            _societyInfo.value = newInfo

            if (cleanPin.isBlank()) {
                // Public Resident Viewer Mode
                _isAdminMode.value = false
                onResult(true, "Entered ${society.name} in Public Resident Transparency Mode.")
            } else {
                val isSuperAdminMatch = (currentSuperAdminPin.isNotBlank() && cleanPin == currentSuperAdminPin) ||
                        (societyCustomPin != null && cleanPin == societyCustomPin)

                if (isSuperAdminMatch || matchedMember != null) {
                    _isAdminMode.value = true
                    val targetMember = matchedMember ?: membersList.firstOrNull { it.isSuperAdmin } ?: membersList.firstOrNull()
                    _currentMember.value = targetMember
                    onResult(true, "Authenticated as ${targetMember?.role ?: "Admin"} for ${society.name}.")
                } else {
                    onResult(false, "Incorrect PIN. You can still enter in Public Resident Mode.")
                }
            }
        }
    }

    /**
     * Create a new draft/registered society with custom Super Admin PIN to enter.
     * Only the Super Admin PIN is configured; regular members have no default PIN.
     */
    fun createNewSociety(
        newSociety: com.example.data.model.Society,
        adminPin: String,
        switchToNewSociety: Boolean = true,
        onResult: (Boolean, String) -> Unit
    ) {
        if (newSociety.name.isBlank()) {
            onResult(false, "Society name is required.")
            return
        }
        val cleanPin = adminPin.trim()
        if (cleanPin.length < 4) {
            onResult(false, "Admin PIN must be at least 4 digits.")
            return
        }
        // If not entered while creating, accounts must be 0
        val cleanOpeningBalance = if (newSociety.openingBalance > 0.0) newSociety.openingBalance else 0.0
        val societyId = "soc_" + System.currentTimeMillis()
        val societyWithId = newSociety.copy(
            id = societyId,
            openingBalance = cleanOpeningBalance,
            adminEmail = newSociety.adminEmail.ifBlank { "president@society.org" },
            contactPhone = newSociety.contactPhone
        )
        preferences.saveSocietyPin(societyId, cleanPin)
        if (newSociety.registrationNo.isNotBlank()) {
            preferences.saveSocietyPin(newSociety.registrationNo, cleanPin)
        }

        val updatedList = _directorySocieties.value.filterNot {
            it.id == societyWithId.id ||
            (it.registrationNo.isNotBlank() && it.registrationNo.equals(societyWithId.registrationNo, ignoreCase = true)) ||
            it.name.equals(societyWithId.name, ignoreCase = true)
        }.toMutableList()
        // Place new society at the top of Home screen registered societies list
        updatedList.add(0, societyWithId)
        _directorySocieties.value = updatedList
        preferences.saveDirectorySocieties(updatedList)

        viewModelScope.launch {
            try {
                if (switchToNewSociety) {
                    repository.initializeNewSocietyCleanLedger(societyWithId, cleanPin)
                    val newInfo = societyWithId.toSocietyInfo()
                    _societyInfo.value = newInfo
                    _isAdminMode.value = true
                    preferences.saveSuperAdminPin(cleanPin)
                    _currentMember.value = com.example.data.model.Member(
                        id = 1,
                        name = societyWithId.presidentName.ifBlank { "Admin" },
                        role = "President & Admin",
                        phone = societyWithId.contactPhone.ifBlank { "9876543210" },
                        email = societyWithId.adminEmail.ifBlank { "admin@society.org" },
                        pin = cleanPin,
                        isSuperAdmin = true,
                        flatNo = "Office"
                    )
                } else {
                    repository.saveSociety(societyWithId)
                }

                onResult(true, "Society '${societyWithId.name}' registered with ₹${cleanOpeningBalance} balance.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to save society.")
            }
        }
    }

    /**
     * Updates emergency and maintenance contact telephone numbers for the active society.
     */
    fun updateEmergencyContacts(
        securityGate: String,
        maintenanceLead: String,
        plumber: String,
        emergencyHelpline: String,
        adminPhone: String,
        adminEmail: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val current = _societyInfo.value
        val updated = current.copy(
            securityGatePhone = securityGate.trim(),
            maintenanceLeadPhone = maintenanceLead.trim(),
            plumberPhone = plumber.trim(),
            emergencyHelplinePhone = emergencyHelpline.trim(),
            contactPhone = adminPhone.trim(),
            adminEmail = adminEmail.trim()
        )
        viewModelScope.launch {
            try {
                repository.saveSocietyInfo(updated)
                _societyInfo.value = updated
                onResult(true, "Emergency & Maintenance helpline contacts updated successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to update emergency contacts.")
            }
        }
    }

    /**
     * Synchronizes all societies, committee members, and transactions from Firebase Firestore.
     */
    fun syncAllFromFirestore(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val success = repository.syncAllFromFirestore()
            if (success) {
                _societyInfo.value = repository.getSocietyInfo()
                _directorySocieties.value = preferences.getSavedDirectorySocieties()
            }
            _isCloudSyncing.value = false
            onComplete?.invoke(success)
        }
    }

    /**
     * Force synchronizes the device with Firestore:
     * - Pushes local directory societies to Firestore
     * - Pulls latest societies, members, committee members, and transactions from Firestore
     * - Refreshes local StateFlows so the UI updates immediately
     */
    fun forceSyncDeviceWithFirestore(onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                // Ensure local societies are saved to Firestore
                val localSocieties = preferences.getSavedDirectorySocieties()
                localSocieties.forEach { soc ->
                    try {
                        firestoreService.saveSociety(soc)
                    } catch (_: Exception) {}
                }

                val success = repository.syncAllFromFirestore()
                val updatedSocieties = preferences.getSavedDirectorySocieties()
                _directorySocieties.value = updatedSocieties
                _societyInfo.value = repository.getSocietyInfo()

                _isCloudSyncing.value = false
                val msg = if (success) {
                    "Synced device with Firestore: ${updatedSocieties.size} societies up to date"
                } else {
                    "Sync completed with device storage (${updatedSocieties.size} societies available)"
                }
                onResult?.invoke(true, msg)
            } catch (e: Exception) {
                _isCloudSyncing.value = false
                onResult?.invoke(false, "Firestore sync error: ${e.message}")
            }
        }
    }

    fun uploadAllToFirestore(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val (success, message) = repository.uploadAllLocalDataToFirestore()
            _isCloudSyncing.value = false
            onResult(success, message)
        }
    }

    /**
     * Initializes the Firestore schema with nested collections under the active society:
     * - /societies/{societyName}
     *   - /members
     *   - /committee_members
     *   - /donations
     *   - /expenses
     *   - /transactions
     *   - /pledges
     *   - /audit_logs
     */
    fun initializeFirestoreSchema(onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val (success, message) = repository.initializeNestedFirestoreSchema()
            _isCloudSyncing.value = false
            syncAllFromFirestore()
            onResult?.invoke(success, message)
        }
    }

    /**
     * Recreates all Firestore collections and local database records with the new nested structure:
     * /societies/{societyName}/[members, donations, expenses, transactions, pledges, committee_members, audit_logs]
     */
    fun recreateAllDataWithNewStructure(onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val (success, message) = repository.recreateAllDataWithNewStructure()
            _isCloudSyncing.value = false
            syncAllFromFirestore()
            onResult?.invoke(success, message)
        }
    }

    fun testCloudConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            val (success, message) = repository.testCloudConnection()
            _isCloudSyncing.value = false
            onResult(success, message)
        }
    }

    /**
     * Delete Society with Super Admin PIN and compulsory reason
     */
    fun deleteEntireSociety(
        pin: String,
        confirmSocietyName: String,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentPin = preferences.getSuperAdminPin()
        val currentInfo = _societyInfo.value
        val socCustomPin = preferences.getSocietyPin(currentInfo.registrationNo) ?: preferences.getSocietyPin(currentInfo.name)
        val cleanPin = pin.trim()

        val isPinCorrect = (currentPin.isNotBlank() && cleanPin == currentPin) ||
                (socCustomPin != null && cleanPin == socCustomPin)

        if (!isPinCorrect) {
            onResult(false, "Invalid Admin PIN.")
            return
        }

        if (!confirmSocietyName.trim().equals(currentInfo.name.trim(), ignoreCase = true)) {
            onResult(false, "Society name does not match current society name.")
            return
        }

        if (reason.trim().length < 8) {
            onResult(false, "Please provide a detailed reason (at least 8 characters) for audit trail.")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteEntireSociety(currentInfo.name)
                val updatedList = _directorySocieties.value.filterNot {
                    it.name.equals(currentInfo.name, ignoreCase = true) ||
                    (it.registrationNo.isNotBlank() && it.registrationNo.equals(currentInfo.registrationNo, ignoreCase = true))
                }
                _directorySocieties.value = updatedList
                preferences.saveDirectorySocieties(updatedList)

                // Log final audit record
                repository.insertAuditLog(
                    com.example.data.model.AuditLog(
                        transactionType = "SOCIETY",
                        transactionRef = currentInfo.name,
                        action = "DELETED",
                        reason = reason.trim(),
                        performedByName = _currentMember.value?.name ?: "Admin",
                        performedByRole = "Admin",
                        oldValueSummary = "Society: ${currentInfo.name} | Reg: ${currentInfo.registrationNo}",
                        newValueSummary = "SOCIETY PERMANENTLY PURGED",
                        timestamp = System.currentTimeMillis()
                    )
                )
                onResult(true, "Society and all associated records deleted successfully.")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Failed to delete society.")
            }
        }
    }

    /**
     * Subscription & Razorpay Management Operations
     */
    fun saveRazorpayKey(keyId: String) {
        preferences.saveRazorpayKeyId(keyId.trim())
        _razorpayKeyId.value = keyId.trim()
    }

    fun activateProSubscription(
        paymentTxnId: String,
        planDurationDays: Int = 365,
        onResult: (Boolean, String) -> Unit
    ) {
        val now = System.currentTimeMillis()
        val currentExpiry = _subscriptionExpiry.value
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val newExpiry = baseTime + (planDurationDays.toLong() * 24 * 60 * 60 * 1000L)
        val cleanTxn = if (paymentTxnId.isNotBlank()) paymentTxnId.trim() else "pay_rzp_${System.currentTimeMillis()}"

        preferences.saveSubscriptionDetails(
            isPro = true,
            expiryTime = newExpiry,
            transactionId = cleanTxn
        )
        _isProSubscriber.value = true
        _subscriptionExpiry.value = newExpiry
        _lastPaymentTxnId.value = cleanTxn

        viewModelScope.launch {
            try {
                repository.insertAuditLog(
                    com.example.data.model.AuditLog(
                        transactionType = "SUBSCRIPTION",
                        transactionRef = cleanTxn,
                        action = "ACTIVATED",
                        reason = "Annual Pro Plan activated (₹499/year via Razorpay).",
                        performedByName = _currentMember.value?.name ?: "Admin",
                        performedByRole = "Billing Admin",
                        oldValueSummary = if (_isProSubscriber.value) "Active Pro" else "Free Trial (3 Months)",
                        newValueSummary = "Pro Active until ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(newExpiry))}",
                        timestamp = now
                    )
                )
                onResult(true, "Pro Subscription activated successfully! Valid for 1 Year.")
            } catch (e: Exception) {
                onResult(true, "Pro Subscription activated successfully!")
            }
        }
    }

    /**
     * Master App Admin: Service Complaints & Support Tickets Operations
     */
    fun submitServiceComplaint(
        complaint: com.example.data.model.AppComplaint,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val currentList = _complaints.value.toMutableList()
        currentList.add(0, complaint)
        _complaints.value = currentList
        onResult(true, "Complaint #${complaint.id} logged. Support team will respond shortly.")
    }

    fun updateComplaintStatus(
        complaintId: String,
        newStatus: String,
        resolutionNotes: String = "",
        adminNotes: String = "",
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val currentList = _complaints.value.map { item ->
            if (item.id == complaintId) {
                item.copy(
                    status = newStatus,
                    resolutionNotes = if (resolutionNotes.isNotBlank()) resolutionNotes else item.resolutionNotes,
                    adminNotes = if (adminNotes.isNotBlank()) adminNotes else item.adminNotes,
                    resolvedAt = if (newStatus == "RESOLVED") System.currentTimeMillis() else item.resolvedAt
                )
            } else {
                item
            }
        }
        _complaints.value = currentList
        onResult(true, "Ticket #$complaintId updated to $newStatus.")
    }

    /**
     * Master App Admin: Premium & License Management across all societies
     */
    fun masterAdminGrantPro(
        society: com.example.data.model.Society,
        days: Int = 365,
        paymentTxnId: String,
        approvalNote: String = "Approved by Master Admin",
        onResult: (Boolean, String) -> Unit
    ) {
        val now = System.currentTimeMillis()
        val currentExpiry = preferences.getSocietySubscriptionExpiry(society.id)
        val baseTime = if (currentExpiry > now) currentExpiry else now
        val newExpiry = baseTime + (days.toLong() * 24 * 60 * 60 * 1000L)
        val cleanTxn = if (paymentTxnId.isNotBlank()) paymentTxnId.trim() else "ADMIN_GRANT_${System.currentTimeMillis()}"

        // Save override for society
        preferences.saveSocietySubscriptionOverride(society.id, true, newExpiry, cleanTxn)
        if (society.registrationNo.isNotBlank()) {
            preferences.saveSocietySubscriptionOverride(society.registrationNo, true, newExpiry, cleanTxn)
        }

        // If currently active society, update live state
        if (society.name == _societyInfo.value.name) {
            preferences.saveSubscriptionDetails(true, newExpiry, cleanTxn)
            _isProSubscriber.value = true
            _subscriptionExpiry.value = newExpiry
            _lastPaymentTxnId.value = cleanTxn
        }

        viewModelScope.launch {
            try {
                repository.insertAuditLog(
                    com.example.data.model.AuditLog(
                        transactionType = "MASTER_ADMIN_SUBSCRIPTION",
                        transactionRef = "${society.name} • $cleanTxn",
                        action = "PRO_APPROVED",
                        reason = approvalNote,
                        performedByName = "Master Admin (Owner)",
                        performedByRole = "Master Admin / App Owner",
                        oldValueSummary = "Society: ${society.name} License Request",
                        newValueSummary = "Pro Active ($days days) until ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(newExpiry))}",
                        timestamp = now
                    )
                )
                onResult(true, "Pro Plan granted to ${society.name} for $days days! Expiry: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(newExpiry))}")
            } catch (e: Exception) {
                onResult(true, "Pro Plan granted to ${society.name} successfully!")
            }
        }
    }

    fun masterAdminRevokePro(
        society: com.example.data.model.Society,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        preferences.saveSocietySubscriptionOverride(society.id, false, 0L, "REVOKED")
        if (society.registrationNo.isNotBlank()) {
            preferences.saveSocietySubscriptionOverride(society.registrationNo, false, 0L, "REVOKED")
        }

        if (society.name == _societyInfo.value.name) {
            preferences.saveSubscriptionDetails(false, 0L, "")
            _isProSubscriber.value = false
            _subscriptionExpiry.value = 0L
        }

        viewModelScope.launch {
            try {
                repository.insertAuditLog(
                    com.example.data.model.AuditLog(
                        transactionType = "MASTER_ADMIN_SUBSCRIPTION",
                        transactionRef = society.name,
                        action = "PRO_REVOKED",
                        reason = reason.ifBlank { "Revoked by Master Admin." },
                        performedByName = "Master Admin (Owner)",
                        performedByRole = "Master Admin / App Owner",
                        oldValueSummary = "Pro Active License",
                        newValueSummary = "Reverted to Free Trial / Standard",
                        timestamp = System.currentTimeMillis()
                    )
                )
                onResult(true, "Pro subscription revoked for ${society.name}.")
            } catch (e: Exception) {
                onResult(true, "Subscription updated.")
            }
        }
    }

    fun masterAdminUpdateGlobalAnnouncement(
        message: String,
        enabled: Boolean,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        preferences.saveGlobalAnnouncement(message, enabled)
        _globalAnnouncement.value = message.trim()
        _isAnnouncementEnabled.value = enabled
        onResult(true, "Global announcement broadcast updated successfully.")
    }

    fun verifyMasterAdminPin(pin: String): Boolean {
        val clean = pin.trim()
        if (clean.isBlank()) return false
        val appAdminPin = preferences.getAppAdminPin().trim()
        if (appAdminPin.isNotBlank()) {
            return clean == appAdminPin
        }
        if (!preferences.isAppAdminPinConfigured()) {
            return clean == "0000"
        }
        return false
    }

    fun verifyMasterAdminAccess(
        enteredEmail: String,
        pin: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val cleanEmail = enteredEmail.trim().lowercase()
        val cleanPin = pin.trim()

        viewModelScope.launch {
            // 1. Verify authorized Master Admin email
            val isAuthorizedEmail = cleanEmail == "societyledger@gmail.com" ||
                    cleanEmail == "rentitease@gmail.com" ||
                    firestoreService.getFirebaseUserEmail()?.equals("societyledger@gmail.com", ignoreCase = true) == true ||
                    firestoreService.getFirebaseUserEmail()?.equals("rentitease@gmail.com", ignoreCase = true) == true

            if (!isAuthorizedEmail) {
                onResult(false, "Access Denied: Only authorized Master Admin ($cleanEmail is not authorized)")
                return@launch
            }

            if (cleanPin.isBlank()) {
                onResult(false, "Please enter Master PIN")
                return@launch
            }

            // 2. Fetch secure Master Admin configuration from Firestore
            val remoteConfig = firestoreService.getMasterAdminConfig()
            val remotePin = (remoteConfig?.get("masterPin") as? String)?.trim()
            val isRemoteConfigured = (remoteConfig?.get("isPinConfigured") as? Boolean == true) ||
                    (!remotePin.isNullOrBlank() && remotePin != "0000")

            if (!remotePin.isNullOrBlank()) {
                preferences.saveAppAdminPin(remotePin)
                preferences.setAppAdminPinConfigured(true)
            }

            val localSavedPin = preferences.getAppAdminPin().trim()
            val isLocallyConfigured = preferences.isAppAdminPinConfigured() ||
                    (localSavedPin.isNotBlank() && localSavedPin != "0000")

            val effectivePin = when {
                !remotePin.isNullOrBlank() -> remotePin
                localSavedPin.isNotBlank() -> localSavedPin
                else -> null
            }

            val isEverConfigured = isRemoteConfigured || isLocallyConfigured

            val isValidPin = when {
                !effectivePin.isNullOrBlank() -> cleanPin == effectivePin
                !isEverConfigured -> cleanPin == "0000"
                else -> false
            }

            if (isValidPin) {
                preferences.saveAppAdminPin(cleanPin)
                preferences.setAppAdminPinConfigured(true)
                onResult(true, "Authentication successful")
            } else {
                onResult(false, "Access Denied: Invalid Master Admin Security PIN")
            }
        }
    }

    fun masterAdminUpdateSuperAdminPin(
        newPin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val clean = newPin.trim()
        if (clean.length < 4) {
            onResult(false, "Master Admin PIN must be at least 4 digits.")
            return
        }
        preferences.saveAppAdminPin(clean)
        preferences.setAppAdminPinConfigured(true)
        viewModelScope.launch {
            try {
                // Save to Firestore so it syncs across all devices
                firestoreService.saveMasterAdminConfig(clean, listOf("societyledger@gmail.com"))
                onResult(true, "Master Admin Security PIN updated across Cloud & all devices.")
            } catch (e: Exception) {
                onResult(true, "Master Admin Console PIN updated locally.")
            }
        }
    }

    /**
     * Master Admin deletion of any society from Console
     * Removes society from Firestore (and its subcollections), directory preferences, and local DB if active.
     */
    fun masterAdminDeleteSociety(
        society: com.example.data.model.Society,
        reason: String = "Deleted by Master Admin from Admin Console",
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Delete from Firestore (by name and id)
                val cleanName = society.name.trim()
                try {
                    firestoreService.deleteSocietyInfo(cleanName)
                    if (society.id.isNotBlank() && !society.id.equals(cleanName, ignoreCase = true)) {
                        firestoreService.deleteSocietyInfo(society.id)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SocietyViewModel", "Error deleting society from firestore: ${e.message}")
                }

                // 2. Remove from Directory list in memory & SharedPreferences
                val currentList = _directorySocieties.value
                val updatedList = currentList.filterNot {
                    it.id == society.id ||
                    (it.registrationNo.isNotBlank() && it.registrationNo.equals(society.registrationNo, ignoreCase = true)) ||
                    it.name.equals(society.name, ignoreCase = true)
                }
                _directorySocieties.value = updatedList
                preferences.saveDirectorySocieties(updatedList)

                // 3. Clear stored pins for this society
                preferences.removeSocietyPin(society.id)
                preferences.removeSocietyPin(society.name)
                if (society.registrationNo.isNotBlank()) {
                    preferences.removeSocietyPin(society.registrationNo)
                }

                // 4. If this was the currently active society, switch or clear
                val currentInfo = _societyInfo.value
                val isCurrentlyActive = currentInfo.name.equals(society.name, ignoreCase = true) ||
                        (currentInfo.registrationNo.isNotBlank() && currentInfo.registrationNo.equals(society.registrationNo, ignoreCase = true))

                if (isCurrentlyActive) {
                    if (updatedList.isNotEmpty()) {
                        val nextSoc = updatedList.first()
                        enterSociety(nextSoc, "") { _, _ -> }
                    } else {
                        repository.clearTransactions()
                        preferences.clearAllSocietyData()
                        _societyInfo.value = com.example.data.model.SocietyInfo()
                    }
                }

                // 5. Audit Log
                try {
                    repository.insertAuditLog(
                        com.example.data.model.AuditLog(
                            transactionType = "SOCIETY",
                            transactionRef = society.name,
                            action = "MASTER_ADMIN_DELETED",
                            reason = reason.ifBlank { "Master Admin Console Deletion" },
                            performedByName = "Master Admin",
                            performedByRole = "Master Admin",
                            oldValueSummary = "Society: ${society.name} | Reg: ${society.registrationNo}",
                            newValueSummary = "PERMANENTLY PURGED FROM CONSOLE",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } catch (_: Exception) {}

                onResult(true, "Society '${society.name}' was permanently deleted from console & cloud.")
            } catch (e: Exception) {
                onResult(false, "Failed to delete society: ${e.message}")
            }
        }
    }

    /**
     * Data Restore / Import from JSON Backup
     */
    fun restoreDataFromJson(
        restoreData: com.example.ui.components.ExportHelper.RestoreBackupData,
        mergeMode: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!restoreData.success) {
            onResult(false, restoreData.errorMessage ?: "Failed to parse backup.")
            return
        }

        viewModelScope.launch {
            try {
                repository.restoreAllData(
                    societyInfo = restoreData.societyInfo,
                    donations = restoreData.donations,
                    expenses = restoreData.expenses,
                    members = restoreData.members,
                    mergeMode = mergeMode
                )

                if (restoreData.societyInfo != null) {
                    _societyInfo.value = restoreData.societyInfo
                }

                val now = System.currentTimeMillis()
                val admin = activeMember.value ?: members.value.firstOrNull { it.isSuperAdmin } ?: Member(name = "Admin", email = "admin@society.org", phone = "", role = "Admin")

                repository.insertAuditLog(
                    com.example.data.model.AuditLog(
                        transactionType = "BACKUP_RESTORE",
                        transactionRef = "${restoreData.donations.size} Donations, ${restoreData.expenses.size} Expenses",
                        action = if (mergeMode) "JSON_RESTORE_MERGE" else "JSON_RESTORE_OVERWRITE",
                        reason = if (mergeMode) "Merged JSON backup records into current ledger." else "Full restore from JSON backup (Replaced ledger records).",
                        performedByName = admin.name,
                        performedByRole = admin.role,
                        oldValueSummary = "Previous Ledger State",
                        newValueSummary = "Restored: ${restoreData.donations.size} donations, ${restoreData.expenses.size} expenses, ${restoreData.members.size} committee members",
                        timestamp = now
                    )
                )

                onResult(
                    true,
                    if (mergeMode) {
                        "Successfully merged ${restoreData.donations.size} donations & ${restoreData.expenses.size} expenses!"
                    } else {
                        "Successfully restored society records (${restoreData.donations.size} donations, ${restoreData.expenses.size} expenses)!"
                    }
                )
            } catch (e: Exception) {
                onResult(false, "Error restoring backup: ${e.localizedMessage}")
            }
        }
    }

    fun hasShownRatingDialog(): Boolean {
        return preferences.hasShownRatingDialog()
    }

    fun setHasShownRatingDialog(shown: Boolean) {
        preferences.setHasShownRatingDialog(shown)
    }
}
