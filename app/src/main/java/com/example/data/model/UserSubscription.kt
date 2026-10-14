package com.example.data.model

/**
 * Model representing a user or society subscription tracked in Firebase Firestore.
 */
data class UserSubscription(
    val userId: String = "",
    val userEmail: String = "",
    val societyId: String = "",
    val isPro: Boolean = false,
    val planType: String = "PRO_ANNUAL", // "FREE_TRIAL", "PRO_ANNUAL", "PRO_LIFETIME"
    val status: String = "ACTIVE", // "ACTIVE", "EXPIRED", "CANCELLED", "TRIAL"
    val trialStartTime: Long = 0L,
    val startDate: Long = 0L,
    val expiryDate: Long = 0L,
    val transactionId: String = "",
    val amount: Double = 499.0,
    val paymentGateway: String = "Razorpay",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Checks if the Pro subscription is active based on validity date and isPro flag.
     */
    val isSubscriptionActive: Boolean
        get() {
            val now = System.currentTimeMillis()
            return isPro && (expiryDate == 0L || expiryDate > now)
        }

    /**
     * Checks if the 90-day trial is active.
     */
    val isTrialActive: Boolean
        get() {
            if (trialStartTime == 0L) return false
            val trialDuration = 90L * 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            return now <= (trialStartTime + trialDuration)
        }

    /**
     * Ad visibility rule: Hides ads exclusively for active subscribed / Pro members.
     */
    val shouldHideAds: Boolean
        get() = isSubscriptionActive

    val daysRemaining: Long
        get() {
            val now = System.currentTimeMillis()
            return if (expiryDate > now) {
                ((expiryDate - now) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0L)
            } else if (isTrialActive) {
                val trialEndTime = trialStartTime + (90L * 24 * 60 * 60 * 1000L)
                ((trialEndTime - now) / (24 * 60 * 60 * 1000L)).coerceAtLeast(0L)
            } else 0L
        }
}
