package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlin.math.roundToInt

@Composable
internal fun ColorPickerChannelSlider(
    label: String,
    labelColor: Color,
    state: SliderState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ColorPickerDialogDefaults.ChannelSliderSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = labelColor,
        )
        Slider(
            state = state,
            modifier = Modifier.weight(1F),
        )
        Text(text = state.value.roundToInt().toString())
    }
}

@ComponentPreview
@Composable
private fun ColorPickerChannelSliderPreview() {
    DiaryTheme {
        Surface {
            ColorPickerChannelSlider(
                label = "R",
                labelColor = ColorPickerDialogDefaults.colors().redLabelColor,
                state = rememberColorPickerState(initialColor = Color(color = 0xFF3A7BD5.toInt())).redSliderState,
            )
        }
    }
}
