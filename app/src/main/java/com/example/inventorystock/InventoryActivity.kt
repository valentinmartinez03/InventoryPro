package com.example.inventorystock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        onUpdateStock = { viewModel.updateStock(product, it) },
                        onEdit = { onEditProduct(product) },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
    }

    @Composable
    fun ProductItem(
        product: Product,
        onUpdateStock: (Int) -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth()
                .clickable { onEdit() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_inventory_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    Text(text = product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = product.category, color = Color.Gray, fontSize = 14.sp)
                    Text(text = "$${product.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Stock: ${product.stock}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { onUpdateStock(product.stock + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "Suma", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { if (product.stock > 0) onUpdateStock(product.stock - 1) }) {
                            Icon(painter = painterResource(id = android.R.drawable.ic_input_delete), contentDescription = "Resta", tint = Color.Gray)
                        }
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
            }
        }
    }
}
