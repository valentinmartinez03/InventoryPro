package com.example.inventorystock.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.inventorystock.R

@Composable
fun BottomNavigationBar(
    selectedItem: Int,
    onNavigateToHome: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = onNavigateToHome,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home_outline),
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = onNavigateToInventory,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_inventory_box_outline),
                    contentDescription = "Inventario"
                )
            },
            label = { Text("Inventario") }
        )
        NavigationBarItem(
            selected = selectedItem == 2,
            onClick = onNavigateToScan,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera_outline),
                    contentDescription = "Escanear"
                )
            },
            label = { Text("Scan") }
        )
        NavigationBarItem(
            selected = selectedItem == 3,
            onClick = onNavigateToProfile,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person_outline),
                    contentDescription = "Perfil"
                )
            },
            label = { Text("Perfil") }
        )
    }
}
