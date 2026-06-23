package com.example.inventorystock.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.R
import com.example.inventorystock.data.model.Product
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.example.inventorystock.viewmodel.ProductDetailViewModel
import com.example.inventorystock.viewmodel.ProductDetailUiState

/**
 * Pantalla de detalle de producto refactorizada a MVVM.
 */
class ProductDetailActivity : ComponentActivity() {

    private val viewModel: ProductDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar el ViewModel con los datos del Intent
        val product = Product(
            id = intent.getStringExtra("PRODUCT_ID") ?: "",
            name = intent.getStringExtra("PRODUCT_NAME") ?: "",
            category = intent.getStringExtra("PRODUCT_CATEGORY") ?: "",
            stock = intent.getIntExtra("PRODUCT_STOCK", 0),
            price = intent.getDoubleExtra("PRODUCT_PRICE", 0.0),
            barcode = intent.getStringExtra("PRODUCT_BARCODE") ?: ""
        )
        viewModel.setProduct(product)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }

            InventoryStockTheme {
                ProductDetailScreen(
                    uiState = uiState,
                    onBack = { finish() },
                    onUpdateStock = { newStock -> viewModel.updateStock(newStock) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    onBack: () -> Unit,
    onUpdateStock: (Int) -> Unit
) {
    val product = uiState.product ?: return

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

            ProductImage(
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
                    Text(text = product.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Categoría: ${product.category}", color = Color.Gray, fontSize = 14.sp)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFEEEEEE))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Stock Disponible:", fontSize = 16.sp)
                        Text(
                            text = "${product.stock} unidades",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Precio:", fontSize = 16.sp)
                        Text(
                            text = "$${product.price}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (product.barcode.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Código: ${product.barcode}", color = Color.Gray, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = { onUpdateStock(product.stock + 1) }, // Ejemplo: aumentar stock
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Aumentar Stock (+1)", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductImage(
    modifier: Modifier = Modifier,
    tintColor: Color
) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = R.drawable.ic_inventory_logo),
        contentDescription = null,
        modifier = modifier,
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tintColor)
    )
}
