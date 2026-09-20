package io.github.taetae98coding.diary.feature.memo.ui.gemini

internal sealed interface MemoGeminiEffect {
    data object SettingRequired : MemoGeminiEffect
}
