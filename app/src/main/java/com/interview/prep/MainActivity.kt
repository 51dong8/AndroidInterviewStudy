package com.interview.prep

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.interview.prep.data.ProgressStore
import com.interview.prep.data.QuestionBank
import com.interview.prep.databinding.ActivityMainBinding
import com.interview.prep.ui.LearnFragment
import com.interview.prep.ui.MineFragment
import com.interview.prep.ui.PracticeFragment
import com.interview.prep.ui.WrongFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragments = mutableMapOf<Int, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        QuestionBank.load(this)
        ProgressStore.init(this)

        binding.bottomNav.setOnItemSelectedListener { item ->
            switchTo(item.itemId)
            true
        }
        // 恢复状态时避免重复添加
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_learn
        }
    }

    private fun switchTo(menuId: Int) {
        val tag = menuId.toString()
        val fm = supportFragmentManager
        val existing = fm.findFragmentByTag(tag)
        val target = existing ?: fragments.getOrPut(menuId) { createFragment(menuId) }

        fm.beginTransaction()
            .replace(R.id.fragment_container, target, tag)
            .commitAllowingStateLoss()
    }

    private fun createFragment(menuId: Int): Fragment = when (menuId) {
        R.id.nav_learn -> LearnFragment()
        R.id.nav_practice -> PracticeFragment()
        R.id.nav_wrong -> WrongFragment()
        R.id.nav_mine -> MineFragment()
        else -> LearnFragment()
    }
}
