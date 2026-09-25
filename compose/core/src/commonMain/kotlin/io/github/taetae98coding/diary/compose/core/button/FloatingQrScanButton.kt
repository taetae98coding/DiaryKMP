package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.QrScanIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
public fun FloatingQrScanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.buttonContentDescription(contentDescription = contentDescription),
        ) {
            QrScanIcon(contentDescription = null)
        }
    }
}

@ComponentPreview
@Composable
private fun FloatingQrScanButtonPreview() {
    DiaryTheme {
        Surface {
            FloatingQrScanButton(onClick = {})
        }
    }
}
