package com.example.nyampo.admin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nyampo.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {
    private val dbUrl = "https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val titleEdit = findViewById<EditText>(R.id.edit_notice_title)
        val bodyEdit  = findViewById<EditText>(R.id.edit_notice_body)
        val postBtn   = findViewById<Button>(R.id.btn_post_notice)
        val infoText  = findViewById<TextView>(R.id.text_admin_info)
        val listView  = findViewById<ListView>(R.id.list_user_stats)

        // Firebase 레퍼런스
        val noticeRef = FirebaseDatabase.getInstance(dbUrl).getReference("notice").child("latest")
        val usersRef  = FirebaseDatabase.getInstance(dbUrl).getReference("users")

        // 공지 등록
        postBtn.setOnClickListener {
            val title = titleEdit.text.toString().trim()
            val body  = bodyEdit.text.toString().trim()
            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 타임스탬프
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            // 저장할 데이터
            val data = mapOf(
                "title"     to title,
                "body"      to body,
                "timestamp" to ts
            )

            noticeRef.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "공지사항 등록 완료", Toast.LENGTH_SHORT).show()
                    titleEdit.text.clear()
                    bodyEdit.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "공지 등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 사용자 통계 조회
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val items = mutableListOf<String>()
                for (child in snap.children) {
                    val id    = child.key ?: continue
                    if (id == "admin") continue
                    val feed  = child.child("feed").getValue(Int::class.java) ?: 0
                    val money = child.child("money").getValue(Int::class.java) ?: 0
                    items.add("ID: $id | 먹이: $feed | 머니: $money")
                }
                listView.adapter = ArrayAdapter(this@AdminActivity,
                    android.R.layout.simple_list_item_1, items)
                infoText.text = "총 사용자 수: ${items.size}"
            }
            override fun onCancelled(e: DatabaseError) {
                Toast.makeText(this@AdminActivity,
                    "유저 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}