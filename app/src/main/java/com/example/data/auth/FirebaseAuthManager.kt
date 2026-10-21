package com.example.data.auth

import android.util.Log
import com.example.data.model.Member
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Committee Access Control Profile & Permissions Summary.
 */
data class CommitteeAccessProfile(
    val member: Member?,
    val roleTitle: String,
    val isSuperAdmin: Boolean,
    val isTreasurer: Boolean,
    val isSecretary: Boolean,
    val isPresident: Boolean,
    val isCommitteeMember: Boolean,
    val isPublicResident: Boolean,
    val permissions: List<String>,
    val badgeLabel: String
)

/**
 * Result state wrapper for Firebase Authentication operations.
 */
sealed class AuthOpResult<out T> {
    data class Success<out T>(val data: T, val message: String = "") : AuthOpResult<T>()
    data class Error(val message: String, val rawCode: String = "", val isRecoverable: Boolean = true) : AuthOpResult<Nothing>()
}

/**
 * Centralized Firebase Authentication Manager for Committee Access Control in Society Ledger.
 *
 * Handles:
 * - Email & Password Sign-In for authorized committee office-bearers.
 * - New Committee Member registration / profile linking.
 * - Password reset verification emails.
 * - Committee role resolution & access tier mapping.
 * - Graceful fallback / offline verification when cloud connectivity is unavailable.
 */
class FirebaseAuthManager private constructor() {

    companion object {
        private const val TAG = "FirebaseAuthManager"

        @Volatile
        private var INSTANCE: FirebaseAuthManager? = null

        fun getInstance(): FirebaseAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAuthManager().also { INSTANCE = it }
            }
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Notice: FirebaseAuth instance unavailable: ${e.message}")
            null
        }
    }

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                _currentUserState.value = firebaseAuth.currentUser
            }
            _currentUserState.value = auth?.currentUser
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Could not attach AuthStateListener: ${e.message}")
        }
    }

    val isInitialized: Boolean
        get() = auth != null

    val currentUser: FirebaseUser?
        get() = auth?.currentUser ?: _currentUserState.value

    val currentEmail: String?
        get() = currentUser?.email

    val isUserSignedIn: Boolean
        get() = currentUser != null

    /**
     * Exchanges a Google ID token for a Firebase session. Never trust a supplied
     * email/name alone: Firebase validates the token with Google before creating a session.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): AuthOpResult<FirebaseUser> {
        if (idToken.isBlank()) return AuthOpResult.Error("Google sign-in did not return an ID token.")
        val authInstance = auth
            ?: return AuthOpResult.Error("Firebase Authentication is unavailable.", rawCode = "FIREBASE_UNAVAILABLE")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return suspendCancellableCoroutine { continuation ->
            authInstance.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        _currentUserState.value = user
                        continuation.resume(AuthOpResult.Success(user, "Google ID token verified by Firebase."))
                    } else {
                        continuation.resume(AuthOpResult.Error("Firebase sign-in returned no user."))
                    }
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Google ID-token Firebase sign-in failed: ${exception?.message}", exception)
                    continuation.resume(AuthOpResult.Error(formatAuthException(exception), rawCode = "GOOGLE_TOKEN_REJECTED"))
                }
            }
        }
    }

    /**
     * Signs in with email and password via Firebase Authentication.
     */
    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): AuthOpResult<FirebaseUser> {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank()) {
            return AuthOpResult.Error("Please enter your registered committee email address.")
        }
        if (trimmedPassword.isBlank()) {
            return AuthOpResult.Error("Please enter your committee account password.")
        }

        val authInstance = auth
        if (authInstance == null) {
            return AuthOpResult.Error(
                "Firebase Auth is initializing in offline/local mode. You may use Demo Committee Sign-In.",
                rawCode = "FIREBASE_UNAVAILABLE"
            )
        }

        return suspendCancellableCoroutine { continuation ->
            authInstance.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            _currentUserState.value = user
                            continuation.resume(AuthOpResult.Success(user, "Signed in successfully"))
                        } else {
                            continuation.resume(AuthOpResult.Error("Authentication succeeded but user profile was empty."))
                        }
                    } else {
                        val exception = task.exception
                        val userFriendlyMessage = formatAuthException(exception)
                        Log.e(TAG, "Firebase signIn failed: ${exception?.message}", exception)
                        continuation.resume(AuthOpResult.Error(userFriendlyMessage))
                    }
                }
        }
    }

    /**
     * Registers a new committee account using Firebase Authentication.
     */
    suspend fun registerCommitteeUser(
        email: String,
        password: String,
        displayName: String
    ): AuthOpResult<FirebaseUser> {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedName = displayName.trim()

        if (trimmedName.isBlank()) {
            return AuthOpResult.Error("Please enter full committee member name.")
        }
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return AuthOpResult.Error("Please enter a valid official committee email address.")
        }
        if (trimmedPassword.length < 6) {
            return AuthOpResult.Error("Password must be at least 6 characters long for committee security.")
        }

        val authInstance = auth
        if (authInstance == null) {
            return AuthOpResult.Error(
                "Firebase Auth service unavailable. Operating in local mode.",
                rawCode = "FIREBASE_UNAVAILABLE"
            )
        }

        return suspendCancellableCoroutine { continuation ->
            authInstance.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            try {
                                val profileUpdate = UserProfileChangeRequest.Builder()
                                    .setDisplayName(trimmedName)
                                    .build()
                                user.updateProfile(profileUpdate)
                            } catch (e: Exception) {
                                Log.w(TAG, "Notice: Failed to set display name: ${e.message}")
                            }
                            _currentUserState.value = user
                            continuation.resume(AuthOpResult.Success(user, "Committee account registered successfully!"))
                        } else {
                            continuation.resume(AuthOpResult.Error("Account created but user profile was not returned."))
                        }
                    } else {
                        val exception = task.exception
                        val userFriendlyMessage = formatAuthException(exception)
                        Log.e(TAG, "Firebase registration failed: ${exception?.message}", exception)
                        continuation.resume(AuthOpResult.Error(userFriendlyMessage))
                    }
                }
        }
    }

    /**
     * Sends a Password Reset email via Firebase Auth.
     */
    suspend fun sendPasswordResetEmail(email: String): AuthOpResult<Unit> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank()) {
            return AuthOpResult.Error("Please enter the committee email to receive reset instructions.")
        }

        val authInstance = auth
        if (authInstance == null) {
            return AuthOpResult.Error("Firebase Auth service unavailable.")
        }

        return suspendCancellableCoroutine { continuation ->
            authInstance.sendPasswordResetEmail(trimmedEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(AuthOpResult.Success(Unit, "Password reset link sent to $trimmedEmail"))
                    } else {
                        val exception = task.exception
                        val msg = formatAuthException(exception)
                        continuation.resume(AuthOpResult.Error(msg))
                    }
                }
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "Error during signOut: ${e.message}")
        }
        _currentUserState.value = null
    }

    /**
     * Resolves committee permissions and access tier by matching email against roster.
     */
    fun resolveAccessProfile(
        email: String?,
        members: List<Member>
    ): CommitteeAccessProfile {
        if (email.isNullOrBlank()) {
            return CommitteeAccessProfile(
                member = null,
                roleTitle = "Public Resident (Read-Only)",
                isSuperAdmin = false,
                isTreasurer = false,
                isSecretary = false,
                isPresident = false,
                isCommitteeMember = false,
                isPublicResident = true,
                permissions = listOf(
                    "View Approved Financial Ledger",
                    "Download Public Contribution Receipts",
                    "Inspect Statutory Audit Summaries"
                ),
                badgeLabel = "PUBLIC TRANSPARENCY"
            )
        }

        // Match against committee roster by email
        // Authorization is based on an exact, verified email-to-roster match only.
        // Name-prefix matching can accidentally grant committee privileges to unrelated accounts.
        val matchedMember = members.find { it.email.isNotBlank() && it.email.equals(email, ignoreCase = true) }

        if (matchedMember == null) {
            return CommitteeAccessProfile(
                member = null,
                roleTitle = "Resident / Flat Owner",
                isSuperAdmin = false,
                isTreasurer = false,
                isSecretary = false,
                isPresident = false,
                isCommitteeMember = false,
                isPublicResident = true,
                permissions = listOf(
                    "View Approved Society Ledger",
                    "Track Flat Contributions & Receipts",
                    "Submit Inquiries & Complaints"
                ),
                badgeLabel = "RESIDENT ACCOUNT"
            )
        }

        val role = matchedMember.role.lowercase()
        val isSuperAdmin = matchedMember.isSuperAdmin || role.contains("super admin")
        val isTreasurer = role.contains("treasurer")
        val isSecretary = role.contains("secretary")
        val isPresident = role.contains("president")

        val permissions = buildList {
            if (isSuperAdmin) {
                add("Full Governance & Administrative Override")
                add("Delete / Void Erroneous Transactions (Mandatory Audit Trail)")
                add("Committee Member Onboarding & PIN Allotment")
                add("Financial Year Closing & Balances Rollover")
                add("Edit Society Registration, PAN & Bye-law Config")
            } else if (isTreasurer) {
                add("Record & Verify Society Donations / Collections")
                add("Issue Digital Receipts with WhatsApp Reminders")
                add("Approve & Disburse Society Vendor Expenses")
                add("Manage Budget Heads & Variance Thresholds")
                add("Export Audited Ledgers & Bank Summaries")
            } else if (isSecretary) {
                add("Record Society Collections & Donor Records")
                add("Generate Contribution Receipts & Certificates")
                add("Access Resident Directory & WhatsApp Reminders")
                add("Review Statutory Audit Trail & Meeting Minutes")
            } else {
                add("Record Society Receipts & Expenses")
                add("Review Live Accounts & Vouchers")
                add("Access Transparency Reports")
            }
        }

        val badgeLabel = when {
            isSuperAdmin -> "SUPER ADMIN ACCESS"
            isTreasurer -> "TREASURER ACCESS"
            isSecretary -> "SECRETARY ACCESS"
            isPresident -> "PRESIDENT ACCESS"
            else -> "EXECUTIVE COMMITTEE"
        }

        return CommitteeAccessProfile(
            member = matchedMember,
            roleTitle = matchedMember.role,
            isSuperAdmin = isSuperAdmin,
            isTreasurer = isTreasurer,
            isSecretary = isSecretary,
            isPresident = isPresident,
            isCommitteeMember = true,
            isPublicResident = false,
            permissions = permissions,
            badgeLabel = badgeLabel
        )
    }

    /**
     * Converts Firebase Auth exceptions into clear, actionable messages.
     */
    private fun formatAuthException(e: Throwable?): String {
        if (e == null) return "Authentication failed. Please check your credentials."

        return when (e) {
            is FirebaseAuthInvalidUserException -> {
                "No registered committee account found for this email. If you are an authorized office bearer, please register or contact your Admin."
            }
            is FirebaseAuthInvalidCredentialsException -> {
                "Incorrect password or invalid email format. Please verify your credentials."
            }
            is FirebaseAuthUserCollisionException -> {
                "An account with this email address already exists. Please select 'Sign In' instead."
            }
            is FirebaseAuthWeakPasswordException -> {
                "The chosen password is too weak. Please use at least 6 characters including numbers or symbols."
            }
            else -> {
                val msg = e.message ?: ""
                when {
                    msg.contains("network", ignoreCase = true) ->
                        "Network connection error. Please verify your internet connection or use Demo PIN login."
                    msg.contains("too-many-requests", ignoreCase = true) ->
                        "Too many unsuccessful attempts. Access is temporarily restricted. Please try again in a few moments."
                    msg.contains("user-disabled", ignoreCase = true) ->
                        "This committee account has been deactivated. Please contact the Society President or Admin."
                    else -> msg.ifBlank { "Authentication failed. Please verify your credentials." }
                }
            }
        }
    }
}
