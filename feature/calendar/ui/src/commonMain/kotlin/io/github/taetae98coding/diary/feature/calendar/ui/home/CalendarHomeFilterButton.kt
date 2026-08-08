package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_filter_action_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_filter_applied_state_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarHomeFilterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filterUiStateProvider: () -> CalendarHomeScaffoldFilterUiState = { CalendarHomeScaffoldFilterUiState() },
) {
    val filterUiState = filterUiStateProvider()
    val appliedStateDescription = stringResource(Res.string.calendar_home_filter_applied_state_description)

    IconButton(
        onClick = onClick,
        modifier =
            modifier.semantics {
                if (filterUiState.isApplied) {
                    stateDescription = appliedStateDescription
                }
            },
        colors =
            IconButtonDefaults.iconButtonColors(
                contentColor =
                    if (filterUiState.isApplied) {
                        DiaryTheme.colorScheme.primary
                    } else {
                        DiaryTheme.colorScheme.onSurfaceVariant
                    },
            ),
    ) {
        FilterIcon(
            contentDescription =
                stringResource(
                    Res.string.calendar_home_filter_action_content_description,
                ),
        )
    }
}

@ComponentPreview
@Composable
private fun CalendarHomeFilterButtonPreview() {
    DiaryTheme {
        Surface {
            CalendarHomeFilterButton(
                onClick = {},
                filterUiStateProvider = { CalendarHomeScaffoldFilterUiState(isApplied = true) },
            )
        }
    }
}
