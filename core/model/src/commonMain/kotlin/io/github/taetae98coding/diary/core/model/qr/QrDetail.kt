package io.github.taetae98coding.diary.core.model.qr

public data class QrDetail(
    val title: String,
    val description: String,
    val value: String,
) {
    public companion object {
        public val EMPTY: QrDetail =
            QrDetail(
                title = "",
                description = "",
                value = "",
            )
    }
}
