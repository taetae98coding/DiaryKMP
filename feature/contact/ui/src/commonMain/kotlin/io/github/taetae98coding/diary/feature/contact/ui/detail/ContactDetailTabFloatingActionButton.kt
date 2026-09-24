package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_update_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoFloatingActionButton
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContactDetailTabFloatingActionButton(
    tab: ContactDetailTab,
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    modifier: Modifier = Modifier,
    isUpdateVisible: Boolean = false,
    isUpdateInProgressProvider: () -> Boolean = { false },
) {
    when (tab) {
        ContactDetailTab.DETAIL ->
            DiaryScaleVisibility(
                visible = isUpdateVisible,
                modifier = modifier,
            ) {
                FloatingCheckButton(
                    onClick = onUpdate,
                    contentDescription = stringResource(Res.string.contact_detail_update_button_content_description),
                    isInProgressProvider = isUpdateInProgressProvider,
                )
            }

        ContactDetailTab.MEMO ->
            ContactDetailMemoFloatingActionButton(
                onClick = onMemoAdd,
                modifier = modifier,
            )
    }
}

@ComponentPreview
@Composable
private fun ContactDetailTabFloatingActionButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isMemoTab: Boolean,
) {
    DiaryTheme {
        Surface {
            ContactDetailTabFloatingActionButton(
                tab = if (isMemoTab) ContactDetailTab.MEMO else ContactDetailTab.DETAIL,
                onUpdate = {},
                onMemoAdd = {},
                isUpdateVisible = true,
            )
        }
    }
}
