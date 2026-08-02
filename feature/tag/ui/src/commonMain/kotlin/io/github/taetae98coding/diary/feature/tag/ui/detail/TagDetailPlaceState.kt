package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.core.model.location.Coordinate

@Stable
internal class TagDetailPlaceState(
    initialViewMode: TagDetailPlaceViewMode = TagDetailPlaceViewMode.LIST,
) {
    var viewMode: TagDetailPlaceViewMode by mutableStateOf(initialViewMode)
        private set

    // 떠 있는 장소 추가 버튼은 탭 본문 밖에 있어 지도를 직접 읽을 수 없으므로, 지도가 옮겨질 때 이 자리에 옮겨 둔다.
    var coordinate: Coordinate? by mutableStateOf(null)
        private set

    val addCoordinate: Coordinate?
        get() =
            when (viewMode) {
                TagDetailPlaceViewMode.LIST -> null
                TagDetailPlaceViewMode.MAP -> coordinate
            }

    fun toggleViewMode() {
        viewMode = viewMode.toggled()
    }

    fun moveMap(coordinate: Coordinate?) {
        this.coordinate = coordinate
    }

    companion object {
        val Saver: Saver<TagDetailPlaceState, String> =
            Saver(
                save = { state -> state.viewMode.name },
                restore = { saved -> TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.valueOf(saved)) },
            )
    }
}

@Composable
internal fun rememberTagDetailPlaceState(): TagDetailPlaceState =
    rememberSaveable(saver = TagDetailPlaceState.Saver) {
        TagDetailPlaceState()
    }
