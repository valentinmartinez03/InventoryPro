package com.example.inventorystock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.google.firebase.firestore.FirebaseFirestore

class AddProductActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val productId = intent.getStringExtra("PRODUCT_ID")
        val initialName = intent.getStringExtra("PRODUCT_NAME") ?: ""
        val initialCategory = intent.getStringExtra("PRODUCT_CATEGORY") ?: ""
        val initialStock = intent.getIntExtra("PRODUCT_STOCK", 0)
        val initialPrice = intent.getDoubleExtra("PRODUCT_PRICE", 0.0)
        val scannedCode = intent.getStringExtra("SCAN_RESULT")
        val initialBarcode = scannedCode ?: intent.getStringExtra("PRODUCT_BARCODE") ?: ""

        setContent {
            InventoryStockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddProductScreen(
                        modifier = Modifier.padding(innerPadding),
                        productId = productId,
                        initialName = initialName,
                        initialCategory = initialCategory,
                        initialStock = initialStock,
                        initialPrice = initialPrice,
                        initialBarcode = initialBarcode,
                        onSaveSuccess = {
                            Toast.makeText(this, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    @Composable
    fun AddProductScreen(
        modifier: Modifier = Modifier,
        productId: String?,
        initialName: String,
        initialCategory: String,
        initialStock: Int,
        initialPrice: Double,
        initialBarcode: String,
        onSaveSuccess: () -> Unit,
        onCancel: () -> Unit
    ) {
        var name by remember { mutableStateOf(initialName) }
        var category by remember { mutableStateOf(initialCategory) }
        var stock by remember { mutableStateOf(initialStock.toString()) }
        var price by remember { mutableStateOf(initialPrice.toString()) }
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(32.dp)
        ) {
            Text(
                text = if (productId != null) "Editar Producto" else "Nuevo Producto",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Completa los datos del item",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (ej: Electrónica)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio ($)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && category.isNotEmpty()) {
                        val product = Product(
                            productId ?: "",
                            name,
                            category,
                            stock.toIntOrNull() ?: 0,
                            price.toDoubleOrNull() ?: 0.0,
                            initialBarcode
                        )
                        
                        val collection = db.collection("products")
                        val task = if (productId != null) {
                            collection.document(productId).set(product)
                        } else {
                            collection.add(product)
                        }

                        task.addOnSuccessListener { onSaveSuccess() }
                            .addOnFailureListener {
                                Toast.makeText(this@AddProductActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (productId != null) "Actualizar Cambios" else "Registrar Producto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    }
}
