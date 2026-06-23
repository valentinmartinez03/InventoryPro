package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventorystock.data.AuthRepository
import com.example.inventorystock.data.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa el estado único de la interfaz de usuario para la pantalla de inicio/login.
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val preferenceManager = PreferenceManager(application)

    sealed class Destination {
        object Onboarding : Destination()
        object Login : Destination()
        object Dashboard : Destination()
    }

    private val _startDestination = MutableStateFlow<Destination?>(null)
    val startDestination: StateFlow<Destination?> = _startDestination

    // Fuente única de verdad para el estado de la UI
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkDestination()
    }

    fun checkDestination() {
        val onboardingFinished = preferenceManager.onboardingFinished
        
        _startDestination.value = when {
            !onboardingFinished -> Destination.Onboarding
            authRepository.currentUser != null -> Destination.Dashboard
            else -> Destination.Login
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, completa todos los campos") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            val result = authRepository.login(email, pass)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = e.message ?: "Error al iniciar sesión") 
                }
            }
        }
    }

    fun resetPassword(email: String, onSent: () -> Unit) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu correo para recuperar la contraseña") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            _uiState.update { it.copy(isLoading = false) }
            result.onSuccess {
                onSent()
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(errorMessage = e.message ?: "Error al enviar correo") 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
