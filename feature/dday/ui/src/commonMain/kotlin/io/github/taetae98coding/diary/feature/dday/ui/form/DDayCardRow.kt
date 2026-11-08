package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun DDayCardRow(
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = label) },
        modifier = modifier,
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@ComponentPreview
@Composable
private fun DDayCardRowPreview() {
    DiaryTheme {
        Surface {
            DDayCardRow(label = "기준일을 1일로 세기") {
                Switch(checked = true, onCheckedChange = null)
            }
        }
    }
}
