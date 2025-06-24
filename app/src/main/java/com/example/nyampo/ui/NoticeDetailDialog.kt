package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.example.nyampo.R

object NoticeDetailDialog {
    fun show(context: Context, date: String, content: String) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_notice_detail, null)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dateText = view.findViewById<TextView>(R.id.text_detail_date)
        val contentText = view.findViewById<TextView>(R.id.text_detail_content)
        val closeButton = view.findViewById<Button>(R.id.button_close_detail)

        dateText.text = "📅 $date"
        contentText.text = content

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
