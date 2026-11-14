package io.github.taetae98coding.diary.feature.routine.ui.form

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
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_AMOUNT_PER_RECORD
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_UNIT
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
internal class RoutineFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val colorState: DiaryColorInputState,
    val ruleState: RoutineRuleInputState,
    val unitState: RoutineUnitInputState,
    val goalAmountState: RoutineAmountInputState,
    val amountPerRecordState: RoutineAmountInputState,
    val startDatePickerDialogState: DialogState,
    val snackbarHostState: SnackbarHostState,
    val tagPickerDialogState: DialogState,
) {
    // 비어 있는 한 번에 기록할 양은 유효 범위 밖의 0으로 넘겨 저장하지 않고 안내를 받게 한다.
    val input: RoutineInput
        get() =
            RoutineInput(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                color = colorState.color.toColorLong(),
                startDate = ruleState.startDate,
                cycle = ruleState.cycle,
                unit = unitState.text,
                goalAmount = goalAmountState.amount,
                amountPerRecord = amountPerRecordState.amount ?: EMPTY_AMOUNT,
            )

    fun isChangedFrom(detail: RoutineDetail): Boolean {
        val input = input

        return input.title != detail.title ||
            input.description != detail.description ||
            input.color != detail.color ||
            input.startDate != detail.startDate ||
            input.cycle != detail.cycle ||
            input.unit.trim() != detail.unit ||
            input.goalAmount != detail.goalAmount ||
            input.amountPerRecord != detail.amountPerRecord
    }

    fun reset(startDate: LocalDate) {
        titleState.clearText()
        ruleState.reset(startDate = startDate)
        unitState.clear()
        goalAmountState.clear()
        amountPerRecordState.setAmount(amount = ROUTINE_DEFAULT_AMOUNT_PER_RECORD)
    }

    private companion object {
        const val EMPTY_AMOUNT = 0
    }
}

@Composable
internal fun rememberRoutineAddFormState(initialStartDate: LocalDate = remember { today() }): RoutineFormState =
    rememberRoutineFormState(
        initialTitle = "",
        initialDescription = "",
        initialColor = randomColor(),
        ruleState = rememberRoutineRuleInputState(initialStartDate = initialStartDate),
        initialUnit = ROUTINE_DEFAULT_UNIT,
        initialGoalAmount = null,
        initialAmountPerRecord = ROUTINE_DEFAULT_AMOUNT_PER_RECORD,
    )

@Composable
internal fun rememberRoutineDetailFormState(initialDetail: RoutineDetail? = null): RoutineFormState {
    if (initialDetail == null) return rememberRoutineAddFormState()

    return rememberRoutineFormState(
        initialTitle = initialDetail.title,
        initialDescription = initialDetail.description,
        initialColor = initialDetail.color.toColor(),
        ruleState =
            rememberRoutineRuleInputState(
                initialStartDate = initialDetail.startDate,
                initialCycle = initialDetail.cycle,
            ),
        initialUnit = initialDetail.unit,
        initialGoalAmount = initialDetail.goalAmount,
        initialAmountPerRecord = initialDetail.amountPerRecord,
    )
}

@Composable
private fun rememberRoutineFormState(
    initialTitle: String,
    initialDescription: String,
    initialColor: Color,
    ruleState: RoutineRuleInputState,
    initialUnit: String,
    initialGoalAmount: Int?,
    initialAmountPerRecord: Int,
): RoutineFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialTitle)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDescription)
    val colorState = rememberDiaryColorInputState(initialColor = initialColor)
    val unitState = rememberRoutineUnitInputState(initialUnit = initialUnit)
    val goalAmountState = rememberRoutineAmountInputState(initialAmount = initialGoalAmount)
    val amountPerRecordState = rememberRoutineAmountInputState(initialAmount = initialAmountPerRecord)
    val startDatePickerDialogState = rememberDialogState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tagPickerDialogState = rememberDialogState()

    return remember(
        titleState,
        descriptionState,
        colorState,
        ruleState,
        unitState,
        goalAmountState,
        amountPerRecordState,
        startDatePickerDialogState,
        snackbarHostState,
        tagPickerDialogState,
    ) {
        RoutineFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            colorState = colorState,
            ruleState = ruleState,
            unitState = unitState,
            goalAmountState = goalAmountState,
            amountPerRecordState = amountPerRecordState,
            startDatePickerDialogState = startDatePickerDialogState,
            snackbarHostState = snackbarHostState,
            tagPickerDialogState = tagPickerDialogState,
        )
    }
}

internal fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
