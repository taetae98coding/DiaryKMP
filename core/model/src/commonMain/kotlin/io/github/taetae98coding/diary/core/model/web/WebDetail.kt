package io.github.taetae98coding.diary.core.model.web

public data class WebDetail(
    val title: String,
    val description: String,
    val url: String,
    val headerList: List<WebHeader>,
) {
    public companion object {
        public val EMPTY: WebDetail =
            WebDetail(
                title = "",
                description = "",
                url = "",
                headerList = emptyList(),
            )
    }
}
