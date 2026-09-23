package io.github.taetae98coding.diary.compose.map.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import io.github.taetae98coding.diary.compose.map.DiaryMapCamera
import io.github.taetae98coding.diary.compose.map.isFinite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun GoogleMapMoveCameraEffect(
    cameraPositionState: CameraPositionState,
    moveCommand: Flow<DiaryMapCamera> = emptyFlow(),
) {
    LaunchedEffect(cameraPositionState, moveCommand) {
        moveCommand.collect { camera ->
            if (!camera.isFinite) return@collect

            cameraPositionState.move(CameraUpdateFactory.newLatLng(LatLng(camera.latitude, camera.longitude)))
        }
    }
}
