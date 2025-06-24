package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nyampo.R
import com.example.nyampo.model.Notice
import com.google.firebase.database.FirebaseDatabase

object NoticeDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.notice_dialog_recycler, null)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_notice)
        val buttonClose = view.findViewById<Button>(R.id.button_close_notice)

        val dbUrl = "https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app"
        val noticeRef = FirebaseDatabase.getInstance(dbUrl).getReference("notice").child("latest")


        // ✅ 공지사항 리스트 (최신이 위로)
        val notices = listOf(
            Notice("2025-06-15", "v1.2 업데이트", "- QR 배경 해금 추가\n- 캐릭터 초기화 개선"),
            Notice("2025-06-10", "미션 시스템 개선", "- 티노 밸런스 조정 등"),
            Notice("2025-06-01", "앱 첫 출시! 🎉", "- 캐릭터 3종 / 배경 5종 제공")
        )


        noticeRef.get().addOnSuccessListener { snapshot ->
            val title = snapshot.child("title").getValue(String::class.java) ?: ""
            val body = snapshot.child("body").getValue(String::class.java) ?: ""
            val timestamp = snapshot.child("timestamp").getValue(String::class.java) ?: ""

            val notices = listOf(Notice(timestamp, title, body))

            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = NoticeAdapter(notices)
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
