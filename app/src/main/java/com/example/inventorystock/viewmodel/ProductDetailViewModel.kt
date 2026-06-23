package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventorystock.data.InventoryRepository
import com.example.inventorystock.data.model.InventoryMovement
import com.example.inventorystock.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InventoryRepository()

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun setProduct(product: Product) {
        _uiState.update { it.copy(product = product) }
    }

    fun updateStock(newStock: Int) {
        val currentProduct = _uiState.value.product ?: return
        
        _uiState.update { it.copy(isLoading = true) }
        
        repository.updateProductStock(currentProduct.id, newStock)
            .addOnSuccessListener {
                val updatedProduct = currentProduct.copy(stock = newStock)
                _uiState.update { it.copy(product = updatedProduct, isLoading = false) }
                
                // Registrar movimiento
                repository.addMovement(InventoryMovement(
                    type = "update",
                    productName = currentProduct.name
                ))
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
