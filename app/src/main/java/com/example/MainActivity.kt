package com.example

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SocietyPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.AppTranslations
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.ui.components.AddEditDonationDialog
import com.example.ui.components.AddEditExpenseDialog
import com.example.ui.components.AddEditMemberDialog
import com.example.ui.components.AppLockScreen
import com.example.ui.components.AuthMemberDialog
import com.example.ui.components.CategoryBudgetDialog
import com.example.ui.components.ChangePinDialog
import com.example.ui.components.CommitteeRestrictedActionDialog
import com.example.ui.components.DonationSuccessLottieDialog
import com.example.ui.components.EditSocietyInfoDialog
import com.example.ui.components.EmailBackupDialog
import com.example.ui.components.FinancialYearRolloverDialog
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.MasterBulkDataEntryDialog
import com.example.ui.components.MarkAsPaidDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.RestoreBackupDialog
import com.example.ui.components.DeleteDonationConfirmationDialog
import com.example.ui.components.DeleteExpenseConfirmationDialog
import com.example.ui.components.SuperAdminDeleteConfirmDialog
import com.example.ui.components.SuperAdminModifyConfirmDialog
import com.example.ui.screens.AboutUsScreen
import com.example.ui.screens.AppAdminConsoleScreen
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.CommitteeLoginScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeleteSocietyScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.DonationScreen
import com.example.ui.screens.DonationsScreen
import com.example.ui.screens.DonorsBroadcastScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.FAQScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainBottomNavigationBar
import com.example.ui.screens.PendingPledgesScreen
import com.example.ui.screens.ReportsAndCommitteeScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.TermsScreen
import com.example.ui.screens.UserAuthScreen
import com.example.ui.screens.UserDataCollectionScreen
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyManagementTheme
import com.example.ui.theme.SocietyTealLight
import com.example.ui.theme.SocietyTealPrimary
import com.example.ui.theme.ThemeToggleIconButton
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.SocietyViewModel
import com.example.ui.viewmodel.UserSubscriptionViewModel
import com.example.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

enum class AppScreen(val label: String, val testTag: String) {
    HOME("Home", "tab_home"),
    DASHBOARD("Overview", "tab_dashboard"),
    DONATIONS("Donations", "tab_donations"),
    NEW_DONATION("New Donation", "screen_new_donation"),
    DONORS("Donors", "tab_donors_broadcast"),
    PLEDGES("Pledges", "tab_pledges"),
    PENDING("Pending", "tab_pending"),
    EXPENSES("Expenses", "tab_expenses"),
    NEW_EXPENSE("New Expense", "screen_new_expense"),
    REPORTS("Reports", "tab_reports"),
    SUBSCRIPTION("Plans", "tab_subscription"),
    AUDIT_LOG("Audit Log", "screen_audit_log"),
    CONTACT("Contact", "screen_contact"),
    ABOUT_US("About Us", "screen_about_us"),
    TERMS("Terms", "screen_terms"),
    USER_DATA_COLLECTION("Privacy", "screen_user_data"),
    DELETE_SOCIETY("Delete Society", "screen_delete_society"),
    FAQ("FAQ", "screen_faq"),
    CHATBOT("AI Helpdesk", "screen_chatbot"),
    ADMIN_CONSOLE("Master Admin", "screen_admin_console"),
    LOGIN("Committee Login", "screen_login")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SocietyViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            SocietyManagementTheme(
                darkTheme = isDarkTheme,
                onToggleTheme = viewModel::toggleTheme
            ) {
                SocietyMainApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = viewModel::toggleTheme
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyMainApp(
    viewModel: SocietyViewModel = viewModel(),
    userSubscriptionViewModel: UserSubscriptionViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val societyPreferences = remember { SocietyPreferences(context) }
    var currentScreen by remember {
        mutableStateOf(if (societyPreferences.isUserLoggedIn()) AppScreen.HOME else AppScreen.LOGIN)
    }

    // STRICT AUTHENTICATION GUARD: Confirm no user should enter landing page or any screen without signin!
    LaunchedEffect(currentScreen) {
        if (!societyPreferences.isUserLoggedIn() && currentScreen != AppScreen.LOGIN) {
            currentScreen = AppScreen.LOGIN
        }
    }

    val screenBackStack = remember { mutableStateListOf<AppScreen>() }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var showPlayStoreRatingDialog by remember { mutableStateOf(false) }

    fun navigateTo(screen: AppScreen) {
        if (currentScreen != screen) {
            screenBackStack.add(currentScreen)
            currentScreen = screen
        }
    }

    BackHandler(enabled = true) {
        if (currentScreen == AppScreen.LOGIN) {
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2500L) {
                (context as? Activity)?.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        } else if (screenBackStack.isNotEmpty()) {
            val previous = screenBackStack.removeAt(screenBackStack.lastIndex)
            currentScreen = previous
        } else if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.DASHBOARD) {
            currentScreen = AppScreen.HOME
        } else if (currentScreen == AppScreen.DASHBOARD) {
            currentScreen = AppScreen.HOME
        } else {
            // Already on HOME screen (Root)
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2500L) {
                showPlayStoreRatingDialog = true
            } else {
                lastBackPressTime = now
                Toast.makeText(context, "Press back again to close & rate on Play Store ⭐", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var chatbotReturnScreen by remember { mutableStateOf(AppScreen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // State from ViewModel
    val appLanguage by viewModel.appLanguage.collectAsState()
    val strings = AppTranslations.getStrings(appLanguage)

    val societyInfo by viewModel.societyInfo.collectAsState()
    val kpis by viewModel.financialKPIs.collectAsState()
    val monthlyData by viewModel.monthlyFlow.collectAsState()
    val categoryExpenses by viewModel.categoryExpenses.collectAsState()
    val paymentModeData by viewModel.paymentModeBreakdown.collectAsState()

    val donations by viewModel.filteredDonations.collectAsState()
    val allDonations by viewModel.donations.collectAsState()
    val pendingDonations by viewModel.pendingDonations.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val allExpenses by viewModel.expenses.collectAsState()

    val members by viewModel.members.collectAsState()
    val activeMember by viewModel.activeMember.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val isSuperAdmin by viewModel.isCurrentSuperAdmin.collectAsState()

    val donationSearchQuery by viewModel.donationSearchQuery.collectAsState()
    val donationStatusFilter by viewModel.donationStatusFilter.collectAsState()
    val donationModeFilter by viewModel.donationModeFilter.collectAsState()
    val donationTimePeriod by viewModel.donationTimePeriod.collectAsState()
    val donationMemberFilter by viewModel.donationMemberFilter.collectAsState()

    val expenseSearchQuery by viewModel.expenseSearchQuery.collectAsState()
    val expenseCategoryFilter by viewModel.expenseCategoryFilter.collectAsState()
    val expenseTimePeriod by viewModel.expenseTimePeriod.collectAsState()
    val expenseMemberFilter by viewModel.expenseMemberFilter.collectAsState()

    // Audit Log & Statutory Compliance State
    val auditLogs by viewModel.filteredAuditLogs.collectAsState()
    val auditedTransactions by viewModel.auditedTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val auditSearchQuery by viewModel.auditSearchQuery.collectAsState()
    val auditActionFilter by viewModel.auditActionFilter.collectAsState()
    val auditTypeFilter by viewModel.auditTypeFilter.collectAsState()
    val isAuditSyncing by viewModel.isAuditSyncing.collectAsState()
    val lastAuditSyncTime by viewModel.lastAuditSyncTime.collectAsState()

    // Firestore Cloud Subscription & Razorpay State via UserSubscriptionViewModel
    val userSubscription by userSubscriptionViewModel.subscription.collectAsState()
    val isProSubscriber by userSubscriptionViewModel.isProSubscriber.collectAsState()
    val trialStartTime = userSubscription.trialStartTime
    val subscriptionExpiry = userSubscription.expiryDate
    val razorpayKeyId by userSubscriptionViewModel.razorpayKeyId.collectAsState()
    val lastPaymentTxnId = userSubscription.transactionId
    val subSyncStatus by userSubscriptionViewModel.syncStatus.collectAsState()
    val isSubLoading by userSubscriptionViewModel.isLoading.collectAsState()

    // Master App Admin & Complaints Console State
    val complaints by viewModel.complaints.collectAsState()
    val globalAnnouncement by viewModel.globalAnnouncement.collectAsState()
    val isAnnouncementEnabled by viewModel.isAnnouncementEnabled.collectAsState()
    val directorySocieties by viewModel.directorySocieties.collectAsState()

    // Donors Directory & WhatsApp Broadcast State
    val filteredDonors by viewModel.filteredDonors.collectAsState()
    val allDonors by viewModel.allDonors.collectAsState()
    val donorSearchQuery by viewModel.donorSearchQuery.collectAsState()
    val donorFilterType by viewModel.donorFilterType.collectAsState()

    // App Lock & Category Budget State
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val appLockPin by viewModel.appLockPin.collectAsState()
    val categoryBudgets by viewModel.categoryBudgets.collectAsState()
    var isAppUnlocked by rememberSaveable { mutableStateOf(false) }
    val isMasterAdminAuthenticated by adminViewModel.isAuthenticated.collectAsState()

    // Dialog States
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showRestoreBackupDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showEmailBackupDialog by remember { mutableStateOf(false) }
    var showAddDonationDialog by remember { mutableStateOf(false) }
    var editingDonation by remember { mutableStateOf<Donation?>(null) }
    var donationForReceipt by remember { mutableStateOf<Donation?>(null) }
    var donationForMarkPaid by remember { mutableStateOf<Donation?>(null) }
    var donationToDelete by remember { mutableStateOf<Donation?>(null) }
    var lastRecordedDonationForSuccessFeedback by remember { mutableStateOf<Donation?>(null) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    var showAuthMemberDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<Member?>(null) }
    var memberToDelete by remember { mutableStateOf<Member?>(null) }

    var showEditSocietyDialog by remember { mutableStateOf(false) }
    var showMasterBulkSetupDialog by remember { mutableStateOf(false) }
    var memberForPinChange by remember { mutableStateOf<Pair<Member, Boolean>?>(null) }
    var showFinancialYearRolloverDialog by remember { mutableStateOf(false) }
    var showGlobalSearchDialog by remember { mutableStateOf(false) }
    var restrictedActionName by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(
        LocalAppLanguage provides appLanguage,
        LocalAppStrings provides strings
    ) {
        if (isAppLockEnabled && !isAppUnlocked) {
            val superAdminPin = members.firstOrNull { it.isSuperAdmin }?.pin ?: ""
            AppLockScreen(
                societyInfo = societyInfo,
                correctPin = appLockPin,
                superAdminPin = superAdminPin,
                onUnlockSuccess = { isAppUnlocked = true }
            )
            return@CompositionLocalProvider
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("society_main_scaffold"),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (currentScreen != AppScreen.LOGIN) {
                    CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = societyInfo.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = when {
                                    !isAdminMode -> strings.publicViewReadOnly
                                    isSuperAdmin -> "${strings.superAdminPrefix}: ${activeMember?.name ?: "President"}"
                                    else -> "${strings.committeePrefix}: ${activeMember?.name ?: "Member"}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = when {
                                        !isAdminMode -> Color(0xFFE65100)
                                        isSuperAdmin -> SapphireNavy
                                        else -> SocietyTealPrimary
                                    }
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(36.dp)
                                .background(SocietyTealPrimary.copy(alpha = 0.12f), CircleShape)
                                .clickable {
                                    currentScreen = if (currentScreen == AppScreen.HOME) AppScreen.DASHBOARD else AppScreen.HOME
                                }
                                .testTag("top_bar_home_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HOME) Icons.Default.Dashboard else Icons.Default.AccountBalance,
                                contentDescription = "Home",
                                tint = SocietyTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        // Global Transaction Search Action Button (Member name, Amount, Receipt ID)
                        IconButton(
                            onClick = { showGlobalSearchDialog = true },
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .testTag("top_bar_global_search_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Global Transaction Search",
                                tint = if (isDarkTheme) Color(0xFF90CAF9) else SapphireNavy
                            )
                        }

                        // Language Switching Action Button
                        IconButton(
                            onClick = { showLanguageDialog = true },
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .testTag("top_bar_language_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isDarkTheme) Color(0xFF1E3A5F) else SapphireNavy.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = appLanguage.shortBadge,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (isDarkTheme) Color(0xFF90CAF9) else SapphireNavy
                                    )
                                )
                            }
                        }

                        // Theme Toggle Button (Light / Dark Mode for late-night ledger comfort)
                        ThemeToggleIconButton(
                            isDark = isDarkTheme,
                            onToggle = onToggleTheme,
                            modifier = Modifier.padding(end = 4.dp),
                            testTag = "theme_toggle_button"
                        )

                        // Top Right Corner: Member Option to Enter / Authenticate with PIN with smooth motion transitions
                        val chipTargetColor = when {
                            activeMember != null -> SocietyTealPrimary
                            isAdminMode && isSuperAdmin -> SapphireNavy
                            isAdminMode -> SocietyTealPrimary
                            else -> IndianSaffron
                        }
                        val animatedChipColor by animateColorAsState(
                            targetValue = chipTargetColor,
                            animationSpec = tween(300),
                            label = "top_chip_color_animation"
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = animatedChipColor,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { showAuthMemberDialog = true }
                                .testTag("top_bar_auth_chip")
                        ) {
                            AnimatedContent(
                                targetState = Triple(isAdminMode, isSuperAdmin, activeMember?.name),
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { -it / 2 })
                                        .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { it / 2 })
                                },
                                label = "top_bar_auth_chip_motion"
                            ) { (admin, superAdmin, memberFullName) ->
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            activeMember != null -> Icons.Default.Person
                                            admin && superAdmin -> Icons.Default.Security
                                            admin -> Icons.Default.AdminPanelSettings
                                            else -> Icons.Default.Public
                                        },
                                        contentDescription = if (activeMember != null) activeMember?.name else if (admin) "Admin Account" else "Public",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = when {
                                            activeMember != null -> memberFullName?.split(" ")?.firstOrNull() ?: activeMember?.name ?: "Member"
                                            admin -> if (superAdmin) strings.superAdminPrefix else "Admin"
                                            else -> "Public"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != AppScreen.LOGIN) {
                MainBottomNavigationBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> navigateTo(screen) },
                    pendingPledgeCount = kpis.pendingCount
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (currentScreen == AppScreen.LOGIN) PaddingValues(0.dp) else paddingValues)
        ) {
            if (currentScreen != AppScreen.LOGIN && isAnnouncementEnabled && globalAnnouncement.isNotBlank()) {
                    Surface(
                        color = IndianSaffron.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Global Announcement",
                                tint = IndianSaffron,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = globalAnnouncement,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                val effectiveScreen = if (!societyPreferences.isUserLoggedIn()) AppScreen.LOGIN else currentScreen
                AnimatedContent(
                    targetState = effectiveScreen,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(260)) + slideInHorizontally(animationSpec = tween(260)) { it / 6 })
                            .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 6 })
                    },
                    label = "screen_transition",
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) { screen ->
            when (screen) {
                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        kpis = kpis,
                        societyInfo = societyInfo,
                        monthlyData = monthlyData,
                        categoryExpenses = categoryExpenses,
                        paymentModeData = paymentModeData,
                        donations = allDonations,
                        expenses = allExpenses,
                        isAdmin = isAdminMode,
                        activeMember = activeMember,
                        members = members,
                        onNavigateToReports = { currentScreen = AppScreen.REPORTS },
                        onNavigateToDonations = { currentScreen = AppScreen.DONATIONS },
                        onNavigateToExpenses = { currentScreen = AppScreen.EXPENSES },
                        onNavigateToPledges = { currentScreen = AppScreen.PLEDGES },
                        onNavigateToDonors = { currentScreen = AppScreen.DONORS },
                        onOpenAddDonation = { showAddDonationDialog = true },
                        onOpenAddExpense = { showAddExpenseDialog = true },
                        onEditSocietyClick = { showEditSocietyDialog = true },
                        onNavigateToHome = { currentScreen = AppScreen.HOME },
                        onOpenAuthDialog = { showAuthMemberDialog = true },
                        onTogglePublicMode = viewModel::setPublicMode
                    )
                }

                AppScreen.DONATIONS -> {
                    DonationsScreen(
                        donations = donations,
                        searchQuery = donationSearchQuery,
                        statusFilter = donationStatusFilter,
                        modeFilter = donationModeFilter,
                        timePeriodFilter = donationTimePeriod,
                        memberFilter = donationMemberFilter,
                        members = members,
                        activeMember = activeMember,
                        societyInfo = societyInfo,
                        isAdmin = isAdminMode,
                        onSearchChange = viewModel::setDonationSearchQuery,
                        onStatusFilterChange = viewModel::setDonationStatusFilter,
                        onModeFilterChange = viewModel::setDonationModeFilter,
                        onTimePeriodChange = viewModel::setDonationTimePeriod,
                        onMemberFilterChange = viewModel::setDonationMemberFilter,
                        onAddDonationClick = { showAddDonationDialog = true },
                        onViewReceipt = { donation -> donationForReceipt = donation },
                        onMarkAsPaid = { donation -> donationForMarkPaid = donation },
                        onEditDonation = { donation ->
                            if (isAdminMode || isSuperAdmin) editingDonation = donation else restrictedActionName = "Modify Transactions (Admin Only)"
                        },
                        onDeleteDonation = { donation ->
                            if (isAdminMode || isSuperAdmin) donationToDelete = donation else restrictedActionName = "Delete Transactions (Admin Only)"
                        },
                        onNavigateToDonors = { navigateTo(AppScreen.DONORS) }
                    )
                }

                AppScreen.NEW_DONATION -> {
                    DonationScreen(
                        societyName = societyInfo.name,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateBack = { currentScreen = AppScreen.DONATIONS },
                        onDonationSaved = { savedDonation ->
                            lastRecordedDonationForSuccessFeedback = savedDonation
                            viewModel.syncAllFromFirestore()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Donation of ₹${savedDonation.amount.toInt()} recorded successfully!")
                            }
                            currentScreen = AppScreen.DONATIONS
                        }
                    )
                }

                AppScreen.DONORS -> {
                    DonorsBroadcastScreen(
                        societyInfo = societyInfo,
                        donors = filteredDonors,
                        allDonorsCount = allDonors.size,
                        searchQuery = donorSearchQuery,
                        onSearchQueryChange = viewModel::setDonorSearchQuery,
                        filterType = donorFilterType,
                        onFilterTypeChange = viewModel::setDonorFilterType,
                        onAddCustomDonor = viewModel::addCustomDonor,
                        isProSubscriber = true,
                        onNavigateBack = { currentScreen = AppScreen.DONATIONS }
                    )
                }

                AppScreen.PLEDGES, AppScreen.PENDING -> {
                    PendingPledgesScreen(
                        pendingDonations = pendingDonations,
                        searchQuery = donationSearchQuery,
                        memberFilter = donationMemberFilter,
                        members = members,
                        activeMember = activeMember,
                        societyInfo = societyInfo,
                        isAdmin = isAdminMode,
                        onSearchChange = viewModel::setDonationSearchQuery,
                        onMemberFilterChange = viewModel::setDonationMemberFilter,
                        onAddPledgeClick = { showAddDonationDialog = true },
                        onMarkAsPaid = { donation -> donationForMarkPaid = donation },
                        onEditDonation = { donation ->
                            if (isSuperAdmin) editingDonation = donation else restrictedActionName = "Modify"
                        },
                        onDeleteDonation = { donation ->
                            if (isAdminMode) donationToDelete = donation else restrictedActionName = "Delete"
                        }
                    )
                }

                AppScreen.EXPENSES -> {
                    ExpensesScreen(
                        expenses = expenses,
                        searchQuery = expenseSearchQuery,
                        categoryFilter = expenseCategoryFilter,
                        timePeriodFilter = expenseTimePeriod,
                        memberFilter = expenseMemberFilter,
                        members = members,
                        activeMember = activeMember,
                        categoryBudgets = categoryBudgets,
                        isAdmin = isAdminMode,
                        isProSubscriber = true,
                        onSearchChange = viewModel::setExpenseSearchQuery,
                        onCategoryFilterChange = viewModel::setExpenseCategoryFilter,
                        onTimePeriodChange = viewModel::setExpenseTimePeriod,
                        onMemberFilterChange = viewModel::setExpenseMemberFilter,
                        onManageBudgetsClick = {
                            if (isAdminMode || isSuperAdmin) showBudgetDialog = true else restrictedActionName = "Set/Modify Budgets (Admin Only)"
                        },
                        onAddExpenseClick = { navigateTo(AppScreen.NEW_EXPENSE) },
                        onOpenExpenseForm = { navigateTo(AppScreen.NEW_EXPENSE) },
                        onEditExpense = { expense ->
                            if (isAdminMode || isSuperAdmin) editingExpense = expense else restrictedActionName = "Modify Transactions (Admin Only)"
                        },
                        onDeleteExpense = { expense ->
                            if (isAdminMode || isSuperAdmin) expenseToDelete = expense else restrictedActionName = "Delete Transactions (Admin Only)"
                        }
                    )
                }

                AppScreen.NEW_EXPENSE -> {
                    ExpenseScreen(
                        societyName = societyInfo.name,
                        categoryBudgets = categoryBudgets,
                        existingExpensesForCategory = allExpenses.sumOf { it.amount },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onNavigateBack = { currentScreen = AppScreen.EXPENSES },
                        onExpenseSaved = { savedExpense ->
                            viewModel.syncAllFromFirestore()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Expense of ₹${savedExpense.amount.toInt()} recorded successfully!")
                            }
                            currentScreen = AppScreen.EXPENSES
                        }
                    )
                }

                AppScreen.REPORTS -> {
                    ReportsAndCommitteeScreen(
                        societyInfo = societyInfo,
                        members = members,
                        currentMember = activeMember,
                        isAdminMode = isAdminMode,
                        isSuperAdmin = isSuperAdmin,
                        isProSubscriber = isProSubscriber,
                        isDarkTheme = isDarkTheme,
                        isAppLockEnabled = isAppLockEnabled,
                        appLockPin = appLockPin,
                        onToggleTheme = onToggleTheme,
                        onToggleAppLock = viewModel::setAppLockEnabled,
                        onUpdateAppLockPin = viewModel::setAppLockPin,
                        onRestoreBackupClick = { showRestoreBackupDialog = true },
                        donations = allDonations,
                        expenses = allExpenses,
                        timePeriodFilter = donationTimePeriod,
                        onTimePeriodChange = viewModel::setDonationTimePeriod,
                        onOpenAuthDialog = { showAuthMemberDialog = true },
                        onTogglePublicMode = viewModel::setPublicMode,
                        onResetData = {
                            viewModel.resetToSampleData()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Society data refreshed successfully")
                            }
                        },
                        onAddMemberClick = {
                            if (isAdminMode && isSuperAdmin) {
                                editingMember = null
                                showAddMemberDialog = true
                            } else {
                                showAuthMemberDialog = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please login as Admin to add committee members.")
                                }
                            }
                        },
                        onEditMemberClick = { member ->
                            if (isAdminMode && isSuperAdmin) {
                                editingMember = member
                                showAddMemberDialog = true
                            } else {
                                showAuthMemberDialog = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please login as Admin to modify committee members.")
                                }
                            }
                        },
                        onDeleteMemberClick = { member ->
                            if (isAdminMode && isSuperAdmin) {
                                memberToDelete = member
                            } else {
                                showAuthMemberDialog = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please login as Admin to remove committee members.")
                                }
                            }
                        },
                        onEditSocietyInfoClick = { showEditSocietyDialog = true },
                        onMasterBulkSetupClick = { showMasterBulkSetupDialog = true },
                        onOpenEmailBackupClick = { showEmailBackupDialog = true },
                        appLanguage = appLanguage,
                        onOpenLanguageDialog = { showLanguageDialog = true },
                        onSelectLanguage = { lang ->
                            viewModel.setAppLanguage(lang)
                            coroutineScope.launch {
                                val langStrings = AppTranslations.getStrings(lang)
                                snackbarHostState.showSnackbar("${langStrings.languageChangedNotice}: ${lang.nativeName}")
                            }
                        },
                        onChangePinClick = { member, isSuperAdminReset ->
                            if (isAdminMode && isSuperAdmin) {
                                memberForPinChange = Pair(member, isSuperAdminReset)
                            } else {
                                showAuthMemberDialog = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please login as Admin to reset member PIN.")
                                }
                            }
                        },
                        onFinancialYearRolloverClick = {
                            showFinancialYearRolloverDialog = true
                        },
                        onNavigateToAuditLog = { currentScreen = AppScreen.AUDIT_LOG },
                        onNavigateToDonors = { currentScreen = AppScreen.DONORS },
                        onNavigateToAdminConsole = { currentScreen = AppScreen.ADMIN_CONSOLE },
                        onNavigateToHome = { currentScreen = AppScreen.HOME },
                        onNavigateToContact = { currentScreen = AppScreen.CONTACT },
                        onNavigateToAboutUs = { currentScreen = AppScreen.ABOUT_US },
                        onNavigateToTerms = { currentScreen = AppScreen.TERMS },
                        onNavigateToUserDataCollection = { currentScreen = AppScreen.USER_DATA_COLLECTION },
                        onNavigateToDeleteSociety = { currentScreen = AppScreen.DELETE_SOCIETY },
                        onNavigateToFaq = { currentScreen = AppScreen.FAQ },
                        onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                        onNavigateToDonationsWithMember = { memberId ->
                            viewModel.setDonationMemberFilter(memberId)
                            currentScreen = AppScreen.DONATIONS
                        },
                        onNavigateToExpensesWithMember = { memberId ->
                            viewModel.setExpenseMemberFilter(memberId)
                            currentScreen = AppScreen.EXPENSES
                        }
                    )
                }

                AppScreen.SUBSCRIPTION -> {
                    SubscriptionScreen(
                        societyInfo = societyInfo,
                        trialStartTime = trialStartTime,
                        isProSubscriber = isProSubscriber,
                        subscriptionExpiryTime = subscriptionExpiry,
                        razorpayKeyId = razorpayKeyId,
                        lastPaymentTxnId = lastPaymentTxnId,
                        onSaveRazorpayKey = userSubscriptionViewModel::saveRazorpayKey,
                        onActivatePro = { txnId, onResult ->
                            userSubscriptionViewModel.activateProSubscription(txnId, planDurationDays = 365, onResult = onResult)
                        },
                        onToggleProStatus = { isPro ->
                            userSubscriptionViewModel.toggleSubscriptionStatus(isPro) { success, msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        },
                        firestoreSyncStatus = subSyncStatus,
                        isLoading = isSubLoading,
                        onRefreshFirestore = {
                            userSubscriptionViewModel.refreshSubscription()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Subscription status refreshed")
                            }
                        },
                        onBack = { currentScreen = AppScreen.ADMIN_CONSOLE },
                        onNavigateToAdminConsole = { currentScreen = AppScreen.ADMIN_CONSOLE }
                    )
                }

                AppScreen.AUDIT_LOG -> {
                    AuditLogScreen(
                        societyInfo = societyInfo,
                        auditLogs = auditLogs,
                        auditedTransactions = auditedTransactions,
                        searchQuery = auditSearchQuery,
                        onSearchQueryChange = viewModel::setAuditSearchQuery,
                        actionFilter = auditActionFilter,
                        onActionFilterChange = viewModel::setAuditActionFilter,
                        typeFilter = auditTypeFilter,
                        onTypeFilterChange = viewModel::setAuditTypeFilter,
                        isSyncing = isAuditSyncing,
                        lastSyncTime = lastAuditSyncTime,
                        onRefresh = {
                            viewModel.refreshAuditLogsFromFirestore()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Statutory audit trail synced successfully")
                            }
                        },
                        onNavigateBack = { currentScreen = AppScreen.REPORTS }
                    )
                }

                AppScreen.HOME -> {
                    val searchableSocieties by viewModel.searchableSocieties.collectAsState()
                    val searchQuery by viewModel.societySearchQuery.collectAsState()
                    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()
                    HomeScreen(
                        currentSociety = societyInfo,
                        searchableSocieties = searchableSocieties,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::setSocietySearchQuery,
                        getMembersForSociety = viewModel::getMembersForSociety,
                        isCloudSyncing = isCloudSyncing,
                        onForceSyncWithFirestore = {
                            viewModel.forceSyncDeviceWithFirestore { success, msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg,
                                        duration = androidx.compose.material3.SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        onRefreshCloud = {
                            viewModel.uploadAllToFirestore { success, msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msg,
                                        duration = androidx.compose.material3.SnackbarDuration.Long
                                    )
                                }
                            }
                        },
                        onUploadAllToCloud = { onResult ->
                            viewModel.uploadAllToFirestore(onResult)
                        },
                        onInitializeSchema = { onResult ->
                            viewModel.initializeFirestoreSchema(onResult)
                        },
                        onRecreateAllData = { onResult ->
                            viewModel.recreateAllDataWithNewStructure(onResult)
                        },
                        onTestCloudConnection = { onResult ->
                            viewModel.testCloudConnection(onResult)
                        },
                        onEnterSocietyAsMember = { soc, member, pin ->
                            var result = false
                            viewModel.enterSocietyAsMember(soc, member, pin) { success, msg ->
                                result = success
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                                if (success) {
                                    currentScreen = AppScreen.DASHBOARD
                                }
                            }
                            result
                        },
                        onEnterSocietyWithPin = { soc, pin ->
                            var result = false
                            viewModel.enterSociety(soc, pin) { success, msg ->
                                result = success
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                                if (success) {
                                    currentScreen = AppScreen.DASHBOARD
                                }
                            }
                            result
                        },
                        onEnterSocietyPublic = { soc ->
                            viewModel.enterSociety(soc, "") { success, msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                                currentScreen = AppScreen.DASHBOARD
                            }
                        },
                        onCreateSociety = { newSoc, pin, switchToNew ->
                            viewModel.createNewSociety(newSoc, pin, switchToNew) { success, msg ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                                if (success && switchToNew) {
                                    currentScreen = AppScreen.DASHBOARD
                                }
                            }
                        },
                        onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
                        isProSubscriber = true,
                        loggedInUserName = societyPreferences.getLoggedInUserName().ifBlank { activeMember?.name.orEmpty() },
                        loggedInUserRole = societyPreferences.getLoggedInUserRole().ifBlank { if (isAdminMode) "Committee" else "Public Resident" },
                        onLogout = {
                            societyPreferences.clearLoggedInUser()
                            userViewModel.signOut()
                            viewModel.logout()
                            currentScreen = AppScreen.LOGIN
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Logged out successfully.")
                            }
                        },
                        onNavigateToAdminConsole = {
                            currentScreen = AppScreen.ADMIN_CONSOLE
                        },
                        onVerifyMasterAdminAccess = adminViewModel::authenticate,
                        onSubmitComplaint = { complaint ->
                            viewModel.submitServiceComplaint(complaint)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Ticket #${complaint.id} logged with Master Admin")
                            }
                        },
                        onNavigateToContact = { currentScreen = AppScreen.CONTACT },
                        onNavigateToAboutUs = { currentScreen = AppScreen.ABOUT_US },
                        onNavigateToTerms = { currentScreen = AppScreen.TERMS },
                        onNavigateToUserDataCollection = { currentScreen = AppScreen.USER_DATA_COLLECTION },
                        onNavigateToDeleteSociety = { currentScreen = AppScreen.DELETE_SOCIETY },
                        onNavigateToFaq = { currentScreen = AppScreen.FAQ },
                        onNavigateToChatbot = {
                            chatbotReturnScreen = AppScreen.HOME
                            currentScreen = AppScreen.CHATBOT
                        }
                    )
                }

                AppScreen.CONTACT -> {
                    ContactScreen(
                        societyInfo = societyInfo,
                        onSubmitComplaint = { complaint ->
                            viewModel.submitServiceComplaint(complaint)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Ticket #${complaint.id} logged with Master Admin")
                            }
                        },
                        onNavigateToFaq = { currentScreen = AppScreen.FAQ },
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.FAQ -> {
                    FAQScreen(
                        societyInfo = societyInfo,
                        onNavigateToContact = { currentScreen = AppScreen.CONTACT },
                        onNavigateToChatbot = {
                            chatbotReturnScreen = AppScreen.FAQ
                            currentScreen = AppScreen.CHATBOT
                        },
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.CHATBOT -> {
                    ChatbotScreen(
                        societyInfo = societyInfo,
                        onBack = { currentScreen = chatbotReturnScreen }
                    )
                }

                AppScreen.ABOUT_US -> {
                    AboutUsScreen(
                        societyInfo = societyInfo,
                        members = members,
                        donations = allDonations,
                        expenses = allExpenses,
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.TERMS -> {
                    TermsScreen(
                        societyInfo = societyInfo,
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.USER_DATA_COLLECTION -> {
                    UserDataCollectionScreen(
                        societyInfo = societyInfo,
                        currentMember = activeMember,
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.DELETE_SOCIETY -> {
                    DeleteSocietyScreen(
                        societyInfo = societyInfo,
                        isSuperAdmin = isSuperAdmin,
                        onConfirmDeleteSociety = { pin, reason ->
                            var isSuccess = false
                            viewModel.deleteEntireSociety(pin, societyInfo.name, reason) { success, msg ->
                                isSuccess = success
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                                if (success) {
                                    currentScreen = AppScreen.HOME
                                }
                            }
                            isSuccess
                        },
                        onBack = { currentScreen = AppScreen.DASHBOARD }
                    )
                }

                AppScreen.ADMIN_CONSOLE -> {
                    AppAdminConsoleScreen(
                        currentSociety = societyInfo,
                        allSocieties = directorySocieties,
                        complaints = complaints,
                        isCurrentProSubscriber = isProSubscriber,
                        subscriptionExpiry = subscriptionExpiry,
                        razorpayKeyId = razorpayKeyId,
                        globalAnnouncement = globalAnnouncement,
                        isAnnouncementEnabled = isAnnouncementEnabled,
                        initialAuthenticated = isMasterAdminAuthenticated,
                        onVerifyMasterAdminAccess = adminViewModel::authenticate,
                        onVerifyMasterPin = adminViewModel::verifyPinDirect,
                        onGrantPro = { soc, days, txnId, note, onResult ->
                            viewModel.masterAdminGrantPro(soc, days, txnId, note, onResult)
                        },
                        onRevokePro = { soc, reason, onResult ->
                            viewModel.masterAdminRevokePro(soc, reason, onResult)
                        },
                        onUpdateComplaintStatus = { complaintId, status, resolution, adminNotes, onResult ->
                            viewModel.updateComplaintStatus(complaintId, status, resolution, adminNotes, onResult)
                        },
                        onSubmitComplaint = { complaint, onResult ->
                            viewModel.submitServiceComplaint(complaint, onResult)
                        },
                        onUpdateAnnouncement = { announcement, isEnabled, onResult ->
                            viewModel.masterAdminUpdateGlobalAnnouncement(announcement, isEnabled, onResult)
                        },
                        onUpdateRazorpayKey = { newKey ->
                            viewModel.saveRazorpayKey(newKey)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Razorpay Key Updated")
                            }
                        },
                        onUpdateMasterPin = { newPin, onResult ->
                            adminViewModel.updateMasterPin(newPin, onResult)
                        },
                        onDeleteSociety = { soc, reason, onResult ->
                            viewModel.masterAdminDeleteSociety(soc, reason, onResult)
                        },
                        onLogout = adminViewModel::logout,
                        onSwitchSociety = { soc ->
                            viewModel.enterSociety(soc, "") { _, _ ->
                                currentScreen = AppScreen.DASHBOARD
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Switched to ${soc.name}")
                                }
                            }
                        },
                        onNavigateToSubscription = { currentScreen = AppScreen.SUBSCRIPTION },
                        members = members,
                        onAddMember = {
                            editingMember = null
                            showAddMemberDialog = true
                        },
                        onEditMember = { member ->
                            editingMember = member
                            showAddMemberDialog = true
                        },
                        onDeleteMember = { member ->
                            memberToDelete = member
                        },
                        onBack = {
                            adminViewModel.logout()
                            currentScreen = if (societyInfo.name.isNotBlank()) AppScreen.DASHBOARD else AppScreen.HOME
                        }
                    )
                }

                AppScreen.LOGIN -> {
                    UserAuthScreen(
                        userViewModel = userViewModel,
                        societyInfo = societyInfo,
                        availableMembers = members,
                        onAuthSuccess = { user ->
                            // Persistent login: user stays logged in until explicit logout
                            val effectiveRole = "Public Resident"
                            societyPreferences.saveLoggedInUser(
                                userId = user.id,
                                email = user.email,
                                name = user.name,
                                role = effectiveRole,
                                phone = user.phone
                            )

                            // Every user first registers and lands in transparent Public resident view!
                            val publicMember = Member(
                                name = user.name.ifBlank { if (user.phone.isNotBlank()) "Resident ${user.phone.takeLast(4)}" else "Public Resident" },
                                email = user.email,
                                role = "Public Resident",
                                phone = user.phone,
                                isSuperAdmin = false
                            )
                            viewModel.setActiveMember(publicMember)
                            viewModel.setPublicMode(true) // Ensure strictly landing in Public View, NOT Admin Mode!

                            // After verification it loads all societies page (HOME) in public view
                            currentScreen = AppScreen.HOME
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Welcome, ${user.name.ifBlank { "Resident" }}! Opened All Societies in Public View.")
                            }
                        },
                        onBack = if (societyPreferences.isUserLoggedIn()) {
                            { currentScreen = AppScreen.HOME }
                        } else null
                    )
                }
            }
        }
    }
}

    // 1. Add / Edit Donation Dialog
    if (showAddDonationDialog || editingDonation != null) {
        val target = editingDonation
        AddEditDonationDialog(
            existingDonation = target,
            currentMember = activeMember,
            societyInfo = societyInfo,
            onSave = { donorName, phone, amount, status, paymentMode, paymentDate, notes, modificationReason ->
                if (target == null) {
                    viewModel.addDonation(donorName, phone, amount, status, paymentMode, paymentDate, notes) { savedDonation ->
                        lastRecordedDonationForSuccessFeedback = savedDonation
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Donation of ₹${amount.toInt()} recorded!")
                    }
                } else {
                    val updated = target.copy(
                        donorName = donorName,
                        phone = phone,
                        amount = amount,
                        status = status,
                        paymentMode = paymentMode,
                        paymentDate = paymentDate,
                        notes = notes
                    )
                    if (modificationReason.isNotBlank() && isSuperAdmin) {
                        viewModel.superAdminModifyDonation(target, updated, modificationReason) { success, msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    } else {
                        viewModel.updateDonation(updated)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Donation record updated")
                        }
                    }
                }
                showAddDonationDialog = false
                editingDonation = null
            },
            onDismiss = {
                showAddDonationDialog = false
                editingDonation = null
            }
        )
    }

    // 2. Add / Edit Expense Dialog
    if (showAddExpenseDialog || editingExpense != null) {
        val target = editingExpense
        val selectedCategory = target?.category ?: "Maintenance"
        val existingExpensesForCat = allExpenses.filter { it.category == selectedCategory }.sumOf { it.amount }
        AddEditExpenseDialog(
            existingExpense = target,
            currentMember = activeMember,
            categoryBudgets = categoryBudgets,
            existingExpensesForCategory = existingExpensesForCat,
            onSave = { title, category, amount, expenseDate, paidTo, paymentMode, receiptUrl, modificationReason ->
                if (target == null) {
                    viewModel.addExpense(title, category, amount, expenseDate, paidTo, paymentMode, receiptUrl)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Expense of ₹${amount.toInt()} recorded!")
                    }
                } else {
                    val updated = target.copy(
                        title = title,
                        category = category,
                        amount = amount,
                        expenseDate = expenseDate,
                        paidTo = paidTo,
                        paymentMode = paymentMode,
                        receiptUrl = receiptUrl
                    )
                    if (modificationReason.isNotBlank() && isSuperAdmin) {
                        viewModel.superAdminModifyExpense(target, updated, modificationReason) { success, msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    } else {
                        viewModel.updateExpense(updated)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Expense record updated")
                        }
                    }
                }
                showAddExpenseDialog = false
                editingExpense = null
            },
            onDismiss = {
                showAddExpenseDialog = false
                editingExpense = null
            }
        )
    }

    // 3. Digital Receipt Dialog
    donationForReceipt?.let { donation ->
        ReceiptDialog(
            donation = donation,
            societyInfo = societyInfo,
            onDismiss = { donationForReceipt = null }
        )
    }

    // 4. Lottie Feedback Celebration Dialog for recorded donation or receipt generation
    lastRecordedDonationForSuccessFeedback?.let { donation ->
        DonationSuccessLottieDialog(
            donation = donation,
            societyInfo = societyInfo,
            onViewReceipt = {
                donationForReceipt = donation
                lastRecordedDonationForSuccessFeedback = null
            },
            onDismiss = {
                lastRecordedDonationForSuccessFeedback = null
            }
        )
    }

    // 5. Mark As Paid Dialog
    donationForMarkPaid?.let { donation ->
        MarkAsPaidDialog(
            donation = donation,
            onConfirm = { paymentMode, paymentDate, notes ->
                viewModel.markDonationAsPaid(donation.id, paymentMode, paymentDate, notes) { updatedDonation ->
                    lastRecordedDonationForSuccessFeedback = updatedDonation
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Pledge marked as PAID! Receipt ${donation.receiptNo} generated.")
                }
                donationForMarkPaid = null
            },
            onDismiss = { donationForMarkPaid = null }
        )
    }

    // 5. Auth / Switch Committee Member Dialog
    if (showAuthMemberDialog) {
        AuthMemberDialog(
            members = members,
            currentMember = activeMember,
            isAdminMode = isAdminMode,
            onSelectMember = { member ->
                viewModel.setActiveMember(member)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Logged in as ${member.name} (${member.role})")
                }
            },
            onTogglePublicMode = { publicMode ->
                viewModel.setPublicMode(publicMode)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(if (publicMode) "Switched to Public Transparency View" else "Switched to Committee Admin Mode")
                }
            },
            onOpenFirebaseLogin = {
                showAuthMemberDialog = false
                currentScreen = AppScreen.LOGIN
            },
            onLogout = {
                showAuthMemberDialog = false
                societyPreferences.clearLoggedInUser()
                userViewModel.signOut()
                viewModel.logout()
                currentScreen = AppScreen.LOGIN
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Logged out successfully.")
                }
            },
            onDismiss = { showAuthMemberDialog = false }
        )
    }

    // 6. Delete Confirmation Dialog for Donation (prevents accidental data loss)
    donationToDelete?.let { donation ->
        DeleteDonationConfirmationDialog(
            donation = donation,
            onDismiss = { donationToDelete = null },
            onConfirmDelete = { reason ->
                if (isSuperAdmin) {
                    viewModel.superAdminDeleteDonation(donation, reason) { success, msg ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                } else {
                    viewModel.deleteDonation(donation)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Donation of ₹${donation.amount.toInt()} deleted successfully.")
                    }
                }
                donationToDelete = null
            }
        )
    }

    // 7. Delete Confirmation Dialog for Expense (prevents accidental data loss)
    expenseToDelete?.let { expense ->
        DeleteExpenseConfirmationDialog(
            expense = expense,
            onDismiss = { expenseToDelete = null },
            onConfirmDelete = { reason ->
                if (isSuperAdmin) {
                    viewModel.superAdminDeleteExpense(expense, reason) { success, msg ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                } else {
                    viewModel.deleteExpense(expense)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Expense of ₹${expense.amount.toInt()} deleted successfully.")
                    }
                }
                expenseToDelete = null
            }
        )
    }

    // 7b. Restricted Action Dialog for Standard Committee Members
    restrictedActionName?.let { action ->
        CommitteeRestrictedActionDialog(
            actionName = action,
            onDismiss = { restrictedActionName = null }
        )
    }

    // 8. Add / Edit Member Dialog (Admin Exclusive - Enabled only after login as admin)
    if ((showAddMemberDialog || editingMember != null) && ((isSuperAdmin && isAdminMode) || isMasterAdminAuthenticated)) {
        val target = editingMember
        AddEditMemberDialog(
            existingMember = target,
            onSave = { name, email, phone, role, pin, isSuperAdminRole, openingBalance, openingBalanceNotes ->
                if (target == null) {
                    viewModel.addMember(name, email, phone, role, pin, isSuperAdminRole, openingBalance, openingBalanceNotes)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Member '$name' added successfully!")
                    }
                } else {
                    viewModel.updateMember(
                        target.copy(
                            name = name,
                            email = email,
                            phone = phone,
                            role = role,
                            pin = pin,
                            isSuperAdmin = isSuperAdminRole
                        )
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Member '$name' updated successfully!")
                    }
                }
                showAddMemberDialog = false
                editingMember = null
            },
            onDismiss = {
                showAddMemberDialog = false
                editingMember = null
            }
        )
    }

    // 9. Delete Confirmation Dialog for Member (Super Admin Exclusive)
    if ((isSuperAdmin && isAdminMode) || isMasterAdminAuthenticated) {
        memberToDelete?.let { member ->
            AlertDialog(
            onDismissRequest = { memberToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text("Remove Committee Member") },
            text = {
                Text(
                    "Are you sure you want to remove ${member.name} (${member.role}) from the committee directory? " +
                    if (member.isSuperAdmin) "\n\nWarning: This member is an Admin." else ""
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMember(member)
                        memberToDelete = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Member ${member.name} removed.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { memberToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    }

    // 10. Edit Society & Bank Details Dialog (Super Admin Exclusive)
    if (showEditSocietyDialog) {
        EditSocietyInfoDialog(
            societyInfo = societyInfo,
            onDismiss = { showEditSocietyDialog = false },
            onSave = { updatedInfo ->
                viewModel.updateSocietyInfo(updatedInfo)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Society profile & bank details updated successfully!")
                }
            }
        )
    }

    // 11. Master Setup & Present Data Bulk Entry Dialog (Super Admin Exclusive)
    if (showMasterBulkSetupDialog) {
        MasterBulkDataEntryDialog(
            initialSocietyInfo = societyInfo,
            onDismiss = { showMasterBulkSetupDialog = false },
            onSaveAll = { updatedSocietyInfo, newDonations, newExpenses, replaceExisting ->
                viewModel.bulkSetupPresentData(
                    newSocietyInfo = updatedSocietyInfo,
                    presentDonations = newDonations,
                    presentExpenses = newExpenses,
                    replaceExistingData = replaceExisting
                )
                coroutineScope.launch {
                    val countMsg = "${newDonations.size} donations & ${newExpenses.size} expenses"
                    snackbarHostState.showSnackbar("Master Setup complete: $countMsg saved!")
                }
            }
        )
    }

    // 12. Language Selection Dialog (8 Indian Languages)
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = appLanguage,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { selectedLang ->
                viewModel.setAppLanguage(selectedLang)
                coroutineScope.launch {
                    val newStrings = AppTranslations.getStrings(selectedLang)
                    snackbarHostState.showSnackbar("${newStrings.languageChangedNotice}: ${selectedLang.nativeName}")
                }
            }
        )
    }

    // 13. Email Backup Dialog (CSV & Ledger Backup to Email)
    if (showEmailBackupDialog) {
        EmailBackupDialog(
            societyInfo = societyInfo,
            donations = allDonations,
            expenses = allExpenses,
            members = members,
            currentMember = activeMember,
            onDismiss = { showEmailBackupDialog = false }
        )
    }

    // 14. Change PIN & Super Admin Reset PIN Dialog
    if ((isSuperAdmin && isAdminMode) || isMasterAdminAuthenticated) {
        memberForPinChange?.let { (member, isSuperAdminReset) ->
            ChangePinDialog(
            member = member,
            isSuperAdminPerformingReset = isSuperAdminReset,
            onSaveNewPin = { newPin ->
                viewModel.changeMemberPin(member.id, newPin) { success ->
                    coroutineScope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("PIN updated successfully for ${member.name}")
                        } else {
                            snackbarHostState.showSnackbar("Failed to update PIN")
                        }
                    }
                }
                memberForPinChange = null
            },
            onResetToDefaultPin = {
                viewModel.resetMemberPin(member.id) { success ->
                    coroutineScope.launch {
                        if (success) {
                            snackbarHostState.showSnackbar("PIN reset to default for ${member.name}")
                        } else {
                            snackbarHostState.showSnackbar("Failed to reset PIN")
                        }
                    }
                }
                memberForPinChange = null
            },
            onDismiss = { memberForPinChange = null }
        )
    }
    }

    // 15. Financial Year Rollover Dialog (Continue to Next FY with Balances)
    if (showFinancialYearRolloverDialog) {
        val totalReceived = allDonations.filter { it.status == "received" }.sumOf { it.amount }
        val totalExpenses = allExpenses.sumOf { it.amount }
        FinancialYearRolloverDialog(
            societyInfo = societyInfo,
            totalReceived = totalReceived,
            totalExpenses = totalExpenses,
            onRolloverToNextFY = { nextFY, archiveTransactions ->
                viewModel.startNextFinancialYear(nextFY, archiveTransactions) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Successfully rolled over to $nextFY with carried forward reserve!")
                    }
                }
                showFinancialYearRolloverDialog = false
            },
            onDismiss = { showFinancialYearRolloverDialog = false }
        )
    }

    // 16. Monthly Category-wise Budget Cap Manager Dialog
    if (showBudgetDialog) {
        val expenseCategories = listOf(
            "Events & Festivals",
            "Maintenance",
            "Repairs",
            "Utilities",
            "Refreshments",
            "Admin & Legal",
            "Miscellaneous"
        )
        CategoryBudgetDialog(
            categories = expenseCategories,
            currentBudgets = categoryBudgets,
            expenses = allExpenses,
            onSaveBudget = { category, budgetAmount ->
                viewModel.setCategoryBudget(category, budgetAmount)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Budget set for $category: ₹${budgetAmount.toInt()}")
                }
            },
            onDismiss = { showBudgetDialog = false }
        )
    }

    // 17. Restore Society Database from JSON Backup Dialog
    if (showRestoreBackupDialog) {
        RestoreBackupDialog(
            onRestore = { jsonString, mergeMode ->
                viewModel.restoreDataFromJson(jsonString, mergeMode) { success, msg ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
                showRestoreBackupDialog = false
            },
            onDismiss = { showRestoreBackupDialog = false }
        )
    }

    // 18. Global Transaction Search Dialog (Member Name, Amount, Receipt ID)
    if (showGlobalSearchDialog) {
        com.example.ui.components.GlobalTransactionSearchDialog(
            donations = allDonations,
            expenses = allExpenses,
            members = members,
            societyInfo = societyInfo,
            onDismiss = { showGlobalSearchDialog = false },
            onViewDonationReceipt = { donation ->
                donationForReceipt = donation
            },
            onMarkDonationAsPaid = { donation ->
                donationForMarkPaid = donation
            }
        )
    }

    // 19. Double-Back Exit & Play Store Rating Dialog
    if (showPlayStoreRatingDialog) {
        var selectedStars by remember { mutableIntStateOf(5) }
        AlertDialog(
            onDismissRequest = { showPlayStoreRatingDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = IndianSaffron,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Enjoying Society Ledger?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your rating on Google Play Store helps communities across India adopt transparent and audited financial management.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(onClick = { selectedStars = i }) {
                                Icon(
                                    imageVector = if (i <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$i stars",
                                    tint = if (i <= selectedStars) IndianSaffron else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "$selectedStars / 5 Stars Selected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SapphireNavy)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val packageName = context.packageName
                        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(playStoreIntent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(webIntent)
                        }
                        showPlayStoreRatingDialog = false
                        (context as? Activity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("⭐ Rate on Play Store & Exit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showPlayStoreRatingDialog = false
                            (context as? Activity)?.finish()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exit App")
                    }
                    TextButton(
                        onClick = { showPlayStoreRatingDialog = false }
                    ) {
                        Text("Keep Using")
                    }
                }
            }
        )
    }
    }
}
