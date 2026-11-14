package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun RoutineCardRow(
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
private fun RoutineCardRowPreview() {
    DiaryTheme {
        Surface {
            RoutineCardRow(label = "시작 날짜") {
                TextButton(onClick = {}) {
                    Text(text = "2026. 7. 19.")
                }
            }
        }
    }
}
