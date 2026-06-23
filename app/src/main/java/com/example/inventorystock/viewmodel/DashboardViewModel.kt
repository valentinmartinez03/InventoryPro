package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventorystock.data.InventoryRepository
import com.example.inventorystock.data.local.AppDatabase
import com.example.inventorystock.data.model.InventoryMovement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalCount: Int = 0,
    val highStockCount: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val movements: List<InventoryMovement> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val productDao = AppDatabase.getDatabase(application).productDao()
    private val repository = InventoryRepository(productDao)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            repository.getProducts().collect { products ->
                _uiState.update { currentState ->
                    currentState.copy(
                        totalCount = products.size,
                        highStockCount = products.count { it.stock > 10 },
                        lowStockCount = products.count { it.stock in 1..10 },
                        outOfStockCount = products.count { it.stock == 0 },
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.getRecentMovements().collect { movementsList ->
                _uiState.update { it.copy(movements = movementsList) }
            }
        }
    }
}
