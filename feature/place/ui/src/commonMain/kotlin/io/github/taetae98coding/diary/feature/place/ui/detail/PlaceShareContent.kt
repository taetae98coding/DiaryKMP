package io.github.taetae98coding.diary.feature.place.ui.detail

import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.shareTextOf
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider

internal fun placeShareContent(
    provider: DiaryMapProvider,
    coordinate: DiaryMapCoordinate,
    inputTitle: String,
    savedTitle: String,
    address: String,
): ShareContent.Text {
    val title = externalMapTitle(inputTitle = inputTitle, savedTitle = savedTitle)
    val mapUri = externalMapWebUri(provider = provider, coordinate = coordinate, title = title, address = address)

    return ShareContent.Text(text = shareTextOf(title, address, mapUri), title = title)
}
