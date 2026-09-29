package com.interview.prep.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.Question
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.FragmentPracticeBinding

/**
 * 刷题 Tab：一屏一题，上下滑动切换。
 * 先回忆 → 展开答案 → 三档自评，模拟面试表达 + 间隔复习。
 */
class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    private var questions: List<Question> = emptyList()
    private var currentCategory: String = ALL_KEY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.practicePager.orientation = ViewPager2.ORIENTATION_VERTICAL

        // 分类筛选
        val names = listOf(ALL_KEY) + QuestionBank.all.map { it.id }
        val labels = listOf("全部题库") + QuestionBank.all.map { it.name }
        binding.practiceCategory.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        binding.practiceCategory.adapter =
            (binding.practiceCategory.adapter as ArrayAdapter<*>)
                .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.practiceCategory.setOnItemSelectedListener(object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (currentCategory == names[pos]) return
                currentCategory = names[pos]
                rebuildQuestions()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        binding.practiceShuffle.setOnClickListener {
            questions = questions.shuffled()
            binding.practicePager.adapter?.notifyDataSetChanged()
            binding.practicePager.setCurrentItem(0, false)
            Toast.makeText(requireContext(), "已随机重排", Toast.LENGTH_SHORT).show()
        }

        rebuildQuestions()
    }

    private fun rebuildQuestions() {
        questions = if (currentCategory == ALL_KEY) {
            QuestionBank.allQuestions
        } else {
            QuestionBank.all.firstOrNull { it.id == currentCategory }?.questions ?: emptyList()
        }
        binding.practiceCount.text = "共 ${questions.size} 题 · 上滑/下滑切换"
        binding.practicePager.adapter = PracticeAdapter(questions) { qid, status ->
            ProgressStore.markStatus(qid, status)
            val msg = when (status) {
                ProgressStore.STATUS_KNOWN -> "已掌握，加入复习队列"
                ProgressStore.STATUS_FUZZY -> "标记模糊，进入复习队列"
                else -> "标记不认识，进入错题本"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
        binding.practicePager.setCurrentItem(0, false)
    }

    override fun onResume() {
        super.onResume()
        (binding.practicePager.adapter as? PracticeAdapter)?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ALL_KEY = "__all__"
    }
}
