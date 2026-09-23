package io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.Res
import io.github.taetae98coding.diary.feature.holiday.ui.holiday_loading_content_description
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeYearContentEvent
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeYearUiState
import io.github.taetae98coding.diary.feature.holiday.ui.previewGoldenHolidayGroup
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GoldenHolidayYear(
    onEvent: (HolidayHomeYearContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> HolidayHomeYearUiState = { HolidayHomeYearUiState.Loading },
    contentPadding: PaddingValues = PaddingValues(),
) {
    DiaryCrossfade(
        targetState = uiStateProvider(),
        modifier = modifier,
        contentKey = { uiState -> uiState::class },
    ) { uiState ->
        when (uiState) {
            is HolidayHomeYearUiState.Loading ->
                DiaryLoadingBox(
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                    contentDescription = stringResource(Res.string.holiday_loading_content_description),
                )

            is HolidayHomeYearUiState.Error ->
                GoldenHolidayErrorDescription(
                    onRetry = { onEvent(HolidayHomeYearContentEvent.ClickRetry) },
                    modifier = Modifier.fillMaxSize().padding(contentPadding),
                )

            is HolidayHomeYearUiState.NotProvided ->
                GoldenHolidayNotProvidedDescription(modifier = Modifier.fillMaxSize().padding(contentPadding))

            is HolidayHomeYearUiState.Loaded ->
                GoldenHolidayList(
                    groupList = uiState.goldenHolidayGroupList,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = DiaryTheme.dimens.screenPaddingValues + contentPadding,
                )
        }
    }
}

@ScreenPreview
@Composable
private fun GoldenHolidayYearPreview() {
    val groupList = remember { listOf(previewGoldenHolidayGroup()) }

    DiaryTheme {
        GoldenHolidayYear(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            uiStateProvider = { HolidayHomeYearUiState.Loaded(goldenHolidayGroupList = groupList) },
        )
    }
}
