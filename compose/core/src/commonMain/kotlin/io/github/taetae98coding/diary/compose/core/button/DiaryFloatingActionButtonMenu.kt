package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.taetae98coding.diary.compose.icon.Checklist
import io.github.taetae98coding.diary.compose.icon.Deselect
import io.github.taetae98coding.diary.compose.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.icon.SelectAll
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
public fun DiaryFloatingActionButtonMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    icon: ImageVector,
    contentDescription: String,
    expandedContentDescription: String,
    items: List<DiaryFloatingActionButtonMenuItem>,
    modifier: Modifier = Modifier,
) {
    val scope =
        DiaryFloatingActionButtonMenuOverrideScope(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            icon = icon,
            contentDescription = contentDescription,
            expandedContentDescription = expandedContentDescription,
            items = items,
            modifier = modifier,
        )

    with(LocalDiaryFloatingActionButtonMenuOverride.current) { scope.FloatingActionButtonMenu() }
}

@ComponentPreview
@Composable
private fun DiaryFloatingActionButtonMenuPreview() {
    var expanded by remember { mutableStateOf(true) }

    DiaryTheme {
        Surface {
            DiaryFloatingActionButtonMenu(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                icon = DiaryIcons.Checklist,
                contentDescription = "Bulk select",
                expandedContentDescription = "Close bulk select",
                items =
                    listOf(
                        DiaryFloatingActionButtonMenuItem(text = "Select all", icon = DiaryIcons.SelectAll, onClick = {}),
                        DiaryFloatingActionButtonMenuItem(text = "Deselect all", icon = DiaryIcons.Deselect, onClick = {}),
                    ),
            )
        }
    }
}
