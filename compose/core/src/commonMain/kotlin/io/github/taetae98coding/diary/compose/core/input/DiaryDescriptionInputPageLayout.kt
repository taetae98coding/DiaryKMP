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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.diary_description_input_label
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
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
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 10, maxHeightInLines = 20),
            )
            DiaryDescriptionInputPreviewPage(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
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
    Markdown(
        content = state.text.toString().withMarkdownHardLineBreak(),
        modifier = modifier,
        typography = previewMarkdownTypography(),
    )
}

@Composable
private fun previewMarkdownTypography(): MarkdownTypography =
    markdownTypography(
        h1 = DiaryTheme.typography.displayLarge.copy(fontSize = 28.sp, lineHeight = 36.sp),
        h2 = DiaryTheme.typography.displayMedium.copy(fontSize = 24.sp, lineHeight = 32.sp),
        h3 = DiaryTheme.typography.displaySmall.copy(fontSize = 22.sp, lineHeight = 30.sp),
        h4 = DiaryTheme.typography.headlineMedium.copy(fontSize = 20.sp, lineHeight = 28.sp),
        h5 = DiaryTheme.typography.headlineSmall.copy(fontSize = 18.sp, lineHeight = 26.sp),
        h6 = DiaryTheme.typography.titleLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
    )

@ComponentPreview
@Composable
private fun DiaryDescriptionInputPageLayoutPreview() {
    DiaryTheme {
        Surface {
            DiaryDescriptionInputPageLayout()
        }
    }
}
