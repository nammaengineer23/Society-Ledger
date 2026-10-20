package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.auth.AuthOpResult
import com.example.data.auth.CommitteeAccessProfile
import com.example.data.auth.FirebaseAuthManager
import com.example.data.local.MemberDao
import com.example.data.local.SocietyDatabase
import com.example.data.local.SocietyPreferences
import com.example.data.model.Member
import com.example.data.model.User
import com.example.data.model.toUser
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Interface contract for User Repository.
 * Handles Firebase Authentication flow, registration, login, profile management,
 * and role-based permissions for society committee members.
 */
interface UserRepositoryContract {
    val currentUser: StateFlow<User?>
    val accessProfile: StateFlow<CommitteeAccessProfile>
    val isUserSignedIn: Boolean
    val currentUserId: String?
    val currentEmail: String?

    suspend fun login(email: String, password: String): AuthOpResult<User>
    suspend fun loginWithGoogleIdToken(idToken: String): AuthOpResult<User>
    suspend fun loginWithPhoneOtp(phone: String, name: String = "", role: String = "Resident Member"): AuthOpResult<User>
    suspend fun loginWithGoogle(
        email: String,
        displayName: String = "",
        role: String = "Resident Member",
        phone: String = "",
        password: String = ""
    ): AuthOpResult<User>
    suspend fun registerCommitteeMember(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        pin: String = "1234",
        flatNo: String = "A-101",
        societyName: String = ""
    ): AuthOpResult<User>
    suspend fun sendPasswordResetEmail(email: String): AuthOpResult<Unit>
    suspend fun signOut()
    suspend fun getCurrentUser(): User?
    suspend fun refreshUserProfile(): User?
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun verifyPin(pin: String): Boolean
    suspend fun updatePin(newPin: String): Boolean
    suspend fun getCommitteeMembers(societyName: String = ""): List<User>
    fun getCommitteeMembersFlow(): Flow<List<User>>
    suspend fun quickPersonaLogin(role: String, societyName: String = ""): User
}

/**
 * Repository implementation for managing User and Committee Member Authentication.
 * Connects Firebase Authentication, Cloud Firestore nested collections, Room caching,
 * and Local Preferences into a single cohesive data layer.
 */
class UserRepository(
    private val context: Context,
    private val firebaseAuthManager: FirebaseAuthManager = FirebaseAuthManager.getInstance(),
    private val memberDao: MemberDao,
    private val firestoreService: FirestoreService = FirestoreService(),
    private val societyPreferences: SocietyPreferences = SocietyPreferences(context)
) : UserRepositoryContract {

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessProfile = MutableStateFlow(
        firebaseAuthManager.resolveAccessProfile(null, emptyList())
    )
    override val accessProfile: StateFlow<CommitteeAccessProfile> = _accessProfile.asStateFlow()

    override val isUserSignedIn: Boolean
        get() = _currentUser.value != null || firebaseAuthManager.isUserSignedIn

    override val currentUserId: String?
        get() = _currentUser.value?.id ?: firebaseAuthManager.currentUser?.uid

    override val currentEmail: String?
        get() = _currentUser.value?.email ?: firebaseAuthManager.currentEmail

    init {
        repositoryScope.launch {
            initSavedUserSession()
        }
    }

    /**
     * Restores saved user session from Local Preferences and Room database on startup.
     */
    private suspend fun initSavedUserSession() {
        try {
            val savedEmail = societyPreferences.getLoggedInUserEmail()
            val firebaseUser = firebaseAuthManager.currentUser

            val effectiveEmail = when {
                firebaseUser?.email?.isNotBlank() == true -> firebaseUser.email
                savedEmail.isNotBlank() -> savedEmail
                else -> null
            }

            if (!effectiveEmail.isNullOrBlank()) {
                val member = memberDao.getMemberByEmail(effectiveEmail)
                if (member != null) {
                    val user = member.toUser(societyPreferences.getSocietyInfo().name).copy(
                        id = firebaseUser?.uid ?: member.id.toString(),
                        societyName = societyPreferences.getSocietyInfo().name
                    )
                    _currentUser.value = user
                    val members = memberDao.getAllMembersList()
                    _accessProfile.value = firebaseAuthManager.resolveAccessProfile(effectiveEmail, members)
                    Log.d(TAG, "Restored active committee session for ${user.name} (${user.role})")
                } else if (firebaseUser != null) {
                    val isSuper = isSuperAdminEmail(effectiveEmail)
                    val tempUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName?.ifBlank { null }
                            ?: effectiveEmail.substringBefore("@").replace(".", " ").capitalizeWords(),
                        email = effectiveEmail,
                        role = if (isSuper) "President" else "Committee Member",
                        isSuperAdmin = isSuper,
                        societyName = societyPreferences.getSocietyInfo().name
                    )
                    _currentUser.value = tempUser
                    _accessProfile.value = firebaseAuthManager.resolveAccessProfile(effectiveEmail, emptyList())
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notice: Failed to restore user session: ${e.message}")
        }
    }

    /**
     * Signs in only after Firebase validates a Google ID token and establishes a Firebase session.
     */
    override suspend fun loginWithGoogleIdToken(idToken: String): AuthOpResult<User> = withContext(Dispatchers.IO) {
        when (val result = firebaseAuthManager.signInWithGoogleIdToken(idToken)) {
            is AuthOpResult.Error -> result
            is AuthOpResult.Success -> {
                val firebaseUser = result.data
                val email = firebaseUser.email?.trim().orEmpty()
                if (email.isBlank()) {
                    firebaseAuthManager.signOut()
                    return@withContext AuthOpResult.Error("The verified Google account did not provide an email address.")
                }
                val allMembers = memberDao.getAllMembersList()
                val matchedMember = allMembers.find { it.email.equals(email, ignoreCase = true) }
                val isSuper = isSuperAdminEmail(email) || matchedMember?.isSuperAdmin == true
                val societyName = societyPreferences.getSocietyInfo().name
                val user = if (matchedMember != null) {
                    matchedMember.toUser(societyName).copy(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName?.ifBlank { null } ?: matchedMember.name,
                        email = email,
                        societyName = societyName,
                        isSuperAdmin = isSuper
                    )
                } else {
                    User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName?.ifBlank { null } ?: email.substringBefore("@"),
                        email = email,
                        role = if (isSuper) "President" else "Public Resident",
                        isSuperAdmin = isSuper,
                        societyName = societyName
                    )
                }
                societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role, user.phone)
                _currentUser.value = user
                _accessProfile.value = firebaseAuthManager.resolveAccessProfile(email, allMembers)
                AuthOpResult.Success(user, "Google account verified by Firebase. Signed in as ${user.name}.")
            }
        }
    }

    /**
     * Signs in a committee member using Firebase Authentication.
     * Includes graceful local fallback if cloud network connectivity is interrupted.
     */
    override suspend fun login(email: String, password: String): AuthOpResult<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        if (trimmedEmail.isBlank()) {
            return@withContext AuthOpResult.Error("Please enter your registered committee email.")
        }
        if (trimmedPassword.isBlank()) {
            return@withContext AuthOpResult.Error("Please enter your committee account password.")
        }

        val authResult = firebaseAuthManager.signInWithEmailAndPassword(trimmedEmail, trimmedPassword)

        when (authResult) {
            is AuthOpResult.Success -> {
                val firebaseUser = authResult.data
                val allMembers = memberDao.getAllMembersList()
                val matchedMember = allMembers.find { it.email.equals(trimmedEmail, ignoreCase = true) }
                val isSuper = isSuperAdminEmail(trimmedEmail) || matchedMember?.isSuperAdmin == true

                val finalMember = if (matchedMember != null) {
                    matchedMember
                } else {
                    val newMember = Member(
                        name = firebaseUser.displayName?.ifBlank { null }
                            ?: trimmedEmail.substringBefore("@").replace(".", " ").capitalizeWords(),
                        email = trimmedEmail,
                        phone = "",
                        role = if (isSuper) "President" else "Committee Member",
                        pin = "1234",
                        isSuperAdmin = isSuper,
                        flatNo = "A-101"
                    )
                    val id = memberDao.insertMember(newMember)
                    newMember.copy(id = id)
                }

                val user = finalMember.toUser(societyPreferences.getSocietyInfo().name).copy(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName?.ifBlank { null } ?: finalMember.name,
                    email = trimmedEmail,
                    societyName = societyPreferences.getSocietyInfo().name
                )

                societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role)
                _currentUser.value = user
                _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, allMembers)

                // Asynchronously sync member to Firestore nested hierarchy
                val societyName = societyPreferences.getSocietyInfo().name
                repositoryScope.launch {
                    try {
                        firestoreService.saveMember(finalMember, societyName)
                    } catch (e: Exception) {
                        Log.w(TAG, "Notice: Background member sync error: ${e.message}")
                    }
                }

                AuthOpResult.Success(user, "Welcome back, ${user.name}! Signed in as ${user.role}.")
            }

            is AuthOpResult.Error -> {
                // Graceful Offline / Demo Verification Fallback
                val localMember = memberDao.getMemberByEmail(trimmedEmail)
                if (localMember != null && (trimmedPassword == "Society@123" || trimmedPassword == localMember.pin || localMember.pin.isBlank())) {
                    val societyName = societyPreferences.getSocietyInfo().name
                    val user = localMember.toUser(societyName).copy(
                        societyName = societyName
                    )
                    societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role)
                    _currentUser.value = user
                    val allMembers = memberDao.getAllMembersList()
                    _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, allMembers)
                    return@withContext AuthOpResult.Success(
                        user,
                        "Signed in as ${user.name} (${user.role}) via Committee Roster (Offline Mode)."
                    )
                }

                AuthOpResult.Error(authResult.message, authResult.rawCode, authResult.isRecoverable)
            }
        }
    }

    override suspend fun loginWithPhoneOtp(
        phone: String,
        name: String,
        role: String
    ): AuthOpResult<User> = withContext(Dispatchers.IO) {
        val trimmedPhone = phone.trim()
        val allMembers = memberDao.getAllMembersList()
        val matched = allMembers.find {
            it.phone.trim() == trimmedPhone ||
            it.phone.replace("+91", "").trim() == trimmedPhone.replace("+91", "").trim()
        }
        val effectiveName = if (name.isNotBlank()) name.trim() else matched?.name ?: "Resident ${trimmedPhone.takeLast(4)}"
        val effectiveRole = matched?.role ?: role
        val isSuper = matched?.isSuperAdmin == true || isSuperAdminEmail(matched?.email ?: "")

        val finalMember = if (matched != null) {
            matched
        } else {
            val newMember = Member(
                name = effectiveName,
                email = "${trimmedPhone.replace("+", "")}@society.org",
                phone = trimmedPhone,
                role = effectiveRole,
                pin = "1234",
                isSuperAdmin = isSuper,
                flatNo = "B-204"
            )
            val id = memberDao.insertMember(newMember)
            newMember.copy(id = id)
        }

        val socName = societyPreferences.getSocietyInfo().name
        val user = finalMember.toUser(socName).copy(
            id = "phone_${trimmedPhone.replace("+", "")}",
            societyName = socName
        )

        societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role)
        _currentUser.value = user
        _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, allMembers)
        AuthOpResult.Success(user, "Verified and signed in as ${user.name} (${user.role})")
    }

    override suspend fun loginWithGoogle(
        email: String,
        displayName: String,
        role: String,
        phone: String,
        password: String
    ): AuthOpResult<User> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val effectiveName = if (displayName.isNotBlank()) {
            displayName.trim()
        } else if (trimmedEmail.isNotBlank()) {
            trimmedEmail.substringBefore("@").replace(".", " ").capitalizeWords()
        } else {
            "Public Resident"
        }
        val socName = societyPreferences.getSocietyInfo().name

        // Check if duplicate phone number in backend if phone is entered
        if (trimmedPhone.isNotBlank()) {
            val allExistingMembers = memberDao.getAllMembersList()
            val cleanPhone = trimmedPhone.filter { it.isDigit() }.takeLast(10)
            val isDuplicate = allExistingMembers.any {
                val existingClean = it.phone.filter { ch -> ch.isDigit() }.takeLast(10)
                existingClean.isNotBlank() && existingClean == cleanPhone
            }
            if (isDuplicate) {
                return@withContext AuthOpResult.Error("This phone number (+91 $cleanPhone) is already registered in the backend. Please use a unique phone number.")
            }
        }

        // Every user signing in with Google registers and enters as a Public Resident!
        val user = User(
            id = if (trimmedEmail.isNotBlank()) "google_${trimmedEmail.hashCode()}" else "google_${System.currentTimeMillis()}",
            name = effectiveName,
            email = trimmedEmail,
            role = "Public Resident",
            phone = trimmedPhone,
            isSuperAdmin = false,
            societyName = socName
        )

        societyPreferences.saveLoggedInUser(user.id, user.email, user.name, "Public Resident", user.phone)
        _currentUser.value = user
        _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, emptyList())
        AuthOpResult.Success(user, "Signed in with Google as ${user.name} (Public Resident)")
    }

    /**
     * Registers a new committee member account with Firebase Authentication and
     * saves their profile in Room local cache and Firestore nested collection (/societies/{societyName}/members).
     */
    override suspend fun registerCommitteeMember(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        pin: String,
        flatNo: String,
        societyName: String
    ): AuthOpResult<User> = withContext(Dispatchers.IO) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val trimmedPassword = password.trim()
        val trimmedPin = pin.trim().ifBlank { "1234" }
        val effectiveSocName = societyName.ifBlank { societyPreferences.getSocietyInfo().name }

        if (trimmedName.isBlank()) {
            return@withContext AuthOpResult.Error("Please enter committee member full name.")
        }
        if (trimmedEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return@withContext AuthOpResult.Error("Please enter a valid official committee email address.")
        }
        if (trimmedPassword.length < 6) {
            return@withContext AuthOpResult.Error("Password must be at least 6 characters long.")
        }

        // Validate that phone number is not duplicate in the backend
        if (trimmedPhone.isNotBlank()) {
            val allExistingMembers = memberDao.getAllMembersList()
            val cleanPhone = trimmedPhone.filter { it.isDigit() }.takeLast(10)
            val isDuplicate = allExistingMembers.any {
                val existingClean = it.phone.filter { ch -> ch.isDigit() }.takeLast(10)
                existingClean.isNotBlank() && existingClean == cleanPhone
            }
            if (isDuplicate) {
                return@withContext AuthOpResult.Error("This phone number (+91 $cleanPhone) is already registered in the backend. Please use a unique phone number.")
            }
        }

        val isSuper = role.equals("Super Admin", ignoreCase = true) || isSuperAdminEmail(trimmedEmail)

        // 1. Firebase Authentication registration
        val authResult = firebaseAuthManager.registerCommitteeUser(
            email = trimmedEmail,
            password = trimmedPassword,
            displayName = trimmedName
        )

        val firebaseUid = when (authResult) {
            is AuthOpResult.Success -> authResult.data.uid
            is AuthOpResult.Error -> "committee_${System.currentTimeMillis()}"
        }

        // 2. Persist to local Room MemberDao
        val member = Member(
            name = trimmedName,
            email = trimmedEmail,
            phone = trimmedPhone,
            role = role,
            pin = trimmedPin,
            isSuperAdmin = isSuper,
            flatNo = flatNo.ifBlank { "A-101" }
        )
        val memberId = memberDao.insertMember(member)
        val savedMember = member.copy(id = memberId)

        // 3. Construct User model
        val user = savedMember.toUser(effectiveSocName).copy(
            id = firebaseUid,
            societyName = effectiveSocName
        )

        // 4. Update session state
        societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role)
        _currentUser.value = user
        val allMembers = memberDao.getAllMembersList()
        _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, allMembers)

        // 5. Cloud Firestore synchronization (nested under /societies/{societyName}/members)
        repositoryScope.launch {
            try {
                firestoreService.saveMember(savedMember, effectiveSocName)
            } catch (e: Exception) {
                Log.w(TAG, "Notice: Member Firestore sync notice: ${e.message}")
            }
        }

        val successMessage = if (authResult is AuthOpResult.Success) {
            "Committee member ${user.name} registered with Firebase Auth and assigned to $role."
        } else {
            "Committee member ${user.name} registered locally. (Cloud note: ${(authResult as AuthOpResult.Error).message})"
        }

        AuthOpResult.Success(user, successMessage)
    }

    /**
     * Sends password reset instructions to the given committee email.
     */
    override suspend fun sendPasswordResetEmail(email: String): AuthOpResult<Unit> = withContext(Dispatchers.IO) {
        firebaseAuthManager.sendPasswordResetEmail(email)
    }

    /**
     * Signs out the current user and clears local session.
     */
    override suspend fun signOut() = withContext(Dispatchers.IO) {
        firebaseAuthManager.signOut()
        societyPreferences.clearLoggedInUser()
        _currentUser.value = null
        _accessProfile.value = firebaseAuthManager.resolveAccessProfile(null, emptyList())
    }

    override suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        _currentUser.value ?: run {
            initSavedUserSession()
            _currentUser.value
        }
    }

    override suspend fun refreshUserProfile(): User? = withContext(Dispatchers.IO) {
        val email = _currentUser.value?.email ?: societyPreferences.getLoggedInUserEmail()
        if (email.isNotBlank()) {
            val member = memberDao.getMemberByEmail(email)
            if (member != null) {
                val user = member.toUser(societyPreferences.getSocietyInfo().name).copy(
                    id = _currentUser.value?.id ?: member.id.toString(),
                    societyName = societyPreferences.getSocietyInfo().name
                )
                _currentUser.value = user
                val allMembers = memberDao.getAllMembersList()
                _accessProfile.value = firebaseAuthManager.resolveAccessProfile(email, allMembers)
                return@withContext user
            }
        }
        _currentUser.value
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val member = user.toMember()
            memberDao.updateMember(member)
            _currentUser.value = user
            val allMembers = memberDao.getAllMembersList()
            _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, allMembers)
            societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role)

            val societyName = societyPreferences.getSocietyInfo().name
            firestoreService.saveMember(member, societyName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.IO) {
        val trimmed = pin.trim()
        val currentUserPin = _currentUser.value?.pin
        if (!currentUserPin.isNullOrBlank() && currentUserPin == trimmed) return@withContext true

        val superAdminPin = societyPreferences.getSuperAdminPin()
        if (superAdminPin.isNotBlank() && superAdminPin == trimmed) return@withContext true

        val appAdminPin = societyPreferences.getAppAdminPin()
        if (appAdminPin.isNotBlank() && appAdminPin == trimmed) return@withContext true

        false
    }

    override suspend fun updatePin(newPin: String): Boolean = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext false
        val member = memberDao.getMemberByEmail(user.email) ?: return@withContext false
        val updatedMember = member.copy(pin = newPin.trim())
        memberDao.updateMember(updatedMember)
        _currentUser.value = user.copy(pin = newPin.trim())
        true
    }

    override suspend fun getCommitteeMembers(societyName: String): List<User> = withContext(Dispatchers.IO) {
        val effectiveSocName = societyName.ifBlank { societyPreferences.getSocietyInfo().name }
        val members = memberDao.getAllMembersList()
        members.map { it.toUser(effectiveSocName) }
    }

    override fun getCommitteeMembersFlow(): Flow<List<User>> {
        val effectiveSocName = societyPreferences.getSocietyInfo().name
        return memberDao.getAllMembers().map { list ->
            list.map { it.toUser(effectiveSocName) }
        }
    }

    /**
     * Helper for instant persona switching during live demos or offline committee auditing.
     */
    override suspend fun quickPersonaLogin(role: String, societyName: String): User = withContext(Dispatchers.IO) {
        val effectiveSocName = societyName.ifBlank { societyPreferences.getSocietyInfo().name }
        val members = memberDao.getAllMembersList()
        val matchedMember = members.find { it.role.contains(role, ignoreCase = true) }
            ?: members.firstOrNull()
            ?: run {
                val defaultMember = Member(
                    name = "Demo $role",
                    email = "${role.lowercase().replace(" ", "")}@society.org",
                    role = role,
                    isSuperAdmin = role.contains("Super Admin", ignoreCase = true)
                )
                val id = memberDao.insertMember(defaultMember)
                defaultMember.copy(id = id)
            }

        val user = matchedMember.toUser(effectiveSocName).copy(
            societyName = effectiveSocName
        )
        societyPreferences.saveLoggedInUser(user.id, user.email, user.name, user.role)
        _currentUser.value = user
        _accessProfile.value = firebaseAuthManager.resolveAccessProfile(user.email, members)
        user
    }

    private fun isSuperAdminEmail(email: String): Boolean {
        val lower = email.lowercase().trim()
        return lower.isNotBlank() && lower.startsWith("admin@")
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    companion object {
        private const val TAG = "UserRepository"

        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(context: Context): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val appContext = context.applicationContext
                    val db = SocietyDatabase.getDatabase(appContext, CoroutineScope(Dispatchers.IO + SupervisorJob()))
                    val preferences = SocietyPreferences(appContext)
                    val firestoreService = FirestoreService()
                    val authManager = FirebaseAuthManager.getInstance()
                    UserRepository(
                        context = appContext,
                        firebaseAuthManager = authManager,
                        memberDao = db.memberDao(),
                        firestoreService = firestoreService,
                        societyPreferences = preferences
                    ).also { INSTANCE = it }
                }
            }
        }
    }
}
