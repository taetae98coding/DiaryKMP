package io.github.taetae98coding.diary.core.model.playlist

public data class MusicDownloadProxySetting(
    val address: String,
) {
    public companion object {
        public val EMPTY: MusicDownloadProxySetting = MusicDownloadProxySetting(address = "")
    }
}
