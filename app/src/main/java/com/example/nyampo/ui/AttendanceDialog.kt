package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.nyampo.R
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

object AttendanceDialog {
    private val firebaseDB = FirebaseDatabase.getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")

    fun show(context: Context, userId: String, checkIcon: ImageView, onFeedReward: () -> Unit) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_calendar, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val calendarView = view.findViewById<CalendarView>(R.id.calendar_view)
        val attendBtn = view.findViewById<Button>(R.id.button_attend)

        val prefs = context.getSharedPreferences("AttendancePrefs_${userId}", Context.MODE_PRIVATE)
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        // 오늘 날짜 UI 고정
        calendarView.date = System.currentTimeMillis()
        calendarView.minDate = System.currentTimeMillis()
        calendarView.maxDate = System.currentTimeMillis()
        calendarView.setOnTouchListener { _, _ -> true }

        val userRef = firebaseDB.getReference("users").child(userId).child("attendance")

        fun updateUI() {
            val alreadyChecked = prefs.getBoolean(todayKey, false)
            checkIcon.visibility = if (alreadyChecked) View.VISIBLE else View.GONE
            attendBtn.text = if (alreadyChecked) "✅ 출석 완료" else "출석하기"
            attendBtn.isEnabled = !alreadyChecked
        }

        updateUI()

        // 출석 버튼 클릭 시: SharedPref + Firebase 동시 저장
        attendBtn.setOnClickListener {
            prefs.edit().putBoolean(todayKey, true).apply()
            userRef.child(todayKey).setValue(true)
            updateUI()
            onFeedReward()
        }

        // 날짜 클릭 시: 해당 날짜가 출석한 날인지 확인
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val picked = String.format("%04d%02d%02d", year, month + 1, dayOfMonth)
            userRef.child(picked).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(context, "✅ ${picked} 출석함", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "❌ ${picked} 출석 안 함", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}