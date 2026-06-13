package com.example.inventorystock

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        // Configurar paddings
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.toolbar_detail).parent as android.view.View
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar datos del Intent
        val name = intent.getStringExtra("PRODUCT_NAME")
        val category = intent.getStringExtra("PRODUCT_CATEGORY")
        val stock = intent.getIntExtra("PRODUCT_STOCK", 0)
        val price = intent.getDoubleExtra("PRODUCT_PRICE", 0.0)
        val barcode = intent.getStringExtra("PRODUCT_BARCODE")

        // Mostrar datos
        findViewById<TextView>(R.id.tv_detail_name).text = name
        findViewById<TextView>(R.id.tv_detail_category).text = "Categoría: $category"
        findViewById<TextView>(R.id.tv_detail_stock).text = "$stock unidades"
        
        // El XML no tiene tv_detail_price ni tv_detail_barcode, así que usaremos Toasts o logs
        // O podemos agregarlos al XML si prefieres. Por ahora evitemos el crash.
        
        findViewById<Button>(R.id.btn_edit).setOnClickListener {
            Toast.makeText(this, "Función para modificar stock próximamente", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_delete).setOnClickListener {
            Toast.makeText(this, "Para eliminar, usa el click largo en la lista", Toast.LENGTH_SHORT).show()
        }
    }
}
