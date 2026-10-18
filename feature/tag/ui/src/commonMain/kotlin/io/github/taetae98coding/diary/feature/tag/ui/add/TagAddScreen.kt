package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagAddFormState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkAddedResultEffect
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkPickerEvent
import kotlin.uuid.Uuid

@Composable
internal fun TagAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    addedResultRequestKey: Uuid?,
    tagAddRequestKey: Uuid,
    componentVisibleProvider: () -> TagAddScaffoldComponentVisible,
    addViewModel: TagAddViewModel,
    linkViewModel: TagAddLinkViewModel,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberTagAddFormState()
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val linkUiState by linkViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = linkViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = linkViewModel.selectableTagPagingData.collectAsLazyPagingItems()

    DiaryTitleInputFocusEffect(state = scaffoldState.titleState)
    TagLinkAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = linkViewModel::link,
    )
    TagAddScreenEffect(
        effect = addViewModel.effect,
        scaffoldState = scaffoldState,
        clearLink = linkViewModel::clear,
        addedResultRequestKey = addedResultRequestKey,
    )

    TagAddScaffold(
        state = scaffoldState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        linkUiStateProvider = { linkUiState },
        onEvent = { event ->
            when (event) {
                is TagAddScaffoldEvent.ClickNavigateUp -> navigateUp()

                is TagAddScaffoldEvent.ClickAdd ->
                    addViewModel.add(
                        detail = scaffoldState.detail,
                        linkedTagIdSet = linkViewModel.selectionUiState.value.linkedTagIdSet,
                    )

                is TagAddScaffoldEvent.ClickLink -> navigateToDetail(event.id)

                is TagAddScaffoldEvent.ClickLinkAdd ->
                    if (selectableTagPagingItems.isConfirmedEmpty()) {
                        navigateToTagAdd()
                    } else {
                        scaffoldState.linkPickerDialogState.show()
                    }
            }
        },
        onLinkPickerEvent = { event ->
            when (event) {
                is TagLinkPickerEvent.ClickAdd -> navigateToTagAdd()
                is TagLinkPickerEvent.Link -> linkViewModel.link(id = event.id)
                is TagLinkPickerEvent.Unlink -> linkViewModel.unlink(id = event.id)
                is TagLinkPickerEvent.ChangeQuery -> linkViewModel.updateQuery(query = event.query)
            }
        },
        modifier = modifier,
        componentVisibleProvider = componentVisibleProvider,
    )
}
