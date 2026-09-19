@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailScopeBottomSheetContent(
    onSelect: (TagScope) -> Unit,
    modifier: Modifier = Modifier,
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.tag_detail_scope_title),
            modifier = Modifier.styleable(style = DiaryTheme.styles.bottomSheetTitle),
            style = DiaryTheme.typography.titleLargeEmphasized,
        )

        Column(
            modifier =
                Modifier
                    .selectableGroup()
                    .styleable(style = DiaryTheme.styles.bottomSheetContent),
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
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).styleable(style = DiaryTheme.styles.bottomSheetRow),
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
