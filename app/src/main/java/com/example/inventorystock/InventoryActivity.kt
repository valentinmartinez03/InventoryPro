package com.example.inventorystock

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class InventoryActivity : AppCompatActivity() {

    private val viewModel: ProductViewModel by viewModels()
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
                viewModel.updateStock(product, newStock)
            },
            onDeleteClick = { product ->
                viewModel.deleteProduct(product)
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

        // Observar datos del ViewModel
        viewModel.products.observe(this) { updatedList ->
            productList.clear()
            productList.addAll(updatedList)
            adapter.notifyDataSetChanged()
        }
    }
}
