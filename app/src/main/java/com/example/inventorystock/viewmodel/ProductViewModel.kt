package com.example.inventorystock.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.inventorystock.data.local.AppDatabase
import com.example.inventorystock.data.model.InventoryMovement
import com.example.inventorystock.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val productDao = AppDatabase.getDatabase(application).productDao()
    
    val products: LiveData<List<Product>> = productDao.getAllProducts().asLiveData()

    init {
        syncWithFirestore()
    }

    private fun syncWithFirestore() {
        db.collection("products").addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener

            if (value != null && !value.isEmpty) {
                viewModelScope.launch {
                    val firestoreProducts = value.map { doc ->
                        val p = doc.toObject(Product::class.java)
                        p.id = doc.id
                        p
                    }
                    for (p in firestoreProducts) {
                        productDao.insertProduct(p)
                    }
                }
            } else {
                createDemoProducts()
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
        val updatedProduct = product.copy(stock = newStock)
        viewModelScope.launch {
            productDao.insertProduct(updatedProduct)
            
            val movement = InventoryMovement(
                type = "update",
                productName = product.name
            )
            db.collection("movements").add(movement)
            db.collection("products").document(product.id).update("stock", newStock)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.deleteProduct(product)
            db.collection("products").document(product.id).delete()

            val movement = InventoryMovement(
                type = "delete",
                productName = product.name
            )
            db.collection("movements").add(movement)
        }
    }

    private fun createDemoProducts() {
        val demos = listOf(
            Product("demo1", "Laptop Gamer", "Electrónica", 5, 1200.0),
            Product("demo2", "Teclado Mecánico", "Accesorios", 15, 80.0),
            Product("demo3", "Monitor 4K", "Electrónica", 8, 350.0)
        )

        for (p in demos) {
            db.collection("products").document(p.id).set(p)
        }
    }
}
