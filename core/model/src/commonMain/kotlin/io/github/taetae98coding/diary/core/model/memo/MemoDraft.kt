package io.github.taetae98coding.diary.core.model.memo

public data class MemoDraft(
    val title: String,
    val description: String,
    val dateTime: MemoDateTime?,
) {
    public companion object {
        public val EMPTY: MemoDraft =
            MemoDraft(
                title = "",
                description = "",
                dateTime = null,
            )
    }
}
