package com.example.data.model

data class SocietyInfo(
    val name: String = "",
    val registrationNo: String = "",
    val upiId: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val branchName: String = "",
    val bankAccount: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pinCode: String = "",
    val contactEmail: String = "",
    val adminEmail: String = "",
    val contactPhone: String = "",
    val financialYear: String = "FY 2026-2027",
    val openingBalance: Double = 0.0,
    val presidentName: String = "",
    val secretaryName: String = "",
    val treasurerName: String = "",
    val securityGatePhone: String = "",
    val maintenanceLeadPhone: String = "",
    val plumberPhone: String = "",
    val emergencyHelplinePhone: String = ""
) {
    val fullBankDetails: String
        get() = if (bankName.isNotBlank() && accountNumber.isNotBlank()) {
            "$bankName A/C: $accountNumber (IFSC: $ifscCode${if (branchName.isNotBlank()) ", $branchName" else ""})"
        } else {
            bankAccount
        }
}

data class FinancialKPIs(
    val openingBalance: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netBalance: Double = 0.0,
    val receivedCount: Int = 0,
    val pendingCount: Int = 0,
    val expenseCount: Int = 0
)

data class CategoryExpenseSummary(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class PaymentModeSummary(
    val mode: String,
    val amount: Double,
    val count: Int
)

data class MonthlyFlowSummary(
    val monthName: String,
    val inflow: Double,
    val outflow: Double
)
