package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.SocietyInfo

class SocietyPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSocietyInfo(): SocietyInfo {
        // Retrieve values without pre-populated fake demo details
        var email = prefs.getString(KEY_ADMIN_EMAIL, "") ?: ""
        if (email.equals("societyledger@gmail.com", ignoreCase = true)) email = ""

        var phone = prefs.getString(KEY_PHONE, "") ?: ""
        if (phone == "9986385925" || phone == "9876543210") phone = ""

        var contactEmail = prefs.getString(KEY_EMAIL, "") ?: ""
        if (contactEmail.equals("contact@greenvalleysociety.org", ignoreCase = true)) contactEmail = ""

        var regNo = prefs.getString(KEY_REG_NO, "") ?: ""
        if (regNo == "RWA/ND/2024/0982") regNo = ""

        var upi = prefs.getString(KEY_UPI_ID, "") ?: ""
        if (upi == "greenvalleysociety@upi") upi = ""

        var bName = prefs.getString(KEY_BANK_NAME, "") ?: ""
        if (bName == "HDFC Bank") bName = ""

        var accNo = prefs.getString(KEY_ACCOUNT_NO, "") ?: ""
        if (accNo == "50100492837192") accNo = ""

        var ifsc = prefs.getString(KEY_IFSC, "") ?: ""
        if (ifsc == "HDFC0001234") ifsc = ""

        var branch = prefs.getString(KEY_BRANCH, "") ?: ""
        if (branch == "Connaught Place, New Delhi") branch = ""

        var bAccount = prefs.getString(KEY_BANK_ACCOUNT, "") ?: ""
        if (bAccount.contains("50100492837192")) bAccount = ""

        var addr = prefs.getString(KEY_ADDRESS, "") ?: ""
        if (addr.contains("Block C & D, Green Valley Enclave", ignoreCase = true)) addr = ""

        var city = prefs.getString(KEY_CITY, "") ?: ""
        if (city == "New Delhi") city = ""

        var state = prefs.getString(KEY_STATE, "") ?: ""
        if (state == "Delhi") state = ""

        var pinCode = prefs.getString(KEY_PIN_CODE, "") ?: ""
        if (pinCode == "110048") pinCode = ""

        var presName = prefs.getString(KEY_PRESIDENT, "") ?: ""
        if (presName == "Ramesh Sharma") presName = ""

        var secName = prefs.getString(KEY_SECRETARY, "") ?: ""
        if (secName == "Suresh Gupta") secName = ""

        var tresName = prefs.getString(KEY_TREASURER, "") ?: ""
        if (tresName == "Vikas Patel") tresName = ""

        var secGate = prefs.getString(KEY_SEC_GATE, "") ?: ""
        if (secGate == "9810011223") secGate = ""

        var maintLead = prefs.getString(KEY_MAINT_LEAD, "") ?: ""
        if (maintLead == "9876543210") maintLead = ""

        var plumber = prefs.getString(KEY_PLUMBER, "") ?: ""
        if (plumber == "9123456780") plumber = ""

        var emergency = prefs.getString(KEY_EMERGENCY, "") ?: ""
        if (emergency == "112") emergency = ""

        var sName = prefs.getString(KEY_NAME, "") ?: ""
        if (sName.contains("Green Valley", ignoreCase = true) || sName.contains("Royal Palms", ignoreCase = true) || sName.contains("Shree Ganesh", ignoreCase = true) || sName.contains("Sunrise Heights", ignoreCase = true)) sName = ""

        return SocietyInfo(
            name = sName,
            registrationNo = regNo,
            upiId = upi,
            bankName = bName,
            accountNumber = accNo,
            ifscCode = ifsc,
            branchName = branch,
            bankAccount = bAccount,
            address = addr,
            city = city,
            state = state,
            pinCode = pinCode,
            contactEmail = contactEmail,
            adminEmail = email,
            contactPhone = phone,
            financialYear = prefs.getString(KEY_FY, "FY 2026-2027") ?: "FY 2026-2027",
            openingBalance = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_OPENING_BALANCE, java.lang.Double.doubleToLongBits(0.0))),
            presidentName = presName,
            secretaryName = secName,
            treasurerName = tresName,
            securityGatePhone = secGate,
            maintenanceLeadPhone = maintLead,
            plumberPhone = plumber,
            emergencyHelplinePhone = emergency
        )
    }

    fun saveSocietyInfo(info: SocietyInfo) {
        prefs.edit().apply {
            putString(KEY_NAME, info.name)
            putString(KEY_REG_NO, info.registrationNo)
            putString(KEY_UPI_ID, info.upiId)
            putString(KEY_BANK_NAME, info.bankName)
            putString(KEY_ACCOUNT_NO, info.accountNumber)
            putString(KEY_IFSC, info.ifscCode)
            putString(KEY_BRANCH, info.branchName)
            putString(KEY_BANK_ACCOUNT, info.fullBankDetails)
            putString(KEY_ADDRESS, info.address)
            putString(KEY_CITY, info.city)
            putString(KEY_STATE, info.state)
            putString(KEY_PIN_CODE, info.pinCode)
            putString(KEY_EMAIL, info.contactEmail)
            putString(KEY_ADMIN_EMAIL, info.adminEmail)
            putString(KEY_PHONE, info.contactPhone)
            putString(KEY_FY, info.financialYear)
            putLong(KEY_OPENING_BALANCE, java.lang.Double.doubleToLongBits(info.openingBalance))
            putString(KEY_PRESIDENT, info.presidentName)
            putString(KEY_SECRETARY, info.secretaryName)
            putString(KEY_TREASURER, info.treasurerName)
            putString(KEY_SEC_GATE, info.securityGatePhone)
            putString(KEY_MAINT_LEAD, info.maintenanceLeadPhone)
            putString(KEY_PLUMBER, info.plumberPhone)
            putString(KEY_EMERGENCY, info.emergencyHelplinePhone)
            apply()
        }
    }

    fun deleteSocietyInfo() {
        clearAllSocietyData()
    }

    fun getAppLanguage(): com.example.data.model.AppLanguage {
        val code = prefs.getString(KEY_APP_LANGUAGE, com.example.data.model.AppLanguage.ENGLISH.code) ?: com.example.data.model.AppLanguage.ENGLISH.code
        return com.example.data.model.AppLanguage.fromCode(code)
    }

    fun saveAppLanguage(language: com.example.data.model.AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language.code).apply()
    }

    fun getSuperAdminPin(): String {
        return prefs.getString(KEY_SUPER_ADMIN_PIN, "") ?: ""
    }

    fun saveSuperAdminPin(pin: String) {
        prefs.edit().putString(KEY_SUPER_ADMIN_PIN, pin.trim()).apply()
    }

    fun getAppAdminPin(): String {
        return prefs.getString(KEY_APP_ADMIN_PIN, "") ?: ""
    }

    fun isAppAdminPinConfigured(): Boolean {
        return prefs.getBoolean(KEY_APP_ADMIN_PIN_CONFIGURED, false)
    }

    fun setAppAdminPinConfigured(configured: Boolean) {
        prefs.edit().putBoolean(KEY_APP_ADMIN_PIN_CONFIGURED, configured).apply()
    }

    fun saveAppAdminPin(pin: String) {
        val clean = pin.trim()
        val editor = prefs.edit().putString(KEY_APP_ADMIN_PIN, clean)
        if (clean.isNotBlank()) {
            editor.putBoolean(KEY_APP_ADMIN_PIN_CONFIGURED, true)
        }
        editor.apply()
    }

    fun saveSocietyPin(societyId: String, pin: String) {
        prefs.edit().putString("society_pin_$societyId", pin).apply()
    }

    fun removeSocietyPin(societyId: String) {
        prefs.edit().remove("society_pin_$societyId").apply()
    }

    fun getSocietyPin(societyId: String): String? {
        return prefs.getString("society_pin_$societyId", null)
    }

    fun hasCompletedInitialDemoSeed(): Boolean {
        return prefs.getBoolean("has_completed_initial_demo_seed", false)
    }

    fun markInitialDemoSeedCompleted() {
        prefs.edit().putBoolean("has_completed_initial_demo_seed", true).apply()
    }

    fun getTrialStartTime(): Long {
        val stored = prefs.getLong(KEY_TRIAL_START_TIME, 0L)
        if (stored == 0L) {
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_TRIAL_START_TIME, now).apply()
            return now
        }
        return stored
    }

    fun isProSubscriber(): Boolean {
        return prefs.getBoolean(KEY_IS_PRO_SUBSCRIBER, false)
    }

    fun getSubscriptionExpiryTime(): Long {
        return prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0L)
    }

    fun getLastPaymentTransactionId(): String {
        return prefs.getString(KEY_LAST_PAYMENT_TXN_ID, "") ?: ""
    }

    fun saveSubscriptionDetails(isPro: Boolean, expiryTime: Long, transactionId: String) {
        prefs.edit()
            .putBoolean(KEY_IS_PRO_SUBSCRIBER, isPro)
            .putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTime)
            .putString(KEY_LAST_PAYMENT_TXN_ID, transactionId)
            .apply()
    }

    fun getRazorpayKeyId(): String {
        return prefs.getString(KEY_RAZORPAY_KEY_ID, "rzp_live_societyledger2026") ?: "rzp_live_societyledger2026"
    }

    fun saveRazorpayKeyId(keyId: String) {
        prefs.edit().putString(KEY_RAZORPAY_KEY_ID, keyId.trim()).apply()
    }

    fun isRegisteredSociety(): Boolean {
        return prefs.getBoolean(KEY_SOCIETY_REGISTERED, true)
    }

    fun setSocietyRegistered(registered: Boolean) {
        prefs.edit().putBoolean(KEY_SOCIETY_REGISTERED, registered).apply()
    }

    // Global App Broadcast Announcement Banner
    fun getGlobalAnnouncement(): String {
        return prefs.getString(KEY_GLOBAL_ANNOUNCEMENT, "Welcome to Society Ledger! Completely free transparency portal for all societies.") ?: ""
    }

    fun isGlobalAnnouncementEnabled(): Boolean {
        return prefs.getBoolean(KEY_GLOBAL_ANNOUNCEMENT_ENABLED, true)
    }

    fun saveGlobalAnnouncement(message: String, enabled: Boolean) {
        prefs.edit()
            .putString(KEY_GLOBAL_ANNOUNCEMENT, message.trim())
            .putBoolean(KEY_GLOBAL_ANNOUNCEMENT_ENABLED, enabled)
            .apply()
    }

    // Society Specific Subscription Overrides
    fun isSocietyPro(societyId: String): Boolean {
        return prefs.getBoolean("soc_pro_$societyId", false)
    }

    fun getSocietySubscriptionExpiry(societyId: String): Long {
        return prefs.getLong("soc_expiry_$societyId", 0L)
    }

    fun saveSocietySubscriptionOverride(societyId: String, isPro: Boolean, expiryTime: Long, txnId: String) {
        prefs.edit()
            .putBoolean("soc_pro_$societyId", isPro)
            .putLong("soc_expiry_$societyId", expiryTime)
            .putString("soc_txn_$societyId", txnId)
            .apply()
    }

    // App Master PIN & Security Lock
    fun isAppLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
    }

    fun getAppLockPin(): String {
        return prefs.getString(KEY_APP_LOCK_PIN, "") ?: ""
    }

    fun setAppLockPin(pin: String) {
        prefs.edit().putString(KEY_APP_LOCK_PIN, pin.trim()).apply()
    }

    // Material 3 Theme Preference (Light / Dark Mode)
    fun isDarkTheme(): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_THEME, false)
    }

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_THEME, isDark).apply()
    }

    // Category-wise Monthly Budget Caps
    fun getCategoryBudget(category: String): Double {
        val bits = prefs.getLong("budget_${category.lowercase().replace(" ", "_")}", 0L)
        return java.lang.Double.longBitsToDouble(bits)
    }

    fun setCategoryBudget(category: String, amount: Double) {
        val bits = java.lang.Double.doubleToLongBits(amount)
        prefs.edit().putLong("budget_${category.lowercase().replace(" ", "_")}", bits).apply()
    }

    fun getAllCategoryBudgets(categories: List<String>): Map<String, Double> {
        return categories.associateWith { getCategoryBudget(it) }
    }

    fun getAllSocieties(): List<SocietyInfo> {
        val jsonStr = prefs.getString("all_saved_societies", null) ?: return listOf(getSocietyInfo())
        return try {
            val jsonArray = org.json.JSONArray(jsonStr)
            val list = mutableListOf<SocietyInfo>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    SocietyInfo(
                        name = obj.optString("name", "Residents Welfare Society"),
                        registrationNo = obj.optString("registrationNo", ""),
                        upiId = obj.optString("upiId", ""),
                        bankName = obj.optString("bankName", ""),
                        accountNumber = obj.optString("accountNumber", ""),
                        ifscCode = obj.optString("ifscCode", ""),
                        branchName = obj.optString("branchName", ""),
                        bankAccount = obj.optString("bankAccount", ""),
                        address = obj.optString("address", ""),
                        city = obj.optString("city", ""),
                        state = obj.optString("state", ""),
                        pinCode = obj.optString("pinCode", ""),
                        contactEmail = obj.optString("contactEmail", "").takeIf { !it.equals("contact@greenvalleysociety.org", ignoreCase = true) } ?: "",
                        adminEmail = obj.optString("adminEmail", "").takeIf { !it.equals("societyledger@gmail.com", ignoreCase = true) } ?: "",
                        contactPhone = obj.optString("contactPhone", "").takeIf { it != "9986385925" && it != "9876543210" } ?: "",
                        financialYear = obj.optString("financialYear", "FY 2026-2027"),
                        openingBalance = obj.optDouble("openingBalance", 0.0),
                        presidentName = obj.optString("presidentName", ""),
                        secretaryName = obj.optString("secretaryName", ""),
                        treasurerName = obj.optString("treasurerName", ""),
                        securityGatePhone = obj.optString("securityGatePhone", ""),
                        maintenanceLeadPhone = obj.optString("maintenanceLeadPhone", ""),
                        plumberPhone = obj.optString("plumberPhone", ""),
                        emergencyHelplinePhone = obj.optString("emergencyHelplinePhone", "")
                    )
                )
            }
            if (list.isEmpty()) listOf(getSocietyInfo()) else list
        } catch (e: Exception) {
            listOf(getSocietyInfo())
        }
    }

    fun saveAllSocieties(societies: List<SocietyInfo>) {
        val jsonArray = org.json.JSONArray()
        societies.forEach { s ->
            val obj = org.json.JSONObject().apply {
                put("name", s.name)
                put("registrationNo", s.registrationNo)
                put("upiId", s.upiId)
                put("bankName", s.bankName)
                put("accountNumber", s.accountNumber)
                put("ifscCode", s.ifscCode)
                put("branchName", s.branchName)
                put("bankAccount", s.bankAccount)
                put("address", s.address)
                put("city", s.city)
                put("state", s.state)
                put("pinCode", s.pinCode)
                put("contactEmail", s.contactEmail)
                put("adminEmail", s.adminEmail)
                put("contactPhone", s.contactPhone)
                put("financialYear", s.financialYear)
                put("openingBalance", s.openingBalance)
                put("presidentName", s.presidentName)
                put("secretaryName", s.secretaryName)
                put("treasurerName", s.treasurerName)
                put("securityGatePhone", s.securityGatePhone)
                put("maintenanceLeadPhone", s.maintenanceLeadPhone)
                put("plumberPhone", s.plumberPhone)
                put("emergencyHelplinePhone", s.emergencyHelplinePhone)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("all_saved_societies", jsonArray.toString()).apply()
    }

    fun getSavedDirectorySocieties(): List<com.example.data.model.Society> {
        val jsonStr = prefs.getString("all_saved_directory_societies_v2", null) ?: return emptyList()
        return try {
            val jsonArray = org.json.JSONArray(jsonStr)
            val list = mutableListOf<com.example.data.model.Society>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    com.example.data.model.Society(
                        id = obj.optString("id", "soc_${System.currentTimeMillis()}"),
                        name = obj.optString("name", "Residents Welfare Society"),
                        registrationNo = obj.optString("registrationNo", ""),
                        upiId = obj.optString("upiId", ""),
                        bankName = obj.optString("bankName", ""),
                        accountNumber = obj.optString("accountNumber", ""),
                        ifscCode = obj.optString("ifscCode", ""),
                        branchName = obj.optString("branchName", ""),
                        bankAccount = obj.optString("bankAccount", ""),
                        address = obj.optString("address", ""),
                        city = obj.optString("city", ""),
                        state = obj.optString("state", ""),
                        pinCode = obj.optString("pinCode", ""),
                        contactEmail = obj.optString("contactEmail", "").takeIf { !it.equals("contact@greenvalleysociety.org", ignoreCase = true) } ?: "",
                        adminEmail = obj.optString("adminEmail", "").takeIf { !it.equals("societyledger@gmail.com", ignoreCase = true) } ?: "",
                        contactPhone = obj.optString("contactPhone", "").takeIf { it != "9986385925" && it != "9876543210" } ?: "",
                        financialYear = obj.optString("financialYear", "FY 2026-2027"),
                        openingBalance = obj.optDouble("openingBalance", 0.0),
                        presidentName = obj.optString("presidentName", ""),
                        secretaryName = obj.optString("secretaryName", ""),
                        treasurerName = obj.optString("treasurerName", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDirectorySocieties(societies: List<com.example.data.model.Society>) {
        val jsonArray = org.json.JSONArray()
        societies.forEach { s ->
            val obj = org.json.JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("registrationNo", s.registrationNo)
                put("upiId", s.upiId)
                put("bankName", s.bankName)
                put("accountNumber", s.accountNumber)
                put("ifscCode", s.ifscCode)
                put("branchName", s.branchName)
                put("bankAccount", s.bankAccount)
                put("address", s.address)
                put("city", s.city)
                put("state", s.state)
                put("pinCode", s.pinCode)
                put("contactEmail", s.contactEmail)
                put("adminEmail", s.adminEmail)
                put("contactPhone", s.contactPhone)
                put("financialYear", s.financialYear)
                put("openingBalance", s.openingBalance)
                put("presidentName", s.presidentName)
                put("secretaryName", s.secretaryName)
                put("treasurerName", s.treasurerName)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("all_saved_directory_societies_v2", jsonArray.toString()).apply()
    }

    fun hasShownRatingDialog(): Boolean {
        return prefs.getBoolean(KEY_HAS_SHOWN_RATING, false)
    }

    fun setHasShownRatingDialog(shown: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SHOWN_RATING, shown).apply()
    }

    fun hasSyncedCloudData(): Boolean {
        return prefs.getBoolean(KEY_HAS_SYNCED_CLOUD_DATA, false)
    }

    fun setHasSyncedCloudData(synced: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SYNCED_CLOUD_DATA, synced).apply()
    }

    fun getLoggedInUserId(): String {
        return prefs.getString(KEY_LOGGED_IN_USER_ID, "") ?: ""
    }

    fun getLoggedInUserEmail(): String {
        return prefs.getString(KEY_LOGGED_IN_USER_EMAIL, "") ?: ""
    }

    fun getLoggedInUserName(): String {
        return prefs.getString(KEY_LOGGED_IN_USER_NAME, "") ?: ""
    }

    fun getLoggedInUserRole(): String {
        return prefs.getString(KEY_LOGGED_IN_USER_ROLE, "") ?: ""
    }

    fun getLoggedInUserPhone(): String {
        return prefs.getString(KEY_LOGGED_IN_USER_PHONE, "") ?: ""
    }

    fun isUserLoggedIn(): Boolean {
        return getLoggedInUserId().isNotBlank() || getLoggedInUserEmail().isNotBlank() || getLoggedInUserPhone().isNotBlank()
    }

    fun saveLoggedInUser(userId: String, email: String, name: String, role: String, phone: String = "") {
        prefs.edit()
            .putString(KEY_LOGGED_IN_USER_ID, userId)
            .putString(KEY_LOGGED_IN_USER_EMAIL, email)
            .putString(KEY_LOGGED_IN_USER_NAME, name)
            .putString(KEY_LOGGED_IN_USER_ROLE, role)
            .putString(KEY_LOGGED_IN_USER_PHONE, phone)
            .apply()
    }

    fun clearLoggedInUser() {
        prefs.edit()
            .remove(KEY_LOGGED_IN_USER_ID)
            .remove(KEY_LOGGED_IN_USER_EMAIL)
            .remove(KEY_LOGGED_IN_USER_NAME)
            .remove(KEY_LOGGED_IN_USER_ROLE)
            .remove(KEY_LOGGED_IN_USER_PHONE)
            .apply()
    }

    fun clearAllSocietyData() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "society_management_prefs"
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_LOGGED_IN_USER_EMAIL = "logged_in_user_email"
        private const val KEY_LOGGED_IN_USER_NAME = "logged_in_user_name"
        private const val KEY_LOGGED_IN_USER_ROLE = "logged_in_user_role"
        private const val KEY_LOGGED_IN_USER_PHONE = "logged_in_user_phone"
        private const val KEY_HAS_SYNCED_CLOUD_DATA = "society_has_synced_cloud_data"
        private const val KEY_APP_LANGUAGE = "society_app_language"
        private const val KEY_NAME = "society_name"
        private const val KEY_REG_NO = "society_reg_no"
        private const val KEY_UPI_ID = "society_upi_id"
        private const val KEY_BANK_NAME = "society_bank_name"
        private const val KEY_ACCOUNT_NO = "society_account_no"
        private const val KEY_IFSC = "society_ifsc"
        private const val KEY_BRANCH = "society_branch"
        private const val KEY_BANK_ACCOUNT = "society_bank_account"
        private const val KEY_ADDRESS = "society_address"
        private const val KEY_CITY = "society_city"
        private const val KEY_STATE = "society_state"
        private const val KEY_PIN_CODE = "society_pin_code"
        private const val KEY_EMAIL = "society_email"
        private const val KEY_ADMIN_EMAIL = "society_admin_email"
        private const val KEY_PHONE = "society_phone"
        private const val KEY_FY = "society_financial_year"
        private const val KEY_OPENING_BALANCE = "society_opening_balance"
        private const val KEY_PRESIDENT = "society_president"
        private const val KEY_SECRETARY = "society_secretary"
        private const val KEY_TREASURER = "society_treasurer"
        private const val KEY_SEC_GATE = "society_sec_gate"
        private const val KEY_MAINT_LEAD = "society_maint_lead"
        private const val KEY_PLUMBER = "society_plumber"
        private const val KEY_EMERGENCY = "society_emergency"
        private const val KEY_SUPER_ADMIN_PIN = "super_admin_pin"
        private const val KEY_APP_ADMIN_PIN = "society_app_admin_pin"
        private const val KEY_APP_ADMIN_PIN_CONFIGURED = "society_app_admin_pin_configured"
        private const val KEY_SOCIETY_REGISTERED = "is_society_registered"
        private const val KEY_TRIAL_START_TIME = "society_trial_start_time"
        private const val KEY_IS_PRO_SUBSCRIBER = "society_is_pro_subscriber"
        private const val KEY_SUBSCRIPTION_EXPIRY = "society_subscription_expiry"
        private const val KEY_LAST_PAYMENT_TXN_ID = "society_last_payment_txn_id"
        private const val KEY_RAZORPAY_KEY_ID = "society_razorpay_key_id"
        private const val KEY_GLOBAL_ANNOUNCEMENT = "society_global_announcement"
        private const val KEY_GLOBAL_ANNOUNCEMENT_ENABLED = "society_global_announcement_enabled"
        private const val KEY_APP_LOCK_ENABLED = "society_app_lock_enabled"
        private const val KEY_APP_LOCK_PIN = "society_app_lock_pin"
        private const val KEY_IS_DARK_THEME = "society_is_dark_theme"
        private const val KEY_HAS_SHOWN_RATING = "has_shown_rating_dialog"
    }
}
