package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository
    private val sessionManager: SessionManager

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _signupState = MutableStateFlow<AuthState>(AuthState.Idle)
    val signupState: StateFlow<AuthState> = _signupState

    init {
        val database = AppDatabase.getDatabase(application)
        authRepository = AuthRepository(database.authDao())
        sessionManager = SessionManager.getInstance(application)
    }

    fun login(email: String, password: String) {
        if (email.trim().isEmpty() || password.isEmpty()) {
            _loginState.value = AuthState.Error("Email and Password are required")
            return
        }

        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.authenticateUser(email, password)
                .onSuccess { user ->
                    sessionManager.login(user.email)
                    _loginState.value = AuthState.Success(user.email)
                }
                .onFailure { error ->
                    _loginState.value = AuthState.Error(error.message ?: "Authentication failed")
                }
        }
    }

    fun signup(fullName: String, email: String, password: String) {
        _signupState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.registerUser(fullName, email, password)
                .onSuccess {
                    sessionManager.login(email.trim().lowercase())
                    _signupState.value = AuthState.Success(email.trim().lowercase())
                }
                .onFailure { error ->
                    _signupState.value = AuthState.Error(error.message ?: "Registration failed")
                }
        }
    }

    fun loginWithGoogle(email: String, fullName: String) {
        if (email.trim().isEmpty()) {
            _loginState.value = AuthState.Error("Email is required")
            return
        }

        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.authenticateOrRegisterGoogleUser(email, fullName)
                .onSuccess { user ->
                    sessionManager.login(user.email)
                    _loginState.value = AuthState.Success(user.email)
                }
                .onFailure { error ->
                    _loginState.value = AuthState.Error(error.message ?: "Google authentication failed")
                }
        }
    }

    fun resetStates() {
        _loginState.value = AuthState.Idle
        _signupState.value = AuthState.Idle
    }

    sealed interface AuthState {
        object Idle : AuthState
        object Loading : AuthState
        data class Success(val email: String) : AuthState
        data class Error(val message: String) : AuthState
    }
}
