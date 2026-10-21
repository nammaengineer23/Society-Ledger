package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthOpResult
import com.example.data.auth.CommitteeAccessProfile
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.Member
import com.example.data.model.User
import com.example.data.repository.UserRepositoryContract
import com.example.ui.viewmodel.UserViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

class FakeUserRepository : UserRepositoryContract {
    private val authManager = FirebaseAuthManager.getInstance()
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessProfile = MutableStateFlow(
        authManager.resolveAccessProfile(null, emptyList())
    )
    override val accessProfile: StateFlow<CommitteeAccessProfile> = _accessProfile.asStateFlow()

    override val isUserSignedIn: Boolean
        get() = _currentUser.value != null

    override val currentUserId: String?
        get() = _currentUser.value?.id

    override val currentEmail: String?
        get() = _currentUser.value?.email

    private val membersList = mutableListOf(
        User(
            id = "user_101",
            name = "Rajesh Sharma",
            email = "rajesh.sharma@society.org",
            role = "President",
            pin = "1234",
            isSuperAdmin = true,
            societyName = "Green Glen Heights"
        )
    )

    override suspend fun login(email: String, password: String): AuthOpResult<User> {
        val user = membersList.find { it.email.equals(email, ignoreCase = true) }
        return if (user != null) {
            _currentUser.value = user
            val member = Member(
                id = 1L,
                name = user.name,
                email = user.email,
                role = user.role,
                isSuperAdmin = user.isSuperAdmin,
                pin = user.pin
            )
            _accessProfile.value = authManager.resolveAccessProfile(user.email, listOf(member))
            AuthOpResult.Success(user, "Welcome back, ${user.name}!")
        } else {
            AuthOpResult.Error("Invalid committee credentials.")
        }
    }

    override suspend fun loginWithGoogleIdToken(idToken: String): AuthOpResult<User> {
        if (idToken.isBlank()) return AuthOpResult.Error("Google ID token is required.")
        val user = User(
            id = "firebase_test_uid",
            name = "Verified Google User",
            email = "verified@gmail.com",
            role = "Public Resident"
        )
        _currentUser.value = user
        _accessProfile.value = authManager.resolveAccessProfile(user.email, emptyList())
        return AuthOpResult.Success(user, "Google ID token verified")
    }

    override suspend fun loginWithPhoneOtp(phone: String, name: String, role: String): AuthOpResult<User> {
        val user = User(id = "user_phone", name = name.ifBlank { "Resident $phone" }, email = "$phone@society.org", role = role)
        _currentUser.value = user
        return AuthOpResult.Success(user, "Phone login success")
    }

    override suspend fun loginWithGoogle(
        email: String,
        displayName: String,
        role: String,
        phone: String,
        password: String
    ): AuthOpResult<User> {
        val user = User(id = "user_google", name = displayName.ifBlank { "Google User" }, email = email, role = role, phone = phone)
        _currentUser.value = user
        return AuthOpResult.Success(user, "Google login success")
    }

    override suspend fun registerCommitteeMember(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        pin: String,
        flatNo: String,
        societyName: String
    ): AuthOpResult<User> {
        val isSuper = role.equals("Super Admin", ignoreCase = true)
        val newUser = User(
            id = "user_${System.currentTimeMillis()}",
            name = name,
            email = email,
            phone = phone,
            role = role,
            pin = pin,
            isSuperAdmin = isSuper,
            societyName = societyName
        )
        membersList.add(newUser)
        _currentUser.value = newUser
        val member = Member(
            id = membersList.size.toLong(),
            name = name,
            email = email,
            phone = phone,
            role = role,
            pin = pin,
            isSuperAdmin = isSuper,
            flatNo = flatNo
        )
        _accessProfile.value = authManager.resolveAccessProfile(email, listOf(member))
        return AuthOpResult.Success(newUser, "Account created successfully!")
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthOpResult<Unit> {
        return AuthOpResult.Success(Unit, "Password reset email sent to $email")
    }

    override suspend fun signOut() {
        _currentUser.value = null
        _accessProfile.value = authManager.resolveAccessProfile(null, emptyList())
    }

    override suspend fun getCurrentUser(): User? = _currentUser.value

    override suspend fun refreshUserProfile(): User? = _currentUser.value

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        _currentUser.value = user
        return Result.success(Unit)
    }

    override suspend fun verifyPin(pin: String): Boolean {
        return _currentUser.value?.pin == pin
    }

    override suspend fun updatePin(newPin: String): Boolean {
        _currentUser.value = _currentUser.value?.copy(pin = newPin)
        return true
    }

    override suspend fun getCommitteeMembers(societyName: String): List<User> = membersList

    override fun getCommitteeMembersFlow(): Flow<List<User>> = flowOf(membersList)

    override suspend fun quickPersonaLogin(role: String, societyName: String): User {
        val user = User(
            id = "persona_$role",
            name = "Demo $role",
            email = "${role.lowercase()}@demo.org",
            role = role,
            isSuperAdmin = role == "Super Admin"
        )
        _currentUser.value = user
        return user
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserAuthTest {

    @Test
    fun testRepositoryLogin_updatesCurrentUserAndProfile() = runBlocking {
        val fakeRepo = FakeUserRepository()

        val result = fakeRepo.login("rajesh.sharma@society.org", "Society@123")

        assertTrue(result is AuthOpResult.Success)
        assertNotNull(fakeRepo.currentUser.value)
        assertEquals("Rajesh Sharma", fakeRepo.currentUser.value?.name)
        assertEquals("President", fakeRepo.currentUser.value?.role)
        assertTrue(fakeRepo.accessProfile.value.isPresident)
        assertTrue(fakeRepo.accessProfile.value.isSuperAdmin)
    }

    @Test
    fun testRepositoryRegistration_createsUserAndSignsIn() = runBlocking {
        val fakeRepo = FakeUserRepository()

        val result = fakeRepo.registerCommitteeMember(
            name = "Priya Sundaram",
            email = "priya.sundaram@society.org",
            phone = "9876543210",
            password = "SecurePassword123",
            role = "Treasurer",
            pin = "4321",
            flatNo = "B-402",
            societyName = "Green Glen Heights"
        )

        assertTrue(result is AuthOpResult.Success)
        assertEquals("Priya Sundaram", fakeRepo.currentUser.value?.name)
        assertEquals("Treasurer", fakeRepo.currentUser.value?.role)
        assertTrue(fakeRepo.accessProfile.value.isTreasurer)
    }

    @Test
    fun testRepositorySignOut_clearsSession() = runBlocking {
        val fakeRepo = FakeUserRepository()

        fakeRepo.login("rajesh.sharma@society.org", "Society@123")
        assertNotNull(fakeRepo.currentUser.value)

        fakeRepo.signOut()
        assertEquals(null, fakeRepo.currentUser.value)
        assertFalse(fakeRepo.isUserSignedIn)
    }

    @Test
    fun testViewModelIntegration_delegatesCorrectly() = runBlocking {
        val fakeRepo = FakeUserRepository()
        val viewModel = UserViewModel(
            application = ApplicationProvider.getApplicationContext(),
            userRepository = fakeRepo
        )

        var callbackCalled = false
        viewModel.login("rajesh.sharma@society.org", "Society@123") { success, _ ->
            callbackCalled = true
        }

        ShadowLooper.idleMainLooper()

        assertTrue(callbackCalled)
        assertEquals("Rajesh Sharma", viewModel.currentUser.value?.name)
        assertTrue(viewModel.accessProfile.value.isPresident)
    }

    @Test
    fun testViewModelRegister_convenienceMethod() = runBlocking {
        val fakeRepo = FakeUserRepository()
        val viewModel = UserViewModel(
            application = ApplicationProvider.getApplicationContext(),
            userRepository = fakeRepo
        )

        var registerCallbackCalled = false
        viewModel.register("new.resident@society.org", "Pass1234", "New Resident") { success, _ ->
            registerCallbackCalled = true
        }

        ShadowLooper.idleMainLooper()

        assertTrue(registerCallbackCalled)
        assertEquals("New Resident", viewModel.currentUser.value?.name)
        assertEquals("new.resident@society.org", viewModel.currentUser.value?.email)
    }

    @Test
    fun accessProfileDoesNotGrantCommitteeRoleByNamePrefix() {
        val manager = FirebaseAuthManager.getInstance()
        val roster = listOf(
            Member(
                id = 7L,
                name = "Admin User",
                email = "adminuser@example.com",
                role = "Super Admin",
                isSuperAdmin = true
            )
        )

        val profile = manager.resolveAccessProfile("adminuser2@example.com", roster)

        assertFalse(profile.isCommitteeMember)
        assertFalse(profile.isSuperAdmin)
        assertTrue(profile.isPublicResident)
    }

    @Test
    fun accessProfileGrantsCommitteeRoleOnlyForExactEmailMatch() {
        val manager = FirebaseAuthManager.getInstance()
        val roster = listOf(
            Member(
                id = 7L,
                name = "Admin User",
                email = "adminuser@example.com",
                role = "Super Admin",
                isSuperAdmin = true
            )
        )

        val profile = manager.resolveAccessProfile("ADMINUSER@example.com", roster)

        assertTrue(profile.isCommitteeMember)
        assertTrue(profile.isSuperAdmin)
    }

    @Test
    fun testLottieAuthLoadingResource_existsAndIsValidJson() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val inputStream = context.resources.openRawResource(R.raw.lottie_auth_loading)
        assertNotNull(inputStream)
        val content = inputStream.bufferedReader().use { it.readText() }
        assertTrue("Lottie file should not be empty", content.isNotBlank())
        assertTrue("Lottie file must contain animation layers", content.contains("\"layers\""))
        assertTrue("Lottie file should specify frame rate", content.contains("\"fr\""))
        assertTrue("Lottie file should define width and height", content.contains("\"w\"") && content.contains("\"h\""))
    }

    @Test
    fun testPasswordVisibilityToggle_logicAndTransformation() {
        var isPasswordVisible = false
        fun getVisualTransformation(visible: Boolean) =
            if (visible) androidx.compose.ui.text.input.VisualTransformation.None
            else androidx.compose.ui.text.input.PasswordVisualTransformation()

        // Initially hidden
        assertEquals(androidx.compose.ui.text.input.PasswordVisualTransformation::class, getVisualTransformation(isPasswordVisible)::class)

        // Toggle to visible (eye icon clicked)
        isPasswordVisible = !isPasswordVisible
        assertTrue(isPasswordVisible)
        assertEquals(androidx.compose.ui.text.input.VisualTransformation.None::class, getVisualTransformation(isPasswordVisible)::class)

        // Toggle back to hidden
        isPasswordVisible = !isPasswordVisible
        assertFalse(isPasswordVisible)
        assertEquals(androidx.compose.ui.text.input.PasswordVisualTransformation::class, getVisualTransformation(isPasswordVisible)::class)
    }

    @Test
    fun testMobilePhoneOtpLogin_successAndUserSet() = runBlocking {
        val fakeRepo = FakeUserRepository()
        val viewModel = UserViewModel(
            application = ApplicationProvider.getApplicationContext(),
            userRepository = fakeRepo
        )

        var loginCallbackCalled = false
        viewModel.loginWithPhoneOtp("9876543210", "Sunil Verma", "Resident Member") { success, _ ->
            loginCallbackCalled = true
        }

        ShadowLooper.idleMainLooper()

        assertTrue(loginCallbackCalled)
        assertEquals("Sunil Verma", viewModel.currentUser.value?.name)
        assertEquals("9876543210@society.org", viewModel.currentUser.value?.email)
    }

    @Test
    fun testGoogleSignInLogin_successAndUserSet() = runBlocking {
        val fakeRepo = FakeUserRepository()
        val viewModel = UserViewModel(
            application = ApplicationProvider.getApplicationContext(),
            userRepository = fakeRepo
        )

        var loginCallbackCalled = false
        viewModel.loginWithGoogle("societyledger@gmail.com", "Rahul Sharma", "Resident Member") { success, _ ->
            loginCallbackCalled = true
        }

        ShadowLooper.idleMainLooper()

        assertTrue(loginCallbackCalled)
        assertEquals("Rahul Sharma", viewModel.currentUser.value?.name)
        assertEquals("societyledger@gmail.com", viewModel.currentUser.value?.email)
    }

    @Test
    fun testPersistentSession_isUserLoggedInAndLogout() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs = com.example.data.local.SocietyPreferences(context)
        prefs.clearLoggedInUser()
        assertFalse(prefs.isUserLoggedIn())

        // Save session on login
        prefs.saveLoggedInUser("user_123", "test@society.org", "Test User", "Resident Member")
        assertTrue(prefs.isUserLoggedIn())
        assertEquals("test@society.org", prefs.getLoggedInUserEmail())
        assertEquals("Test User", prefs.getLoggedInUserName())

        // Clear session on logout
        prefs.clearLoggedInUser()
        assertFalse(prefs.isUserLoggedIn())
    }
}
