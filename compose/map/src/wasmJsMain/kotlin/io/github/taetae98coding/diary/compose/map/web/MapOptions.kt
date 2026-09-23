@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.map.web

import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.isFinite
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

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
