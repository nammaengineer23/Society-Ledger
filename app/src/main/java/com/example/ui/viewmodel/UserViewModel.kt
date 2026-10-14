package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthOpResult
import com.example.data.auth.CommitteeAccessProfile
import com.example.data.model.User
import com.example.data.repository.UserRepository
import com.example.data.repository.UserRepositoryContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State representation for User Authentication and Committee Access Control.
 */
data class UserUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val accessProfile: CommitteeAccessProfile? = null,
    val isAuthenticated: Boolean = false,
    val isSuperAdmin: Boolean = false,
    val isTreasurer: Boolean = false,
    val isSecretary: Boolean = false,
    val isPresident: Boolean = false,
    val isCommitteeMember: Boolean = false,
    val roleBadge: String = "PUBLIC RESIDENT",
    val permissions: List<String> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isRegistrationSuccess: Boolean = false
)

/**
 * ViewModel managing the Firebase Authentication flow, registration, login,
 * role authorization, and session state for society committee members.
 */
class UserViewModel(
    application: Application,
    private val userRepository: UserRepositoryContract
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        UserRepository.getInstance(application)
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _isRegistrationSuccess = MutableStateFlow(false)
    val isRegistrationSuccess: StateFlow<Boolean> = _isRegistrationSuccess.asStateFlow()

    val currentUser: StateFlow<User?> = userRepository.currentUser
    val accessProfile: StateFlow<CommitteeAccessProfile> = userRepository.accessProfile

    val isAuthenticated: StateFlow<Boolean> = combine(currentUser, accessProfile) { user, profile ->
        user != null || profile.isCommitteeMember
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSuperAdmin: StateFlow<Boolean> = accessProfile.combine(currentUser) { profile, user ->
        profile.isSuperAdmin || user?.isSuperAdmin == true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val committeeMembers: StateFlow<List<User>> = userRepository.getCommitteeMembersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<UserUiState> = combine(
        _isLoading,
        currentUser,
        accessProfile,
        _successMessage,
        _errorMessage
    ) { loading, user, profile, success, error ->
        UserUiState(
            isLoading = loading,
            currentUser = user,
            accessProfile = profile,
            isAuthenticated = user != null,
            isSuperAdmin = profile.isSuperAdmin || user?.isSuperAdmin == true,
            isTreasurer = profile.isTreasurer,
            isSecretary = profile.isSecretary,
            isPresident = profile.isPresident,
            isCommitteeMember = profile.isCommitteeMember,
            roleBadge = profile.badgeLabel,
            permissions = profile.permissions,
            successMessage = success,
            errorMessage = error,
            isRegistrationSuccess = _isRegistrationSuccess.value
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserUiState()
    )

    init {
        refreshUser()
    }

    /**
     * Signs in a committee member using Firebase Authentication.
     */
    fun login(
        email: String,
        password: String,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = userRepository.login(email, password)

            _isLoading.value = false
            when (result) {
                is AuthOpResult.Success -> {
                    _successMessage.value = result.message
                    onResult?.invoke(true, result.message)
                }
                is AuthOpResult.Error -> {
                    _errorMessage.value = result.message
                    onResult?.invoke(false, result.message)
                }
            }
        }
    }

    /**
     * Signs in or registers a member using verified Mobile Phone Number and OTP.
     */
    fun loginWithPhoneOtp(
        phone: String,
        name: String = "",
        role: String = "Public Resident",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = userRepository.loginWithPhoneOtp(phone, name, role)

            _isLoading.value = false
            when (result) {
                is AuthOpResult.Success -> {
                    _successMessage.value = result.message
                    onResult?.invoke(true, result.message)
                }
                is AuthOpResult.Error -> {
                    _errorMessage.value = result.message
                    onResult?.invoke(false, result.message)
                }
            }
        }
    }

    /**
     * Signs in with Google account identity credentials.
     */
    fun loginWithGoogle(
        email: String,
        displayName: String = "",
        role: String = "Public Resident",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null

            val result = userRepository.loginWithGoogle(email, displayName, role)

            _isLoading.value = false
            when (result) {
                is AuthOpResult.Success -> {
                    _successMessage.value = result.message
                    onResult?.invoke(true, result.message)
                }
                is AuthOpResult.Error -> {
                    _errorMessage.value = result.message
                    onResult?.invoke(false, result.message)
                }
            }
        }
    }

    /**
     * Convenience registration function for standard user registration with email and password.
     */
    fun register(
        email: String,
        password: String,
        name: String = "",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        val derivedName = if (name.isNotBlank()) {
            name.trim()
        } else {
            val prefix = email.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { part ->
                    part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            if (prefix.isNotBlank()) prefix else "Resident Member"
        }
        registerCommitteeMember(
            name = derivedName,
            email = email.trim(),
            phone = "",
            password = password,
            role = "Resident Member",
            pin = "",
            flatNo = "",
            societyName = "",
            onResult = onResult
        )
    }

    /**
     * Registers a new committee member account with Firebase Authentication,
     * seeds their profile in Room local storage and Cloud Firestore nested collections.
     */
    fun registerCommitteeMember(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        pin: String = "",
        flatNo: String = "A-101",
        societyName: String = "",
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            _isRegistrationSuccess.value = false

            val result = userRepository.registerCommitteeMember(
                name = name,
                email = email,
                phone = phone,
                password = password,
                role = role,
                pin = pin,
                flatNo = flatNo,
                societyName = societyName
            )

            _isLoading.value = false
            when (result) {
                is AuthOpResult.Success -> {
                    _isRegistrationSuccess.value = true
                    _successMessage.value = result.message
                    onResult?.invoke(true, result.message)
                }
                is AuthOpResult.Error -> {
                    _isRegistrationSuccess.value = false
                    _errorMessage.value = result.message
                    onResult?.invoke(false, result.message)
                }
            }
        }
    }

    /**
     * Sends a password reset email via Firebase Authentication.
     */
    fun sendPasswordReset(
        email: String,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = userRepository.sendPasswordResetEmail(email)

            _isLoading.value = false
            when (result) {
                is AuthOpResult.Success -> {
                    _successMessage.value = result.message
                    onResult?.invoke(true, result.message)
                }
                is AuthOpResult.Error -> {
                    _errorMessage.value = result.message
                    onResult?.invoke(false, result.message)
                }
            }
        }
    }

    /**
     * Signs out the current user and clears session tokens.
     */
    fun signOut(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.signOut()
            _isLoading.value = false
            _successMessage.value = "Signed out successfully."
            _errorMessage.value = null
            onComplete?.invoke()
        }
    }

    /**
     * Verifies the entered 4-digit security PIN against the active committee member.
     */
    suspend fun verifyPin(pin: String): Boolean {
        return userRepository.verifyPin(pin)
    }

    /**
     * Updates the on-device security PIN for the current committee member.
     */
    fun updatePin(
        newPin: String,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (newPin.trim().length != 4) {
                val msg = "PIN must be exactly 4 digits."
                _errorMessage.value = msg
                onResult?.invoke(false, msg)
                return@launch
            }
            val success = userRepository.updatePin(newPin)
            if (success) {
                val msg = "Security PIN updated successfully."
                _successMessage.value = msg
                onResult?.invoke(true, msg)
            } else {
                val msg = "Failed to update PIN. Ensure you are signed in."
                _errorMessage.value = msg
                onResult?.invoke(false, msg)
            }
        }
    }

    /**
     * Quick persona switcher for testing or offline demonstration.
     */
    fun quickPersonaLogin(role: String, onResult: ((User) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = userRepository.quickPersonaLogin(role)
            _isLoading.value = false
            _successMessage.value = "Logged in as ${user.name} (${user.role})"
            onResult?.invoke(user)
        }
    }

    /**
     * Clears error and success messages from the UI state.
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Refreshes user profile and permissions from local/cloud repository.
     */
    fun refreshUser() {
        viewModelScope.launch {
            userRepository.refreshUserProfile()
        }
    }
}
