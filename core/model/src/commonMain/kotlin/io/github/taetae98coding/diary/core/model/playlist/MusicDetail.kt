package io.github.taetae98coding.diary.core.model.playlist

public data class MusicDetail(
    val title: String,
    val artist: String,
) {
    public companion object {
        public val EMPTY: MusicDetail =
            MusicDetail(
                title = "",
                artist = "",
            )
    }
}
