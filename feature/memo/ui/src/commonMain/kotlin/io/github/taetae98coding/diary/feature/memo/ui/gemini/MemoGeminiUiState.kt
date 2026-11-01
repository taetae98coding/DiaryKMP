package io.github.taetae98coding.diary.feature.memo.ui.gemini

import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress

internal data class MemoGeminiUiState(
    val isButtonVisible: Boolean = false,
    val step: MemoGeminiStep = MemoGeminiStep.CLOSED,
    val draft: MemoDraft = MemoDraft.EMPTY,
    val appliedFieldSet: Set<MemoGeminiField> = emptySet(),
    val progress: MemoDraftProgress = MemoDraftProgress.EMPTY,
    val failure: MemoGeminiFailure? = null,
    val failureDescription: String = "",
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
    QUOTA_EXCEEDED,
    MODEL_NOT_FOUND,
    BLOCKED,
    TRUNCATED,
    TIMEOUT,
    NETWORK,
    SERVER,
    UNKNOWN,
}
