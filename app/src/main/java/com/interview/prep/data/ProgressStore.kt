package com.interview.prep.data

import android.content.Context
import org.json.JSONObject

/**
 * 学习进度存储（SharedPreferences + JSON）。
 *
 * 状态值 STATUS_*：0 未学 / 1 掌握 / 2 模糊 / 3 不认识
 * 错题集合：status = 2 或 3 的题目（带最近一次标记时间）。
 */
object ProgressStore {

    const val STATUS_NONE = 0
    const val STATUS_KNOWN = 1
    const val STATUS_FUZZY = 2
    const val STATUS_UNKNOWN = 3

    private const val PREFS = "interview_progress"
    private const val KEY_STATES = "states"      // {"qid": status}
    private const val KEY_STARS = "stars"        // {"qid": true}
    private const val KEY_WRONG_TIME = "wrong_time" // {"qid": timestamp}

    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    // ---- states ----
    fun status(qid: String): Int =
        states().optInt(qid, STATUS_NONE)

    fun markStatus(qid: String, status: Int) {
        val s = states()
        s.put(qid, status)
        save(KEY_STATES, s)
        if (status == STATUS_FUZZY || status == STATUS_UNKNOWN) {
            val t = wrongTime()
            t.put(qid, System.currentTimeMillis())
            save(KEY_WRONG_TIME, t)
        } else {
            val t = wrongTime()
            t.remove(qid)
            save(KEY_WRONG_TIME, t)
        }
    }

    /** 错题（模糊 + 不认识），按最近标记时间倒序 */
    fun wrongQuestions(): List<Pair<Question, Long>> {
        val states = states()
        val times = wrongTime()
        return QuestionBank.allQuestions
            .filter { states.optInt(it.id, STATUS_NONE) == STATUS_FUZZY ||
                states.optInt(it.id, STATUS_NONE) == STATUS_UNKNOWN }
            .map { it to times.optLong(it.id, 0L) }
            .sortedByDescending { it.second }
    }

    // ---- stars ----
    fun isStarred(qid: String): Boolean = stars().optBoolean(qid, false)

    fun toggleStar(qid: String): Boolean {
        val s = stars()
        val now = !s.optBoolean(qid, false)
        s.put(qid, now)
        save(KEY_STARS, s)
        return now
    }

    fun starredQuestions(): List<Question> {
        val stars = stars()
        return QuestionBank.allQuestions.filter { stars.optBoolean(it.id, false) }
    }

    // ---- stats ----
    fun studiedCount(): Int = states().length()

    fun knownCount(): Int = countOf(STATUS_KNOWN)

    fun fuzzyCount(): Int = countOf(STATUS_FUZZY)

    fun unknownCount(): Int = countOf(STATUS_UNKNOWN)

    fun categoryProgress(catId: String): Pair<Int, Int> {
        val questions = QuestionBank.all.firstOrNull { it.id == catId }?.questions ?: return 0 to 0
        val done = questions.count { states().optInt(it.id, STATUS_NONE) != STATUS_NONE }
        return done to questions.size
    }

    private fun countOf(status: Int): Int {
        val s = states()
        var n = 0
        val keys = s.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            if (s.optInt(k, STATUS_NONE) == status) n++
        }
        return n
    }

    fun resetAll() {
        prefs.edit().remove(KEY_STATES).remove(KEY_STARS).remove(KEY_WRONG_TIME).apply()
    }

    // ---- json helpers ----
    private fun states(): JSONObject =
        JSONObject(prefs.getString(KEY_STATES, "{}") ?: "{}")

    private fun stars(): JSONObject =
        JSONObject(prefs.getString(KEY_STARS, "{}") ?: "{}")

    private fun wrongTime(): JSONObject =
        JSONObject(prefs.getString(KEY_WRONG_TIME, "{}") ?: "{}")

    private fun save(key: String, jo: JSONObject) {
        prefs.edit().putString(key, jo.toString()).apply()
    }
}
