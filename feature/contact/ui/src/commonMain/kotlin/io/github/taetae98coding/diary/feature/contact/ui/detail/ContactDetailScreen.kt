package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState

@Composable
internal fun ContactDetailScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible,
    viewModel: ContactDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? ContactDetailUiState.Content

    // 조회한 연락처가 정해지면 그 값으로 입력을 한 번만 채운다.
    key(content?.id) {
        val state = rememberContactDetailFormState(initialDetail = content?.detail ?: ContactDetail.EMPTY)

        ContactDetailScreenEffect(
            navigateUp = navigateUp,
            effect = viewModel.effect,
            state = state,
        )

        ContactDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is ContactDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
                    is ContactDetailScaffoldEvent.ClickUpdate -> viewModel.update(detail = state.detail)
                    is ContactDetailScaffoldEvent.ClickDelete -> viewModel.delete()
                }
            },
            modifier = modifier,
            state = state,
            uiStateProvider = { uiState },
            componentVisibleProvider = componentVisibleProvider,
        )
    }
}
