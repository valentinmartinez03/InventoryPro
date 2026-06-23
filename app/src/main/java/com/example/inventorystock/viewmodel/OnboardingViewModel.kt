package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.inventorystock.data.PreferenceManager

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val preferenceManager = PreferenceManager(application)

    fun completeOnboarding() {
        preferenceManager.onboardingFinished = true
    }
}
