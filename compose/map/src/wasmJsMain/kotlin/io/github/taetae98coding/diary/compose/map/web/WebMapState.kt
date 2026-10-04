@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.compose.map.web

import org.w3c.dom.HTMLElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

internal external interface WebMapState : JsAny {
    fun attach(element: HTMLElement)

    fun release(element: HTMLElement)

    fun showSpot(
        latitude: Double,
        longitude: Double,
    )

    fun clearSpot()

    fun setPins(pinsJson: String)

    fun moveTo(
        latitude: Double,
        longitude: Double,
    )
}

internal fun interface WebMapStateFactory {
    fun create(
        options: JsAny,
        pinMarkerJson: String,
        isSpotSelectable: Boolean,
        isPinSelectable: Boolean,
        onCamera: (
            latitude: Double,
            longitude: Double,
            zoom: Double,
            south: Double,
            north: Double,
            west: Double,
            east: Double,
        ) -> Unit,
        onSpot: (latitude: Double, longitude: Double) -> Unit,
        onPin: (id: String) -> Unit,
    ): WebMapState
}
