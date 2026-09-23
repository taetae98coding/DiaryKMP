package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail

@Stable
internal class MusicFormState(
    val linkState: MusicLinkInputState,
    val titleState: DiaryTitleInputState,
    val artistState: MusicArtistInputState,
    val hostState: SnackbarHostState,
    private val thumbnailState: MutableState<String>,
) {
    val thumbnail: String
        get() = thumbnailState.value

    val detail: MusicDetail
        get() =
            MusicDetail(
                link = linkState.text.toString(),
                title = titleState.text.toString(),
                artist = artistState.text.toString(),
                thumbnail = thumbnail,
            )

    val link: String
        get() = linkState.text.toString()

    fun clearText() {
        linkState.clearText()
        titleState.clearText()
        artistState.clearText()
        thumbnailState.value = ""
    }

    fun fill(
        title: String,
        artist: String,
        thumbnail: String,
    ) {
        titleState.setText(title)
        artistState.setText(artist)
        thumbnailState.value = thumbnail
    }
}

@Composable
internal fun rememberMusicAddFormState(): MusicFormState = rememberMusicFormState(initialDetail = MusicDetail.EMPTY)

@Composable
internal fun rememberMusicDetailFormState(initialDetail: MusicDetail = MusicDetail.EMPTY): MusicFormState = rememberMusicFormState(initialDetail = initialDetail)

@Composable
private fun rememberMusicFormState(initialDetail: MusicDetail): MusicFormState {
    val linkState = rememberMusicLinkInputState(initialText = initialDetail.link)
    val titleState = rememberDiaryTitleInputState(initialText = initialDetail.title)
    val artistState = rememberMusicArtistInputState(initialText = initialDetail.artist)
    val hostState = remember { SnackbarHostState() }
    val thumbnailState = rememberSaveable { mutableStateOf(initialDetail.thumbnail) }

    return remember(linkState, titleState, artistState, hostState, thumbnailState) {
        MusicFormState(
            linkState = linkState,
            titleState = titleState,
            artistState = artistState,
            hostState = hostState,
            thumbnailState = thumbnailState,
        )
    }
}
