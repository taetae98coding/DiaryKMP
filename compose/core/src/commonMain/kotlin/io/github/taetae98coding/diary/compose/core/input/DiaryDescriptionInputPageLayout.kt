package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_description_input_label
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.text.DiaryMarkdown
import io.github.taetae98coding.diary.compose.core.textfield.ClearTextField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun DiaryDescriptionInputPageLayout(
    modifier: Modifier = Modifier,
    state: DiaryDescriptionInputState = rememberDiaryDescriptionInputState(),
) {
    Layout(
        content = {
            ClearTextField(
                state = state.textFieldState,
                label = { Text(text = stringResource(Res.string.diary_description_input_label)) },
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = DiaryDescriptionInputDefaults.MIN_HEIGHT_IN_LINES, maxHeightInLines = DiaryDescriptionInputDefaults.MAX_HEIGHT_IN_LINES),
            )
            DiaryDescriptionInputPreviewPage(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(DiaryTheme.dimens.cardContentPadding),
                state = state,
            )
        },
        modifier =
            modifier
                .clipToBounds()
                .anchoredDraggable(state = state.swipeState, orientation = Orientation.Horizontal),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val pageConstraints = constraints.copy(minWidth = width, maxWidth = width)
        val inputPlaceable = measurables[0].measure(pageConstraints)
        val previewPlaceable =
            measurables[1].measure(
                pageConstraints.copy(minHeight = inputPlaceable.height, maxHeight = inputPlaceable.height),
            )

        state.swipeState.updateAnchors(
            DraggableAnchors {
                DiaryDescriptionInputPage.Input at 0f
                DiaryDescriptionInputPage.Preview at -width.toFloat()
            },
        )

        layout(width, inputPlaceable.height) {
            val offset = state.swipeState.requireOffset().roundToInt()

            inputPlaceable.place(x = offset, y = 0)
            previewPlaceable.place(x = offset + width, y = 0)
        }
    }
}

@Composable
private fun DiaryDescriptionInputPreviewPage(
    modifier: Modifier = Modifier,
    state: DiaryDescriptionInputState = rememberDiaryDescriptionInputState(),
) {
    DiaryMarkdown(
        content = state.text.toString(),
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun DiaryDescriptionInputPageLayoutPreview() {
    DiaryTheme {
        Surface {
            DiaryDescriptionInputPageLayout()
        }
    }
}
