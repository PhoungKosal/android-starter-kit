package com.t3r.android_starter_kit.core.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.t3r.android_starter_kit.R

/**
 * Manages in-app locale switching using Jetpack's AppCompat locale backport.
 * Persists the user's locale choice across app restarts automatically.
 */
object LocaleManager {

    enum class AppLocale(
        val tag: String,
        val displayNameRes: Int,
        val flag: String,
    ) {
        ENGLISH("en", R.string.language_english, "\uD83C\uDDEC\uD83C\uDDE7"),
        KHMER("km", R.string.language_khmer, "\uD83C\uDDF0\uD83C\uDDED"),
        CHINESE("zh", R.string.language_chinese, "\uD83C\uDDE8\uD83C\uDDF3");

        companion object {
            fun fromTag(tag: String?): AppLocale =
                entries.firstOrNull { it.tag == tag } ?: ENGLISH
        }
    }

    fun setLocale(locale: AppLocale) {
        val localeList = LocaleListCompat.forLanguageTags(locale.tag)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getCurrentLocale(): AppLocale {
        val current = AppCompatDelegate.getApplicationLocales()
        if (current.isEmpty) return AppLocale.ENGLISH
        val tag = current[0]?.language ?: "en"
        return AppLocale.entries.firstOrNull { it.tag == tag } ?: AppLocale.ENGLISH
    }
}
