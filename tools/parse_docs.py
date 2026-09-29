# -*- coding: utf-8 -*-
"""
解析 AutoSettings面试备考完整版.md 与 Answer.md，
生成 Android 面试 App 使用的 assets/interview_data.json。
"""
import json
import os
import re

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_DIR = os.path.dirname(BASE_DIR)  # interview 目录（两个 md 所在）
AUTO_PATH = os.path.join(SRC_DIR, "AutoSettings面试备考完整版.md")
ANSWER_PATH = os.path.join(SRC_DIR, "Answer.md")
OUT_PATH = os.path.join(BASE_DIR, "app", "src", "main", "assets", "interview_data.json")


def clean_md(text: str) -> str:
    """还原 HTML 实体与 markdown 转义，去掉引用符号，压缩空行。"""
    text = text.replace("&#x20;", " ")
    text = text.replace("&#x27;", "'")
    text = text.replace("&#34;", '"')
    text = text.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("&quot;", '"')
    text = re.sub(r"\\([<>_*])", r"\1", text)
    lines = []
    for ln in text.split("\n"):
        stripped = ln.strip()
        if stripped.startswith(">"):
            ln = stripped.lstrip(">").lstrip()
        lines.append(ln.rstrip())
    # 压缩连续空行
    out = []
    blank = False
    for ln in lines:
        if ln.strip() == "":
            if not blank:
                out.append("")
            blank = True
        else:
            out.append(ln)
            blank = False
    return "\n".join(out).strip()


FIELD_MAP = {
    "①": "origin",
    "②": "project_source",
    "③": "analysis",
    "④": "extension",
    "⑤": "review",
    "⑥": "key_points",
}

FIELD_HEAD_RE = re.compile(r"^(?:#{4}\s*|\*\*)\s*(%s)[^：:*]*[*：\s]*$" % "|".join(FIELD_MAP.keys()))


def field_of(line: str):
    """若该行是六段式字段标题，返回字段名；否则 None。"""
    m = FIELD_HEAD_RE.match(line.strip())
    if m:
        return FIELD_MAP[m.group(1)]
    return None


def parse_auto() -> list:
    with open(AUTO_PATH, encoding="utf-8") as f:
        lines = f.read().split("\n")

    categories = []
    current_cat = None
    current_q = None
    current_field = None
    cat_index = 0
    q_index = 0

    for ln in lines:
        s = ln.strip()
        if s.startswith("## ") and not s.startswith("#### "):
            name = s[3:].strip()
            if name in ("目录",):
                current_cat = None
                continue
            cat_index += 1
            current_cat = {
                "id": "auto_%d" % cat_index,
                "name": name,
                "source": "AutoSettings",
                "questions": [],
            }
            categories.append(current_cat)
            current_q = None
            current_field = None
            continue
        if current_cat is None:
            continue
        if s.startswith("### "):
            q_index += 1
            title = s[4:].strip()
            current_q = {
                "id": "auto_%d_%d" % (cat_index, q_index),
                "title": title,
                "type": "deep",
                "origin": "",
                "project_source": "",
                "analysis": "",
                "extension": "",
                "review": "",
                "key_points": "",
                "body": "",
            }
            current_cat["questions"].append(current_q)
            current_field = None
            continue
        if current_q is None:
            continue
        f = field_of(s)
        if f:
            current_field = f
            continue
        if current_field:
            if current_q[current_field]:
                current_q[current_field] += "\n" + ln
            else:
                current_q[current_field] = ln
        else:
            if current_q["body"]:
                current_q["body"] += "\n" + ln
            else:
                current_q["body"] = ln

    # 清洗 + 判定类型（无任何字段的题目视为 note）
    for cat in categories:
        for q in cat["questions"]:
            has_field = any(q[k] for k in ("origin", "project_source", "analysis",
                                           "extension", "review", "key_points"))
            if not has_field:
                q["type"] = "note"
            for k in ("origin", "project_source", "analysis", "extension", "review",
                      "key_points", "body"):
                q[k] = clean_md(q[k])
    return categories


Q_TITLE_RE = re.compile(r"^###\s*(?:[\d.]+\s*)?<span\s+id=\"[^\"]*\"\s*>(.*?)</span>\s*$")


def parse_answer() -> list:
    with open(ANSWER_PATH, encoding="utf-8") as f:
        lines = f.read().split("\n")

    categories = []
    current_cat = None
    current_q = None
    cat_index = 0
    q_index = 0
    pending = []  # 无题目章节的正文缓冲

    def flush_pending():
        nonlocal pending
        if current_cat is not None and not current_cat["questions"] and pending:
            body = "\n".join(pending)
            current_cat["questions"].append({
                "id": current_cat["id"] + "_1",
                "title": current_cat["name"],
                "type": "note",
                "origin": "",
                "project_source": "",
                "analysis": "",
                "extension": "",
                "review": "",
                "key_points": "",
                "body": body,
            })
        pending = []

    for ln in lines:
        s = ln.strip()
        if s.startswith("## "):
            flush_pending()
            name = s[3:].strip()
            cat_index += 1
            current_cat = {
                "id": "answer_%d" % cat_index,
                "name": name,
                "source": "Answer",
                "questions": [],
            }
            categories.append(current_cat)
            current_q = None
            continue
        if current_cat is None:
            continue
        m = Q_TITLE_RE.match(s)
        if m:
            q_index += 1
            title = m.group(1).strip()
            current_q = {
                "id": "answer_%d_%d" % (cat_index, q_index),
                "title": title,
                "type": "general",
                "origin": "",
                "project_source": "",
                "analysis": "",
                "extension": "",
                "review": "",
                "key_points": "",
                "body": "",
            }
            current_cat["questions"].append(current_q)
            continue
        if s.startswith("### "):
            # 无 span id 的 ### 子标题降级为粗体行
            ln = "**" + s[4:].strip() + "**"
        if current_q is not None:
            if current_q["body"]:
                current_q["body"] += "\n" + ln
            else:
                current_q["body"] = ln
        else:
            pending.append(ln)
    flush_pending()

    for cat in categories:
        for q in cat["questions"]:
            q["body"] = clean_md(q["body"])
    return categories


def main():
    auto_cats = parse_auto()
    answer_cats = parse_answer()

    total = sum(len(c["questions"]) for c in auto_cats + answer_cats)
    print("AutoSettings 分类数: %d, 题目数: %d" % (len(auto_cats),
          sum(len(c["questions"]) for c in auto_cats)))
    print("Answer 分类数: %d, 题目数: %d" % (len(answer_cats),
          sum(len(c["questions"]) for c in answer_cats)))
    print("总题目数: %d" % total)

    data = {"version": 1, "categories": auto_cats + answer_cats}
    os.makedirs(os.path.dirname(OUT_PATH), exist_ok=True)
    with open(OUT_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)

    # 验证 JSON 合法
    with open(OUT_PATH, encoding="utf-8") as f:
        json.load(f)
    print("JSON 已写入: %s (%.1f KB)" % (OUT_PATH, os.path.getsize(OUT_PATH) / 1024))


if __name__ == "__main__":
    main()
