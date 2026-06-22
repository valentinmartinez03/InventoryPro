package com.example.inventorystock.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventorystock.data.model.Product
import com.example.inventorystock.ui.components.BottomNavigationBar
import com.example.inventorystock.ui.components.ProductItem
import com.example.inventorystock.viewmodel.ProductViewModel
import com.example.inventorystock.ui.theme.InventoryStockTheme


class InventoryActivity : ComponentActivity() {

    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryStockTheme {
                InventoryScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onNavigateToScan = {
                        startActivity(Intent(this, ScanActivity::class.java))
                        finish()
                    },
                    onNavigateToProfile = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    },
                    onEditProduct = { product ->
                        val intent = Intent(this, AddProductActivity::class.java).apply {
                            putExtra("PRODUCT_ID", product.id)
                            putExtra("PRODUCT_NAME", product.name)
                            putExtra("PRODUCT_CATEGORY", product.category)
                            putExtra("PRODUCT_STOCK", product.stock)
                            putExtra("PRODUCT_PRICE", product.price)
                            putExtra("PRODUCT_BARCODE", product.barcode)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: ProductViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onEditProduct: (Product) -> Unit
) {
    val products: List<Product> by viewModel.products.observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 1,
                onNavigateToHome = onNavigateToHome,
                onNavigateToInventory = { },
                onNavigateToScan = onNavigateToScan,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(products) { product ->
                ProductItem(
                    product = product,
                    onIncreaseStock = { viewModel.onIncreaseStock(product) },
                    onDecreaseStock = { viewModel.onDecreaseStock(product) },
                    onEdit = { onEditProduct(product) },
                    onDelete = { viewModel.deleteProduct(product) }
                )
            }
        }
    }
}
