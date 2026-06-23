package com.example.inventorystock.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventorystock.R
import com.example.inventorystock.ui.theme.InventoryStockTheme
import com.example.inventorystock.viewmodel.MainViewModel
import com.example.inventorystock.viewmodel.LoginUiState


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val startDestination by viewModel.startDestination.collectAsState()
            val uiState by viewModel.uiState.collectAsState()
            val context = LocalContext.current


            LaunchedEffect(startDestination) {
                when (startDestination) {
                    is MainViewModel.Destination.Onboarding -> {
                        startActivity(Intent(context, OnboardingActivity::class.java))
                        finish()
                    }
                    is MainViewModel.Destination.Dashboard -> {
                        startActivity(Intent(context, DashboardActivity::class.java))
                        finish()
                    }
                    else -> { /* Permanecer en Login */ }
                }
            }


            LaunchedEffect(uiState.errorMessage) {
                uiState.errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    startActivity(Intent(context, DashboardActivity::class.java))
                    finish()
                }
            }

            InventoryStockTheme {
                if (startDestination is MainViewModel.Destination.Login) {
                    LoginScreen(
                        uiState = uiState,
                        onLogin = { email, pass -> viewModel.login(email, pass) },
                        onForgotPassword = { email ->
                            viewModel.resetPassword(email) {
                                Toast.makeText(context, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onNavigateToRegister = {
                            startActivity(Intent(context, RegisterActivity::class.java))
                        }
                    )
                } else {
                    // Pantalla de carga mientras se decide el destino
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLogin: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_inventory_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "InventoryPro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Control de Stock Inteligente",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = "Iniciar Sesión",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                enabled = !uiState.isLoading
            )

            TextButton(
                onClick = { onForgotPassword(email) },
                modifier = Modifier.align(Alignment.End),
                enabled = !uiState.isLoading
            ) {
                Text("¿Olvidaste tu contraseña?", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Button(
                    onClick = { onLogin(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Acceder", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading
            ) {
                Text("¿No tienes cuenta? Regístrate aquí", color = Color.Gray)
            }
        }
    }
}
