package io.github.taetae98coding.diary.feature.memo.ui.gemini

import io.github.taetae98coding.diary.core.model.memo.MemoDraft

internal data class MemoGeminiUiState(
    val isButtonVisible: Boolean = false,
    val step: MemoGeminiStep = MemoGeminiStep.CLOSED,
    val draft: MemoDraft = MemoDraft.EMPTY,
    val appliedFieldSet: Set<MemoGeminiField> = emptySet(),
    val failure: MemoGeminiFailure? = null,
) {
    val hasDraft: Boolean
        get() = memoGeminiFieldList.any { field -> field.hasValue(draft = draft) }
}

internal enum class MemoGeminiStep {
    CLOSED,
    PROMPT,
    GENERATING,
    RESULT,
}

internal enum class MemoGeminiField {
    TITLE,
    DESCRIPTION,
    DATE_TIME,
    ;

    fun hasValue(draft: MemoDraft): Boolean =
        when (this) {
            TITLE -> draft.title.isNotEmpty()
            DESCRIPTION -> draft.description.isNotEmpty()
            DATE_TIME -> draft.dateTime != null
        }
}

internal enum class MemoGeminiFailure {
    INVALID_API_KEY,
    UNKNOWN,
}
