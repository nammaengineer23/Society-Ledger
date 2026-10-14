package com.example.data.local

import com.example.data.model.AuditLog
import com.example.data.model.CommitteeMember
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.Pledge
import com.example.data.model.Transaction
import com.example.data.model.AppComplaint

object PreloadedData {
    val COMMITTEE_MEMBERS: List<Member> = emptyList()
    val INITIAL_DONATIONS: List<Donation> = emptyList()
    val INITIAL_EXPENSES: List<Expense> = emptyList()
    val INITIAL_TRANSACTIONS: List<Transaction> = emptyList()
    val INITIAL_PLEDGES: List<Pledge> = emptyList()
    val INITIAL_COMMITTEE_MEMBERS: List<CommitteeMember> = emptyList()
    val INITIAL_AUDIT_LOGS: List<AuditLog> = emptyList()
    val INITIAL_COMPLAINTS: List<AppComplaint> = emptyList()
}
