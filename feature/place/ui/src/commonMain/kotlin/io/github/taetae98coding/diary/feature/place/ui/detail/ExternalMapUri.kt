package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapProvider
import io.github.taetae98coding.diary.library.kotlin.text.encodeUriComponent

private const val NAVER_MAP_URI = "https://map.naver.com/"
private const val GOOGLE_MAP_SEARCH_URI = "https://www.google.com/maps/search/"

// Google Maps URLs는 좌표를 구분하는 쉼표를 인코딩해 넘기도록 정하고 있다.
private const val ENCODED_COMMA = "%2C"

// 네이버 지도가 좌표만 넘긴 주소에 붙이는 확대 수준과 같게 둔다.
private const val EXTERNAL_MAP_ZOOM = 15

internal fun externalMapTitle(
    inputTitle: String,
    savedTitle: String,
): String = inputTitle.ifBlank { savedTitle }

// 주소는 앱 밖으로 나가는 값이므로 제공자 상수 이름에 기대지 않고 제공자별 주소를 여기에서 정한다.
internal fun externalMapUri(
    provider: DiaryMapProvider,
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
): String =
    when (provider) {
        DiaryMapProvider.NAVER -> naverMapUri(coordinate = coordinate, title = title)
        DiaryMapProvider.GOOGLE -> googleMapUri(coordinate = coordinate, title = title, address = address)
    }

// 외부 지도를 열 수 없는 환경에서도 화면을 그대로 유지하고 사용자에게 알리지 않는다.
internal fun UriHandler.openExternalMap(
    provider: DiaryMapProvider,
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
) {
    runCatching { openUri(externalMapUri(provider = provider, coordinate = coordinate, title = title, address = address)) }
}

private fun naverMapUri(
    coordinate: DiaryMapCoordinate,
    title: String,
): String {
    val location = "$NAVER_MAP_URI?lng=${coordinate.longitude}&lat=${coordinate.latitude}"

    return if (title.isBlank()) location else "$location&title=${title.encodeUriComponent()}"
}

// 검색어와 좌표를 함께 넘기는 형식은 Google Maps URLs에 없으므로, 검색어가 없을 때만 문서화된 좌표 검색 주소를 쓴다.
private fun googleMapUri(
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
): String {
    val query = googleMapQuery(title = title, address = address)

    return if (query.isBlank()) {
        "$GOOGLE_MAP_SEARCH_URI?api=1&query=${coordinate.latitude}$ENCODED_COMMA${coordinate.longitude}"
    } else {
        "$GOOGLE_MAP_SEARCH_URI${query.encodeUriComponent()}/@${coordinate.latitude},${coordinate.longitude},${EXTERNAL_MAP_ZOOM}z"
    }
}

// Google 지도는 검색어로 장소를 특정하므로, 같은 이름의 다른 장소가 잡히지 않도록 제목과 주소를 함께 넘긴다.
private fun googleMapQuery(
    title: String,
    address: String,
): String =
    listOf(title, address)
        .filter { value -> value.isNotBlank() }
        .joinToString(separator = " ")
