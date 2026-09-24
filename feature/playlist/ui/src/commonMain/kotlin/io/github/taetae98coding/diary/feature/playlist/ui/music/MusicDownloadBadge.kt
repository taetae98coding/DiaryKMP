package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.icon.CheckIcon
import io.github.taetae98coding.diary.compose.core.icon.ErrorIcon
import io.github.taetae98coding.diary.compose.core.icon.ScheduleIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState

internal const val MUSIC_DOWNLOAD_BADGE_TEST_TAG: String = "MusicDownloadBadge"

@Composable
internal fun MusicDownloadBadge(
    state: MusicDownloadState,
    modifier: Modifier = Modifier,
) {
    val name = state.downloadStateName()

    Surface(
        modifier =
            modifier
                .defaultMinSize(minWidth = MusicDownloadBadgeDefaults.MinSize, minHeight = MusicDownloadBadgeDefaults.MinSize)
                // clearAndSetSemantics는 앞뒤에 따로 붙인 시맨틱을 지우므로 태그도 이 블록 안에서 정한다.
                .clearAndSetSemantics {
                    contentDescription = name
                    testTag = MUSIC_DOWNLOAD_BADGE_TEST_TAG
                },
        shape = CircleShape,
        color = DiaryTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (state is MusicDownloadState.Failed) DiaryTheme.colorScheme.error else DiaryTheme.colorScheme.onSurface,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = MusicDownloadBadgeDefaults.HorizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is MusicDownloadState.Pending -> ScheduleIcon(modifier = Modifier.size(MusicDownloadBadgeDefaults.IconSize))
                is MusicDownloadState.Running -> RunningContent(state = state)
                is MusicDownloadState.Done -> CheckIcon(modifier = Modifier.size(MusicDownloadBadgeDefaults.IconSize))
                is MusicDownloadState.Failed -> ErrorIcon(modifier = Modifier.size(MusicDownloadBadgeDefaults.IconSize))
            }
        }
    }
}

@Composable
private fun RunningContent(state: MusicDownloadState.Running) {
    val progress = state.progress

    if (progress == null) {
        CircularProgressIndicator(
            modifier = Modifier.size(MusicDownloadBadgeDefaults.IconSize),
            color = LocalContentColor.current,
            strokeWidth = MusicDownloadBadgeDefaults.IndeterminateStrokeWidth,
        )
    } else {
        Text(
            text = progress.toPercentText(),
            style = DiaryTheme.typography.bodySmall,
        )
    }
}

private class MusicDownloadBadgePreviewParameter : PreviewParameterProvider<MusicDownloadState> {
    override val values: Sequence<MusicDownloadState> =
        sequenceOf(
            MusicDownloadState.Pending,
            MusicDownloadState.Running(progress = null),
            MusicDownloadState.Running(progress = 0.62F),
            MusicDownloadState.Done,
            MusicDownloadState.Failed,
        )
}

@ComponentPreview
@Composable
private fun MusicDownloadBadgePreview(
    @PreviewParameter(MusicDownloadBadgePreviewParameter::class) state: MusicDownloadState,
) {
    DiaryTheme {
        MusicDownloadBadge(state = state)
    }
}
