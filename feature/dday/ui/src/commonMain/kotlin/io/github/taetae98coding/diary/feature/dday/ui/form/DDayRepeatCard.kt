package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.ClearButton
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.icon.RemoveIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.domain.dday.preset.DDayPreset
import io.github.taetae98coding.diary.domain.dday.preset.value
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_add_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_label
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_number_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_number_label
import io.github.taetae98coding.diary.feature.dday.ui.dday_repeat_remove_button_content_description
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DDayRepeatCard(
    modifier: Modifier = Modifier,
    state: DDayFormState = rememberDDayAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()

    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(alignment = Alignment.TopStart),
        ) {
            NameRow(
                onAdd = {
                    val row = state.ruleState.addRepeat()
                    coroutineScope.launch {
                        // 새 행이 배치된 뒤에야 그 자리를 알 수 있다.
                        withFrameNanos { }
                        row.bringIntoViewRequester.bringIntoView()
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(state.repeatCardBringIntoViewRequester),
            )

            state.ruleState.repeatRowList.forEachIndexed { index, row ->
                key(row.key) {
                    DDayRepeatRow(
                        onRemove = { state.ruleState.removeRepeat(key = row.key) },
                        state = row,
                        position = index + 1,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(row.bringIntoViewRequester),
                    )
                }
            }
        }
    }
}

@Composable
private fun NameRow(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val addContentDescription = stringResource(Res.string.dday_repeat_add_button_content_description)

    Row(
        modifier = modifier.padding(start = DiaryTheme.dimens.cardContentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.dday_repeat_label),
            modifier = Modifier.weight(1F),
            style = DiaryTheme.typography.labelLarge,
        )
        DiaryTooltipBox(text = addContentDescription) {
            IconButton(onClick = onAdd) {
                AddIcon(contentDescription = addContentDescription)
            }
        }
    }
}

@Composable
private fun DDayRepeatRow(
    onRemove: () -> Unit,
    state: DDayRepeatRowState,
    position: Int,
    modifier: Modifier = Modifier,
) {
    val removeContentDescription = stringResource(Res.string.dday_repeat_remove_button_content_description, position.toString())

    Row(
        modifier =
            modifier.padding(
                start = DiaryTheme.dimens.cardContentPadding,
                end = DiaryTheme.dimens.cardContentPadding,
                bottom = DiaryTheme.dimens.itemSpacing,
            ),
        horizontalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberInput(
            state = state,
            position = position,
            modifier = Modifier.weight(1F),
        )
        DDayRepeatUnitSelector(
            state = state,
            position = position,
        )
        DiaryTooltipBox(text = removeContentDescription) {
            IconButton(onClick = onRemove) {
                RemoveIcon(contentDescription = removeContentDescription)
            }
        }
    }
}

@Composable
private fun NumberInput(
    state: DDayRepeatRowState,
    position: Int,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val hasNumber by remember(state) { derivedStateOf { state.numberState.text.isNotEmpty() } }
    val numberContentDescription = stringResource(Res.string.dday_repeat_number_content_description, position.toString())

    OutlinedTextField(
        state = state.numberState,
        modifier =
            modifier
                .focusRequester(focusRequester)
                .semantics { contentDescription = numberContentDescription },
        label = { Text(text = stringResource(Res.string.dday_repeat_number_label)) },
        trailingIcon = {
            DiaryScaleFadeVisibility(visible = hasNumber) {
                ClearButton(
                    onClick = {
                        state.numberState.clearText()
                        focusRequester.requestFocus()
                    },
                )
            }
        },
        inputTransformation = DDayRepeatNumberInputTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}

@ComponentPreview
@Composable
private fun DDayRepeatCardPreview() {
    val state = rememberDDayAddFormState()

    remember(state) { state.ruleState.applyPreset(value = DDayPreset.BABY.value()) }

    DiaryTheme {
        Surface {
            DDayRepeatCard(state = state)
        }
    }
}
