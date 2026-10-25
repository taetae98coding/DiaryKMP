package io.github.taetae98coding.diary.compose.map.naver

import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.compose.map.web.CAMERA_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.compose.map.web.PINS_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.PIN_MARKER_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.PIN_SELECTABLE_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.SPOT_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.SPOT_SELECTABLE_PLACEHOLDER
import io.github.taetae98coding.diary.compose.map.web.pinMarkerToScriptValue
import io.github.taetae98coding.diary.compose.map.web.toScriptValue

// 네이버 지도 SDK는 문서가 https가 아니면 인증·타일·스타일을 http로 요청하는데, WKWebView는 ATS 때문에 이를 막는다.
// SDK를 고쳐 쓰면 SDK가 갱신될 때마다 깨지므로, 문서를 네이버가 인증하는 localhost의 https 출처로 열어 SDK를 그대로 쓴다.
internal const val NAVER_MAP_BASE_URL = "https://localhost/naver-map"

internal fun createNaverMapHtml(
    camera: DiaryMapCamera?,
    spot: DiaryMapCoordinate? = null,
    isSpotSelectable: Boolean = false,
    pins: List<DiaryMapPin> = emptyList(),
    isPinSelectable: Boolean = false,
): String? =
    createNaverMapHtml(
        ncpKeyId = naverMapNcpKeyId(),
        camera = camera,
        spot = spot,
        isSpotSelectable = isSpotSelectable,
        pins = pins,
        isPinSelectable = isPinSelectable,
    )

private fun naverMapNcpKeyId(): String =
    System
        .getProperty(NAVER_MAP_NCP_KEY_ID_PROPERTY)
        .orEmpty()

internal fun createNaverMapHtml(
    ncpKeyId: String,
    camera: DiaryMapCamera?,
    spot: DiaryMapCoordinate? = null,
    isSpotSelectable: Boolean = false,
    pins: List<DiaryMapPin> = emptyList(),
    isPinSelectable: Boolean = false,
): String? {
    val normalizedKey = ncpKeyId.trim()
    if (!normalizedKey.matches(NCP_KEY_ID_PATTERN)) {
        return null
    }

    return NAVER_MAP_HTML_TEMPLATE
        .replace(NCP_KEY_ID_PLACEHOLDER, normalizedKey)
        .replace(CAMERA_PLACEHOLDER, camera.toScriptValue())
        .replace(SPOT_PLACEHOLDER, spot.toScriptValue())
        .replace(SPOT_SELECTABLE_PLACEHOLDER, isSpotSelectable.toScriptValue())
        .replace(PINS_PLACEHOLDER, pins.toScriptValue())
        .replace(PIN_SELECTABLE_PLACEHOLDER, isPinSelectable.toScriptValue())
        .replace(PIN_MARKER_PLACEHOLDER, pinMarkerToScriptValue())
}

private const val NAVER_MAP_NCP_KEY_ID_PROPERTY =
    "io.github.taetae98coding.diary.naverMapNcpKeyId"
private const val NCP_KEY_ID_PLACEHOLDER = "{{NAVER_MAP_NCP_KEY_ID}}"
private val NCP_KEY_ID_PATTERN = Regex("[A-Za-z0-9_-]+")
private val NAVER_MAP_HTML_TEMPLATE: String by lazy {
    checkNotNull(MapHttpServer::class.java.getResourceAsStream("/naver-map.html"))
        .bufferedReader()
        .use { it.readText() }
}
