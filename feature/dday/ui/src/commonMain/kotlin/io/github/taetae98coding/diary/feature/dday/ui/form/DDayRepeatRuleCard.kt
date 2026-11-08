package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.icon.CheckIcon
import io.github.taetae98coding.diary.compose.core.icon.DropDownIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_end_date_clear_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_end_date_label
import io.github.taetae98coding.diary.feature.dday.ui.dday_end_date_not_set
import io.github.taetae98coding.diary.feature.dday.ui.dday_overflow_policy_clamp_to_month_end
import io.github.taetae98coding.diary.feature.dday.ui.dday_overflow_policy_label
import io.github.taetae98coding.diary.feature.dday.ui.dday_overflow_policy_next_month_first_day
import io.github.taetae98coding.diary.feature.dday.ui.dday_overflow_policy_skip
import org.jetbrains.compose.resources.stringResource

internal val ddayOverflowPolicyList: List<DDayOverflowPolicy> =
    listOf(
        DDayOverflowPolicy.CLAMP_TO_MONTH_END,
        DDayOverflowPolicy.SKIP,
        DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY,
    )

@Composable
internal fun DDayRepeatRuleCard(
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            EndDateRow(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )
            OverflowPolicyRow(
                state = state.ruleState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.ruleState.endDate ?: state.ruleState.baseDate },
        onConfirm = state.ruleState::selectEndDate,
        dialogState = state.endDatePickerDialogState,
        minDateProvider = { state.ruleState.baseDate },
    )
}

@Composable
private fun EndDateRow(
    state: DDayFormState,
    modifier: Modifier = Modifier,
) {
    val dateButtonFocusRequester = remember { FocusRequester() }
    val endDate = state.ruleState.endDate

    DDayCardRow(
        label = stringResource(Res.string.dday_end_date_label),
        modifier = modifier,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = state.endDatePickerDialogState::show,
                modifier = Modifier.focusRequester(dateButtonFocusRequester),
            ) {
                if (endDate == null) {
                    Text(text = stringResource(Res.string.dday_end_date_not_set))
                } else {
                    DDayDateText(date = endDate)
                }
            }
            DiaryScaleFadeVisibility(visible = endDate != null) {
                ClearButton(
                    onClick = {
                        state.ruleState.clearEndDate()
                        dateButtonFocusRequester.requestFocus()
                    },
                    contentDescription = stringResource(Res.string.dday_end_date_clear_button_content_description),
                )
            }
        }
    }
}

@Composable
private fun OverflowPolicyRow(
    state: DDayRuleInputState,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    DDayCardRow(
        label = stringResource(Res.string.dday_overflow_policy_label),
        modifier = modifier,
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
                Text(text = state.overflowPolicy.label())
                DropDownIcon()
            }
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
            ) {
                ddayOverflowPolicyList.forEach { policy ->
                    DDaySelectMenuItem(
                        text = policy.label(),
                        isSelected = policy == state.overflowPolicy,
                        onClick = {
                            state.selectOverflowPolicy(overflowPolicy = policy)
                            isExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun DDaySelectMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        modifier = modifier.semantics { selected = isSelected },
        // 고르지 않은 항목도 체크 아이콘의 자리를 비워 두어 이름의 시작을 맞춘다.
        leadingIcon = { CheckIcon(modifier = Modifier.alpha(if (isSelected) 1F else 0F)) },
    )
}

@Composable
internal fun DDayOverflowPolicy.label(): String =
    when (this) {
        DDayOverflowPolicy.CLAMP_TO_MONTH_END -> stringResource(Res.string.dday_overflow_policy_clamp_to_month_end)
        DDayOverflowPolicy.SKIP -> stringResource(Res.string.dday_overflow_policy_skip)
        DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY -> stringResource(Res.string.dday_overflow_policy_next_month_first_day)
    }

@ComponentPreview
@Composable
private fun DDayRepeatRuleCardPreview() {
    DiaryTheme {
        Surface {
            DDayRepeatRuleCard()
        }
    }
}

@ComponentPreview
@Composable
private fun DDaySelectMenuItemPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isSelected: Boolean,
) {
    DiaryTheme {
        Surface {
            DDaySelectMenuItem(
                text = DDayOverflowPolicy.CLAMP_TO_MONTH_END.label(),
                isSelected = isSelected,
                onClick = {},
            )
        }
    }
}
