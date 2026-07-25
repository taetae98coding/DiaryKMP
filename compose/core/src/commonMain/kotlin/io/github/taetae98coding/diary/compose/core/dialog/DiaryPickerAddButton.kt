package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryPickerAddButton(
    onClick: () -> Unit,
    label: String,
    actionLabel: String,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier =
            modifier.semantics {
                onClick(label = actionLabel, action = null)
            },
    ) {
        AddIcon()
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = label)
    }
}

@ComponentPreview
@Composable
private fun DiaryPickerAddButtonPreview() {
    DiaryTheme {
        Surface {
            DiaryPickerAddButton(
                onClick = {},
                label = "태그 추가",
                actionLabel = "태그 추가",
            )
        }
    }
}
