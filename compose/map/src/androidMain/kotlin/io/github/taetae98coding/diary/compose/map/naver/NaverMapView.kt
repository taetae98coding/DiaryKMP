package io.github.taetae98coding.diary.compose.map.naver

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMapOptions
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun rememberNaverMapClient(mapView: MapView): NaverMapClient? {
    var client by remember(mapView) { mutableStateOf<NaverMapClient?>(null) }

    DisposableEffect(mapView) {
        mapView.getMapAsync { naverMap -> client = naverMap }

        onDispose {
            client = null
        }
    }

    return client
}

@Composable
internal fun rememberNaverMapViewWithLifecycle(camera: DiaryMapCamera?): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context, naverMapOptions(camera)) }
    val savedInstanceState =
        rememberSaveable(
            saver =
                Saver(
                    save = { it.apply(mapView::onSaveInstanceState) },
                    restore = { it },
                ),
        ) {
            Bundle()
        }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(savedInstanceState)
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    else -> Unit
                }
            }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    return mapView
}

private fun naverMapOptions(camera: DiaryMapCamera?): NaverMapOptions {
    val options =
        NaverMapOptions()
            .compassEnabled(true)
            .scaleBarEnabled(true)
            .zoomControlEnabled(true)
            .indoorEnabled(true)
            .indoorLevelPickerEnabled(true)
            .logoClickEnabled(true)
            .scrollGesturesEnabled(true)
            .zoomGesturesEnabled(true)
            .tiltGesturesEnabled(true)
            .rotateGesturesEnabled(true)
            .stopGesturesEnabled(true)

    if (camera == null) return options

    return options.camera(
        CameraPosition(
            LatLng(camera.latitude, camera.longitude),
            camera.zoom,
        ),
    )
}
