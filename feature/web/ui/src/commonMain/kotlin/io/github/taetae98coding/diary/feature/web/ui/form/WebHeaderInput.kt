package io.github.taetae98coding.diary.feature.web.ui.form

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.icon.RemoveIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_header_add_button_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_header_input_label
import io.github.taetae98coding.diary.feature.web.ui.web_header_name_input_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_header_name_input_label
import io.github.taetae98coding.diary.feature.web.ui.web_header_remove_button_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_header_value_input_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_header_value_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebHeaderInput(
    modifier: Modifier = Modifier,
    state: WebHeaderInputState = rememberWebHeaderInputState(),
) {
    Card(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(alignment = Alignment.TopStart),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.web_header_input_label),
                    style = DiaryTheme.typography.labelLarge,
                )
                IconButton(onClick = state::add) {
                    AddIcon(contentDescription = stringResource(Res.string.web_header_add_button_content_description))
                }
            }

            state.rowList.forEachIndexed { index, row ->
                key(row) {
                    WebHeaderRow(
                        onRemove = { state.remove(row) },
                        state = row,
                        position = index + 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun WebHeaderRow(
    onRemove: () -> Unit,
    state: WebHeaderRowState,
    position: Int,
    modifier: Modifier = Modifier,
) {
    val nameContentDescription = stringResource(Res.string.web_header_name_input_content_description, position.toString())
    val valueContentDescription = stringResource(Res.string.web_header_value_input_content_description, position.toString())

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClearTextField(
            state = state.nameState,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = nameContentDescription },
            label = { Text(text = stringResource(Res.string.web_header_name_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        ClearTextField(
            state = state.valueState,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = valueContentDescription },
            label = { Text(text = stringResource(Res.string.web_header_value_input_label)) },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        IconButton(onClick = onRemove) {
            RemoveIcon(contentDescription = stringResource(Res.string.web_header_remove_button_content_description))
        }
    }
}

@ComponentPreview
@Composable
private fun WebHeaderInputPreview() {
    DiaryTheme {
        Surface {
            WebHeaderInput(
                state =
                    rememberWebHeaderInputState(
                        initialRowList = listOf(WebHeaderRowState(initialName = "Authorization", initialValue = "Bearer token")),
                    ),
            )
        }
    }
}
