package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.preset.DDayPreset
import io.github.taetae98coding.diary.domain.dday.preset.value
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatInputList
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
internal class DDayFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val colorState: DiaryColorInputState,
    val ruleState: DDayRuleInputState,
    val baseDatePickerDialogState: DialogState,
    val endDatePickerDialogState: DialogState,
    val repeatCardBringIntoViewRequester: BringIntoViewRequester,
    val scrollState: ScrollState,
    val snackbarHostState: SnackbarHostState,
    val tagPickerDialogState: DialogState,
) {
    val input: DDayInput
        get() =
            DDayInput(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                color = colorState.color.toColorLong(),
                baseDate = ruleState.baseDate,
                countsBaseDay = ruleState.countsBaseDay,
                repeatList = ruleState.repeatInputList,
                endDate = ruleState.endDate,
                overflowPolicy = ruleState.overflowPolicy,
            )

    fun isChangedFrom(detail: DDayDetail): Boolean {
        val input = input
        // 숫자가 빈 간격이 있으면 저장된 내용과 다른 것으로 보아 수정을 실행해 안내를 받을 수 있게 한다.
        val repeats = input.repeatList.toRepeatSetOrNull() ?: return true

        return input.title != detail.title ||
            input.description != detail.description ||
            input.color != detail.color ||
            input.baseDate != detail.baseDate ||
            input.countsBaseDay != detail.countsBaseDay ||
            repeats != detail.repeats ||
            input.endDate != detail.endDate ||
            input.overflowPolicy != detail.overflowPolicy
    }
}

@Composable
internal fun rememberDDayAddFormState(initialBaseDate: LocalDate = remember { today() }): DDayFormState {
    val customValue = remember { DDayPreset.CUSTOM.value() }

    return rememberDDayFormState(
        initialTitle = "",
        initialDescription = "",
        initialColor = randomColor(),
        ruleState =
            rememberDDayRuleInputState(
                initialBaseDate = initialBaseDate,
                initialCountsBaseDay = customValue.countsBaseDay,
                initialEndDate = customValue.endDate,
                initialOverflowPolicy = customValue.overflowPolicy,
                initialRepeatList = customValue.repeatList,
            ),
    )
}

@Composable
internal fun rememberDDayDetailFormState(initialDetail: DDayDetail? = null): DDayFormState {
    if (initialDetail == null) return rememberDDayAddFormState()

    return rememberDDayFormState(
        initialTitle = initialDetail.title,
        initialDescription = initialDetail.description,
        initialColor = initialDetail.color.toColor(),
        ruleState =
            rememberDDayRuleInputState(
                initialBaseDate = initialDetail.baseDate,
                initialCountsBaseDay = initialDetail.countsBaseDay,
                initialEndDate = initialDetail.endDate,
                initialOverflowPolicy = initialDetail.overflowPolicy,
                initialRepeatList = remember { initialDetail.repeats.toRepeatInputList() },
            ),
    )
}

@Composable
private fun rememberDDayFormState(
    initialTitle: String,
    initialDescription: String,
    initialColor: Color,
    ruleState: DDayRuleInputState,
): DDayFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialTitle)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDescription)
    val colorState = rememberDiaryColorInputState(initialColor = initialColor)
    val baseDatePickerDialogState = rememberDialogState()
    val endDatePickerDialogState = rememberDialogState()
    val repeatCardBringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tagPickerDialogState = rememberDialogState()

    return remember(
        titleState,
        descriptionState,
        colorState,
        ruleState,
        baseDatePickerDialogState,
        endDatePickerDialogState,
        repeatCardBringIntoViewRequester,
        scrollState,
        snackbarHostState,
        tagPickerDialogState,
    ) {
        DDayFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            colorState = colorState,
            ruleState = ruleState,
            baseDatePickerDialogState = baseDatePickerDialogState,
            endDatePickerDialogState = endDatePickerDialogState,
            repeatCardBringIntoViewRequester = repeatCardBringIntoViewRequester,
            scrollState = scrollState,
            snackbarHostState = snackbarHostState,
            tagPickerDialogState = tagPickerDialogState,
        )
    }
}

internal fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
