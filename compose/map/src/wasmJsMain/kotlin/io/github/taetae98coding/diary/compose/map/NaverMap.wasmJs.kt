@file:OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalWasmJsInterop::class,
)

package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.w3c.dom.HTMLElement
import kotlin.uuid.Uuid

@Composable
internal actual fun NaverMap(
    modifier: Modifier,
    state: DiaryMapState,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)?,
    onPinClick: ((Uuid) -> Unit)?,
) {
    val naverMapState =
        remember {
            NaverMapState(
                options = naverMapOptions().withCamera(state.camera),
                pinMarkerJson = pinMarkerToScriptValue(),
                isSpotSelectable = onSpotClick != null,
                isPinSelectable = onPinClick != null,
                onCamera = { latitude, longitude, zoom, south, north, west, east ->
                    state.moveCamera(
                        DiaryMapCamera(
                            latitude = latitude,
                            longitude = longitude,
                            zoom = zoom,
                            bounds =
                                DiaryMapBounds(south = south, north = north, west = west, east = east)
                                    .takeIf { bounds -> bounds.isFinite },
                        ),
                    )
                },
                onSpot = { latitude, longitude ->
                    onSpotClick?.invoke(DiaryMapCoordinate(latitude = latitude, longitude = longitude))
                },
                onPin = { id ->
                    Uuid.parseOrNull(id)?.let { pinId -> onPinClick?.invoke(pinId) }
                },
            )
        }

    SpotMarkerEffect(
        mapState = naverMapState,
        state = state,
    )
    PinMarkersEffect(
        mapState = naverMapState,
        state = state,
    )
    MoveCameraEffect(
        mapState = naverMapState,
        moveCommand = state.moveCommand,
    )

    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLElement).apply {
                style.width = "100%"
                style.height = "100%"
                naverMapState.attach(this)
            }
        },
        modifier = modifier,
        onRelease = { element ->
            naverMapState.release(element)
        },
    )
}

@Composable
private fun SpotMarkerEffect(
    mapState: NaverMapState,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    LaunchedEffect(mapState, state) {
        snapshotFlow { state.spot }
            .collect { spot ->
                if (spot == null || !spot.isFinite) {
                    mapState.clearSpot()
                } else {
                    mapState.showSpot(latitude = spot.latitude, longitude = spot.longitude)
                }
            }
    }
}

@Composable
private fun PinMarkersEffect(
    mapState: NaverMapState,
    state: DiaryMapState = rememberDiaryMapState(),
) {
    LaunchedEffect(mapState, state) {
        snapshotFlow { state.pins }
            .collect { pins ->
                mapState.setPins(pinsJson = pins.toScriptValue())
            }
    }
}

@Composable
private fun MoveCameraEffect(
    mapState: NaverMapState,
    moveCommand: Flow<DiaryMapCamera> = emptyFlow(),
) {
    LaunchedEffect(mapState, moveCommand) {
        moveCommand.collect { camera ->
            if (!camera.isFinite) return@collect

            mapState.moveTo(latitude = camera.latitude, longitude = camera.longitude)
        }
    }
}
