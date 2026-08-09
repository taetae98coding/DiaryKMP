package io.github.taetae98coding.diary.compose.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlin.uuid.Uuid

@Composable
public fun DiaryMap(
    modifier: Modifier = Modifier,
    state: DiaryMapState = rememberDiaryMapState(),
    layout: DiaryMapLayout = defaultDiaryMapLayout,
    onSpotClick: ((DiaryMapCoordinate) -> Unit)? = null,
    onPinClick: ((Uuid) -> Unit)? = null,
) {
    val dispatchSpotClick = rememberDispatch(callback = onSpotClick)
    val dispatchPinClick = rememberDispatch(callback = onPinClick)

    val map =
        remember(state, dispatchSpotClick, dispatchPinClick) {
            movableContentOf {
                DiaryMapContent(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    onSpotClick = dispatchSpotClick,
                    onPinClick = dispatchPinClick,
                )
            }
        }

    when (layout) {
        DiaryMapLayout.OVERLAY ->
            DiaryMapOverlayLayout(
                state = state,
                modifier = modifier,
                map = map,
            )

        DiaryMapLayout.CARD ->
            DiaryMapCardLayout(
                state = state,
                modifier = modifier,
                map = map,
            )
    }
}

/**
 * 지도는 [movableContentOf]로 붙잡아 두므로 콜백이 키로 쓰인다.
 * 호출자가 매번 새로 만드는 람다를 그대로 키로 쓰면 지도가 다시 만들어지므로,
 * 신원이 고정된 콜백으로 감싸 최신 콜백을 호출하고 사용하지 않을 때만 `null`로 둔다.
 */
@Composable
private fun <T> rememberDispatch(callback: ((T) -> Unit)?): ((T) -> Unit)? {
    val latestCallback by rememberUpdatedState(callback)
    val stableCallback: (T) -> Unit = remember { { value -> latestCallback?.invoke(value) } }

    return stableCallback.takeIf { callback != null }
}

@ScreenPreview
@Composable
private fun DiaryMapPreview() {
    DiaryTheme {
        DiaryMap(modifier = Modifier.fillMaxSize())
    }
}
