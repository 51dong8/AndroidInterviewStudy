# Android 面试刷题 App

基于你文件夹中两份文档（`AutoSettings面试备考完整版.md` 深度题 + `Answer.md` 通用题）构建的 Android 面试刷题应用，共 **169 道题 / 17 个分类**。

## 运行方式（推荐 Android Studio）

1. 用 **Android Studio** 打开本目录：`C:\Users\Admin\Desktop\interview\AndroidInterviewApp`
2. 若提示 Gradle JDK 问题：`File > Settings > Build, Execution, Deployment > Build Tools > Gradle`，将 **Gradle JDK** 选为 `C:\Users\Admin\.jdks\jdk-17`（本机已装好 JDK 17；本机 Android Studio 自带的 JDK 25 与 Kotlin 1.9 不兼容，务必用 JDK 17）
3. 连接设备或启动模拟器后，点 Run（绿色三角）即可安装运行

也可以命令行构建：

```
C:\Users\Admin\.jdks\jdk-17\bin\java -version
set JAVA_HOME=C:\Users\Admin\.jdks\jdk-17
set ANDROID_HOME=C:\Users\Admin\AppData\Local\Android\Sdk
gradlew.bat assembleDebug
```

APK 输出位置：`app\build\outputs\apk\debug\app-debug.apk`（已构建成功，约 6MB）。

## 功能

| Tab | 说明 |
|---|---|
| 学习 | 17 个知识分类 → 题目列表（带掌握状态）→ 分层详情（题目原文 / 项目出处 / 深度解读 / 知识扩展 / 复习题 / 背诵要点） |
| 刷题 | 一屏一题，上下滑动切换；**先回忆再展开答案**（主动回忆）；三档自评：掌握了 / 模糊 / 不认识；支持按分类筛选、随机重排、收藏 |
| 错题 | 模糊 + 不认识的题自动进入复习队列，按标记时间排序，可一键标记已掌握 |
| 我的 | 学习统计（总题数 / 已学习 / 已掌握 / 待复习 / 收藏）+ 分类进度 + 重置进度 |

## 数据说明

- 题库：`app/src/main/assets/interview_data.json`（由 `tools/parse_docs.py` 从两份 md 自动解析生成）
- 学习进度保存在本地 SharedPreferences，卸载应用或点击"重置"会清空
- 文档中少数仅列题目无答案的题（如"Android 高级知识点"题单），App 会显示"本题暂无内容"提示，可自行对照文档补充

## 技术栈

Kotlin + XML + Material Components + ViewPager2（垂直翻页）+ ViewBinding，无网络依赖，数据全部内置。
构建环境：Gradle 8.6 / AGP 8.4.0 / Kotlin 1.9.24 / JDK 17 / compileSdk 34 / minSdk 24（Android 7.0+）。
