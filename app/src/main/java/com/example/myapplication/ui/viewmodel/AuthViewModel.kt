package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context) : ViewModel() {
    private val preferences: SharedPreferences = context.getSharedPreferences("briefy_prefs", Context.MODE_PRIVATE)

    private val _isAuthenticated = MutableStateFlow(isUserAuthenticated())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _authToken = MutableStateFlow<String?>(getStoredAuthToken())
    val authToken: StateFlow<String?> = _authToken

    private fun isUserAuthenticated(): Boolean {
        return !getStoredAuthToken().isNullOrEmpty()
    }

    private fun getStoredAuthToken(): String? {
        return preferences.getString(AUTH_TOKEN_KEY, null)
    }

    fun saveAuthToken(token: String) {
        preferences.edit().putString(AUTH_TOKEN_KEY, token).apply()
        _authToken.value = token
        _isAuthenticated.value = true
    }

    fun logout() {
        preferences.edit().remove(AUTH_TOKEN_KEY).apply()
        _authToken.value = null
        _isAuthenticated.value = false
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
    }
}