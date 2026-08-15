package com.example.quizprototype_phy_che_deepseek

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object SecurityManager {
    private const val PREFS_NAME = "app_activation_prefs"
    private const val KEY_ACTIVATED = "is_activated"
    private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"
    private const val KEY_FIRST_LAUNCH = "first_launch_date"

    fun isPrivacyAccepted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false)
    }

    fun setPrivacyAccepted(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, true).apply()
    }
    private const val TRIAL_DAYS = 7
    
    // هذا هو "الملح" السري الخاص بك كأستاذ. لا تغيره بعد توزيع التطبيق.
    private const val SECRET_SALT = "PHYSICS_CHEM_ALGERIA_2024"

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId.take(8).uppercase()
    }

    fun generateActivationKey(deviceId: String): String {
        val raw = deviceId + SECRET_SALT
        val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        // نأخذ 6 رموز فقط لتسهيل الكتابة على التلميذ
        return bytes.joinToString("") { "%02x".format(it) }.take(6).uppercase()
    }

    fun isActivated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ACTIVATED, false)
    }

    fun setActivated(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ACTIVATED, true).apply()
    }

    fun getTrialInfo(context: Context): Pair<Int, Int> { // (الأيام المتبقية، الأيام الكلية)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        
        if (firstLaunch == 0L) {
            firstLaunch = System.currentTimeMillis()
            prefs.edit().putLong(KEY_FIRST_LAUNCH, firstLaunch).apply()
        }

        val currentTime = System.currentTimeMillis()
        val diffInMillies = currentTime - firstLaunch
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies).toInt()
        
        val remaining = (TRIAL_DAYS - diffInDays).coerceAtLeast(0)
        return remaining to TRIAL_DAYS
    }
}
