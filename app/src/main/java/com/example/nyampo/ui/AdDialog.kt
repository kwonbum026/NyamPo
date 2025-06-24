package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import com.example.nyampo.R
import com.example.nyampo.MainActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AdDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_ad, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val buttonBack = view.findViewById<ImageButton>(R.id.imageButton_back)
        val buttonGetFeed = view.findViewById<Button>(R.id.button_get_feed)
        buttonGetFeed.isEnabled = false  // 초기 비활성화
        val timerText = view.findViewById<TextView>(R.id.text_ad_timer)

        buttonGetFeed.isEnabled = false // 초기 비활성화


        buttonGetFeed.setOnClickListener {
            // 중복 클릭 방지
            buttonGetFeed.isEnabled = false
            buttonGetFeed.text = "획득 완료!"

            // 먹이 +1 처리
            val prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
            val currentFeed = prefs.getInt("leafCount", 0)
            val newFeed = currentFeed + 2
            prefs.edit().putInt("leafCount", newFeed).apply()

            // Firebase 반영
            val userId = (context as? MainActivity)?.intent?.getStringExtra("userId")
            if (!userId.isNullOrEmpty()) {
                val userRef = FirebaseDatabase
                    .getInstance("https://nyampo-7d71d-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users")
                    .child(userId)
                userRef.child("feed").setValue(newFeed)

                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                userRef.child("adsWatched").child(today).setValue(true)
            }

            Toast.makeText(context, "먹이를 1개 획득했습니다!", Toast.LENGTH_SHORT).show()
        }

        buttonBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
}