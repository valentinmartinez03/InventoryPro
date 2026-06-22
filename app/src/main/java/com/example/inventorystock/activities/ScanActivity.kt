package com.example.inventorystock.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

class ScanActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryStockTheme {
                ScanScreen(
                    onNavigateToHome = {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    },
                    onNavigateToInventory = {
                        startActivity(Intent(this, InventoryActivity::class.java))
                        finish()
                    },
                    onProductFound = { product ->
                        val intent = Intent(this, ProductDetailActivity::class.java).apply {
                            putExtra("PRODUCT_NAME", product.name)
                            putExtra("PRODUCT_CATEGORY", product.category)
                            putExtra("PRODUCT_STOCK", product.stock)
                            putExtra("PRODUCT_PRICE", product.price)
                            putExtra("PRODUCT_BARCODE", product.barcode)
                        }
                        startActivity(intent)
                        finish()
                    },
                    onNewProduct = { code ->
                        val intent = Intent(this, AddProductActivity::class.java).apply {
                            putExtra("SCAN_RESULT", code)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    fun ScanScreen(
        onNavigateToHome: () -> Unit,
        onNavigateToInventory: () -> Unit,
        onProductFound: (Product) -> Unit,
        onNewProduct: (String) -> Unit
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
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
                    onNavigateToProfile = {
                        startActivity(Intent(context, ProfileActivity::class.java))
                        finish()
                    }
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
                    CameraPreview(
                        onBarcodeDetected = { code ->
                            searchBarcode(code, onProductFound, onNewProduct)
                        }
                    )

                    // Overlay
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(2.dp)
                    ) {
                        // Scan line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Red)
                                .align(Alignment.Center)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Escanear Código",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(300.dp))
                        Text(
                            text = "Alinea el código dentro del recuadro",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = "Se requiere permiso de cámara",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
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
                        // Log error
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

    private fun searchBarcode(
        code: String,
        onProductFound: (Product) -> Unit,
        onNewProduct: (String) -> Unit
    ) {
        db.collection("products")
            .whereEqualTo("barcode", code)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val product = documents.documents[0].toObject(Product::class.java)
                    product?.let {
                        it.id = documents.documents[0].id
                        onProductFound(it)
                    }
                } else {
                    onNewProduct(code)
                }
            }
    }
}
