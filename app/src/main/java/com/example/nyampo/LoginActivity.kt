package com.example.nyampo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nyampo.admin.AdminActivity
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firebaseDB = FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
        val usersRef = firebaseDB.getReference("users")

        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val signupButton = findViewById<Button>(R.id.buttonSignup)
        val loginButton = findViewById<Button>(R.id.buttonLogin)

        signupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val userId = editTextId.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (userId == "admin" && password == "1234") {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (userId.isEmpty()) {
                Toast.makeText(this, "아이디를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase에서 ID 조회
            usersRef.child(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "존재하지 않는 아이디입니다", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val savedPassword = snapshot.child("password").getValue(String::class.java)
                    if (savedPassword == password) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                        val selectedChar = snapshot.child("selectedCharacter").getValue(String::class.java)
                        val nextIntent = if (selectedChar.isNullOrEmpty()) {
                            Intent(this, ChooseherotoroActivity::class.java)
                        } else {
                            Intent(this, MainActivity::class.java)
                        }

                        nextIntent.putExtra("userId", userId)
                        startActivity(nextIntent)
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "로그인 실패: ${e.message}", e)
                    Toast.makeText(this, "로그인 오류 발생", Toast.LENGTH_SHORT).show()
                }
        }
    }
}