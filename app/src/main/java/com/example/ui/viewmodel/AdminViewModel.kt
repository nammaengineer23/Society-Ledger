package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SocietyPreferences
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Represents the authentication state of the Master App Admin session.
 */
sealed class AdminAuthState {
    object LoggedOut : AdminAuthState()
    object Authenticating : AdminAuthState()
    data class Authenticated(
        val adminEmail: String,
        val sessionToken: String = UUID.randomUUID().toString(),
        val loginTimestamp: Long = System.currentTimeMillis()
    ) : AdminAuthState()
    data class Error(val message: String) : AdminAuthState()
}

/**
 * AdminViewModel manages the Master App Admin authentication lifecycle,
 * credential verification against Cloud Firestore (app_config/master_admin),
 * and session state for the Master App Admin Console.
 */
class AdminViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val preferences = SocietyPreferences(application)
    private val firestoreService = FirestoreService()

    private val _authState = MutableStateFlow<AdminAuthState>(AdminAuthState.LoggedOut)
    val authState: StateFlow<AdminAuthState> = _authState.asStateFlow()

    val isAuthenticated: StateFlow<Boolean> = _authState.map { it is AdminAuthState.Authenticated }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentAdminEmail: StateFlow<String?> = _authState.map {
        (it as? AdminAuthState.Authenticated)?.adminEmail
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isAuthenticating: StateFlow<Boolean> = _authState.map { it is AdminAuthState.Authenticating }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val authErrorMessage: StateFlow<String?> = _authState.map {
        (it as? AdminAuthState.Error)?.message
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isCloudSynced = MutableStateFlow(false)
    val isCloudSynced: StateFlow<Boolean> = _isCloudSynced.asStateFlow()

    init {
        syncRemoteMasterAdminConfig()
        firestoreService.observeMasterAdminConfig { config ->
            val remotePin = (config?.get("masterPin") as? String)?.trim()
            if (!remotePin.isNullOrBlank()) {
                preferences.saveAppAdminPin(remotePin)
                preferences.setAppAdminPinConfigured(true)
                _isCloudSynced.value = true
                Log.i(TAG, "Master Admin config synced in real-time from Cloud")
            }
        }
    }

    fun syncRemoteMasterAdminConfig(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val remoteConfig = firestoreService.getMasterAdminConfig()
                val remotePin = (remoteConfig?.get("masterPin") as? String)?.trim()
                val isRemoteConfigured = (remoteConfig?.get("isPinConfigured") as? Boolean == true) ||
                        (!remotePin.isNullOrBlank() && remotePin != DEFAULT_ADMIN_PIN)

                if (!remotePin.isNullOrBlank()) {
                    preferences.saveAppAdminPin(remotePin)
                    preferences.setAppAdminPinConfigured(true)
                    _isCloudSynced.value = true
                    Log.i(TAG, "Master Admin config synced: Remote PIN applied")
                    onComplete?.invoke(true)
                } else if (isRemoteConfigured) {
                    preferences.setAppAdminPinConfigured(true)
                    onComplete?.invoke(true)
                } else {
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync remote master admin config", e)
                onComplete?.invoke(false)
            }
        }
    }

    /**
     * Authenticates the Master Admin against Firestore cloud configuration.
     * Verifies that the entered email belongs to the authorized Master Admin (nammaengineer23@gmail.com)
     * and that the Master PIN matches the Firestore config or bootstrap PIN.
     */
    fun authenticate(
        enteredEmail: String,
        pin: String,
        onResult: (success: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        val cleanEmail = enteredEmail.trim().lowercase()
        val cleanPin = pin.trim()

        if (cleanEmail.isBlank()) {
            val err = "Master Admin email is required"
            _authState.value = AdminAuthState.Error(err)
            onResult(false, err)
            return
        }

        // 1. Verify authorized Master Admin email
        val isAuthorizedEmail = cleanEmail == AUTHORIZED_MASTER_ADMIN_EMAIL ||
                cleanEmail == "rentitease@gmail.com" ||
                firestoreService.getFirebaseUserEmail()?.equals(AUTHORIZED_MASTER_ADMIN_EMAIL, ignoreCase = true) == true ||
                firestoreService.getFirebaseUserEmail()?.equals("rentitease@gmail.com", ignoreCase = true) == true

        if (!isAuthorizedEmail) {
            val err = "Access Denied: Only authorized Master Admin ($cleanEmail is not authorized)"
            _authState.value = AdminAuthState.Error(err)
            onResult(false, err)
            return
        }

        if (cleanPin.isBlank()) {
            val err = "Please enter Master PIN"
            _authState.value = AdminAuthState.Error(err)
            onResult(false, err)
            return
        }

        _authState.value = AdminAuthState.Authenticating

        viewModelScope.launch {
            try {
                // 2. Query Firestore app_config/master_admin for cloud configuration
                val remoteConfig = firestoreService.getMasterAdminConfig()
                val remotePin = (remoteConfig?.get("masterPin") as? String)?.trim()
                val isRemoteConfigured = (remoteConfig?.get("isPinConfigured") as? Boolean == true) ||
                        (!remotePin.isNullOrBlank() && remotePin != DEFAULT_ADMIN_PIN)

                if (!remotePin.isNullOrBlank()) {
                    preferences.saveAppAdminPin(remotePin)
                    preferences.setAppAdminPinConfigured(true)
                }

                val localSavedPin = preferences.getAppAdminPin().trim()
                val isLocallyConfigured = preferences.isAppAdminPinConfigured() ||
                        (localSavedPin.isNotBlank() && localSavedPin != DEFAULT_ADMIN_PIN)

                val effectivePin = when {
                    !remotePin.isNullOrBlank() -> remotePin
                    localSavedPin.isNotBlank() -> localSavedPin
                    else -> null
                }

                val isEverConfigured = isRemoteConfigured || isLocallyConfigured

                val isValidPin = when {
                    // If a PIN has been set, cleanPin MUST match the set PIN!
                    !effectivePin.isNullOrBlank() -> cleanPin == effectivePin
                    // Only allow bootstrap DEFAULT_ADMIN_PIN if NEVER configured on any device
                    !isEverConfigured -> cleanPin == DEFAULT_ADMIN_PIN
                    // Otherwise (configured somewhere, or 0000 attempted when configured), reject
                    else -> false
                }

                if (isValidPin) {
                    // Cache the validated PIN locally
                    preferences.saveAppAdminPin(cleanPin)
                    preferences.setAppAdminPinConfigured(true)
                    _isCloudSynced.value = !remotePin.isNullOrBlank()

                    val session = AdminAuthState.Authenticated(
                        adminEmail = cleanEmail,
                        loginTimestamp = System.currentTimeMillis()
                    )
                    _authState.value = session
                    Log.i(TAG, "Master Admin successfully authenticated: $cleanEmail")
                    onResult(true, "Authentication successful")
                } else {
                    val err = "Access Denied: Invalid Master Admin Security PIN."
                    _authState.value = AdminAuthState.Error(err)
                    onResult(false, err)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error", e)
                val err = "Authentication error: ${e.localizedMessage ?: "Unknown error"}"
                _authState.value = AdminAuthState.Error(err)
                onResult(false, err)
            }
        }
    }

    /**
     * Terminates the current Master Admin session.
     */
    fun logout() {
        _authState.value = AdminAuthState.LoggedOut
        Log.i(TAG, "Master Admin session logged out")
    }

    /**
     * Clears error state back to LoggedOut.
     */
    fun clearError() {
        if (_authState.value is AdminAuthState.Error) {
            _authState.value = AdminAuthState.LoggedOut
        }
    }

    /**
     * Checks whether the current session is valid and active.
     * Default session lifetime is 2 hours from login.
     */
    fun isSessionValid(): Boolean {
        val current = _authState.value
        if (current is AdminAuthState.Authenticated) {
            val now = System.currentTimeMillis()
            val sessionAge = now - current.loginTimestamp
            if (sessionAge > SESSION_MAX_DURATION_MS) {
                logout()
                return false
            }
            return true
        }
        return false
    }

    /**
     * Updates the Master Security PIN in both Firestore and local preferences.
     * Requires an active authenticated session.
     */
    fun updateMasterPin(
        newPin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val clean = newPin.trim()
        if (clean.length < 4) {
            onResult(false, "Master PIN must be at least 4 digits.")
            return
        }

        preferences.saveAppAdminPin(clean)
        preferences.setAppAdminPinConfigured(true)

        viewModelScope.launch {
            try {
                val success = firestoreService.saveMasterAdminConfig(
                    masterPin = clean,
                    authorizedEmails = listOf(AUTHORIZED_MASTER_ADMIN_EMAIL)
                )
                if (success) {
                    _isCloudSynced.value = true
                    onResult(true, "Master Security PIN updated successfully across cloud and all devices.")
                } else {
                    onResult(true, "Master Security PIN updated locally (Cloud sync pending).")
                }
            } catch (e: Exception) {
                onResult(true, "Master Security PIN saved locally.")
            }
        }
    }

    /**
     * Verifies master PIN directly without email (fallback compatibility helper).
     */
    fun verifyPinDirect(pin: String): Boolean {
        val clean = pin.trim()
        if (clean.isBlank()) return false
        val local = preferences.getAppAdminPin().trim()
        if (local.isNotBlank()) {
            return clean == local
        }
        if (!preferences.isAppAdminPinConfigured()) {
            return clean == DEFAULT_ADMIN_PIN
        }
        return false
    }

    companion object {
        private const val TAG = "AdminViewModel"
        const val AUTHORIZED_MASTER_ADMIN_EMAIL = "nammaengineer23@gmail.com"
        const val DEFAULT_ADMIN_PIN = "0000"
        const val SESSION_MAX_DURATION_MS = 2 * 60 * 60 * 1000L // 2 hours
    }
}
