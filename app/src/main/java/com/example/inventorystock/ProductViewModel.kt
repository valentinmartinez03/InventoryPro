package com.example.inventorystock

import android.app.Application
import androidx.lifecycle.*
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
                    // Actualizar base de datos local (Room) con los datos de la nube
                    for (p in firestoreProducts) {
                        productDao.insertProduct(p)
                    }
                }
            } else {
                createDemoProducts()
            }
        }
    }

    fun updateStock(product: Product, newStock: Int) {
        val updatedProduct = product.copy(stock = newStock)
        viewModelScope.launch {
            // 1. Actualizar localmente de inmediato (Room)
            productDao.insertProduct(updatedProduct)
            
            // 2. Registrar movimiento
            val movement = InventoryMovement(
                type = "update",
                productName = product.name
            )
            db.collection("movements").add(movement)

            // 3. Actualizar nube (Firestore) sin bloquear la UI
            db.collection("products").document(product.id).update("stock", newStock)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.deleteProduct(product) // Local
            db.collection("products").document(product.id).delete() // Nube

            // Registrar movimiento
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
