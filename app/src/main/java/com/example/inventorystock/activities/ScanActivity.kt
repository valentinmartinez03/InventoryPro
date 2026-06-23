package com.example.inventorystock.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.inventorystock.data.model.Product
import com.example.inventorystock.ui.components.BottomNavigationBar
import com.example.inventorystock.ui.theme.InventoryStockTheme
import androidx.activity.viewModels
import com.example.inventorystock.viewmodel.ProductViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Actividad de Escaneo refactorizada.
 * Maneja la cámara para detección de códigos y delega la búsqueda al ViewModel.
 */
class ScanActivity : ComponentActivity() {

    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryStockTheme {
                ScanScreen(
                    onBarcodeDetected = { code ->
                        viewModel.findProductByBarcode(code) { product ->
                            if (product != null) {
                                navigateToDetail(product)
                            } else {
                                navigateToAddProduct(code)
                            }
                        }
                    },
                    onNavigateToHome = {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onNavigateToInventory = {
                        startActivity(Intent(this, InventoryActivity::class.java))
                        finish()
                    },
                    onNavigateToProfile = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    private fun navigateToDetail(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("PRODUCT_NAME", product.name)
            putExtra("PRODUCT_CATEGORY", product.category)
            putExtra("PRODUCT_STOCK", product.stock)
            putExtra("PRODUCT_PRICE", product.price)
            putExtra("PRODUCT_BARCODE", product.barcode)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToAddProduct(code: String) {
        val intent = Intent(this, AddProductActivity::class.java).apply {
            putExtra("SCAN_RESULT", code)
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun ScanScreen(
    onBarcodeDetected: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 2,
                onNavigateToHome = onNavigateToHome,
                onNavigateToInventory = onNavigateToInventory,
                onNavigateToScan = { },
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                CameraPreview(onBarcodeDetected = onBarcodeDetected)

                // Overlay de escaneo
                ScanOverlay(modifier = Modifier.align(Alignment.Center))

                // Textos informativos
                ScanInfoTexts()
            } else {
                Text(
                    text = "Se requiere permiso de cámara para escanear",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ScanOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(280.dp)
            .background(Color.White.copy(alpha = 0.1f))
            .padding(2.dp)
    ) {
        // Línea de escaneo animada (estática por ahora)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Red)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun ScanInfoTexts() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Escanear Código",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(320.dp))
        Text(
            text = "Alinea el código dentro del recuadro",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CameraPreview(onBarcodeDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isScanning by remember { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (isScanning) {
                                processImageProxy(imageProxy) { code ->
                                    if (isScanning) {
                                        isScanning = false
                                        onBarcodeDetected(code)
                                    }
                                }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    // Manejar error de cámara
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(imageProxy: ImageProxy, onCodeFound: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { onCodeFound(it) }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
