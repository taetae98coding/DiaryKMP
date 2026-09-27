package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoContent
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.contactDetailTabShortcut
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.rememberContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailScreen(
    navigateUp: () -> Unit,
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    id: Uuid,
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible,
    viewModel: ContactDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val tabState = key(id) { rememberContactDetailTabState() }
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? ContactDetailUiState.Content
    val state = key(content?.id) { rememberContactDetailFormState(initialDetail = content?.detail ?: ContactDetail.EMPTY) }
    val isUpdateEnabled by rememberIsUpdateEnabled(state = state, uiStateProvider = { uiState })

    ContactDetailScreenEffect(
        navigateUp = navigateUp,
        effect = viewModel.effect,
        state = state,
    )
    DismissUndoSnackbarEffect(keyProvider = { tabState.tab }, hostState = state.hostState)

    ContactDetailScaffold(
        onEvent = { event ->
            when (event) {
                is ContactDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
                is ContactDetailScaffoldEvent.ClickUpdate -> viewModel.update(detail = state.detail)
                is ContactDetailScaffoldEvent.ClickFavorite -> viewModel.toggleFavorite()
                is ContactDetailScaffoldEvent.ClickDelete -> viewModel.delete()
            }
        },
        modifier =
            modifier.contactDetailTabShortcut(
                onUpdate = { viewModel.update(detail = state.detail) },
                onMemoAdd = navigateToMemoAdd,
                state = tabState,
                isUpdateEnabledProvider = { isUpdateEnabled },
            ),
        state = state,
        tabState = tabState,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
        tabFloatingActionButton = { tab ->
            ContactDetailTabFloatingActionButton(
                tab = tab,
                onUpdate = { viewModel.update(detail = state.detail) },
                onMemoAdd = navigateToMemoAdd,
                isUpdateVisible = isUpdateEnabled,
                isUpdateInProgressProvider = { content?.isUpdateInProgress == true },
            )
        },
    ) { tab ->
        ContactDetailTabContent(
            tab = tab,
            id = id,
            navigateToMemoDetail = navigateToMemoDetail,
            viewModelStoreProvider = viewModelStoreProvider,
            state = state,
            uiStateProvider = { uiState },
        )
    }
}

@Composable
private fun ContactDetailTabContent(
    tab: ContactDetailTab,
    id: Uuid,
    navigateToMemoDetail: (Uuid) -> Unit,
    viewModelStoreProvider: ViewModelStoreProvider,
    state: ContactFormState,
    uiStateProvider: () -> ContactDetailUiState,
) {
    when (tab) {
        ContactDetailTab.DETAIL ->
            ContactDetailScaffoldContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                uiStateProvider = uiStateProvider,
            )

        ContactDetailTab.MEMO ->
            ContactDetailMemoContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoDetail = navigateToMemoDetail,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.hostState,
            )
    }
}

@Composable
internal fun rememberIsUpdateEnabled(
    state: ContactFormState,
    uiStateProvider: () -> ContactDetailUiState,
): State<Boolean> =
    remember(state) {
        derivedStateOf {
            val content = uiStateProvider() as? ContactDetailUiState.Content
            content != null && state.detail != content.detail
        }
    }
