package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents the cumulative financial account and custodial standing of a committee member.
 */
data class CommitteeMemberAccount(
    val member: Member,
    val totalReceivedAmount: Double,
    val receivedCount: Int,
    val totalPendingAmount: Double,
    val pendingCount: Int,
    val totalExpensesAmount: Double,
    val expensesCount: Int,
    val netBalance: Double, // totalReceivedAmount - totalExpensesAmount
    val cashReceived: Double,
    val onlineReceived: Double,
    val chequeReceived: Double,
    val cashExpenses: Double,
    val onlineExpenses: Double,
    val cashInHand: Double, // cashReceived - cashExpenses
    val donations: List<Donation>,
    val expenses: List<Expense>
) {
    val totalTransactionsCount: Int get() = receivedCount + pendingCount + expensesCount

    val balanceStatusText: String get() = when {
        netBalance > 0.0 -> "In Custody"
        netBalance < 0.0 -> "Claimable"
        else -> "Settled"
    }

    /**
     * Formats a clean, text-based member ledger statement suitable for sharing via WhatsApp or Email.
     */
    fun toShareableStatement(societyName: String): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val nowFormatted = dateFormat.format(Date())

        val sign = if (netBalance >= 0) "+" else ""
        return buildString {
            appendLine("📋 *COMMITTEE MEMBER CUMULATIVE ACCOUNT*")
            appendLine("🏛️ Society: $societyName")
            appendLine("👤 Member: ${member.name} (${member.role})")
            if (member.flatNo.isNotBlank()) appendLine("🏢 Flat / Unit: ${member.flatNo}")
            if (member.phone.isNotBlank()) appendLine("📞 Phone: +91 ${member.phone}")
            appendLine("─────────────────────────")
            appendLine("💰 *CUMULATIVE COLLECTIONS:* ₹${String.format(Locale.US, "%,.0f", totalReceivedAmount)} ($receivedCount receipts)")
            appendLine("   • Cash Received: ₹${String.format(Locale.US, "%,.0f", cashReceived)}")
            appendLine("   • UPI / Online: ₹${String.format(Locale.US, "%,.0f", onlineReceived)}")
            if (chequeReceived > 0) {
                appendLine("   • Cheque / Bank: ₹${String.format(Locale.US, "%,.0f", chequeReceived)}")
            }
            appendLine()
            appendLine("💸 *CUMULATIVE EXPENSES:* ₹${String.format(Locale.US, "%,.0f", totalExpensesAmount)} ($expensesCount vouchers)")
            appendLine("   • Cash Spent: ₹${String.format(Locale.US, "%,.0f", cashExpenses)}")
            appendLine("   • Online / Bank: ₹${String.format(Locale.US, "%,.0f", onlineExpenses)}")
            appendLine()
            appendLine("⚖️ *NET CUSTODIAL POSITION:* $sign₹${String.format(Locale.US, "%,.0f", netBalance)} ($balanceStatusText)")
            appendLine("💵 *Physical Cash In-Hand:* ₹${String.format(Locale.US, "%,.0f", cashInHand)}")
            if (pendingCount > 0) {
                appendLine("⏳ *Pending Pledges Follow-up:* ₹${String.format(Locale.US, "%,.0f", totalPendingAmount)} ($pendingCount pledges)")
            }
            appendLine("─────────────────────────")
            appendLine("📅 Statement Generated: $nowFormatted")
            appendLine("Verified via Society Ledger Committee Governance")
        }
    }
}

/**
 * Utility to calculate cumulative financial accounts for committee members.
 */
object CommitteeAccountCalculator {

    fun isDonationOfMember(donation: Donation, member: Member): Boolean {
        if (donation.isDeleted) return false
        if (member.id > 0L && donation.recordedById == member.id) return true
        if (member.name.isNotBlank() && donation.recordedByName.contains(member.name, ignoreCase = true)) return true
        if (member.email.isNotBlank() && donation.userEmail.isNotBlank() && donation.userEmail.equals(member.email, ignoreCase = true)) return true
        // If the member is Super Admin or President and record has default admin signature
        if (member.isSuperAdmin && (
            donation.recordedByName.contains("Super Admin", ignoreCase = true) ||
            donation.recordedByName.equals("Admin", ignoreCase = true) ||
            donation.recordedById == 1L
        )) return true
        return false
    }

    fun isExpenseOfMember(expense: Expense, member: Member): Boolean {
        if (expense.isDeleted) return false
        if (member.id > 0L && expense.recordedById == member.id) return true
        if (member.name.isNotBlank() && expense.recordedByName.contains(member.name, ignoreCase = true)) return true
        if (member.email.isNotBlank() && expense.userEmail.isNotBlank() && expense.userEmail.equals(member.email, ignoreCase = true)) return true
        // If the member is Super Admin or President and record has default admin signature
        if (member.isSuperAdmin && (
            expense.recordedByName.contains("Super Admin", ignoreCase = true) ||
            expense.recordedByName.equals("Admin", ignoreCase = true) ||
            expense.recordedById == 1L
        )) return true
        return false
    }

    fun calculateAccount(
        member: Member,
        allDonations: List<Donation>,
        allExpenses: List<Expense>
    ): CommitteeMemberAccount {
        val memberDonations = allDonations.filter { isDonationOfMember(it, member) }
        val memberExpenses = allExpenses.filter { isExpenseOfMember(it, member) }

        val received = memberDonations.filter { it.status.equals("received", ignoreCase = true) }
        val pending = memberDonations.filter { it.status.equals("pending", ignoreCase = true) }

        val totalReceived = received.sumOf { it.amount }
        val totalPending = pending.sumOf { it.amount }
        val totalExpenses = memberExpenses.sumOf { it.amount }
        val net = totalReceived - totalExpenses

        val cashRec = received.filter { it.paymentMode.contains("Cash", ignoreCase = true) }.sumOf { it.amount }
        val onlineRec = received.filter {
            it.paymentMode.contains("UPI", ignoreCase = true) ||
            it.paymentMode.contains("Online", ignoreCase = true)
        }.sumOf { it.amount }
        val chequeRec = received.filter {
            it.paymentMode.contains("Cheque", ignoreCase = true) ||
            it.paymentMode.contains("Bank", ignoreCase = true)
        }.sumOf { it.amount }

        val cashExp = memberExpenses.filter { it.paymentMode.contains("Cash", ignoreCase = true) }.sumOf { it.amount }
        val onlineExp = memberExpenses.filter { !it.paymentMode.contains("Cash", ignoreCase = true) }.sumOf { it.amount }
        val cashInHand = cashRec - cashExp

        return CommitteeMemberAccount(
            member = member,
            totalReceivedAmount = totalReceived,
            receivedCount = received.size,
            totalPendingAmount = totalPending,
            pendingCount = pending.size,
            totalExpensesAmount = totalExpenses,
            expensesCount = memberExpenses.size,
            netBalance = net,
            cashReceived = cashRec,
            onlineReceived = onlineRec,
            chequeReceived = chequeRec,
            cashExpenses = cashExp,
            onlineExpenses = onlineExp,
            cashInHand = cashInHand,
            donations = memberDonations,
            expenses = memberExpenses
        )
    }

    fun calculateAllAccounts(
        members: List<Member>,
        allDonations: List<Donation>,
        allExpenses: List<Expense>
    ): List<CommitteeMemberAccount> {
        return members.map { calculateAccount(it, allDonations, allExpenses) }
    }
}
