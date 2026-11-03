package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.graphics.rememberTemplateUIImage
import io.github.taetae98coding.diary.compose.core.icon.ArrowBack
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons

@Composable
internal fun rememberUiKitTopBarNavigateUpButton(navigateUp: DiaryTopBarNavigateUp): UiKitTopBarButton =
    UiKitTopBarButton(
        id = "navigateUp",
        image = rememberTemplateUIImage(imageVector = DiaryIcons.ArrowBack, size = DiaryTopBarDefaults.IosIconSize),
        text = null,
        contentDescription = navigateUp.contentDescription.orEmpty(),
        stateDescription = null,
        isEnabled = true,
        isInProgress = false,
        isHighlighted = false,
        isToggle = false,
        isVisible = true,
        onClick = navigateUp.onClick,
    )

@Composable
internal fun rememberUiKitTopBarButton(action: DiaryTopBarAction): UiKitTopBarButton =
    when (action) {
        is DiaryTopBarAction.Icon ->
            UiKitTopBarButton(
                id = action.icon.buttonKey,
                image = rememberTemplateUIImage(imageVector = action.icon.imageVector, size = DiaryTopBarDefaults.IosIconSize),
                text = null,
                contentDescription = action.contentDescription,
                stateDescription = null,
                isEnabled = action.isEnabled,
                isInProgress = action.isInProgress,
                isHighlighted = false,
                isToggle = false,
                isVisible = action.isVisible,
                onClick = action.onClick,
            )

        is DiaryTopBarAction.Toggle ->
            UiKitTopBarButton(
                id = action.icon.buttonKey,
                image = rememberTemplateUIImage(imageVector = action.icon.imageVector, size = DiaryTopBarDefaults.IosIconSize),
                text = null,
                contentDescription = action.contentDescription,
                stateDescription = null,
                isEnabled = true,
                isInProgress = action.isInProgress,
                isHighlighted = action.isChecked,
                isToggle = true,
                isVisible = true,
                onClick = action.onClick,
            )

        is DiaryTopBarAction.Applied ->
            UiKitTopBarButton(
                id = action.icon.buttonKey,
                image = rememberTemplateUIImage(imageVector = action.icon.imageVector, size = DiaryTopBarDefaults.IosIconSize),
                text = null,
                contentDescription = action.contentDescription,
                stateDescription = action.appliedStateDescription.takeIf { action.isApplied },
                isEnabled = true,
                isInProgress = false,
                isHighlighted = action.isApplied,
                isToggle = false,
                isVisible = true,
                onClick = action.onClick,
            )

        is DiaryTopBarAction.Text ->
            UiKitTopBarButton(
                id = TEXT_BUTTON_ID,
                image = null,
                text = action.text,
                contentDescription = action.contentDescription,
                stateDescription = null,
                isEnabled = true,
                isInProgress = false,
                isHighlighted = false,
                isToggle = false,
                isVisible = true,
                onClick = action.onClick,
            )
    }

private const val TEXT_BUTTON_ID = "text"
