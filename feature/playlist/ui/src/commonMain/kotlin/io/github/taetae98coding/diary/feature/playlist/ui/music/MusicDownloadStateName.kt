package io.github.taetae98coding.diary.feature.playlist.ui.music

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.music_download_done_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_download_failed_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_download_pending_content_description
import io.github.taetae98coding.diary.feature.playlist.ui.music_download_running_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private const val PERCENT_SCALE = 100

internal fun MusicDownloadState.Running.toPercentText(): String = "${(progress * PERCENT_SCALE).roundToInt()}%"

@Composable
internal fun MusicDownloadState.downloadStateName(): String =
    when (this) {
        is MusicDownloadState.Pending -> stringResource(Res.string.music_download_pending_content_description)
        is MusicDownloadState.Running -> stringResource(Res.string.music_download_running_content_description, toPercentText())
        is MusicDownloadState.Done -> stringResource(Res.string.music_download_done_content_description)
        is MusicDownloadState.Failed -> stringResource(Res.string.music_download_failed_content_description)
    }
