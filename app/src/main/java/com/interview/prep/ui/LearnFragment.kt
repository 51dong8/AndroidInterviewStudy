package com.interview.prep.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.interview.prep.R
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.FragmentLearnBinding

/** 学习 Tab：展示全部知识分类，点击进入题目列表。 */
class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.categoryList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CategoryAdapter { catId, catName ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                    QuestionListFragment.newInstance(catId, catName), "qlist_$catId")
                .addToBackStack("qlist")
                .commit()
        }
        binding.categoryList.adapter = adapter
        adapter.submitList(QuestionBank.all)
    }

    override fun onResume() {
        super.onResume()
        (binding.categoryList.adapter as? CategoryAdapter)?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
