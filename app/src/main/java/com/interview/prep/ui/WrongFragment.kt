package com.interview.prep.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.interview.prep.R
import com.interview.prep.data.ProgressStore
import com.interview.prep.databinding.FragmentWrongBinding

/** 错题 Tab：模糊 + 不认识的题目，即复习队列。 */
class WrongFragment : Fragment() {

    private var _binding: FragmentWrongBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWrongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.wrongList.layoutManager = LinearLayoutManager(requireContext())
        binding.wrongEmpty.visibility = View.GONE
        refresh()
    }

    private fun refresh() {
        val wrongs = ProgressStore.wrongQuestions()
        binding.wrongCount.text = "共 ${wrongs.size} 题待复习"

        if (wrongs.isEmpty()) {
            binding.wrongList.visibility = View.GONE
            binding.wrongEmpty.visibility = View.VISIBLE
            return
        }
        binding.wrongList.visibility = View.VISIBLE
        binding.wrongEmpty.visibility = View.GONE

        val adapter = WrongAdapter(wrongs,
            onOpen = { qid -> openDetail(qid) },
            onRemove = { qid ->
                ProgressStore.markStatus(qid, ProgressStore.STATUS_KNOWN)
                Toast.makeText(requireContext(), "已标记掌握，移出错题本", Toast.LENGTH_SHORT).show()
                refresh()
            })
        binding.wrongList.adapter = adapter
    }

    private fun openDetail(qid: String) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,
                QuestionDetailFragment.newInstance(qid), "wrong_detail_$qid")
            .addToBackStack("wrong_detail")
            .commit()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
