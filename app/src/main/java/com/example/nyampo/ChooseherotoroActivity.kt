package com.example.nyampo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nyampo.ui.FeedDialog
import com.google.firebase.database.FirebaseDatabase

class ChooseherotoroActivity : AppCompatActivity() {

    private lateinit var userId: String
    private val userRef by lazy {
        FirebaseDatabase
            .getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
            .child(userId)
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chooseherotoro)

        // userId 전달받기
        userId = intent.getStringExtra("userId") ?: run {
            Toast.makeText(this, "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 툴바 설정
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "캐릭터 선택"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 시스템 패딩
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        unlockOtherCharacterIfLevel10()
        // 해로 선택
        val heroButton = findViewById<Button>(R.id.buttonSelectHero)
        heroButton.setOnClickListener {
            saveSelectedCharacterToFirebase("hero")
            unlockOtherCharacterIfLevel10()
        }

        // 토로 선택
        val toroButton = findViewById<Button>(R.id.buttonSelectToro)
        toroButton.setOnClickListener {
            saveSelectedCharacterToFirebase("toro")
            unlockOtherCharacterIfLevel10()
        }
    }

    private fun saveSelectedCharacter(character: String) {
        val firebaseDB = FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
        val userRef = firebaseDB.getReference("users").child(userId)

        // Firebase에 저장
        userRef.child("selectedCharacter").setValue(character)
        userRef.child("characters").setValue(listOf(character))
        userRef.child("level").setValue(1)

        // 로컬 SharedPreferences도 업데이트 (선택사항)
        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("selectedMascotIndex", if (character == "hero") 0 else 1)
        editor.putBoolean("unlock_haero", character == "hero")
        editor.putBoolean("unlock_toro", character == "toro")
        editor.apply()

        // MainActivity로 이동
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("selected_mascot", character)
        startActivity(intent)
        finish()
    }
    private fun saveSelectedCharacterToFirebase(chosen: String) {
        userRef.child("selectedCharacter").setValue(chosen)
            .addOnFailureListener { e ->
                Log.e("ChooseHeroToro", "선택 저장 실패: ${e.message}")
            }
    }

    private fun unlockOtherCharacterIfLevel10() {
        userRef.get().addOnSuccessListener { snap ->
            val level = snap.child("level").getValue(Int::class.java) ?: 1
            val selected = snap.child("selectedCharacter").getValue(String::class.java) ?: return@addOnSuccessListener
            val owned = snap.child("characters")
                .children.mapNotNull { it.getValue(String::class.java) }
                .toMutableSet()

            // 레벨 10 이상, 캐릭터 2개 미만일 때만 언락
            if (level >= 10 && owned.size < 2) {
                val newChar = if (selected == "hero") "toro" else "hero"
                owned.add(newChar)
                userRef.child("characters").setValue(owned.toList())

                // 팝업 띄우기 (FeedDialog 활용)
                val displayName = if (newChar == "hero") "해로" else "토로"
                val iconRes = if (newChar == "hero") R.drawable.haero else R.drawable.toro
                FeedDialog.showGetFeedPopup(
                    this,
                    message = "$displayName 캐릭터 획득!",
                    iconResId = iconRes
                ) { /* 확인만 누르면 dismiss */ }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
