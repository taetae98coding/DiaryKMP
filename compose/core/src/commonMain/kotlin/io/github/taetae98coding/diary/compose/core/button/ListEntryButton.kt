package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.icon.NextIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

private val LABEL_SPACING = 4.dp

@Composable
public fun ListEntryButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
    ) {
        Text(text = label)
        Spacer(modifier = Modifier.width(LABEL_SPACING))
        NextIcon(modifier = Modifier.size(ButtonDefaults.IconSize))
    }
}

@ComponentPreview
@Composable
private fun ListEntryButtonPreview() {
    DiaryTheme {
        Surface {
            ListEntryButton(
                onClick = {},
                label = "Finished memos",
            )
        }
    }
}
