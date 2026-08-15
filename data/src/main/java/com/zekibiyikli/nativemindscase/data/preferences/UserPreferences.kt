package com.zekibiyikli.nativemindscase.data.preferences

data class UserPreferences(
    val darkThemeEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val userId: String? = null
)
