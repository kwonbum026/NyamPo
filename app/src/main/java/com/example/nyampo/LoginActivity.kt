package com.example.nyampo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // 상태바/내비게이션바 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idInput = findViewById<EditText>(R.id.editTextId)
        val pwInput = findViewById<EditText>(R.id.editTextPassword)
        val loginBtn = findViewById<Button>(R.id.buttonLogin)
        val signupBtn = findViewById<Button>(R.id.buttonSignup)

        val dbUrl = "https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app"
        val usersRef = FirebaseDatabase.getInstance(dbUrl).getReference("users")

        // 회원가입 버튼 클릭 시
        signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 로그인 버튼 클릭 시
        loginBtn.setOnClickListener {
            val userId = idInput.text.toString().trim()
            val inputPw = pwInput.text.toString().trim()

            if (userId.isEmpty() || inputPw.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 모두 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            usersRef.child(userId).get()
                .addOnSuccessListener { snap ->
                    if (!snap.exists()) {
                        Toast.makeText(this, "존재하지 않는 아이디입니다", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val storedPw = snap.child("password").value?.toString() ?: ""
                    Log.d("LoginActivity", "DB pw='$storedPw', inputPw='$inputPw'")

                    if (storedPw == inputPw) {
                        if (userId == "admin") {
                            startActivity(Intent(this, com.example.nyampo.admin.AdminActivity::class.java))
                        } else {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("userId", userId)
                            startActivity(intent)
                        }
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "로그인 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}