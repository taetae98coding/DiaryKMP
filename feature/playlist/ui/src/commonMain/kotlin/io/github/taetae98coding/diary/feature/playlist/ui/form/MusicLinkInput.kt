package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.music.MusicThumbnail
import io.github.taetae98coding.diary.feature.playlist.ui.music_link_fetch_button_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_link_fetch_button_label
import io.github.taetae98coding.diary.feature.playlist.ui.music_link_input_label
import io.github.taetae98coding.diary.feature.playlist.ui.music_thumbnail_preview_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MusicLinkInput(
    onFetchClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: MusicLinkInputState = rememberMusicLinkInputState(),
    isFetchInProgressProvider: () -> Boolean = { false },
    thumbnailProvider: () -> String = { "" },
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
        ) {
            ClearTextField(
                state = state.textFieldState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(state.focusRequester),
                label = { Text(text = stringResource(Res.string.music_link_input_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                lineLimits = TextFieldLineLimits.SingleLine,
            )
            FetchButton(
                onClick = onFetchClick,
                modifier = Modifier.padding(horizontal = MusicLinkInputDefaults.FetchButtonHorizontalPadding, vertical = MusicLinkInputDefaults.FetchButtonVerticalPadding),
                isFetchInProgressProvider = isFetchInProgressProvider,
            )
            ThumbnailPreview(thumbnailProvider = thumbnailProvider)
        }
    }
}

@Composable
private fun FetchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFetchInProgressProvider: () -> Boolean = { false },
) {
    val contentDescription = stringResource(Res.string.music_link_fetch_button_content_description)

    TextButton(
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
    ) {
        DiaryCrossfade(targetState = isFetchInProgressProvider()) { isFetchInProgress ->
            if (isFetchInProgress) {
                CircularWavyProgressIndicator(modifier = Modifier.size(MusicLinkInputDefaults.FetchInProgressIndicatorSize))
            } else {
                Text(text = stringResource(Res.string.music_link_fetch_button_label))
            }
        }
    }
}

@Composable
private fun ThumbnailPreview(
    modifier: Modifier = Modifier,
    thumbnailProvider: () -> String = { "" },
) {
    val contentDescription = stringResource(Res.string.music_thumbnail_preview_content_description)

    AnimatedVisibility(
        visible = thumbnailProvider().isNotBlank(),
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        MusicThumbnail(
            modifier = Modifier.semantics { this.contentDescription = contentDescription },
            thumbnailProvider = thumbnailProvider,
        )
    }
}

@ComponentPreview
@Composable
private fun MusicLinkInputPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFetchInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            MusicLinkInput(
                onFetchClick = {},
                state = rememberMusicLinkInputState(initialText = "https://youtu.be/dQw4w9WgXcQ"),
                isFetchInProgressProvider = { isFetchInProgress },
                thumbnailProvider = { "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg" },
            )
        }
    }
}
