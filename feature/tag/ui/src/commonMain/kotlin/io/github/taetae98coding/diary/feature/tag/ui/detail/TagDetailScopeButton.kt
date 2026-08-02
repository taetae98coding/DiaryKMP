package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_action_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_applied_state_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailScopeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAppliedProvider: () -> Boolean = { false },
) {
    val isApplied = isAppliedProvider()
    val appliedStateDescription = stringResource(Res.string.tag_detail_scope_applied_state_description)

    IconButton(
        onClick = onClick,
        modifier =
            modifier.semantics {
                if (isApplied) {
                    stateDescription = appliedStateDescription
                }
            },
        colors =
            IconButtonDefaults.iconButtonColors(
                contentColor =
                    if (isApplied) {
                        DiaryTheme.colorScheme.primary
                    } else {
                        DiaryTheme.colorScheme.onSurfaceVariant
                    },
            ),
    ) {
        FilterIcon(contentDescription = stringResource(Res.string.tag_detail_scope_action_content_description))
    }
}

@ComponentPreview
@Composable
private fun TagDetailScopeButtonPreview() {
    DiaryTheme {
        Surface {
            TagDetailScopeButton(
                onClick = {},
                isAppliedProvider = { true },
            )
        }
    }
}
