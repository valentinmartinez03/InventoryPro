package com.example.inventorystock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var productId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val tvTitle = findViewById<TextView>(R.id.tv_add_title)
        val nameField = findViewById<EditText>(R.id.et_product_name)
        val categoryField = findViewById<EditText>(R.id.et_product_category)
        val stockField = findViewById<EditText>(R.id.et_product_stock)
        val priceField = findViewById<EditText>(R.id.et_product_price)
        val btnSave = findViewById<Button>(R.id.btn_save_product)
        val tvCancel = findViewById<TextView>(R.id.tv_cancel)

        // Verificar si es edición
        productId = intent.getStringExtra("PRODUCT_ID")
        if (productId != null) {
            tvTitle.text = "Editar Producto"
            nameField.setText(intent.getStringExtra("PRODUCT_NAME"))
            categoryField.setText(intent.getStringExtra("PRODUCT_CATEGORY"))
            stockField.setText(intent.getIntExtra("PRODUCT_STOCK", 0).toString())
            priceField.setText(intent.getDoubleExtra("PRODUCT_PRICE", 0.0).toString())
            btnSave.text = "Actualizar Cambios"
        }

        tvCancel.setOnClickListener { finish() }

        val scannedCode = intent.getStringExtra("SCAN_RESULT")
        val finalBarcode = scannedCode ?: intent.getStringExtra("PRODUCT_BARCODE") ?: ""

        btnSave.setOnClickListener {
            val name = nameField.text.toString()
            val category = categoryField.text.toString()
            val stock = stockField.text.toString().toIntOrNull() ?: 0
            val price = priceField.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && category.isNotEmpty()) {
                val product = Product(productId ?: "", name, category, stock, price, finalBarcode)
                
                val collection = db.collection("products")
                val task = if (productId != null) {
                    collection.document(productId!!).set(product)
                } else {
                    collection.add(product)
                }

                task.addOnSuccessListener {
                    Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Nombre y Categoría son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
