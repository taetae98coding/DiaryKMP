package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_VIEW_SCOPE
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_view_scope_all
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_view_scope_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_view_scope_today
import org.jetbrains.compose.resources.stringResource

internal val routineViewScopeList: List<RoutineViewScope> = listOf(RoutineViewScope.TODAY, RoutineViewScope.ALL)

@Composable
internal fun RoutineHomeViewScopeSection(
    onEvent: (RoutineHomeFilterBottomSheetEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewScopeProvider: () -> RoutineViewScope = { ROUTINE_DEFAULT_VIEW_SCOPE },
) {
    val selectedViewScope = viewScopeProvider()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        Text(
            text = stringResource(Res.string.routine_home_filter_view_scope_title),
            modifier = Modifier.semantics { heading() },
            style = DiaryTheme.typography.titleMedium,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            routineViewScopeList.forEachIndexed { index, viewScope ->
                SegmentedButton(
                    selected = viewScope == selectedViewScope,
                    onClick = { onEvent(RoutineHomeFilterBottomSheetEvent.SetViewScope(viewScope = viewScope)) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = routineViewScopeList.size),
                    modifier = Modifier.weight(1F),
                    icon = {},
                    label = {
                        Text(
                            text = viewScope.label(),
                            maxLines = 1,
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun RoutineViewScope.label(): String =
    when (this) {
        RoutineViewScope.TODAY -> stringResource(Res.string.routine_home_filter_view_scope_today)
        RoutineViewScope.ALL -> stringResource(Res.string.routine_home_filter_view_scope_all)
    }

private class RoutineViewScopePreviewParameter : PreviewParameterProvider<RoutineViewScope> {
    override val values: Sequence<RoutineViewScope> = routineViewScopeList.asSequence()
}

@ComponentPreview
@Composable
private fun RoutineHomeViewScopeSectionPreview(
    @PreviewParameter(RoutineViewScopePreviewParameter::class) viewScope: RoutineViewScope,
) {
    DiaryTheme {
        Surface {
            RoutineHomeViewScopeSection(
                onEvent = {},
                viewScopeProvider = { viewScope },
            )
        }
    }
}
