package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailTopBar(
    onEvent: (ContactDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> ContactDetailUiState = { ContactDetailUiState.Loading },
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible = { ContactDetailScaffoldComponentVisible() },
) {
    TopAppBar(
        // 제목과 액션은 서로 다른 자리이므로 각 슬롯에서 상태를 읽어, 한쪽이 바뀔 때 다른 쪽까지 다시 그리지 않는다.
        title = {
            val uiState = uiStateProvider()

            if (uiState is ContactDetailUiState.Content) {
                Text(
                    text = uiState.detail.name,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(ContactDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.contact_navigate_up_button_content_description),
                )
            }
        },
        actions = {
            val uiState = uiStateProvider()

            if (uiState is ContactDetailUiState.Content) {
                DeleteButton(
                    onClick = { onEvent(ContactDetailScaffoldEvent.ClickDelete) },
                    contentDescription = stringResource(Res.string.contact_detail_delete_button_content_description),
                    isInProgressProvider = { uiState.isDeleteInProgress },
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun ContactDetailTopBarPreview() {
    DiaryTheme {
        ContactDetailTopBar(
            onEvent = {},
            uiStateProvider = { ContactDetailUiState.Content(id = Uuid.NIL, detail = ContactDetail.EMPTY.copy(name = "김철수")) },
        )
    }
}
