package com.example.nyampo.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nyampo.R

class AdminLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        val idInput = findViewById<EditText>(R.id.edit_admin_id)
        val pwInput = findViewById<EditText>(R.id.edit_admin_pw)
        val loginBtn = findViewById<Button>(R.id.btn_admin_login)

        loginBtn.setOnClickListener {
            val id = idInput.text.toString().trim()
            val pw = pwInput.text.toString().trim()
            if (id == "admin" && pw == "1234") {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}