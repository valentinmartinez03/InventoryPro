package com.example.inventorystock.data

import com.example.inventorystock.data.local.ProductDao
import com.example.inventorystock.data.model.InventoryMovement
import com.example.inventorystock.data.model.Product
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InventoryRepository(private val productDao: ProductDao? = null) {
    private val db = FirebaseFirestore.getInstance()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        productDao?.let { dao ->
            repositoryScope.launch {
                trySend(dao.getAllProducts().first())
            }
        }

        val subscription = db.collection("products")
            .addSnapshotListener { value, _ ->
                val products = value?.map { doc ->
                    val p = doc.toObject(Product::class.java)
                    p.id = doc.id
                    p
                } ?: emptyList()
                
                productDao?.let { dao ->
                    repositoryScope.launch {
                        dao.deleteAll()
                        products.forEach { dao.insertProduct(it) }
                    }
                }

                trySend(products)
            }
        awaitClose { subscription.remove() }
    }

    fun getCategories(): Flow<List<String>> = callbackFlow {
        val subscription = db.collection("categories")
            .addSnapshotListener { value, _ ->
                val categories = value?.map { it.id } ?: emptyList()
                trySend(categories)
            }
        awaitClose { subscription.remove() }
    }

    fun saveCategory(categoryName: String): Task<Void> {
        return db.collection("categories").document(categoryName).set(mapOf("name" to categoryName))
    }

    fun deleteCategory(categoryName: String): Task<Void> {
        return db.collection("categories").document(categoryName).delete()
    }

    fun getRecentMovements(limit: Long = 3): Flow<List<InventoryMovement>> = callbackFlow {
        val subscription = db.collection("movements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { value, _ ->
                val movements = value?.toObjects(InventoryMovement::class.java) ?: emptyList()
                trySend(movements)
            }
        awaitClose { subscription.remove() }
    }

    fun updateProductStock(productId: String, newStock: Int): Task<Void> {
        return db.collection("products").document(productId).update("stock", newStock)
    }

    fun deleteProduct(productId: String): Task<Void> {
        return db.collection("products").document(productId).delete()
    }

    fun addMovement(movement: InventoryMovement): Task<DocumentReference> {
        return db.collection("movements").add(movement)
    }

    fun saveProduct(product: Product): Task<Void> {
        val docRef = if (product.id.isEmpty()) {
            db.collection("products").document()
        } else {
            db.collection("products").document(product.id)
        }
        val productWithId = if (product.id.isEmpty()) product.copy(id = docRef.id) else product
        return docRef.set(productWithId)
    }

    fun findProductByBarcode(barcode: String): Task<com.google.firebase.firestore.QuerySnapshot> {
        return db.collection("products")
            .whereEqualTo("barcode", barcode)
            .get()
    }

    fun initializeDemoProducts() {
        val demos = listOf(
            Product("demo1", "Laptop Gamer", "Electrónica", 5, 1200.0),
            Product("demo2", "Teclado Mecánico", "Accesorios", 15, 80.0),
            Product("demo3", "Monitor 4K", "Electrónica", 8, 350.0)
        )
        demos.forEach { saveProduct(it) }
    }
}
