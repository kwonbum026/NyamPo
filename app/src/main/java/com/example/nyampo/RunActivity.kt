package com.example.nyampo

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nyampo.ui.FeedDialog
import com.google.android.gms.location.*
import com.example.nyampo.MainActivity

class RunActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var haero: ImageButton
    private lateinit var toro: ImageButton
    private lateinit var tino: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var walkingText: TextView
    private lateinit var noticeFeed1: TextView
    private lateinit var noticeFeed2: TextView
    private lateinit var stepAddButton: Button
    private lateinit var backButton: ImageButton

    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lastLocation: Location? = null
    private var currentSpeed = 0.0

    private var stepCount = 0
    private var distance = 0.0
    private var leafCount = 0
    private var grantedFeedCount = 0
    private var progressCount = 0
    private var bonusFeedGranted = false


    private val stepLength = 0.75
    private val stepsPerFeed = 3000
    private val stepsPerProgress = 10
    private val maxSteps = 10000

    private lateinit var userId: String
    private lateinit var today: String
    private lateinit var prefs: SharedPreferences

    private val LOCATION_PERMISSION_CODE = 1001
    private val REQUIRED_PERMISSIONS: Array<String>
        get() {
            val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            return permissions.toTypedArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        haero = findViewById(R.id.imageButton_haero)
        toro = findViewById(R.id.imageButton_toro)
        tino = findViewById(R.id.imageButton_tino)
        progressBar = findViewById(R.id.ProgressBar)
        walkingText = findViewById(R.id.total_walking)
        noticeFeed1 = findViewById(R.id.notice_feed1)
        noticeFeed2 = findViewById(R.id.notice_feed2)
        stepAddButton = findViewById(R.id.button)
        backButton = findViewById(R.id.imageButton_back)
        backButton.setOnClickListener {
            goToMain()
        }


        val mascotIndex = intent.getIntExtra("mascotIndex", 0)
        when (mascotIndex) {
            0 -> haero.visibility = View.VISIBLE
            1 -> toro.visibility = View.VISIBLE
            2 -> tino.visibility = View.VISIBLE
        }

        haero.setOnClickListener { giveFeed() }
        toro.setOnClickListener { giveFeed() }
        tino.setOnClickListener { giveFeed() }
        stepAddButton.setOnClickListener {
            repeat(100) { simulateStep() }
        }

        noticeFeed1.visibility = View.INVISIBLE
        noticeFeed2.visibility = View.INVISIBLE

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, LOCATION_PERMISSION_CODE)
        } else {
            startTracking()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startTracking() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Toast.makeText(this, "걸음 센서를 지원하지 않는 기기입니다", Toast.LENGTH_LONG).show()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                lastLocation?.let {
                    val deltaTime = (location.time - it.time) / 1000.0
                    if (deltaTime > 0) {
                        val distanceMoved = location.distanceTo(it)
                        currentSpeed = distanceMoved / deltaTime
                    }
                }
                lastLocation = location
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR && currentSpeed > 0.5) {
            simulateStep()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun simulateStep() {
        if (stepCount >= maxSteps) {
            if (stepCount == maxSteps) {
                updateUi() // 💡 코드 중복 제거
            }
            return
        }

        stepCount++
        updateUi()
    }

    private fun updateUi() {
        distance = stepCount * stepLength
        progressBar.progress = (stepCount * 100) / maxSteps
        updateFeedNotice()
        walkingText.text = "걸음 수: $stepCount\n총 거리: ${String.format("%.2f", distance)} m"
    }




    private fun availableFeedCount(): Int {
        return (stepCount / stepsPerFeed) - grantedFeedCount
    }

    private fun updateFeedNotice() {
        val available = availableFeedCount()
        val bonusAvailable = stepCount >= maxSteps && !bonusFeedGranted

        if (available > 0 || bonusAvailable) {
            noticeFeed1.visibility = View.VISIBLE
            noticeFeed2.visibility = View.VISIBLE
        } else {
            noticeFeed1.visibility = View.INVISIBLE
            noticeFeed2.visibility = View.INVISIBLE
        }
    }


    private fun giveFeed() {
        val regularAvailable = availableFeedCount()
        val bonusAvailable = stepCount >= maxSteps && !bonusFeedGranted

        if (regularAvailable > 0) {
            grantedFeedCount++
            leafCount++
            FeedDialog.showGetFeedPopup(this)
            updateFeedNotice()
        } else if (bonusAvailable) {
            bonusFeedGranted = true      // ✅ 먼저 보너스 지급 처리
            leafCount++
            FeedDialog.showGetFeedPopup(this)
            updateFeedNotice()          // ✅ 보너스 조건 사라졌음을 반영!
        } else {
            Toast.makeText(this, "아직 먹이를 받을 수 없어요!", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (hasPermissions()) {
                startTracking()
            } else {
                Toast.makeText(this, "필수 권한이 거부되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
