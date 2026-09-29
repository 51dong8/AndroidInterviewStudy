package com.interview.prep.data

import org.json.JSONObject

/**
 * 题目模型。两种文档来源统一成这一种结构：
 * - type=deep     AutoSettings 六段式深度题（origin/projectSource/analysis/extension/review/keyPoints）
 * - type=general  Answer.md 通用题（body 为参考答案正文）
 * - type=note     软素质 / 整章笔记（body 为框架正文）
 */
data class Question(
    val id: String,
    val title: String,
    val type: String,
    val origin: String,
    val projectSource: String,
    val analysis: String,
    val extension: String,
    val review: String,
    val keyPoints: String,
    val body: String
) {
    companion object {
        fun fromJson(jo: JSONObject): Question = Question(
            id = jo.optString("id"),
            title = jo.optString("title"),
            type = jo.optString("type", "general"),
            origin = jo.optString("origin"),
            projectSource = jo.optString("project_source"),
            analysis = jo.optString("analysis"),
            extension = jo.optString("extension"),
            review = jo.optString("review"),
            keyPoints = jo.optString("key_points"),
            body = jo.optString("body")
        )
    }

    /** 详情页按类型展示的核心答案内容 */
    fun answerText(): String = when (type) {
        "deep" -> keyPoints.ifBlank { analysis }
        else -> body
    }
}

data class Category(
    val id: String,
    val name: String,
    val source: String,
    val questions: List<Question>
) {
    companion object {
        fun fromJson(jo: JSONObject): Category = Category(
            id = jo.optString("id"),
            name = jo.optString("name"),
            source = jo.optString("source"),
            questions = run {
                val arr = jo.optJSONArray("questions") ?: return@run emptyList()
                (0 until arr.length()).map { Question.fromJson(arr.getJSONObject(it)) }
            }
        )
    }
}
