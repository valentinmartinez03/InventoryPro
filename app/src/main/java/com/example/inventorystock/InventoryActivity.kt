package com.example.inventorystock

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class InventoryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_inventory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ProductAdapter(
            products = productList,
            onUpdateStock = { product, newStock ->
                updateStock(product, newStock)
            },
            onDeleteClick = { product ->
                deleteProduct(product)
            }
        )
        recyclerView.adapter = adapter

        // Configurar navegación
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_inventory)
        bottomNav.selectedItemId = R.id.nav_inventory
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_inventory -> true
                R.id.nav_scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        loadProducts()
    }

    private fun loadProducts() {
        db.collection("products").addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            productList.clear()
            if (value != null && !value.isEmpty) {
                for (doc in value) {
                    val product = doc.toObject(Product::class.java)
                    product.id = doc.id
                    productList.add(product)
                }
            } else {
                createDemoProducts()
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateStock(product: Product, newStock: Int) {
        db.collection("products").document(product.id)
            .update("stock", newStock)
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar stock", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDemoProducts() {
        val demos = listOf(
            Product("", "Laptop Gamer", "Electrónica", 5, 1200.0),
            Product("", "Teclado Mecánico", "Accesorios", 15, 80.0),
            Product("", "Monitor 4K", "Electrónica", 8, 350.0)
        )

        for (p in demos) {
            db.collection("products").add(p)
        }
    }

    private fun deleteProduct(product: Product) {
        db.collection("products").document(product.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${product.name} eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }
}
