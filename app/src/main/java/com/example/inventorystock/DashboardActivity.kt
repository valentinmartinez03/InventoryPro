package com.example.inventorystock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryStockTheme {
                DashboardScreen(
                    onNavigateToInventory = { startActivity(Intent(this, InventoryActivity::class.java)) },
                    onNavigateToScan = { startActivity(Intent(this, ScanActivity::class.java)) },
                    onNavigateToProfile = { startActivity(Intent(this, ProfileActivity::class.java)) },
                    onNavigateToAdd = { startActivity(Intent(this, AddProductActivity::class.java)) }
                )
            }
        }
    }

    @Composable
    fun DashboardScreen(
        onNavigateToInventory: () -> Unit,
        onNavigateToScan: () -> Unit,
        onNavigateToProfile: () -> Unit,
        onNavigateToAdd: () -> Unit
    ) {
        var totalCount by remember { mutableStateOf(0) }
        var inStockCount by remember { mutableStateOf(0) }
        var criticalCount by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            db.collection("products").addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                val products = value.toObjects(Product::class.java)
                totalCount = products.size
                inStockCount = products.count { it.stock > 0 }
                criticalCount = products.count { it.stock == 0 }
            }
        }

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = 0,
                    onNavigateToHome = { },
                    onNavigateToInventory = onNavigateToInventory,
                    onNavigateToScan = onNavigateToScan,
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Panel de Control",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 24.dp, top = 56.dp)
                    )
                }

                // Stats Card
                Card(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = (-40).dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "Total", value = totalCount.toString(), color = MaterialTheme.colorScheme.primary)
                        StatItem(label = "En Stock", value = inStockCount.toString(), color = Color(0xFF2980B9))
                        StatItem(label = "Crítico", value = criticalCount.toString(), color = Color(0xFFE74C3C))
                    }
                }

                Text(
                    text = "Acciones Rápidas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 0.dp, bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Nuevo Item",
                        icon = Icons.Default.Add,
                        onClick = onNavigateToAdd
                    )
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        title = "Inventario",
                        icon = Icons.AutoMirrored.Filled.List,
                        onClick = onNavigateToInventory
                    )
                }
            }
        }
    }

    @Composable
    fun StatItem(label: String, value: String, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
        }
    }

    @Composable
    fun ActionCard(modifier: Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
        Card(
            modifier = modifier
                .padding(8.dp)
                .height(120.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2980B9))
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onNavigateToHome: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(85.dp),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = selectedItem == 0,
            onClick = onNavigateToHome
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Inventario") },
            label = { Text("Inventario") },
            selected = selectedItem == 1,
            onClick = onNavigateToInventory
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Escanear") },
            label = { Text("Escanear") },
            selected = selectedItem == 2,
            onClick = onNavigateToScan
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = selectedItem == 3,
            onClick = onNavigateToProfile
        )
    }
}
