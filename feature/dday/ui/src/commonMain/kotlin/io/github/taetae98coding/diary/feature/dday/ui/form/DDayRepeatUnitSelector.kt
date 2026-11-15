@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import io.github.taetae98coding.diary.compose.core.menu.DiarySelectMenuItem
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_unit_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_unit_day
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_unit_label
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_unit_month
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_unit_year
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayRepeatUnitSelector(
    state: DDayRepeatRowState,
    position: Int,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val unitContentDescription = stringResource(Res.string.dday_repeat_unit_content_description, position.toString())
    val unitLabelList = ddayRepeatUnitList.map { unit -> unit.label() }

    MaxLabelWidthLayout(
        labelList = unitLabelList,
        modifier = modifier,
    ) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { expanded -> isExpanded = expanded },
        ) {
            UnitField(
                value = state.unit.label(),
                isExpanded = isExpanded,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .semantics { contentDescription = unitContentDescription },
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
            ) {
                ddayRepeatUnitList.forEach { unit ->
                    DiarySelectMenuItem(
                        text = unit.label(),
                        isSelected = unit == state.unit,
                        onClick = {
                            state.selectUnit(unit = unit)
                            isExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitField(
    value: String,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        label = { Text(text = stringResource(Res.string.dday_repeat_unit_label)) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
        singleLine = true,
    )
}

// 단위를 바꿔도 숫자 입력의 폭이 흔들리지 않도록, 가장 긴 단위 이름이 들어가는 폭으로 고정한다.
@Composable
private fun MaxLabelWidthLayout(
    labelList: List<String>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        // 최소 폭을 0이 아닌 값으로 넘겨 입력 칸의 기본 최소 폭 대신 내용 폭으로 측정한다.
        val measureConstraints = Constraints(minWidth = 1, maxWidth = constraints.maxWidth)
        val width =
            labelList
                .mapIndexed { index, label ->
                    subcompose(slotId = index) {
                        UnitField(
                            value = label,
                            isExpanded = false,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }.first()
                        .measure(measureConstraints)
                        .width
                }.maxOrNull()
                ?.coerceAtMost(constraints.maxWidth) ?: 0
        val placeable =
            subcompose(slotId = CONTENT_SLOT_ID, content = content)
                .first()
                .measure(constraints.copy(minWidth = width, maxWidth = width))

        layout(width = placeable.width, height = placeable.height) {
            placeable.place(x = 0, y = 0)
        }
    }
}

private const val CONTENT_SLOT_ID = "content"

@Composable
internal fun DDayRepeatUnit.label(): String =
    when (this) {
        DDayRepeatUnit.DAY -> stringResource(Res.string.dday_repeat_unit_day)
        DDayRepeatUnit.MONTH -> stringResource(Res.string.dday_repeat_unit_month)
        DDayRepeatUnit.YEAR -> stringResource(Res.string.dday_repeat_unit_year)
    }

@ComponentPreview
@Composable
private fun DDayRepeatUnitSelectorPreview() {
    DiaryTheme {
        Surface {
            DDayRepeatUnitSelector(
                state = DDayRepeatRowState(key = "1", initialNumber = "100", initialUnit = DDayRepeatUnit.DAY),
                position = 1,
            )
        }
    }
}
