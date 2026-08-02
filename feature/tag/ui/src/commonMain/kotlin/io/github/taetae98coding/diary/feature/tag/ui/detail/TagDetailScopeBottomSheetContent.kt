package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_title
import org.jetbrains.compose.resources.stringResource

private val CONTENT_HORIZONTAL_PADDING = 24.dp
private val CONTENT_BOTTOM_PADDING = 16.dp
private val TITLE_VERTICAL_PADDING = 12.dp
private val ROW_MIN_HEIGHT = 56.dp

@Composable
internal fun TagDetailScopeBottomSheetContent(
    onSelect: (TagScope) -> Unit,
    modifier: Modifier = Modifier,
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.tag_detail_scope_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CONTENT_HORIZONTAL_PADDING, vertical = TITLE_VERTICAL_PADDING),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(bottom = CONTENT_BOTTOM_PADDING),
        ) {
            tagDetailScopeList.forEach { item ->
                ScopeRow(
                    onClick = { onSelect(item) },
                    modifier = Modifier.fillMaxWidth(),
                    scope = item,
                    isSelectedProvider = { item == scopeProvider() },
                )
            }
        }
    }
}

@Composable
private fun ScopeRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scope: TagScope = TagScope.SELF,
    isSelectedProvider: () -> Boolean = { false },
) {
    val isSelected = isSelectedProvider()

    Row(
        modifier =
            modifier
                .heightIn(min = ROW_MIN_HEIGHT)
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(horizontal = CONTENT_HORIZONTAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(tagDetailScopeLabel(scope = scope)),
            modifier = Modifier.weight(1F),
            style = DiaryTheme.typography.bodyLarge,
        )

        RadioButton(
            selected = isSelected,
            onClick = null,
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailScopeBottomSheetContentPreview() {
    DiaryTheme {
        TagDetailScopeBottomSheetContent(
            onSelect = {},
            scopeProvider = { TagScope.CHILD },
        )
    }
}
