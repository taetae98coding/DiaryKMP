package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagDetailFormState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkAddedResultEffect
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkPickerDialogHost
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkPickerEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailFormContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToTagAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagDetailUiState = { TagDetailUiState.Loading },
    state: TagFormState = rememberTagDetailFormState(initialDetail = TagDetail.EMPTY),
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.DETAIL, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val viewModel = koinViewModel<TagDetailLinkViewModel> { parametersOf(id) }
        val linkUiState by viewModel.uiState.collectAsStateWithLifecycle()
        val tagPagingItems = viewModel.tagPagingData.collectAsLazyPagingItems()

        TagLinkAddedResultEffect(
            requestKey = tagAddRequestKey,
            onTagAdded = viewModel::link,
        )

        TagDetailFormTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailFormContentEvent.ClickLink ->
                        if (tagPagingItems.isConfirmedEmpty()) {
                            navigateToTagAdd()
                        } else {
                            state.linkPickerDialogState.show()
                        }

                    is TagDetailFormContentEvent.ClickTag -> navigateToDetail(event.id)
                }
            },
            modifier = modifier,
            uiStateProvider = uiStateProvider,
            linkUiStateProvider = { linkUiState },
            state = state,
        )

        TagLinkPickerDialogHost(
            dialogState = state.linkPickerDialogState,
            onEvent = { event ->
                when (event) {
                    is TagLinkPickerEvent.ClickAdd -> navigateToTagAdd()
                    is TagLinkPickerEvent.Link -> viewModel.link(tagId = event.id)
                    is TagLinkPickerEvent.Unlink -> viewModel.unlink(tagId = event.id)
                    is TagLinkPickerEvent.ChangeQuery -> viewModel.updateQuery(query = event.query)
                }
            },
            tagPagingItems = tagPagingItems,
            uiStateProvider = { linkUiState },
        )
    }
}
