package io.github.taetae98coding.diary.core.model.memo

public data class MemoDetail(
    val title: String,
    val description: String,
    val color: Long,
    val dateTime: MemoDateTime?,
) {
    public companion object {
        public val EMPTY: MemoDetail = MemoDetail(title = "", description = "", color = 0L, dateTime = null)
    }
}
