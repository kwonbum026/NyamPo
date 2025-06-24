package com.example.nyampo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.widget.Toast
import com.example.nyampo.databinding.ActivityGpsactivityBinding
import androidx.core.app.ActivityCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GPSActivity : AppCompatActivity(), OnMapReadyCallback {
    // 지도를 사용할 준비가 된 경우, Map 인스턴스를 제공하는 호출 함수(OnMapReadyCallback) 상속
    private val LOCATION_PERMISSION_REQUEST_CODE = 5000 // 위치 정보 수집 허용 요청 코드

    // 위치 정보 정확성을 높여주는 접근성 허가
    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // binding 과 위치 정보, 네이버 맵 객체 선언
    private lateinit var binding : ActivityGpsactivityBinding
    private lateinit var locationSource : FusedLocationSource
    private lateinit var naverMap : NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGpsactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initMapView() // 지도 초기화 함수 호출

        // 툴바를 액션바로 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)  // 앱 이름 숨기기
        // 툴바의 뒤로가기 버튼 클릭 시 MainActivity로 이동
        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // FusedLocationSource 는 지자기, 가속도 센서를 활용해 최적의 위치 정보를 반환하는 구현체
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 허가를 받았다면 기기를 통해 맵 띄우고, 받지 않았다면 위치 정보 수집 허용 요청
        if (hasPermission()){
            initMapView()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // 맵을 띄우기 위한 준비 함수

    override fun onMapReady(naverMap: NaverMap) {
        var resent_result = "한국공학대학교"
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true // 현재 위치를 아이콘으로 표시할 지 여부

        //추가
        // 허가 여부에 따라 현재 위치를 나타내는 점 상태 변경
        if (hasPermission()) {
            naverMap.locationTrackingMode =
                LocationTrackingMode.Follow // 현재 위치를 나타내는 파란 점의 상태 (Follow)
        } else {
            naverMap.locationTrackingMode =
                LocationTrackingMode.None // 현재 위치를 나타내지 않는 기본 지도 상태 (None)
        }

//        // 움직임 시, 초기화 되는 설정은 방향이 정해지지 않고 현재 위치만 표시하는 None 상태
//        if (::naverMap.isInitialized){
//            naverMap.locationTrackingMode = LocationTrackingMode.None
//        }
//        naverMap.locationTrackingMode = LocationTrackingMode.Follow
//
//        naverMap.setOnMapLongClickListener{point, coord -> // 네이버 지도 화면을 길게 클릭했을 때, 클릭 지점의 위도, 경도 표시
//            Toast.makeText(
//                this, "${coord.latitude}, ${coord.longitude}",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
        val marker_TUK = Marker()
        marker_TUK.position = LatLng(37.34003500120548, 126.73351773396644) // 한국 공학 대학교의 마커
        marker_TUK.captionText = "한국공학대학교"
        marker_TUK.map = naverMap

        val marker_lighthouse = Marker()
        marker_lighthouse.position = LatLng(37.34569980655277, 126.68753259225184) // 오이도 빨강 등대의 마커
        marker_lighthouse.captionText = "오이도 빨강 등대"
        marker_lighthouse.map = naverMap

        val marker_WP = Marker()
        marker_WP.position = LatLng(37.324101707990806, 126.68068670473025) // 웨이브 파크의 마커
        marker_WP.captionText = "웨이브 파크"
        marker_WP.map = naverMap

        val marker_eco_park = Marker()
        marker_eco_park.position = LatLng(37.39098632622522, 126.78110270666343) // 갯골 생태 공원의 마커
        marker_eco_park.captionText = "갯골 생태 공원"
        marker_eco_park.map = naverMap

        naverMap.addOnLocationChangeListener { location -> // 현재 위치 이동 시, toast 메시지와 텍스트 뷰의 텍스트 변경 함수
            if (resent_result == "한국공학대학교") {
                val result = String.format("%.2f", calculate(location, marker_TUK) / 1000)
                binding.tv.text = "현재 위치와 " + resent_result + "와의 거리\n = " + "${result} km"
            } else if (resent_result == "오이도 빨강 등대") {
                val result = String.format("%.2f", calculate(location, marker_lighthouse) / 1000)
                binding.tv.text = "현재 위치와 " + resent_result + "와의 거리\n = " + "${result} km"
            } else if (resent_result == "웨이브 파크") {
                val result = String.format("%.2f", calculate(location, marker_WP) / 1000)
                binding.tv.text = "현재 위치와 " + resent_result + "와의 거리\n = " + "${result} km"
            } else if (resent_result == "갯골 생태 공원") {
                val result = String.format("%.2f", calculate(location, marker_eco_park) / 1000)
                binding.tv.text = "현재 위치와 " + resent_result + "와의 거리\n = " + "${result} km"
            }
        }

        binding.imageBtn.setOnClickListener { // 버튼 클릭 시, 다른 랜드 마크 와의 거리 표시
            if (resent_result == "한국공학대학교") {
                resent_result = "오이도 빨강 등대"
            } else if (resent_result == "오이도 빨강 등대") {
                resent_result = "웨이브 파크"
            } else if (resent_result == "웨이브 파크") {
                resent_result = "갯골 생태 공원"
            } else if (resent_result == "갯골 생태 공원") {
                resent_result = "한국공학대학교"
            }
        }
    }

    private fun calculate(location: Location,  // 위도, 경도를 이용해 두 점 사이의 거리를 계산하는 함수
                          marker: Marker
    ): Double {
        val r = 6371e3  // 지구 반지름 (미터 단위)
        val locRad = location.latitude * PI / 180 // 라디안 변환
        val markRad = marker.position.latitude * PI / 180
        val deltaLat = (marker.position.latitude - location.latitude) * PI / 180
        val deltaLon = (marker.position.longitude - location.longitude) * PI / 180

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(locRad) * cos(markRad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }


    // 맵 뷰의 초기 설정 함수
    private fun initMapView() {
        val fm = supportFragmentManager // mapFragment 의 상호작용을 가능하게 하는 역할
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment? // mapFragment 로 불러오고, 객체가 없다면 null 반환
            ?: MapFragment.newInstance().also{ // 객체가 없다면 새로운 mapFragment 생성 후, 레이아웃에 추가
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this) // 초기화된 mapFragment 를 비동기로 가져오고, 이 객체를 CallbackListener 로 등록
    }

    // 위치 권한이 있을 경우 true 를, 없을 경우 false 를 반환하는 함수
    private fun hasPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // 사용자의 위치 정보 이용 권한 허용 여부에 따른 결과 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (::naverMap.isInitialized) {
            if (locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            } else {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}