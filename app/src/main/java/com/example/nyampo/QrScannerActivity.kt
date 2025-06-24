package com.example.nyampo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class QrScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        userId = intent.getStringExtra("userId") ?: run {
            Toast.makeText(this, "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        previewView = findViewById(R.id.previewView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(false)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.icon_arrow_back)
        }

        toolbar.setNavigationOnClickListener { finish() }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()

            val scanner = BarcodeScanning.getClient(options)

            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                    processImageProxy(scanner, imageProxy)
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("QrScannerActivity", "카메라 바인딩 실패", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { code ->
                            handleQRCode(code)
                            scanner.close()
                            imageProxy.close()
                            return@addOnSuccessListener
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QrScannerActivity", "QR 분석 실패", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun handleQRCode(code: String) {
        val backgroundKey = when (code.lowercase()) {
            "base" -> "background_base"
            "oido" -> "background_oido"
            "park" -> "background_park"
            "wavepark" -> "background_wavepark"
            "tuk" -> "background_tuk"
            else -> null
        }

        val backgroundIndex = when (backgroundKey) {
            "background_base" -> 0
            "background_oido" -> 1
            "background_park" -> 2
            "background_wavepark" -> 3
            "background_tuk" -> 4
            else -> -1
        }

//        if (backgroundKey != null && backgroundIndex != -1) {
//            val userRef = FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
//                .getReference("users").child(userId)
//
//            userRef.child("backgrounds").get().addOnSuccessListener { snapshot ->
//                val current = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableSet()
//                if (!current.contains(backgroundKey)) {
//                    current.add(backgroundKey)
//                    userRef.child("backgrounds").setValue(current.toList()).addOnCompleteListener {
//                        Toast.makeText(this, "배경화면을 획득했습니다!", Toast.LENGTH_SHORT).show()
//
//                        val intent = Intent(this, MainActivity::class.java).apply {
//                            putExtra("userId", userId)
//                            putExtra("background_index", backgroundIndex)
//                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//                        }
//                        startActivity(intent)
//                        finish()
//                    }
//                } else {
//                    Toast.makeText(this, "이미 획득한 배경입니다.", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//        } else {
//            Toast.makeText(this, "알 수 없는 QR 코드입니다.", Toast.LENGTH_SHORT).show()
//            finish()
//        }
        if (backgroundKey != null && backgroundIndex != -1) {
            getCurrentLocation { currentLat, currentLon ->
                val targetLatLon = when (code.lowercase()) {
                    "tuk" -> 37.340035 to 126.733518
                    "oido" -> 37.345700 to 126.687533
                    "wavepark" -> 37.324102 to 126.680687
                    "park" -> 37.390986 to 126.781103
                    else -> null
                }

                if (targetLatLon == null) {
                    Toast.makeText(this, "알 수 없는 QR 코드입니다.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@getCurrentLocation
                }

                val distance = calculateDistance(currentLat, currentLon, targetLatLon.first, targetLatLon.second)

                if (distance > 100.0) {
                    Toast.makeText(this, "해당 장소 근처에서만 인증이 가능합니다.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@getCurrentLocation
                }

                val userRef = FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId)

                userRef.child("backgrounds").get().addOnSuccessListener { snapshot ->
                    val current = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableSet()
                    if (!current.contains(backgroundKey)) {
                        current.add(backgroundKey)
                        userRef.child("backgrounds").setValue(current.toList()).addOnCompleteListener {
                            Toast.makeText(this, "배경화면을 획득했습니다!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("userId", userId)
                                putExtra("background_index", backgroundIndex)
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "이미 획득한 배경입니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        } else {
            Toast.makeText(this, "알 수 없는 QR 코드입니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(callback: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
