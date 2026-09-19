package io.github.taetae98coding.diary.core.model.playlist

public data class MusicDetail(
    val link: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
) {
    public companion object {
        public val EMPTY: MusicDetail =
            MusicDetail(
                link = "",
                title = "",
                artist = "",
                thumbnail = "",
            )
    }
}
