package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import com.example.nyampo.LoginActivity
import com.example.nyampo.R

object SettingDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_setting, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val buttonTutorial = view.findViewById<Button>(R.id.button_tutorial)
        val buttonLogout = view.findViewById<Button>(R.id.button_logout)

        buttonTutorial.setOnClickListener {
            TutorialDialog.show(context)
        }


        buttonLogout.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)

        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
}
