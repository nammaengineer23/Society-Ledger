package com.example.data.model

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val shortBadge: String,
    val scriptSample: String,
    val region: String,
    val isEssential: Boolean = false
) {
    KANNADA(
        code = "kn",
        nativeName = "ಕನ್ನಡ",
        englishName = "Kannada",
        shortBadge = "ಕ",
        scriptSample = "ನಮಸ್ಕಾರ",
        region = "Karnataka (Essential)",
        isEssential = true
    ),
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        shortBadge = "EN",
        scriptSample = "Hello",
        region = "All India / Universal",
        isEssential = true
    ),
    HINDI(
        code = "hi",
        nativeName = "हिंदी",
        englishName = "Hindi",
        shortBadge = "हि",
        scriptSample = "नमस्ते",
        region = "North & Central India",
        isEssential = true
    ),
    MARATHI(
        code = "mr",
        nativeName = "मराठी",
        englishName = "Marathi",
        shortBadge = "म",
        scriptSample = "नमस्कार",
        region = "Maharashtra",
        isEssential = false
    ),
    GUJARATI(
        code = "gu",
        nativeName = "ગુજરાતી",
        englishName = "Gujarati",
        shortBadge = "ગુ",
        scriptSample = "નમસ્તે",
        region = "Gujarat",
        isEssential = false
    ),
    BENGALI(
        code = "bn",
        nativeName = "বাংলা",
        englishName = "Bengali",
        shortBadge = "বা",
        scriptSample = "নমস্কার",
        region = "West Bengal & Tripura",
        isEssential = false
    ),
    TAMIL(
        code = "ta",
        nativeName = "தமிழ்",
        englishName = "Tamil",
        shortBadge = "த",
        scriptSample = "வணக்கம்",
        region = "Tamil Nadu & Puducherry",
        isEssential = false
    ),
    TELUGU(
        code = "te",
        nativeName = "తెలుగు",
        englishName = "Telugu",
        shortBadge = "తె",
        scriptSample = "నమస్కారం",
        region = "Andhra Pradesh & Telangana",
        isEssential = false
    );

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }

        fun getEssentialLanguages(): List<AppLanguage> {
            return entries.filter { it.isEssential }
        }

        fun getRegionalLanguages(): List<AppLanguage> {
            return entries.filter { !it.isEssential }
        }
    }
}

