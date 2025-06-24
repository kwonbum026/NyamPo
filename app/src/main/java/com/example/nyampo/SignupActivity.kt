package com.example.nyampo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private var isIdChecked = false
    private var isPhoneChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val firebaseDB = FirebaseDatabase.getInstance(
            "https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app"
        )

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "회원가입"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val editTextId = findViewById<EditText>(R.id.editTextId)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextPasswordConfirm = findViewById<EditText>(R.id.editTextPasswordConfirm)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextNickname = findViewById<EditText>(R.id.editTextNickname)
        val editTextReferral = findViewById<EditText>(R.id.editTextReferral)
        val warningText = findViewById<TextView>(R.id.textPasswordWarning)
        val checkIdButton = findViewById<AppCompatButton>(R.id.buttonCheckId)
        val checkPhoneButton = findViewById<Button>(R.id.buttonCheckPhone)
        val signupButton = findViewById<Button>(R.id.buttonSignup)

        // ID 중복 확인
        checkIdButton.setOnClickListener {
            val enteredId = editTextId.text.toString()
            if (!isValidIdFormat(enteredId)) {
                showInvalidIdPopup(editTextId)
                isIdChecked = false
            } else {
                val userRef = firebaseDB.getReference("users").child(enteredId)
                userRef.get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("Firebase", "ID 조회 성공: exists=${snapshot.exists()}")
                        if (snapshot.exists()) {
                            showPopup(R.layout.popup_checkid_overlap) { editTextId.setText("") }
                            isIdChecked = false
                        } else {
                            showAvailablePopup()
                            isIdChecked = true
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "ID 중복 확인 실패: ${e.message}", e)
                        Toast.makeText(this, "ID 확인 중 오류 발생", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 전화번호 중복 확인
        checkPhoneButton.setOnClickListener {
            val phone = editTextPhone.text.toString()
            if (phone.length != 11) {
                showPopup(R.layout.popup_phonenum_invalid) { editTextPhone.setText("") }
                isPhoneChecked = false
            } else {
                val usersRef = firebaseDB.getReference("users")
                usersRef.get()
                    .addOnSuccessListener { snapshot ->
                        var exists = false
                        for (child in snapshot.children) {
                            val savedPhone = child.child("phone").getValue(String::class.java)
                            if (savedPhone == phone) {
                                exists = true
                                break
                            }
                        }

                        if (exists) {
                            showPopup(R.layout.popup_phonenum_overlap) { editTextPhone.setText("") }
                            isPhoneChecked = false
                        } else {
                            showPopup(R.layout.popup_phonenum_valid) {}
                            isPhoneChecked = true
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "전화번호 중복 확인 실패: ${e.message}", e)
                        Toast.makeText(this, "전화번호 확인 오류", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // 회원가입 버튼 클릭
        signupButton.setOnClickListener {
            val id = editTextId.text.toString()
            val pw = editTextPassword.text.toString()
            val pwConfirm = editTextPasswordConfirm.text.toString()
            val phone = editTextPhone.text.toString()
            val nickname = editTextNickname.text.toString()

            // 유효성 검사
            if (id.isBlank() || pw.isBlank() || pwConfirm.isBlank() || phone.isBlank() || nickname.isBlank()) {
                Toast.makeText(this, "비어있는 칸을 확인해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isIdChecked) {
                Toast.makeText(this, "아이디 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isPhoneChecked) {
                Toast.makeText(this, "전화번호 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidPassword(pw)) {
                warningText.visibility = TextView.VISIBLE
                Toast.makeText(this, "비밀번호 조건을 확인해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                warningText.visibility = TextView.GONE
            }
            if (pw != pwConfirm) {
                Toast.makeText(this, "비밀번호 확인이 다릅니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (nickname.length > 8) {
                Toast.makeText(this, "닉네임은 8자 이내로 작성해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase에 사용자 정보 저장
            val userRef = firebaseDB.getReference("users").child(id)
            val referralId = editTextReferral.text.toString().trim()
            val feed = if (referralId.isNotBlank()) 20 else 10 // 추천인 있으면 20, 없으면 10
            val userData = mapOf(
                "password" to pw,
                "phone" to phone,
                "nickname" to nickname,
                "money" to 0,
                "feed" to 0,
                "selectedCharacter" to null,
                "level" to 1,
                "characters" to emptyList<String>(),
                "backgrounds" to listOf("background_base"),
                "attendance" to emptyList<String>(),
                "referral" to if (referralId.isNotBlank()) referralId else null
            )
            userRef.setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 1000)
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "회원가입 실패: ${e.message}", e)
                    Toast.makeText(this, "회원가입 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun isValidIdFormat(id: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9]{5,}$")
        return regex.matches(id)
    }

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
        return regex.matches(password)
    }

    private fun showPopup(layoutResId: Int, onClose: () -> Unit) {
        val popupView = layoutInflater.inflate(layoutResId, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        val closeBtn = popupView.findViewById<Button>(R.id.popup_close_btn)
        closeBtn?.setOnClickListener {
            popupWindow.dismiss()
            onClose()
        }
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
    }

    @SuppressLint("InflateParams")
    private fun showAvailablePopup() {
        val popupView = layoutInflater.inflate(R.layout.popup_checkid_valid, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        val useBtn = popupView.findViewById<Button>(R.id.popup_use_btn)
        useBtn.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.CENTER, 0, 0)
    }

    private fun showInvalidIdPopup(editTextId: EditText) {
        showPopup(R.layout.popup_checkid_invalid) {
            editTextId.setText("")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
