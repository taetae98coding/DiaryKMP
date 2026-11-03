@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.Close
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun MaterialDiaryFloatingActionButtonMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    icon: DiaryFloatingActionButtonMenuIcon,
    contentDescription: String,
    expandedContentDescription: String,
    items: List<DiaryFloatingActionButtonMenuItem>,
    modifier: Modifier = Modifier,
) {
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = onExpandedChange,
            ) {
                val isClosing by remember { derivedStateOf { checkedProgress > DiaryFloatingActionButtonMenuDefaults.CLOSE_ICON_PROGRESS } }
                val iconModifier =
                    with(ToggleFloatingActionButtonDefaults) {
                        Modifier.animateIcon(checkedProgress = { checkedProgress })
                    }

                if (isClosing) {
                    Icon(imageVector = DiaryIcons.Close, contentDescription = expandedContentDescription, modifier = iconModifier)
                } else {
                    Icon(imageVector = icon.imageVector, contentDescription = contentDescription, modifier = iconModifier)
                }
            }
        },
        modifier = modifier,
    ) {
        items.forEach { item ->
            FloatingActionButtonMenuItem(
                onClick = item.onClick,
                text = { Text(text = item.text) },
                icon = { Icon(imageVector = item.icon.imageVector, contentDescription = null) },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MaterialDiaryFloatingActionButtonMenuPreview() {
    DiaryTheme {
        Surface {
            MaterialDiaryFloatingActionButtonMenu(
                expanded = true,
                onExpandedChange = {},
                icon = DiaryFloatingActionButtonMenuIcon.CHECKLIST,
                contentDescription = "Bulk select",
                expandedContentDescription = "Close bulk select",
                items = listOf(DiaryFloatingActionButtonMenuItem(text = "Select all", icon = DiaryFloatingActionButtonMenuIcon.SELECT_ALL, onClick = {})),
            )
        }
    }
}
