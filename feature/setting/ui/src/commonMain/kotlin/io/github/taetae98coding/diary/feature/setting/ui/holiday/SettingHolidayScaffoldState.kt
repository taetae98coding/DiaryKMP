package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filter

@Stable
internal class SettingHolidayScaffoldState(
    val queryState: TextFieldState,
) {
    var isBulkActionExpanded: Boolean by mutableStateOf(false)
        private set

    val query: String
        get() = queryState.text.toString()

    val isFiltering: Boolean by derivedStateOf { query.isNotBlank() }

    fun expandBulkAction() {
        isBulkActionExpanded = true
    }

    fun collapseBulkAction() {
        isBulkActionExpanded = false
    }
}

@Composable
internal fun rememberSettingHolidayScaffoldState(queryState: TextFieldState = rememberTextFieldState()): SettingHolidayScaffoldState {
    val state = remember(queryState) { SettingHolidayScaffoldState(queryState = queryState) }

    LaunchedEffect(state) {
        snapshotFlow { state.isFiltering }
            .filter { isFiltering -> isFiltering }
            .collect { state.collapseBulkAction() }
    }

    return state
}
