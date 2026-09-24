package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.holiday.list.HolidaySettingListDefaults
import io.github.taetae98coding.diary.feature.setting.ui.previewHolidaySettingList
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_loading_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidayScaffold(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
    uiStateProvider: () -> SettingHolidayUiState = { SettingHolidayUiState.Loading },
    componentVisibleProvider: () -> SettingHolidayScaffoldComponentVisible = {
        SettingHolidayScaffoldComponentVisible()
    },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SettingHolidayTopBar(
                onEvent = onEvent,
                state = state,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        SettingHolidayBody(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            uiStateProvider = uiStateProvider,
        )
    }
}

@Composable
private fun SettingHolidayBody(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
    uiStateProvider: () -> SettingHolidayUiState = { SettingHolidayUiState.Loading },
) {
    Box(modifier = modifier) {
        DiaryCrossfade(
            targetState = uiStateProvider(),
            modifier = Modifier.fillMaxSize(),
            contentKey = { uiState -> uiState::class },
        ) { uiState ->
            when (uiState) {
                is SettingHolidayUiState.Loading ->
                    DiaryLoadingBox(
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = stringResource(Res.string.setting_holiday_loading_content_description),
                    )

                is SettingHolidayUiState.Loaded ->
                    HolidaySettingContent(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        holidaySettingList = uiState.holidaySettingList,
                        listBottomPadding = HolidaySettingListDefaults.BottomPadding,
                    )
            }
        }

        if (!state.isFiltering) {
            SettingHolidayBulkActionMenu(
                onEvent = onEvent,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = DiaryTheme.dimens.screenVerticalPadding),
                state = state,
            )
        }
    }
}

private class SettingHolidayUiStatePreviewParameter : PreviewParameterProvider<SettingHolidayUiState> {
    override val values: Sequence<SettingHolidayUiState> =
        sequenceOf(
            SettingHolidayUiState.Loading,
            SettingHolidayUiState.Loaded(holidaySettingList = previewHolidaySettingList()),
        )
}

@ScreenPreview
@Composable
private fun SettingHolidayScaffoldPreview(
    @PreviewParameter(SettingHolidayUiStatePreviewParameter::class) uiState: SettingHolidayUiState,
) {
    DiaryTheme {
        SettingHolidayScaffold(
            onEvent = {},
            uiStateProvider = { uiState },
        )
    }
}
