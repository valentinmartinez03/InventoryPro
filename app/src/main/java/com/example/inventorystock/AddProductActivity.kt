package com.example.inventorystock

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val nameField = findViewById<EditText>(R.id.et_product_name)
        val categoryField = findViewById<EditText>(R.id.et_product_category)
        val stockField = findViewById<EditText>(R.id.et_product_stock)
        val priceField = findViewById<EditText>(R.id.et_product_price)
        val btnSave = findViewById<Button>(R.id.btn_save_product)

        // Si venimos del escáner, mostrar el código en el nombre o en un campo nuevo
        val scannedCode = intent.getStringExtra("SCAN_RESULT")
        if (scannedCode != null) {
            Toast.makeText(this, "Código: $scannedCode", Toast.LENGTH_LONG).show()
        }

        btnSave.setOnClickListener {
            val name = nameField.text.toString()
            val category = categoryField.text.toString()
            val stock = stockField.text.toString().toIntOrNull() ?: 0
            val price = priceField.text.toString().toDoubleOrNull() ?: 0.0
            val barcode = scannedCode ?: ""

            if (name.isNotEmpty() && category.isNotEmpty()) {
                val product = Product("", name, category, stock, price, barcode)
                
                db.collection("products").add(product)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
                        finish() // Regresa al Dashboard
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
