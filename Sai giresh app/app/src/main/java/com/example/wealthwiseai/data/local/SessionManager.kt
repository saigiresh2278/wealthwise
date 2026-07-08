package com.example.wealthwiseai.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("wealthwise_session_prefs", Context.MODE_PRIVATE)

    private val _userEmail = MutableStateFlow<String?>(prefs.getString(KEY_USER_EMAIL, null))
    val userEmail: StateFlow<String?> = _userEmail

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    fun login(email: String) {
        prefs.edit().apply {
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
        _userEmail.value = email
        _isLoggedIn.value = true
    }

    fun logout() {
        prefs.edit().apply {
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
        _userEmail.value = null
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
