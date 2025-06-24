package com.example.nyampo.ui

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import com.example.nyampo.R

object TutorialDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.tutorial_dialog, null)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)

        val imageView = view.findViewById<ImageView>(R.id.imageView_tutorial)
        val buttonNext = view.findViewById<Button>(R.id.button_next)
        val buttonPrev = view.findViewById<Button>(R.id.button_prev)
        val buttonClose = view.findViewById<Button>(R.id.button_close)
        // 현재 페이지 상태
        var currentPage = 1

        fun loadSvg(resourceId: Int) {
            val svg = SVG.getFromResource(context, resourceId)
            val drawable = PictureDrawable(svg.renderToPicture())
            imageView.setImageDrawable(drawable)
        }

        // 초기 상태: 1페이지
        loadSvg(R.raw.tutorial_page_1)
        buttonPrev.visibility = View.GONE
        buttonClose.visibility = View.GONE
        buttonNext.visibility = View.VISIBLE

        buttonNext.setOnClickListener {
            loadSvg(R.raw.tutorial_page_2)
            currentPage = 2
            buttonPrev.visibility = View.VISIBLE
            buttonClose.visibility = View.VISIBLE
            buttonNext.visibility = View.GONE
        }

        buttonPrev.setOnClickListener {
            loadSvg(R.raw.tutorial_page_1)
            currentPage = 1
            buttonPrev.visibility = View.GONE
            buttonClose.visibility = View.GONE
            buttonNext.visibility = View.VISIBLE
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
