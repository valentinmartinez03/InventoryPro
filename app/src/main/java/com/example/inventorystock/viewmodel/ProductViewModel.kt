package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventorystock.data.InventoryRepository
import com.example.inventorystock.data.local.AppDatabase
import com.example.inventorystock.data.model.InventoryMovement
import com.example.inventorystock.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val productDao = AppDatabase.getDatabase(application).productDao()
    private val repository = InventoryRepository(productDao)

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        observeProducts()
        observeCategories()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getProducts().collect { productList ->
                if (productList.isEmpty()) {
                    repository.initializeDemoProducts()
                } else {
                    _uiState.update { it.copy(products = productList, isLoading = false) }
                }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            repository.getCategories().collect { categoryList ->
                _uiState.update { it.copy(categories = categoryList) }
            }
        }
    }

    fun saveCategory(categoryName: String) {
        if (categoryName.isBlank()) return
        viewModelScope.launch {
            repository.saveCategory(categoryName)
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun deleteCategory(categoryName: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryName)
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun onIncreaseStock(product: Product) {
        updateStock(product, product.stock + 1)
    }

    fun onDecreaseStock(product: Product) {
        if (product.stock > 0) {
            updateStock(product, product.stock - 1)
        }
    }

    private fun updateStock(product: Product, newStock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(product.id, newStock)
                .addOnSuccessListener {
                    val movement = InventoryMovement(
                        type = "update",
                        productName = product.name
                    )
                    repository.addMovement(movement)
                }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product.id)
                .addOnSuccessListener {
                    val movement = InventoryMovement(
                        type = "delete",
                        productName = product.name
                    )
                    repository.addMovement(movement)
                }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSuccess = false) }
            repository.saveProduct(product)
                .addOnSuccessListener {
                    val movement = InventoryMovement(
                        type = if (product.id.isEmpty()) "new" else "update",
                        productName = product.name
                    )
                    repository.addMovement(movement)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message, isSuccess = false) }
                }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun findProductByBarcode(barcode: String, onResult: (Product?) -> Unit) {
        repository.findProductByBarcode(barcode)
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val product = doc.toObject(Product::class.java)
                    product?.id = doc.id
                    onResult(product)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
