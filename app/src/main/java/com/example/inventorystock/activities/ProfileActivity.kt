package com.example.inventorystock.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.ui.components.BottomNavigationBar
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryStockTheme {
                ProfileScreen(
                    onNavigateToHome = {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onNavigateToInventory = {
                        startActivity(Intent(this, InventoryActivity::class.java))
                        finish()
                    },
                    onNavigateToScan = {
                        startActivity(Intent(this, ScanActivity::class.java))
                        finish()
                    },
                    onLogout = {
                        auth.signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    fun ProfileScreen(
        onNavigateToHome: () -> Unit,
        onNavigateToInventory: () -> Unit,
        onNavigateToScan: () -> Unit,
        onLogout: () -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email ?: "No identificado"
        val name = email.split("@").getOrNull(0) ?: "Usuario"

        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedItem = 3,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToInventory = onNavigateToInventory,
                    onNavigateToScan = onNavigateToScan,
                    onNavigateToProfile = { }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFFF4F6F8))
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )

                // Profile Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-50).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = email, color = Color.Gray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            ProfileMenuItem(icon = Icons.Default.Info, title = "Información personal")
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            ProfileMenuItem(icon = Icons.Default.Notifications, title = "Notificaciones")
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            ProfileMenuItem(icon = Icons.Default.Lock, title = "Seguridad")
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            ProfileMenuItem(icon = Icons.Default.Settings, title = "Configuración")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Red)
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileMenuItem(icon: ImageVector, title: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 15.sp)
        }
    }
}
