package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.compose.map.web.WebMap
import kotlin.uuid.Uuid

@Composable
internal fun NaverMap(
    httpServer: MapHttpServer,
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    WebMap(
        httpServer = httpServer,
        createHtml = { camera, spot, isSpotSelectable, pins, isPinSelectable ->
            createNaverMapHtml(
                camera = camera,
                spot = spot,
                isSpotSelectable = isSpotSelectable,
                pins = pins,
                isPinSelectable = isPinSelectable,
            )
        },
        modifier = modifier,
        baseUrl = NAVER_MAP_BASE_URL,
        state = state,
        onSpotClick = onSpotClick,
        onPinClick = onPinClick,
    )
}
