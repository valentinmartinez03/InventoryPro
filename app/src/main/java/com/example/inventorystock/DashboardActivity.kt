package com.example.inventorystock

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Configurar clics en las tarjetas de acción rápida
        findViewById<CardView>(R.id.card_add).setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        findViewById<CardView>(R.id.card_search).setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        // Configurar clics en el menú inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_inventory -> {
                    startActivity(Intent(this, InventoryActivity::class.java))
                    true
                }
                R.id.nav_scan -> {
                    startActivity(Intent(this, ScanActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Cargar estadísticas reales desde Firestore
        loadStatistics()
    }

    private fun loadStatistics() {
        val tvTotal = findViewById<TextView>(R.id.tv_stat_total)
        val tvStock = findViewById<TextView>(R.id.tv_stat_stock)
        val tvCritical = findViewById<TextView>(R.id.tv_stat_critical)

        db.collection("products").addSnapshotListener { value, error ->
            if (error != null || value == null) return@addSnapshotListener

            val products = value.toObjects(Product::class.java)
            
            val totalCount = products.size
            val inStockCount = products.count { it.stock > 0 }
            val criticalCount = products.count { it.stock == 0 }

            tvTotal.text = totalCount.toString()
            tvStock.text = inStockCount.toString()
            tvCritical.text = criticalCount.toString()
        }
    }
}
