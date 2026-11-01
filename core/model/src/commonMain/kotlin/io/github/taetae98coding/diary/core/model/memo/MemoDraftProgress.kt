package io.github.taetae98coding.diary.core.model.memo

public data class MemoDraftProgress(
    val title: String,
    val description: String,
) {
    public companion object {
        public val EMPTY: MemoDraftProgress =
            MemoDraftProgress(
                title = "",
                description = "",
            )
    }
}
