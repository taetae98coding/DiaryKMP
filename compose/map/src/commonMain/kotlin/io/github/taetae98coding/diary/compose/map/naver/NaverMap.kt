package io.github.taetae98coding.diary.compose.map.naver

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import kotlin.uuid.Uuid

@Composable
internal expect fun NaverMap(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
)
