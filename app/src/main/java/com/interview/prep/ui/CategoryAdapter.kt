package com.interview.prep.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.interview.prep.data.Category
import com.interview.prep.data.ProgressStore
import com.interview.prep.databinding.ItemCategoryBinding

/** 分类列表适配器：名称 + 来源标识 + 进度。 */
class CategoryAdapter(
    private val onClick: (catId: String, catName: String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var items: List<Category> = emptyList()

    fun submitList(list: List<Category>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cat = items[position]
        val (done, total) = ProgressStore.categoryProgress(cat.id)
        holder.binding.catName.text = cat.name
        holder.binding.catSource.text =
            if (cat.source == "AutoSettings") "项目深题" else "通用题库"
        holder.binding.catProgress.text = "$done/$total"
        holder.binding.catBar.max = total
        holder.binding.catBar.progress = done
        holder.binding.root.setOnClickListener { onClick(cat.id, cat.name) }
    }

    class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)
}
