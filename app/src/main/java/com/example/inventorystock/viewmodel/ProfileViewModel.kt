package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.inventorystock.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val email: String = "",
    val name: String = "",
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = authRepository.currentUser
        val email = user?.email ?: "No identificado"
        val name = email.split("@").getOrNull(0) ?: "Usuario"
        
        _uiState.value = ProfileUiState(
            email = email,
            name = name
        )
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }
}
