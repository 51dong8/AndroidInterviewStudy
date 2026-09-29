package com.interview.prep.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.interview.prep.R
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.Question
import com.interview.prep.databinding.ItemQuestionBinding

/** 题目列表行：标题 + 掌握状态徽标。 */
class QuestionListAdapter(
    private val onClick: (qid: String, title: String) -> Unit
) : RecyclerView.Adapter<QuestionListAdapter.VH>() {

    private var items: List<Question> = emptyList()

    fun submitList(list: List<Question>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val q = items[position]
        holder.binding.qTitle.text = q.title
        val status = ProgressStore.status(q.id)
        val (text, color) = when (status) {
            ProgressStore.STATUS_KNOWN -> "已掌握" to R.color.status_known
            ProgressStore.STATUS_FUZZY -> "模糊" to R.color.status_fuzzy
            ProgressStore.STATUS_UNKNOWN -> "待复习" to R.color.status_unknown
            else -> "未学习" to R.color.status_none
        }
        holder.binding.qBadge.text = text
        holder.binding.qBadge.setTextColor(
            ContextCompat.getColor(holder.itemView.context, color)
        )
        holder.binding.root.setOnClickListener { onClick(q.id, q.title) }
    }

    class VH(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root)
}
