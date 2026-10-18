package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider

@Stable
internal fun interface ExternalMapOpener {
    suspend fun open(
        provider: DiaryMapProvider,
        coordinate: DiaryMapCoordinate,
        title: String,
        address: String,
    ): Boolean
}

@Composable
internal expect fun rememberExternalMapOpener(): ExternalMapOpener
