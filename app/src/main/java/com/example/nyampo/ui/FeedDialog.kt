package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.nyampo.R

object FeedDialog {
    fun showFeedPopup(
        context: Context,
        leafCount: Int,
        onSuccess: (newLeaf: Int, newMoney: Int, reward: Int) -> Unit
    ) {
        if (leafCount <= 0) {
            return
        }

        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_feed, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val confirmBtn = view.findViewById<Button>(R.id.button_feed_confirm)
        val textView = view.findViewById<TextView>(R.id.textView)

        val reward = (10..100).random()
        textView.text = "$reward 시루 획득!"

        confirmBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            onSuccess(leafCount - 1, reward, reward)
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    fun showGetFeedPopup(
        context: Context,
        message: String = "먹이 획득!",
        iconResId: Int = R.drawable.leaf_icon,
        onReward: ((Int) -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_get_feed, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val confirmBtn = view.findViewById<Button>(R.id.button_feed_confirm)
        val messageText = view.findViewById<TextView>(R.id.textView)
        val iconView = view.findViewById<ImageView>(R.id.leaf_icon)

        messageText.text = message
        iconView.setImageResource(iconResId)

        confirmBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            onReward?.invoke(1)
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

}
