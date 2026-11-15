package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineRecordInputState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.initialRecordDate
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.rememberRoutineRecordInputState
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineAmountInputState
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineDetailFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.today
import kotlinx.datetime.LocalDate

@Stable
internal class RoutineDetailScaffoldState(
    val formState: RoutineFormState,
    val recordInputState: RoutineRecordInputState,
    val listState: LazyListState,
) {
    suspend fun focusAmount(amountState: RoutineAmountInputState) {
        if (listState.layoutInfo.visibleItemsInfo.none { info -> info.index == FORM_INDEX }) {
            listState.scrollToItem(index = FORM_INDEX)
            // 폼이 다시 배치된 뒤에야 입력에 초점을 줄 수 있다.
            withFrameNanos { }
        }
        amountState.focus()
    }

    private companion object {
        const val FORM_INDEX = 0
    }
}

@Composable
internal fun rememberRoutineDetailScaffoldState(
    initialDetail: RoutineDetail? = null,
    today: LocalDate = remember { today() },
): RoutineDetailScaffoldState {
    val formState = rememberRoutineDetailFormState(initialDetail = initialDetail)
    val recordInputState =
        rememberRoutineRecordInputState(
            initialDate = initialDetail?.initialRecordDate(today = today) ?: today,
            initialAmount = initialDetail?.amountPerRecord,
        )
    val listState = rememberLazyListState()

    return remember(formState, recordInputState, listState) {
        RoutineDetailScaffoldState(
            formState = formState,
            recordInputState = recordInputState,
            listState = listState,
        )
    }
}
