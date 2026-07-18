package io.github.taetae98coding.diary.core.model.tag

public data class TagDetail(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Long,
) {
    public val emojiWithTitle: String
        get() = if (emoji.isEmpty()) title else "$emoji $title"

    public companion object {
        public val EMPTY: TagDetail = TagDetail(emoji = "", title = "", description = "", color = 0L)
    }
}
