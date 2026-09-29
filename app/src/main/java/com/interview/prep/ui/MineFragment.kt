package com.interview.prep.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.FragmentMineBinding

/** 我的 Tab：学习统计 + 分类进度 + 重置。 */
class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()

        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("重置学习进度")
                .setMessage("将清空所有掌握状态、错题本和收藏，确定继续吗？")
                .setPositiveButton("重置") { _, _ ->
                    ProgressStore.resetAll()
                    refresh()
                    Toast.makeText(requireContext(), "已重置", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun refresh() {
        val total = QuestionBank.totalCount
        val studied = ProgressStore.studiedCount()
        val known = ProgressStore.knownCount()
        val review = ProgressStore.fuzzyCount() + ProgressStore.unknownCount()
        val starred = ProgressStore.starredQuestions().size

        binding.statTotal.text = total.toString()
        binding.statStudied.text = studied.toString()
        binding.statKnown.text = known.toString()
        binding.statReview.text = review.toString()
        binding.statStar.text = starred.toString()

        binding.overallProgress.text = if (total == 0) "0%" else "${(studied * 100 / total)}%"
        binding.overallBar.max = total
        binding.overallBar.progress = studied

        // 分类进度
        val container = binding.catProgressList
        container.removeAllViews()
        for (cat in QuestionBank.all) {
            val (done, size) = ProgressStore.categoryProgress(cat.id)
            val row = com.google.android.material.card.MaterialCardView(requireContext())
            row.radius = 12f
            row.setCardBackgroundColor(0xFFF7F8FA.toInt())
            val inner = LinearLayout(requireContext())
            inner.orientation = LinearLayout.VERTICAL
            inner.setPadding(dp(14), dp(10), dp(14), dp(10))

            val name = android.widget.TextView(requireContext())
            name.text = "${cat.name}（$done/$size）"
            name.setTextColor(0xFF202124.toInt())
            name.textSize = 14f
            name.setTypeface(name.typeface, android.graphics.Typeface.BOLD)
            inner.addView(name)

            val bar = android.widget.ProgressBar(requireContext(), null,
                android.R.attr.progressBarStyleHorizontal)
            bar.max = size
            bar.progress = done
            val barLp = android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(6))
            barLp.topMargin = dp(8)
            inner.addView(bar, barLp)

            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 0, 0, dp(8))
            container.addView(row, lp)
            row.addView(inner)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
