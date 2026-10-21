package com.example.data.model

/**
 * Accounting rules for a committee member's advance/expense account.
 *
 * Positive net balance means the member must return funds to the trust.
 * Negative net balance means the trust owes reimbursement to the member.
 * Only COMPLETED ledger entries affect the balance; pending entries remain visible
 * in the ledger but are not treated as settled cash movement.
 */
data class CommitteeMemberAccountBalance(
    val memberId: Long,
    val netAmountDueToTrust: Double
) {
    val amountMemberMustReturn: Double
        get() = netAmountDueToTrust.coerceAtLeast(0.0)

    val amountTrustMustReimburse: Double
        get() = (-netAmountDueToTrust).coerceAtLeast(0.0)

    val isSettled: Boolean
        get() = kotlin.math.abs(netAmountDueToTrust) < 0.005

    val statusLabel: String
        get() = when {
            isSettled -> "SETTLED"
            netAmountDueToTrust > 0 -> "MEMBER_OWES_TRUST"
            else -> "TRUST_OWES_MEMBER"
        }

    companion object {
        const val OPENING_BALANCE = "MEMBER_OPENING_BALANCE"
        const val MEMBER_EXPENSE = "MEMBER_EXPENSE"
        const val SETTLEMENT_TO_MEMBER = "SETTLEMENT_TO_MEMBER"
        const val SETTLEMENT_FROM_MEMBER = "SETTLEMENT_FROM_MEMBER"

        /**
         * A positive ledger movement is an advance paid by the trust to the member.
         * Expenses and member returns reduce that advance; trust reimbursements increase it.
         */
        fun calculate(memberId: Long, transactions: List<Transaction>): CommitteeMemberAccountBalance {
            require(memberId > 0L) { "A valid committee member ID is required" }
            var balance = 0.0
            transactions.asSequence()
                .filter { it.committeeMemberId == memberId && !it.isDeleted }
                .filter { it.status.equals("COMPLETED", ignoreCase = true) }
                .forEach { entry ->
                    require(entry.amount.isFinite() && entry.amount >= 0.0) {
                        "Ledger transaction amount must be a finite, non-negative value"
                    }
                    when (entry.transactionType.uppercase()) {
                        OPENING_BALANCE, SETTLEMENT_TO_MEMBER -> balance += entry.amount
                        MEMBER_EXPENSE, SETTLEMENT_FROM_MEMBER -> balance -= entry.amount
                    }
                }
            return CommitteeMemberAccountBalance(memberId, balance)
        }
    }
}
