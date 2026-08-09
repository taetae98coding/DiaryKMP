package io.github.taetae98coding.diary.compose.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import com.naver.maps.map.NaverMap as NaverMapClient

@Composable
internal fun NaverMapMoveCameraEffect(
    client: NaverMapClient? = null,
    moveCommand: Flow<DiaryMapCamera> = emptyFlow(),
) {
    LaunchedEffect(client, moveCommand) {
        if (client == null) return@LaunchedEffect

        moveCommand.collect { camera ->
            if (!camera.isFinite) return@collect

            client.moveCamera(CameraUpdate.scrollTo(LatLng(camera.latitude, camera.longitude)))
        }
    }
}
