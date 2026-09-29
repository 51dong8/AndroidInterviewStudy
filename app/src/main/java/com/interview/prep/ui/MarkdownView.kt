package com.interview.prep.ui

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 轻量 Markdown 渲染器：把清洗后的题库文本渲染成一组 TextView。
 * 支持：代码块（等宽 + 灰底）、列表项、标题行、**加粗**、普通段落。
 */
class MarkdownView(context: Context) : LinearLayout(context) {

    init {
        orientation = VERTICAL
    }

    fun render(markdown: String, compact: Boolean = false) {
        removeAllViews()
        if (markdown.isBlank()) {
            addText("（本题暂无内容，建议对照原文档补充）", compact, dim = true)
            return
        }

        val bodySize = if (compact) 14f else 16f
        val inCode = arrayOf(false)
        val codeLines = StringBuilder()

        fun flushCode() {
            if (codeLines.isNotEmpty()) {
                addCodeBlock(codeLines.toString(), compact)
                codeLines.setLength(0)
            }
        }

        for (raw in markdown.lines()) {
            val line = raw.trimEnd()
            val s = line.trim()

            if (s.startsWith("```")) {
                if (inCode[0]) {
                    // 代码块结束
                    flushCode()
                    inCode[0] = false
                } else {
                    flushCode()
                    inCode[0] = true
                }
                continue
            }

            if (inCode[0]) {
                codeLines.append(line).append('\n')
                continue
            }

            when {
                s.isEmpty() -> {
                    // 段落间距由 TextView 自带 padding 承担
                }
                s.startsWith("#### ") || s.startsWith("##### ") || s.startsWith("###### ") -> {
                    flushCode()
                    addText(s.substringAfter("### ").substringAfter("#").trim(),
                        compact, bold = true, sizeScale = 1.1f, color = ACCENT)
                }
                s.startsWith("### ") -> {
                    flushCode()
                    addText(s.removePrefix("### ").trim(), compact, bold = true, sizeScale = 1.15f)
                }
                s.startsWith("## ") -> {
                    flushCode()
                    addText(s.removePrefix("## ").trim(), compact, bold = true, sizeScale = 1.2f)
                }
                s.startsWith("- ") || s.startsWith("* ") || s.startsWith("• ") -> {
                    flushCode()
                    addBullet(s.drop(2).trim(), bodySize)
                }
                s.matches(Regex("\\d+\\.\\s+.*")) && !s.contains("http") -> {
                    flushCode()
                    addText("• " + s.replaceFirst(Regex("\\d+\\.\\s+"), "").trim(), compact,
                        bold = false, bodySize = bodySize, indent = dp(16))
                }
                else -> {
                    flushCode()
                    addText(s, compact, bodySize = bodySize)
                }
            }
        }
        flushCode()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun addText(
        text: String,
        compact: Boolean,
        bold: Boolean = false,
        dim: Boolean = false,
        bodySize: Float = if (compact) 14f else 16f,
        sizeScale: Float = 1f,
        color: Int = -1,
        indent: Int = 0
    ) {
        val tv = TextView(context)
        tv.text = parseInline(text)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, bodySize * sizeScale)
        tv.setTypeface(null, if (bold) Typeface.BOLD else Typeface.NORMAL)
        tv.setTextColor(if (color >= 0) color else if (dim) 0xFF999999.toInt() else 0xFF202124.toInt())
        tv.setLineSpacing(dp(3).toFloat(), 1f)
        if (indent > 0) tv.setPadding(dp(8), 0, 0, 0)
        tv.setPaddingRelative(indent + dp(4), dp(4), dp(4), dp(4))
        addView(tv, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun addBullet(text: String, bodySize: Float) {
        val tv = TextView(context)
        val sb = SpannableStringBuilder("•  ")
        sb.append(parseInline(text))
        tv.text = sb
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, bodySize)
        tv.setTextColor(0xFF202124.toInt())
        tv.setLineSpacing(dp(3).toFloat(), 1f)
        tv.setPaddingRelative(dp(8), dp(3), dp(4), dp(3))
        addView(tv, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun addCodeBlock(code: String, compact: Boolean) {
        val tv = TextView(context)
        tv.text = code.trimEnd()
        tv.typeface = Typeface.MONOSPACE
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (compact) 12.5f else 14f)
        tv.setTextColor(0xFF3C4043.toInt())
        tv.setBackgroundColor(0xFFF1F3F4.toInt())
        tv.setPadding(dp(12), dp(8), dp(12), dp(8))
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp(4), dp(6), dp(4), dp(6))
        addView(tv, lp)
    }

    /** 解析行内 **加粗** 与 `反引号` */
    private fun parseInline(text: String): Spanned {
        val sb = SpannableStringBuilder(text)
        // **bold**
        val boldRe = Regex("\\*\\*(.+?)\\*\\*")
        for (m in boldRe.findAll(text)) {
            try {
                sb.setSpan(StyleSpan(Typeface.BOLD), m.range.first, m.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } catch (_: Exception) {
            }
        }
        // `code`
        val codeRe = Regex("`([^`]+)`")
        for (m in codeRe.findAll(text)) {
            try {
                sb.setSpan(Typeface.MONOSPACE, m.range.first, m.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } catch (_: Exception) {
            }
        }
        return sb
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        private const val ACCENT = 0xFF1A73E8.toInt()
    }
}
