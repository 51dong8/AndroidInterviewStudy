package com.interview.prep.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.interview.prep.R
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.FragmentQuestionListBinding

/** 分类下的题目列表。 */
class QuestionListFragment : Fragment() {

    private var _binding: FragmentQuestionListBinding? = null
    private val binding get() = _binding!!

    private var catId: String = ""
    private var catName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catId = arguments?.getString(ARG_CAT_ID) ?: ""
        catName = arguments?.getString(ARG_CAT_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.qlTitle.text = catName
        binding.qlBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        val category = QuestionBank.all.firstOrNull { it.id == catId }
        val questions = category?.questions ?: emptyList()
        binding.qlCount.text = "共 ${questions.size} 题"

        binding.qlList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = QuestionListAdapter { qid, _ ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                    QuestionDetailFragment.newInstance(qid), "detail_$qid")
                .addToBackStack("detail")
                .commit()
        }
        binding.qlList.adapter = adapter
        adapter.submitList(questions)
    }

    override fun onResume() {
        super.onResume()
        (binding.qlList.adapter as? QuestionListAdapter)?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CAT_ID = "cat_id"
        private const val ARG_CAT_NAME = "cat_name"

        fun newInstance(catId: String, catName: String): QuestionListFragment =
            QuestionListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CAT_ID, catId)
                    putString(ARG_CAT_NAME, catName)
                }
            }
    }
}
