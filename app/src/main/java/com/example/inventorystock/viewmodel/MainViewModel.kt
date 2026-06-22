package com.example.inventorystock.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    sealed class Destination {
        object Onboarding : Destination()
        object Login : Destination()
        object Dashboard : Destination()
    }

    private val _startDestination = MutableStateFlow<Destination?>(null)
    val startDestination: StateFlow<Destination?> = _startDestination

    init {
        checkDestination()
    }

    fun checkDestination() {
        val onboardingFinished = prefs.getBoolean("onboarding_finished", false)
        
        _startDestination.value = when {
            !onboardingFinished -> Destination.Onboarding
            auth.currentUser != null -> Destination.Dashboard
            else -> Destination.Login
        }
    }
}
