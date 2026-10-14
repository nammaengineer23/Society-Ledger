package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SocietyPreferences
import com.example.data.model.UserSubscription
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for managing and tracking user subscription status with Firebase Firestore.
 * Provides real-time synchronization and a toggle condition that conditionally hides
 * ad-view components exclusively for subscribed Pro members.
 */
class UserSubscriptionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferences = SocietyPreferences(application)
    private val firestoreService = FirestoreService()

    private val _subscription = MutableStateFlow(
        UserSubscription(
            userId = firestoreService.getCurrentFirebaseUser()?.first ?: "user_default",
            userEmail = firestoreService.getCurrentFirebaseUser()?.second ?: "nammaengineer23@gmail.com",
            societyId = preferences.getSocietyInfo().registrationNo,
            isPro = preferences.isProSubscriber(),
            planType = if (preferences.isProSubscriber()) "PRO_ANNUAL" else "FREE_TRIAL",
            status = if (preferences.isProSubscriber()) "ACTIVE" else "TRIAL",
            trialStartTime = preferences.getTrialStartTime(),
            expiryDate = preferences.getSubscriptionExpiryTime(),
            transactionId = preferences.getLastPaymentTransactionId()
        )
    )
    val subscription: StateFlow<UserSubscription> = _subscription.asStateFlow()

    // Single source of truth for ad visibility condition
    val isProSubscriber: StateFlow<Boolean> = _subscription.map { it.isSubscriptionActive }
        .stateIn(viewModelScope, SharingStarted.Eagerly, preferences.isProSubscriber())

    // Convenience alias matching the prompt terminology
    val isSubscribed: StateFlow<Boolean> = isProSubscriber

    // Helper to evaluate if ads should be hidden
    val shouldHideAds: StateFlow<Boolean> = _subscription.map { it.shouldHideAds }
        .stateIn(viewModelScope, SharingStarted.Eagerly, preferences.isProSubscriber())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _syncStatus = MutableStateFlow("Cloud Synced")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _razorpayKeyId = MutableStateFlow(preferences.getRazorpayKeyId())
    val razorpayKeyId: StateFlow<String> = _razorpayKeyId.asStateFlow()

    private var firestoreListenJob: Job? = null

    init {
        // Initial setup and Firestore sync
        val currentUserId = firestoreService.getCurrentFirebaseUser()?.first ?: "user_default"
        val currentSocietyId = preferences.getSocietyInfo().registrationNo
        startFirestoreSync(currentUserId, currentSocietyId)
    }

    /**
     * Listens to real-time subscription changes from Firestore collection "subscriptions".
     */
    fun startFirestoreSync(userId: String, societyId: String) {
        firestoreListenJob?.cancel()
        firestoreListenJob = viewModelScope.launch {
            try {
                firestoreService.getUserSubscriptionFlow(userId, societyId).collect { remoteSub ->
                    if (remoteSub != null) {
                        _subscription.value = remoteSub
                        // Keep local preferences in sync with Firestore
                        preferences.saveSubscriptionDetails(
                            isPro = remoteSub.isPro,
                            expiryTime = remoteSub.expiryDate,
                            transactionId = remoteSub.transactionId
                        )
                        _syncStatus.value = "Live Cloud Sync Active (${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())})"
                    } else {
                        // Document doesn't exist yet on Firestore, push local state to initialize
                        val localSub = _subscription.value.copy(
                            userId = userId,
                            societyId = societyId,
                            trialStartTime = preferences.getTrialStartTime()
                        )
                        firestoreService.saveUserSubscription(localSub)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Notice in subscription Firestore sync: ${e.message}")
                _syncStatus.value = "Local Mode (Cloud sync offline)"
            }
        }
    }

    /**
     * Toggles subscription status directly between Pro Subscribed and Free/Trial.
     * Updates Firestore cloud document and local preferences immediately to toggle ad visibility.
     */
    fun toggleSubscriptionStatus(isPro: Boolean, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val now = System.currentTimeMillis()
                val oneYearMs = 365L * 24 * 60 * 60 * 1000L
                val expiry = if (isPro) now + oneYearMs else 0L
                val txn = if (isPro) "toggle_pro_${now}" else ""

                val updatedSub = _subscription.value.copy(
                    isPro = isPro,
                    planType = if (isPro) "PRO_ANNUAL" else "FREE_TRIAL",
                    status = if (isPro) "ACTIVE" else "TRIAL",
                    startDate = if (isPro) now else 0L,
                    expiryDate = expiry,
                    transactionId = txn,
                    lastUpdated = now
                )

                // Update memory & local preferences
                _subscription.value = updatedSub
                preferences.saveSubscriptionDetails(
                    isPro = isPro,
                    expiryTime = expiry,
                    transactionId = txn
                )

                // Sync to Firestore
                val firestoreSaved = firestoreService.saveUserSubscription(updatedSub)
                if (firestoreSaved) {
                    _syncStatus.value = "Updated in Cloud at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
                }

                val msg = if (isPro) {
                    "Subscribed to Pro! Ads are now hidden across the app."
                } else {
                    "Switched to Free Tier. Ads and partner spotlights are visible."
                }
                onResult?.invoke(true, msg)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle subscription", e)
                onResult?.invoke(false, e.message ?: "Failed to update subscription.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Activates Pro subscription via payment checkout / Razorpay.
     */
    fun activateProSubscription(
        paymentTxnId: String,
        planDurationDays: Int = 365,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val now = System.currentTimeMillis()
                val currentExpiry = _subscription.value.expiryDate
                val baseTime = if (currentExpiry > now) currentExpiry else now
                val newExpiry = baseTime + (planDurationDays.toLong() * 24 * 60 * 60 * 1000L)
                val cleanTxn = paymentTxnId.ifBlank { "pay_rzp_${System.currentTimeMillis()}" }

                val proSub = _subscription.value.copy(
                    isPro = true,
                    planType = "PRO_ANNUAL",
                    status = "ACTIVE",
                    startDate = now,
                    expiryDate = newExpiry,
                    transactionId = cleanTxn,
                    amount = 499.0,
                    paymentGateway = "Razorpay",
                    lastUpdated = now
                )

                // Update memory and local persistence
                _subscription.value = proSub
                preferences.saveSubscriptionDetails(
                    isPro = true,
                    expiryTime = newExpiry,
                    transactionId = cleanTxn
                )

                // Push to Firestore
                val saved = firestoreService.saveUserSubscription(proSub)
                if (saved) {
                    _syncStatus.value = "Pro Active in Cloud"
                }

                val expiryFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(newExpiry))
                onResult(true, "Pro Subscription activated! Valid until $expiryFormatted. Ads are hidden.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to activate Pro subscription", e)
                onResult(false, e.message ?: "Activation failed.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Manually triggers a reload from Firestore.
     */
    fun refreshSubscription() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val sub = firestoreService.getUserSubscription(
                    userId = _subscription.value.userId,
                    societyId = _subscription.value.societyId
                )
                if (sub != null) {
                    _subscription.value = sub
                    preferences.saveSubscriptionDetails(sub.isPro, sub.expiryDate, sub.transactionId)
                    _syncStatus.value = "Refreshed from Cloud at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Refresh failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveRazorpayKey(keyId: String) {
        val clean = keyId.trim()
        preferences.saveRazorpayKeyId(clean)
        _razorpayKeyId.value = clean
    }

    companion object {
        private const val TAG = "UserSubscriptionVM"
    }
}
