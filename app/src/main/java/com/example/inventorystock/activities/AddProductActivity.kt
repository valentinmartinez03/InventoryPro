package com.example.inventorystock.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import com.example.inventorystock.data.model.Product
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.example.inventorystock.viewmodel.ProductViewModel

class AddProductActivity : ComponentActivity() {

    private val viewModel: ProductViewModel by viewModels()

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
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    Toast.makeText(context, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let {
                    Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }

            InventoryStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddProductScreen(
                        productId = productId,
                        initialName = initialName,
                        initialCategory = initialCategory,
                        initialStock = initialStock,
                        initialPrice = initialPrice,
                        initialBarcode = initialBarcode,
                        isLoading = uiState.isLoading,
                        categories = uiState.categories,
                        onSave = { product -> viewModel.saveProduct(product) },
                        onSaveCategory = { viewModel.saveCategory(it) },
                        onDeleteCategory = { viewModel.deleteCategory(it) },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddProductScreen(
    productId: String?,
    initialName: String,
    initialCategory: String,
    initialStock: Int,
    initialPrice: Double,
    initialBarcode: String,
    isLoading: Boolean,
    categories: List<String>,
    onSave: (Product) -> Unit,
    onSaveCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var category by remember { mutableStateOf(initialCategory) }
    var stock by remember { mutableStateOf(initialStock.toString()) }
    var price by remember { mutableStateOf(initialPrice.toString()) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var showAllCategories by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Eliminar Categoría") },
            text = { Text("¿Estás seguro de que quieres eliminar la categoría \"$cat\"? Esto no eliminará los productos asociados, pero ya no aparecerá como opción.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(cat)
                        if (category == cat) category = ""
                        categoryToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Categorías",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.take(4).chunked(2).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { 
                                    Text(
                                        text = cat, 
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) 
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { categoryToDelete = cat },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .border(
                                                width = 1.5.dp,
                                                color = Color.Red.copy(alpha = 0.6f),
                                                shape = CircleShape
                                            )
                                            .padding(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                        if (rowCategories.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (categories.size > 4) {
                TextButton(
                    onClick = { showAllCategories = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ver todas (${categories.size})", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (selecciona o escribe una nueva)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = stock,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            stock = newValue
                        }
                    },
                    label = { Text("Stock") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { newValue ->
                        val normalizedValue = newValue.replace(',', '.')
                        if (normalizedValue.matches(Regex("""^\d*\.?\d{0,2}$""")) || normalizedValue.isEmpty()) {
                            price = normalizedValue
                        }
                    },
                    label = { Text("Precio ($$)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty() && category.isNotEmpty()) {
                        if (!categories.contains(category)) {
                            onSaveCategory(category)
                        }

                        val product = Product(
                            productId ?: "",
                            name,
                            category,
                            stock.toIntOrNull() ?: 0,
                            price.toDoubleOrNull() ?: 0.0,
                            initialBarcode
                        )
                        onSave(product)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (productId != null) "Actualizar Cambios" else "Registrar Producto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        }

        if (showAllCategories) {
            AlertDialog(
                onDismissRequest = { showAllCategories = false },
                title = { Text("Seleccionar Categoría", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        categories.forEach { cat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        category = cat
                                        showAllCategories = false
                                    },
                                color = if (category == cat) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat,
                                        modifier = Modifier.weight(1f),
                                        fontSize = 16.sp,
                                        fontWeight = if (category == cat) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (category == cat) {
                                        Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAllCategories = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}
