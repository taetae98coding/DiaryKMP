@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleFadeVisibility
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.buttonContentDescription
import io.github.taetae98coding.diary.compose.core.icon.DropDownIcon
import io.github.taetae98coding.diary.compose.core.icon.DropUpIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.DiarySearchInputField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
internal fun MaterialDiaryTopBar(
    modifier: Modifier = Modifier,
    title: DiaryTopBarTitle? = null,
    navigateUp: DiaryTopBarNavigateUp? = null,
    actions: List<DiaryTopBarAction> = emptyList(),
    isTitleCentered: Boolean = false,
) {
    val titleContent: @Composable () -> Unit = { title?.let { MaterialTopBarTitle(title = it) } }
    val navigationIcon: @Composable () -> Unit = {
        navigateUp?.let { NavigateUpButton(onClick = it.onClick, contentDescription = it.contentDescription) }
    }
    val actionsContent: @Composable RowScope.() -> Unit = { actions.forEach { action -> MaterialTopBarAction(action = action) } }
    val subtitle = (title as? DiaryTopBarTitle.Text)?.subtitle

    when {
        subtitle != null ->
            TopAppBar(
                title = titleContent,
                subtitle = { Text(text = subtitle, maxLines = DiaryTopBarDefaults.TITLE_MAX_LINES) },
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
            )

        isTitleCentered ->
            CenterAlignedTopAppBar(
                title = titleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
            )

        else ->
            TopAppBar(
                title = titleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
            )
    }
}

@Composable
private fun MaterialTopBarTitle(title: DiaryTopBarTitle) {
    when (title) {
        is DiaryTopBarTitle.Text ->
            if (title.isMarquee) {
                Text(
                    text = title.text,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = DiaryTopBarDefaults.TITLE_MAX_LINES,
                )
            } else {
                Text(text = title.text)
            }

        is DiaryTopBarTitle.Button -> MaterialTopBarTitleButton(title = title)

        is DiaryTopBarTitle.Search ->
            DiarySearchInputField(
                placeholder = title.placeholder,
                modifier = Modifier.fillMaxWidth(),
                state = title.state,
                focusRequester = title.focusRequester,
            )
    }
}

@Composable
private fun MaterialTopBarTitleButton(title: DiaryTopBarTitle.Button) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(DiaryTopBarDefaults.TitleButtonCornerSize))
                .clickable(role = Role.Button, onClick = title.onClick)
                .padding(
                    horizontal = DiaryTopBarDefaults.TitleButtonHorizontalPadding,
                    vertical = DiaryTopBarDefaults.TitleButtonVerticalPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title.text)
        DiaryCrossfade(targetState = title.isExpanded) { isExpanded ->
            if (isExpanded) {
                DropUpIcon(contentDescription = title.collapseContentDescription)
            } else {
                DropDownIcon(contentDescription = title.expandContentDescription)
            }
        }
    }
}

@Composable
private fun MaterialTopBarAction(action: DiaryTopBarAction) {
    when (action) {
        is DiaryTopBarAction.Icon ->
            DiaryScaleFadeVisibility(visible = action.isVisible) {
                MaterialTopBarIconButton(action = action)
            }

        is DiaryTopBarAction.Toggle -> MaterialTopBarToggleButton(action = action)

        is DiaryTopBarAction.Applied -> MaterialTopBarAppliedButton(action = action)

        is DiaryTopBarAction.Text -> MaterialTopBarTextButton(action = action)
    }
}

@Composable
private fun MaterialTopBarIconButton(action: DiaryTopBarAction.Icon) {
    DiaryTooltipBox(text = action.contentDescription) {
        IconButton(
            onClick = action.onClick,
            modifier = Modifier.buttonContentDescription(contentDescription = action.contentDescription),
            enabled = action.isEnabled,
        ) {
            MaterialTopBarActionIcon(icon = action.icon, isInProgress = action.isInProgress)
        }
    }
}

@Composable
private fun MaterialTopBarToggleButton(action: DiaryTopBarAction.Toggle) {
    DiaryTooltipBox(text = action.contentDescription) {
        IconToggleButton(
            checked = action.isChecked,
            onCheckedChange = { action.onClick() },
            modifier = Modifier.buttonContentDescription(contentDescription = action.contentDescription),
        ) {
            MaterialTopBarActionIcon(icon = action.icon, isInProgress = action.isInProgress)
        }
    }
}

@Composable
private fun MaterialTopBarAppliedButton(action: DiaryTopBarAction.Applied) {
    IconButton(
        onClick = action.onClick,
        modifier =
            Modifier.semantics {
                contentDescription = action.contentDescription
                if (action.isApplied) {
                    stateDescription = action.appliedStateDescription
                }
            },
        colors =
            IconButtonDefaults.iconButtonColors(
                contentColor =
                    if (action.isApplied) {
                        DiaryTheme.colorScheme.primary
                    } else {
                        DiaryTheme.colorScheme.onSurfaceVariant
                    },
            ),
    ) {
        Icon(imageVector = action.icon.imageVector, contentDescription = null)
    }
}

@Composable
private fun MaterialTopBarTextButton(action: DiaryTopBarAction.Text) {
    OutlinedIconButton(
        onClick = action.onClick,
        modifier =
            Modifier
                .minimumInteractiveComponentSize()
                .size(IconButtonDefaults.extraSmallContainerSize())
                .semantics { contentDescription = action.contentDescription },
        shape = IconButtonDefaults.extraSmallSquareShape,
    ) {
        Text(
            text = action.text,
            style = DiaryTheme.typography.labelMediumEmphasized,
        )
    }
}

@Composable
private fun MaterialTopBarActionIcon(
    icon: DiaryTopBarIcon,
    isInProgress: Boolean,
) {
    key(icon.buttonKey) {
        // 진행 표시와 상태별 아이콘이 같은 자리에서 서로를 대체하므로 전환 대상을 하나로 묶어 한 번만 교차 전환한다.
        DiaryCrossfade(targetState = MaterialTopBarActionIconContent(icon = icon, isInProgress = isInProgress)) { content ->
            if (content.isInProgress) {
                CircularWavyProgressIndicator(modifier = Modifier.size(DiaryTheme.dimens.inProgressIndicatorSize))
            } else {
                Icon(imageVector = content.icon.imageVector, contentDescription = null)
            }
        }
    }
}

private data class MaterialTopBarActionIconContent(
    val icon: DiaryTopBarIcon,
    val isInProgress: Boolean,
)

private class TopBarActionPreviewStatePreviewParameter : PreviewParameterProvider<TopBarActionPreviewState> {
    override val values: Sequence<TopBarActionPreviewState> =
        sequenceOf(
            TopBarActionPreviewState(),
            TopBarActionPreviewState(isInProgress = true, isChecked = true, isApplied = true),
        )
}

private data class TopBarActionPreviewState(
    val isInProgress: Boolean = false,
    val isChecked: Boolean = false,
    val isApplied: Boolean = false,
)

@ComponentPreview
@Composable
private fun MaterialDiaryTopBarPreview(
    @PreviewParameter(TopBarActionPreviewStatePreviewParameter::class) state: TopBarActionPreviewState,
) {
    DiaryTheme {
        MaterialDiaryTopBar(
            title = DiaryTopBarTitle.Text(text = "제목", subtitle = "보조 제목"),
            navigateUp = DiaryTopBarNavigateUp(contentDescription = "뒤로가기", onClick = {}),
            actions =
                listOf(
                    DiaryTopBarAction.Text(text = "19", contentDescription = "오늘로 이동", onClick = {}),
                    DiaryTopBarAction.Applied(
                        icon = DiaryTopBarIcon.FILTER,
                        contentDescription = "필터",
                        isApplied = state.isApplied,
                        appliedStateDescription = "필터 적용됨",
                        onClick = {},
                    ),
                    DiaryTopBarAction.Toggle(
                        icon = DiaryTopBarIcon.FINISH,
                        contentDescription = "완료",
                        isChecked = state.isChecked,
                        onClick = {},
                    ),
                    DiaryTopBarAction.Icon(
                        icon = DiaryTopBarIcon.DELETE,
                        contentDescription = "삭제",
                        onClick = {},
                        isInProgress = state.isInProgress,
                    ),
                ),
        )
    }
}

@ComponentPreview
@Composable
private fun MaterialDiaryTopBarSearchPreview() {
    DiaryTheme {
        MaterialDiaryTopBar(
            title =
                DiaryTopBarTitle.Search(
                    state = rememberTextFieldState(),
                    placeholder = "검색어를 입력하세요",
                    focusRequester = remember { FocusRequester() },
                ),
            navigateUp = DiaryTopBarNavigateUp(contentDescription = "뒤로가기", onClick = {}),
        )
    }
}
