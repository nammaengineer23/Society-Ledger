package com.example.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommitteeMemberAccountBalanceTest {
    private fun entry(type: String, amount: Double, memberId: Long = 7L, status: String = "COMPLETED") =
        Transaction(
            transactionType = type,
            committeeMemberId = memberId,
            title = type,
            category = "Committee Member Account",
            amount = amount,
            partyName = "Test Member",
            status = status
        )

    @Test
    fun openingAdvanceAndExpensesCalculateAmountMemberMustReturn() {
        val balance = CommitteeMemberAccountBalance.calculate(
            7L,
            listOf(
                entry(CommitteeMemberAccountBalance.OPENING_BALANCE, 10_000.0),
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 3_000.0)
            )
        )

        assertEquals(7_000.0, balance.amountMemberMustReturn, 0.001)
        assertEquals(0.0, balance.amountTrustMustReimburse, 0.001)
        assertEquals("MEMBER_OWES_TRUST", balance.statusLabel)
    }

    @Test
    fun expensesBeyondAdvanceMeanTrustOwesMember() {
        val balance = CommitteeMemberAccountBalance.calculate(
            7L,
            listOf(
                entry(CommitteeMemberAccountBalance.OPENING_BALANCE, 10_000.0),
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 12_000.0)
            )
        )

        assertEquals(0.0, balance.amountMemberMustReturn, 0.001)
        assertEquals(2_000.0, balance.amountTrustMustReimburse, 0.001)
        assertEquals("TRUST_OWES_MEMBER", balance.statusLabel)
    }

    @Test
    fun eitherSettlementDirectionReducesOutstandingBalance() {
        val memberReturns = CommitteeMemberAccountBalance.calculate(
            7L,
            listOf(
                entry(CommitteeMemberAccountBalance.OPENING_BALANCE, 10_000.0),
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 3_000.0),
                entry(CommitteeMemberAccountBalance.SETTLEMENT_FROM_MEMBER, 7_000.0)
            )
        )
        val trustReimburses = CommitteeMemberAccountBalance.calculate(
            7L,
            listOf(
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 2_000.0),
                entry(CommitteeMemberAccountBalance.SETTLEMENT_TO_MEMBER, 2_000.0)
            )
        )

        assertTrue(memberReturns.isSettled)
        assertTrue(trustReimburses.isSettled)
        assertEquals("SETTLED", memberReturns.statusLabel)
        assertEquals("SETTLED", trustReimburses.statusLabel)
    }

    @Test
    fun pendingDeletedAndOtherMembersTransactionsDoNotChangeBalance() {
        val balance = CommitteeMemberAccountBalance.calculate(
            7L,
            listOf(
                entry(CommitteeMemberAccountBalance.OPENING_BALANCE, 1_000.0),
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 200.0, status = "PENDING"),
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 300.0, memberId = 8L),
                entry(CommitteeMemberAccountBalance.MEMBER_EXPENSE, 400.0).copy(isDeleted = true)
            )
        )

        assertEquals(1_000.0, balance.netAmountDueToTrust, 0.001)
        assertFalse(balance.isSettled)
    }
}
