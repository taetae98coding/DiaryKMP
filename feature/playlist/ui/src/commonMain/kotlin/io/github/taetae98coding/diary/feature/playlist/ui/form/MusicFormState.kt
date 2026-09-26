package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.playlist.link.toYoutubeVideoThumbnailOrNull

@Stable
internal class MusicFormState(
    val titleState: DiaryTitleInputState,
    val artistState: MusicArtistInputState,
    val linkState: MusicLinkInputState,
    val hostState: SnackbarHostState,
) {
    // 링크는 글자마다 바뀌지만 썸네일은 영상 ID가 바뀔 때만 바뀌므로 파생 값으로 줄인다.
    val thumbnail: String by derivedStateOf { link.toYoutubeVideoThumbnailOrNull().orEmpty() }

    val detail: MusicDetail
        get() =
            MusicDetail(
                title = titleState.text.toString(),
                artist = artistState.text.toString(),
                link = link,
            )

    val link: String
        get() = linkState.text.toString()

    fun clearText() {
        titleState.clearText()
        artistState.clearText()
        linkState.clearText()
    }

    fun fill(
        link: String,
        title: String,
        artist: String,
    ) {
        if (this.link != link) return

        titleState.setText(title)
        artistState.setText(artist)
    }
}

@Composable
internal fun rememberMusicAddFormState(): MusicFormState = rememberMusicFormState(initialDetail = MusicDetail.EMPTY)

@Composable
internal fun rememberMusicDetailFormState(initialDetail: MusicDetail = MusicDetail.EMPTY): MusicFormState = rememberMusicFormState(initialDetail = initialDetail)

@Composable
private fun rememberMusicFormState(initialDetail: MusicDetail): MusicFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialDetail.title)
    val artistState = rememberMusicArtistInputState(initialText = initialDetail.artist)
    val linkState = rememberMusicLinkInputState(initialText = initialDetail.link)
    val hostState = remember { SnackbarHostState() }

    return remember(titleState, artistState, linkState, hostState) {
        MusicFormState(
            titleState = titleState,
            artistState = artistState,
            linkState = linkState,
            hostState = hostState,
        )
    }
}
