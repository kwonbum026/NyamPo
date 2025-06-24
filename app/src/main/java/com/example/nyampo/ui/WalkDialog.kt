package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import com.example.nyampo.GPSActivity
import com.example.nyampo.RunActivity
import com.example.nyampo.R

object WalkDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_chose_run, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        val runButton = view.findViewById<Button>(R.id.btn_change_mascot)
        val landmarkButton = view.findViewById<Button>(R.id.btn_change_background)

        val runIcon = view.findViewById<ImageButton>(R.id.runIcon)
        val lighthouseIcon = view.findViewById<ImageView>(R.id.lighthouseIcon)

        runButton.setOnClickListener {
            val mascotIndex = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
                .getInt("selectedMascotIndex", 0)

            val intent = Intent(context, RunActivity::class.java)
            intent.putExtra("mascotIndex", mascotIndex)
            context.startActivity(intent)
            dialog.dismiss()
        }

        landmarkButton.setOnClickListener {
            context.startActivity(Intent(context, GPSActivity::class.java))
            dialog.dismiss()
        }

        runIcon.setOnClickListener {
            val mascotIndex = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
                .getInt("selectedMascotIndex", 0)

            val intent = Intent(context, RunActivity::class.java)
            intent.putExtra("mascotIndex", mascotIndex)
            context.startActivity(intent)
            dialog.dismiss()
        }

        lighthouseIcon.setOnClickListener {
            context.startActivity(Intent(context, GPSActivity::class.java))
            dialog.dismiss()
        }

        dialog.show()
    }
}
