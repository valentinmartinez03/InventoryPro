package com.example.inventorystock.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.data.model.InventoryMovement
import com.example.inventorystock.ui.components.BottomNavigationBar
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.example.inventorystock.viewmodel.DashboardViewModel

class DashboardActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryStockTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToInventory = { startActivity(Intent(this, InventoryActivity::class.java)) },
                    onNavigateToScan = { startActivity(Intent(this, ScanActivity::class.java)) },
                    onNavigateToProfile = { startActivity(Intent(this, ProfileActivity::class.java)) },
                    onNavigateToAdd = { startActivity(Intent(this, AddProductActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val totalCount by viewModel.totalCount.collectAsState()
    val inStockCount by viewModel.inStockCount.collectAsState()
    val criticalCount by viewModel.criticalCount.collectAsState()
    val movements by viewModel.movements.collectAsState()

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF145A32),
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF58D68D)
                            )
                        )
                    )
            ) {
                Text(
                    text = "Panel de Control",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 56.dp)
                )
            }

            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-80).dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Productos en Inventario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem("Total", totalCount.toString(), MaterialTheme.colorScheme.primary)
                        StatItem("En Stock", inStockCount.toString(), Color(0xFF2980B9))
                        StatItem("Sin Stock", criticalCount.toString(), Color(0xFFE74C3C))
                    }
                }
            }

            // Agrupamos el contenido inferior y lo subimos con offset para compensar la Card
            Column(modifier = Modifier.offset(y = (-50).dp)) {
                SectionTitle("Acciones Rápidas")

                Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    ActionCard(Modifier.weight(1f), "Nuevo Item", Icons.Default.Add, onNavigateToAdd)
                    ActionCard(Modifier.weight(1f), "Inventario", Icons.AutoMirrored.Filled.List, onNavigateToInventory)
                }

                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("Actividad Reciente")

                RecentActivityCard(movements)
                
                // Spacer extra para compensar el offset y que no se corte el scroll al final
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
    )
}

@Composable
fun RecentActivityCard(movements: List<InventoryMovement>) {
    Card(
        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (movements.isEmpty()) {
                Text("No hay actividad reciente", modifier = Modifier.padding(16.dp), color = Color.Gray)
            } else {
                movements.forEachIndexed { index, movement ->
                    MovementItem(movement)
                    if (index < movements.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = Color(0xFFF0F0F0))
                    }
                }
            }
        }
    }
}

@Composable
fun MovementItem(movement: InventoryMovement) {
    val (title, icon, color) = when (movement.type) {
        "new" -> Triple("Producto nuevo", Icons.Default.Add, Color(0xFF81C784))
        "update" -> Triple("Actualización", Icons.Default.Edit, Color(0xFF64B5F6))
        "delete" -> Triple("Eliminación", Icons.Default.Delete, Color(0xFFE57373))
        else -> Triple("Movimiento", Icons.Default.Info, Color.Gray)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = movement.productName, color = Color.Gray, fontSize = 13.sp)
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
        modifier = modifier.padding(8.dp).height(120.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF2980B9))
        }
    }
}
