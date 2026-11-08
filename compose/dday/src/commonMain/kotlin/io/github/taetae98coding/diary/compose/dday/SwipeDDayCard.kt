package io.github.taetae98coding.diary.compose.dday

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeToDeleteBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.dday.DDay
import org.jetbrains.compose.resources.stringResource

@Composable
public fun SwipeDDayCard(
    onEvent: (DDayListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: DDayListState = rememberDDayListState(),
    dday: DDay? = null,
) {
    SwipeToDeleteBox(
        deleteContentDescription = stringResource(Res.string.dday_list_delete_content_description),
        onDelete = { dday?.let { value -> onEvent(DDayListEvent.SwipeDelete(id = value.id)) } },
        modifier = modifier,
        key = dday?.id,
        gesturesEnabled = dday != null,
    ) {
        DDayCard(
            onClick = { dday?.let { value -> onEvent(DDayListEvent.ClickDDay(id = value.id)) } },
            modifier = Modifier.fillMaxWidth(),
            state = state,
            dday = dday,
        )
    }
}

@ComponentPreview
@Composable
private fun SwipeDDayCardPreview() {
    DiaryTheme {
        SwipeDDayCard(
            onEvent = {},
            state = rememberDDayListState(initialToday = previewDDayToday()),
            dday = previewDDay(title = "만난 날"),
        )
    }
}
