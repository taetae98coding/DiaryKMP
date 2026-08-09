@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.map

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

// 시작 위치를 지정하지 않았거나 지도가 쓸 수 없는 값이면 제공자의 기본 위치와 기본 확대 수준을 그대로 쓴다.
internal fun JsAny.withCamera(camera: DiaryMapCamera?): JsAny {
    if (camera == null || !camera.isFinite) return this

    return mergeCamera(
        options = this,
        latitude = camera.latitude,
        longitude = camera.longitude,
        zoom = camera.zoom,
    )
}

@Suppress("UnusedParameter")
private fun mergeCamera(
    options: JsAny,
    latitude: Double,
    longitude: Double,
    zoom: Double,
): JsAny =
    js(
        """
        Object.assign({}, options, {
            center: { lat: latitude, lng: longitude },
            zoom: zoom
        })
        """,
    )
