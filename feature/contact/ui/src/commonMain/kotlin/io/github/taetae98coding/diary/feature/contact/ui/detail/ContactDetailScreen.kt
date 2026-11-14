package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.compose.core.share.ShareFileType
import io.github.taetae98coding.diary.compose.core.share.ShareResult
import io.github.taetae98coding.diary.compose.core.share.shareFailedMessage
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.contact.share.toContactShareFile
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoContent
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.contactDetailTabShortcut
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.rememberContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.handleContactFormEvent
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    id: Uuid,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible,
    contactViewModel: ContactDetailViewModel,
    tagViewModel: EntityDetailTagViewModel,
    contentSharerFactory: ContentSharerFactory,
    modifier: Modifier = Modifier,
) {
    val tabState = key(id) { rememberContactDetailTabState() }
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val uiState by contactViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()
    val content = uiState as? ContactDetailUiState.Content
    val state = key(content?.id) { rememberContactDetailFormState(initialDetail = content?.detail ?: ContactDetail.EMPTY) }
    val isUpdateEnabled by rememberIsUpdateEnabled(state = state, uiStateProvider = { uiState })
    val share = rememberShareContact(state = state, contentSharer = contentSharerFactory.rememberContentSharer(), storedNameProvider = { content?.detail?.name.orEmpty() })

    EntityTagAddedResultEffect(requestKey = tagAddRequestKey, onTagAdded = tagViewModel::add)
    ContactDetailScreenEffect(
        navigateUp = navigateUp,
        effect = contactViewModel.effect,
        state = state,
    )

    DismissUndoSnackbarEffect(keyProvider = { tabState.tab }, hostState = state.snackbarHostState)

    ContactDetailScaffold(
        onEvent = { event -> handleContactDetailScaffoldEvent(event = event, contactViewModel = contactViewModel, state = state, navigateUp = navigateUp, share = share) },
        onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        modifier =
            modifier.contactDetailTabShortcut(
                onUpdate = { contactViewModel.update(detail = state.detail) },
                onMemoAdd = navigateToMemoAdd,
                state = tabState,
                isUpdateEnabledProvider = { isUpdateEnabled },
            ),
        state = state,
        tabState = tabState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
        isShareSupported = share != null,
        tabFloatingActionButton = { tab ->
            ContactDetailTabFloatingActionButton(
                tab = tab,
                onUpdate = { contactViewModel.update(detail = state.detail) },
                onMemoAdd = navigateToMemoAdd,
                isUpdateVisible = isUpdateEnabled,
                isUpdateInProgressProvider = { content?.isUpdateInProgress == true },
            )
        },
    ) { tab ->
        ContactDetailTabContent(
            tab = tab,
            id = id,
            navigateToTagAdd = navigateToTagAdd,
            navigateToTagDetail = navigateToTagDetail,
            navigateToMemoDetail = navigateToMemoDetail,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
            viewModelStoreProvider = viewModelStoreProvider,
            state = state,
            selectableTagPagingItems = selectableTagPagingItems,
            uiStateProvider = { uiState },
            tagUiStateProvider = { tagUiState },
        )
    }
}

private fun handleContactDetailScaffoldEvent(
    event: ContactDetailScaffoldEvent,
    contactViewModel: ContactDetailViewModel,
    state: ContactFormState,
    navigateUp: () -> Unit,
    share: (() -> Unit)?,
) {
    when (event) {
        is ContactDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is ContactDetailScaffoldEvent.ClickShare -> share?.invoke()
        is ContactDetailScaffoldEvent.ClickUpdate -> contactViewModel.update(detail = state.detail)
        is ContactDetailScaffoldEvent.ClickFavorite -> contactViewModel.toggleFavorite()
        is ContactDetailScaffoldEvent.ClickDelete -> contactViewModel.delete()
    }
}

@Composable
private fun ContactDetailTabContent(
    tab: ContactDetailTab,
    id: Uuid,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    viewModelStoreProvider: ViewModelStoreProvider,
    state: ContactFormState,
    selectableTagPagingItems: LazyPagingItems<Tag>,
    uiStateProvider: () -> ContactDetailUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
) {
    when (tab) {
        ContactDetailTab.DETAIL ->
            ContactDetailScaffoldContent(
                onFormEvent = { event ->
                    handleContactFormEvent(
                        event = event,
                        state = state,
                        selectableTagPagingItems = selectableTagPagingItems,
                        navigateToTagAdd = navigateToTagAdd,
                        navigateToTagDetail = navigateToTagDetail,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                state = state,
                uiStateProvider = uiStateProvider,
                tagUiStateProvider = tagUiStateProvider,
            )

        ContactDetailTab.MEMO ->
            ContactDetailMemoContent(
                id = id,
                viewModelStoreProvider = viewModelStoreProvider,
                navigateToMemoDetail = navigateToMemoDetail,
                navigateToMemoFinishedList = navigateToMemoFinishedList,
                modifier = Modifier.fillMaxSize(),
                snackbarHostState = state.snackbarHostState,
            )
    }
}

@Composable
private fun rememberShareContact(
    state: ContactFormState,
    contentSharer: ContentSharer,
    storedNameProvider: () -> String,
): (() -> Unit)? {
    val coroutineScope = rememberCoroutineScope()
    val failedMessage = shareFailedMessage()
    val latestStoredNameProvider by rememberUpdatedState(storedNameProvider)

    if (!contentSharer.isFileSupported(type = ShareFileType.VCARD)) return null

    return remember(state, contentSharer, coroutineScope, failedMessage) {
        {
            val file = state.detail.toContactShareFile(storedName = latestStoredNameProvider())
            val content =
                ShareContent.File(
                    bytes = file.vCard.encodeToByteArray(),
                    name = file.name,
                    type = ShareFileType.VCARD,
                    title = file.name,
                )

            coroutineScope.launch {
                if (contentSharer.share(content = content) == ShareResult.FAILED) {
                    state.snackbarHostState.showImmediate(message = failedMessage)
                }
            }
        }
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
