package com.interview.prep.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.interview.prep.R
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.FragmentQuestionDetailBinding

/** 题目详情页：按题型分层展示（题目原文 → 出处 → 解读 → 扩展 → 复习 → 背诵要点）。 */
class QuestionDetailFragment : Fragment() {

    private var _binding: FragmentQuestionDetailBinding? = null
    private val binding get() = _binding!!

    private var qid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qid = arguments?.getString(ARG_QID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.detailBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val q = QuestionBank.findQuestion(qid)
        if (q == null) {
            binding.detailTitle.text = "题目不存在"
            return
        }

        binding.detailTitle.text = q.title
        binding.detailStar.isChecked = ProgressStore.isStarred(q.id)
        binding.detailStar.setOnClickListener {
            val starred = ProgressStore.toggleStar(q.id)
            binding.detailStar.isChecked = starred
        }

        val container = binding.detailContent
        fun section(title: String, content: String, compact: Boolean = false) {
            if (content.isBlank()) return
            val header = android.widget.TextView(requireContext())
            header.text = title
            header.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            header.textSize = 15f
            header.setTypeface(header.typeface, android.graphics.Typeface.BOLD)
            header.setPadding(0, dp(10), 0, dp(2))
            container.addView(header)
            val md = MarkdownView(requireContext())
            md.render(content, compact)
            container.addView(md)
        }

        when (q.type) {
            "deep" -> {
                section("题目原文", q.origin)
                section("项目出处", q.projectSource, compact = true)
                section("深度解读", q.analysis)
                section("知识扩展", q.extension)
                section("复习题", q.review)
                section("背诵要点", q.keyPoints)
            }
            else -> {
                section("参考答案", q.body)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        private const val ARG_QID = "qid"

        fun newInstance(qid: String): QuestionDetailFragment =
            QuestionDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_QID, qid) }
            }
    }
}
