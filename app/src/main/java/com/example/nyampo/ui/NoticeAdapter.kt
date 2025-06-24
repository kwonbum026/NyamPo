package com.example.nyampo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nyampo.R
import com.example.nyampo.model.Notice

class NoticeAdapter(private val notices: List<Notice>) : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.text_notice_date)
        val titleText: TextView = itemView.findViewById(R.id.text_notice_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notice, parent, false)
        return NoticeViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = notices[position]
        holder.dateText.text = "📅 ${notice.date}"
        holder.titleText.text = notice.title

        // 🔍 클릭 시 상세 내용 다이얼로그 띄우기
        holder.itemView.setOnClickListener {
            NoticeDetailDialog.show(holder.itemView.context, notice.date, notice.content)
        }
    }

    override fun getItemCount(): Int = notices.size
}
