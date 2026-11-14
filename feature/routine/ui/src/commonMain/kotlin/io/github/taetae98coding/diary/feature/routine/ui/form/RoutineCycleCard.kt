package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.icon.DropDownIcon
import io.github.taetae98coding.diary.compose.core.menu.DiarySelectMenuItem
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_cycle_daily
import io.github.taetae98coding.diary.feature.routine.ui.routine_cycle_label
import io.github.taetae98coding.diary.feature.routine.ui.routine_cycle_monthly
import io.github.taetae98coding.diary.feature.routine.ui.routine_cycle_weekdays
import io.github.taetae98coding.diary.feature.routine.ui.routine_cycle_weekends
import io.github.taetae98coding.diary.feature.routine.ui.routine_cycle_weekly
import org.jetbrains.compose.resources.stringResource

internal val routineCycleList: List<RoutineCycle> =
    listOf(
        RoutineCycle.DAILY,
        RoutineCycle.WEEKDAYS,
        RoutineCycle.WEEKENDS,
        RoutineCycle.WEEKLY,
        RoutineCycle.MONTHLY,
    )

@Composable
internal fun RoutineCycleCard(
    modifier: Modifier = Modifier,
    state: RoutineFormState = rememberRoutineAddFormState(),
) {
    val ruleState = state.ruleState
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(modifier = modifier) {
        RoutineCardRow(
            label = stringResource(Res.string.routine_cycle_label),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box {
                TextButton(
                    onClick = { isExpanded = true },
                    modifier =
                        Modifier.semantics {
                            role = Role.DropdownList
                            if (isExpanded) {
                                collapse {
                                    isExpanded = false
                                    true
                                }
                            } else {
                                expand {
                                    isExpanded = true
                                    true
                                }
                            }
                        },
                ) {
                    Text(text = ruleState.cycle.label())
                    DropDownIcon()
                }
                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                ) {
                    routineCycleList.forEach { cycle ->
                        DiarySelectMenuItem(
                            text = cycle.label(),
                            isSelected = cycle == ruleState.cycle,
                            onClick = {
                                ruleState.selectCycle(cycle = cycle)
                                isExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RoutineCycle.label(): String =
    when (this) {
        RoutineCycle.DAILY -> stringResource(Res.string.routine_cycle_daily)
        RoutineCycle.WEEKDAYS -> stringResource(Res.string.routine_cycle_weekdays)
        RoutineCycle.WEEKENDS -> stringResource(Res.string.routine_cycle_weekends)
        RoutineCycle.WEEKLY -> stringResource(Res.string.routine_cycle_weekly)
        RoutineCycle.MONTHLY -> stringResource(Res.string.routine_cycle_monthly)
    }

@ComponentPreview
@Composable
private fun RoutineCycleCardPreview() {
    DiaryTheme {
        Surface {
            RoutineCycleCard()
        }
    }
}
