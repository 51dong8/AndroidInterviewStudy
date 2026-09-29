package com.interview.prep.data

import android.content.Context
import org.json.JSONObject

/**
 * 题库加载器：读取 assets/interview_data.json，惰性解析并缓存。
 * 所有 UI 通过 [QuestionBank] 获取分类与题目。
 */
object QuestionBank {

    private var categories: List<Category>? = null

    val all: List<Category>
        get() = categories ?: emptyList()

    val allQuestions: List<Question>
        get() = all.flatMap { it.questions }

    val totalCount: Int
        get() = allQuestions.size

    fun load(context: Context): List<Category> {
        categories?.let { return it }
        val json = context.assets.open("interview_data.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(json)
        val arr = root.getJSONArray("categories")
        val list = (0 until arr.length()).map { Category.fromJson(arr.getJSONObject(it)) }
        categories = list
        return list
    }

    fun findQuestion(id: String): Question? =
        allQuestions.firstOrNull { it.id == id }
}
