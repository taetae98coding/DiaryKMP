package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryFloatingActionButtonMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    icon: DiaryFloatingActionButtonMenuIcon,
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
                icon = DiaryFloatingActionButtonMenuIcon.CHECKLIST,
                contentDescription = "Bulk select",
                expandedContentDescription = "Close bulk select",
                items =
                    listOf(
                        DiaryFloatingActionButtonMenuItem(text = "Select all", icon = DiaryFloatingActionButtonMenuIcon.SELECT_ALL, onClick = {}),
                        DiaryFloatingActionButtonMenuItem(text = "Deselect all", icon = DiaryFloatingActionButtonMenuIcon.DESELECT, onClick = {}),
                    ),
            )
        }
    }
}
