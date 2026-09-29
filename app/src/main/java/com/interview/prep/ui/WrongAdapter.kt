package com.interview.prep.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.interview.prep.data.Question
import com.interview.prep.databinding.ItemWrongBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 错题列表：题名 + 状态 + 标记时间 + 移除按钮。 */
class WrongAdapter(
    private val items: List<Pair<Question, Long>>,
    private val onOpen: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<WrongAdapter.VH>() {

    private val timeFmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemWrongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (q, ts) = items[position]
        holder.binding.wTitle.text = q.title
        holder.binding.wTime.text = if (ts > 0) "标记于 " + timeFmt.format(Date(ts)) else ""
        holder.binding.wRemove.setOnClickListener { onRemove(q.id) }
        holder.binding.root.setOnClickListener { onOpen(q.id) }
    }

    class VH(val binding: ItemWrongBinding) : RecyclerView.ViewHolder(binding.root)
}
