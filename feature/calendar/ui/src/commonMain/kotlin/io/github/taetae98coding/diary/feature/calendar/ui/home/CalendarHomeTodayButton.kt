@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_today_button_content_description
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarHomeTodayButton(
    today: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(Res.string.calendar_home_today_button_content_description)

    OutlinedIconButton(
        onClick = onClick,
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .size(IconButtonDefaults.extraSmallContainerSize())
                .semantics { this.contentDescription = contentDescription },
        shape = IconButtonDefaults.extraSmallSquareShape,
    ) {
        Text(
            text = today.day.toString(),
            style = DiaryTheme.typography.labelMediumEmphasized,
        )
    }
}

@ComponentPreview
@Composable
private fun CalendarHomeTodayButtonPreview() {
    DiaryTheme {
        Surface {
            CalendarHomeTodayButton(
                today = LocalDate(year = 2026, month = 7, day = 19),
                onClick = {},
            )
        }
    }
}
