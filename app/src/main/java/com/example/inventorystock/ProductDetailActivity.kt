package com.example.inventorystock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.ui.theme.InventoryStockTheme

class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val name = intent.getStringExtra("PRODUCT_NAME") ?: ""
        val category = intent.getStringExtra("PRODUCT_CATEGORY") ?: ""
        val stock = intent.getIntExtra("PRODUCT_STOCK", 0)
        val price = intent.getDoubleExtra("PRODUCT_PRICE", 0.0)
        val barcode = intent.getStringExtra("PRODUCT_BARCODE") ?: ""

        setContent {
            InventoryStockTheme {
                ProductDetailScreen(
                    name = name,
                    category = category,
                    stock = stock,
                    price = price,
                    barcode = barcode,
                    onBack = { finish() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProductDetailScreen(
        name: String,
        category: String,
        stock: Int,
        price: Double,
        barcode: String,
        onBack: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalles del producto", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF4F6F8)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_inventory_logo),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    tintColor = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Categoría: $category", color = Color.Gray, fontSize = 14.sp)
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Stock Disponible:", fontSize = 16.sp)
                            Text(text = "$stock unidades", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Precio:", fontSize = 16.sp)
                            Text(text = "$$price", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        if (barcode.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Código: $barcode", color = Color.Gray, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { Toast.makeText(this@ProductDetailActivity, "Función próximamente", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Modificar Stock", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Helper for Image tint since Image doesn't have it directly in Compose foundation
    @Composable
    fun Image(
        painter: androidx.compose.ui.graphics.painter.Painter,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        tintColor: Color
    ) {
        androidx.compose.foundation.Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
        )
    }
}
