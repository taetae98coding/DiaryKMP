package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.dialog.DialogState

private const val TAB_INDEX = 0
private const val VIEW_MODE_INDEX = 1

@Stable
internal class WebDetailScaffoldState(
    initialTab: WebDetailTab,
    initialViewMode: WebDetailViewMode,
) {
    var tab: WebDetailTab by mutableStateOf(initialTab)
        private set

    var viewMode: WebDetailViewMode by mutableStateOf(initialViewMode)
        private set

    val viewModeSheetState: DialogState = DialogState()

    fun select(tab: WebDetailTab) {
        this.tab = tab
    }

    fun select(viewMode: WebDetailViewMode) {
        this.viewMode = viewMode
    }

    companion object {
        val Saver: Saver<WebDetailScaffoldState, Any> =
            listSaver(
                save = { state -> listOf(state.tab.name, state.viewMode.name) },
                restore = { value ->
                    WebDetailScaffoldState(
                        initialTab = WebDetailTab.valueOf(value[TAB_INDEX]),
                        initialViewMode = WebDetailViewMode.valueOf(value[VIEW_MODE_INDEX]),
                    )
                },
            )
    }
}

@Composable
internal fun rememberWebDetailScaffoldState(
    initialTab: WebDetailTab = WebDetailTab.PAGE,
    initialViewMode: WebDetailViewMode = WebDetailViewMode.URL,
): WebDetailScaffoldState =
    rememberSaveable(saver = WebDetailScaffoldState.Saver) {
        WebDetailScaffoldState(initialTab = initialTab, initialViewMode = initialViewMode)
    }
