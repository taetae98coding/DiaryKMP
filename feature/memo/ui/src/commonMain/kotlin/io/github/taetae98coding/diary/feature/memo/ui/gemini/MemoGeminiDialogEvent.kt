package io.github.taetae98coding.diary.feature.memo.ui.gemini

internal sealed interface MemoGeminiDialogEvent {
    data class ClickGenerate(
        val prompt: String,
    ) : MemoGeminiDialogEvent

    data object ClickCancel : MemoGeminiDialogEvent

    data class ClickApply(
        val field: MemoGeminiField,
    ) : MemoGeminiDialogEvent
}
