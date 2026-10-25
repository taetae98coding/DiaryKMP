package io.github.taetae98coding.diary.core.model.checklist

public data class ChecklistDetail(
    val title: String,
    val description: String,
) {
    public companion object {
        public val EMPTY: ChecklistDetail =
            ChecklistDetail(
                title = "",
                description = "",
            )
    }
}
