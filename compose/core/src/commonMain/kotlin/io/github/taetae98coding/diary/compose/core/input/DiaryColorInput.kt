@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.ColorPickerHost
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.compose.ui.color.contentColor
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.compose.ui.color.toRgbString
import kotlinx.coroutines.launch

@Composable
public fun DiaryColorInput(
    modifier: Modifier = Modifier,
    state: DiaryColorInputState = rememberDiaryColorInputState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val dialogState = rememberDialogState()
    val contentColor by remember(state) { derivedStateOf { state.color.contentColor() } }
    val shape = MaterialTheme.shapes.medium

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .styleable {
                    shape(shape)
                    clip()
                    background(state.color)
                }.clickable(onClick = dialogState::show)
                .padding(DiaryTheme.dimens.cardContentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.color.toHexString(),
            color = contentColor,
            style = DiaryTheme.typography.titleMediumEmphasized,
        )
        Text(
            text = state.color.toRgbString(),
            color = contentColor,
            style = DiaryTheme.typography.labelSmall,
        )
    }

    ColorPickerHost(
        initialColorProvider = { state.color },
        onConfirm = { color ->
            coroutineScope.launch { state.animateTo(color = color) }
        },
        dialogState = dialogState,
    )
}

@ComponentPreview
@Composable
private fun DiaryColorInputPreview() {
    DiaryTheme {
        Surface {
            DiaryColorInput(state = rememberDiaryColorInputState(initialColor = Color(color = 0xFF3A7BD5.toInt())))
        }
    }
}
