@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.color_picker_dialog_random_button_content_description
import io.github.taetae98coding.diary.compose.core.icon.RefreshIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.transparentContainer
import io.github.taetae98coding.diary.compose.core.textfield.transparentIndicator
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.compose.ui.color.contentColor
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ColorPickerPreview(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val contentColor by remember(state) { derivedStateOf { state.color.contentColor() } }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ColorPickerDialogDefaults.PreviewHeight)
                .styleable { background(state.color) },
    ) {
        IconButton(
            onClick = {
                coroutineScope.launch { state.animateColorTo(value = randomColor()) }
            },
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(ColorPickerDialogDefaults.RandomButtonPadding),
            colors = IconButtonDefaults.iconButtonColors(contentColor = contentColor),
        ) {
            RefreshIcon(contentDescription = stringResource(Res.string.color_picker_dialog_random_button_content_description))
        }
        TextField(
            state = state.hexTextFieldState,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            lineLimits = TextFieldLineLimits.SingleLine,
            colors =
                TextFieldDefaults
                    .colors()
                    .transparentIndicator()
                    .transparentContainer()
                    .copy(
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor,
                        cursorColor = contentColor,
                    ),
        )
    }
}

@ComponentPreview
@Composable
private fun ColorPickerPreviewPreview() {
    DiaryTheme {
        Surface {
            ColorPickerPreview(state = rememberColorPickerState(initialColor = Color(color = 0xFF3A7BD5.toInt())))
        }
    }
}
