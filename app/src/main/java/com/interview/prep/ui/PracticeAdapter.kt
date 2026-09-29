package com.interview.prep.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.Question
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.ItemPracticeCardBinding

/**
 * 刷题卡片适配器：每张卡片 = 一屏一题。
 * 交互：先回忆 → 展开答案 → 三档自评 → 上下滑切下一题。
 */
class PracticeAdapter(
    private val items: List<Question>,
    private val onAssess: (qid: String, status: Int) -> Unit
) : RecyclerView.Adapter<PracticeAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPracticeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position)
    }

    inner class VH(val binding: ItemPracticeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(q: Question, position: Int) {
            val catName = QuestionBank.all.firstOrNull {
                it.questions.any { qq -> qq.id == q.id }
            }?.name ?: ""
            binding.cardIndex.text = "#${position + 1}"
            binding.cardCategory.text = catName
            binding.cardTitle.text = q.title

            // 收藏
            binding.cardStar.isChecked = ProgressStore.isStarred(q.id)
            binding.cardStar.setOnClickListener {
                val starred = ProgressStore.toggleStar(q.id)
                binding.cardStar.isChecked = starred
            }

            // 答案默认折叠（主动回忆）
            binding.cardAnswerArea.removeAllViews()
            binding.cardAnswerArea.visibility = android.view.View.GONE
            binding.cardAnswerButton.visibility = android.view.View.VISIBLE
            binding.cardAnswerButton.setOnClickListener {
                val md = MarkdownView(binding.root.context)
                md.render(q.answerText(), compact = true)
                binding.cardAnswerArea.removeAllViews()
                binding.cardAnswerArea.addView(md)
                binding.cardAnswerArea.visibility = android.view.View.VISIBLE
                binding.cardAnswerButton.visibility = android.view.View.GONE
                binding.cardHint.visibility = android.view.View.GONE
            }

            // 当前状态高亮
            val status = ProgressStore.status(q.id)
            updateStatusUi(status)

            binding.btnKnown.setOnClickListener {
                onAssess(q.id, ProgressStore.STATUS_KNOWN)
                updateStatusUi(ProgressStore.STATUS_KNOWN)
            }
            binding.btnFuzzy.setOnClickListener {
                onAssess(q.id, ProgressStore.STATUS_FUZZY)
                updateStatusUi(ProgressStore.STATUS_FUZZY)
            }
            binding.btnUnknown.setOnClickListener {
                onAssess(q.id, ProgressStore.STATUS_UNKNOWN)
                updateStatusUi(ProgressStore.STATUS_UNKNOWN)
            }
        }

        private fun updateStatusUi(status: Int) {
            val ctx = binding.root.context
            val res = ctx.resources
            fun style(btn: android.widget.Button, active: Boolean, colorRes: Int) {
                if (active) {
                    btn.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(
                            res.getColor(colorRes, ctx.theme))
                    btn.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    btn.backgroundTintList =
                        android.content.res.ColorStateList.valueOf(
                            res.getColor(com.interview.prep.R.color.btn_bg, ctx.theme))
                    btn.setTextColor(res.getColor(com.interview.prep.R.color.text_main, ctx.theme))
                }
            }
            style(binding.btnKnown, status == ProgressStore.STATUS_KNOWN,
                com.interview.prep.R.color.status_known)
            style(binding.btnFuzzy, status == ProgressStore.STATUS_FUZZY,
                com.interview.prep.R.color.status_fuzzy)
            style(binding.btnUnknown, status == ProgressStore.STATUS_UNKNOWN,
                com.interview.prep.R.color.status_unknown)
        }
    }
}
