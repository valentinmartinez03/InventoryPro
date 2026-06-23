package com.example.inventorystock.data

import android.content.Context
import androidx.core.content.edit

/**
 * Clase encargada de gestionar las preferencias compartidas de la aplicación.
 * Centraliza el acceso a los datos persistentes simples.
 */
class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_FINISHED = "onboarding_finished"
    }

    /**
     * Indica si el usuario ha completado las pantallas de bienvenida.
     */
    var onboardingFinished: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_FINISHED, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_FINISHED, value) }
}
