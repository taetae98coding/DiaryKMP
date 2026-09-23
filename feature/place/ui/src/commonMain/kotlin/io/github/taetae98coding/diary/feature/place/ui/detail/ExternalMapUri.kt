package io.github.taetae98coding.diary.feature.place.ui.detail

import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.library.kotlin.text.encodeUriComponent

private const val NAVER_MAP_WEB_URI = "https://map.naver.com/"
private const val GOOGLE_MAP_WEB_SEARCH_URI = "https://www.google.com/maps/search/"

private const val NAVER_MAP_APP_PLACE_URI = "nmap://place"
private const val NAVER_MAP_APP_MAP_URI = "nmap://map"
private const val GOOGLE_MAP_APP_URI = "comgooglemaps://"

// Google Maps URLs는 좌표를 구분하는 쉼표를 인코딩해 넘기도록 정하고 있다.
private const val ENCODED_COMMA = "%2C"

private const val EXTERNAL_MAP_ZOOM = 15

internal fun externalMapTitle(
    inputTitle: String,
    savedTitle: String,
): String = inputTitle.ifBlank { savedTitle }

// 주소는 앱 밖으로 나가는 값이므로 제공자 상수 이름에 기대지 않고 제공자별 주소를 여기에서 정한다.
internal fun externalMapWebUri(
    provider: DiaryMapProvider,
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
): String =
    when (provider) {
        DiaryMapProvider.NAVER -> naverMapWebUri(coordinate = coordinate, title = title)
        DiaryMapProvider.GOOGLE -> googleMapWebUri(coordinate = coordinate, title = title, address = address)
    }

// 네이버 지도 앱은 호출한 앱을 식별하는 appname을 모든 주소에 요구하고, 이름을 붙인 지점은 place로만 표시한다.
internal fun naverMapAppUri(
    coordinate: DiaryMapCoordinate,
    title: String,
    appName: String,
): String {
    val location = "lat=${coordinate.latitude}&lng=${coordinate.longitude}"
    val caller = "appname=${appName.encodeUriComponent()}"

    return if (title.isBlank()) {
        "$NAVER_MAP_APP_MAP_URI?$location&$caller"
    } else {
        "$NAVER_MAP_APP_PLACE_URI?$location&name=${title.encodeUriComponent()}&$caller"
    }
}

// Google 지도 앱은 검색어를 q로, 그 검색어를 찾을 중심을 center로 받으므로 웹 지도와 달리 둘을 따로 넘긴다.
internal fun googleMapAppUri(
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
): String {
    val center = "${coordinate.latitude},${coordinate.longitude}"
    val query = googleMapQuery(title = title, address = address)

    return if (query.isBlank()) {
        "$GOOGLE_MAP_APP_URI?q=$center"
    } else {
        "$GOOGLE_MAP_APP_URI?q=${query.encodeUriComponent()}&center=$center"
    }
}

private fun naverMapWebUri(
    coordinate: DiaryMapCoordinate,
    title: String,
): String {
    val location = "$NAVER_MAP_WEB_URI?lng=${coordinate.longitude}&lat=${coordinate.latitude}"

    return if (title.isBlank()) location else "$location&title=${title.encodeUriComponent()}"
}

// 검색어와 좌표를 함께 넘기는 형식은 Google Maps URLs에 없으므로, 검색어가 없을 때만 문서화된 좌표 검색 주소를 쓴다.
private fun googleMapWebUri(
    coordinate: DiaryMapCoordinate,
    title: String,
    address: String,
): String {
    val query = googleMapQuery(title = title, address = address)

    return if (query.isBlank()) {
        "$GOOGLE_MAP_WEB_SEARCH_URI?api=1&query=${coordinate.latitude}$ENCODED_COMMA${coordinate.longitude}"
    } else {
        "$GOOGLE_MAP_WEB_SEARCH_URI${query.encodeUriComponent()}/@${coordinate.latitude},${coordinate.longitude},${EXTERNAL_MAP_ZOOM}z"
    }
}

private fun googleMapQuery(
    title: String,
    address: String,
): String =
    listOf(title, address)
        .filter { value -> value.isNotBlank() }
        .joinToString(separator = " ")
