package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputFocusEffect
import io.github.taetae98coding.diary.compose.core.paging.isConfirmedEmpty
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.form.TagFormState
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagAddFormState
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkAddedResultEffect
import io.github.taetae98coding.diary.feature.tag.ui.link.TagLinkPickerEvent
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_succeeded_message
import io.github.taetae98coding.diary.feature.tag.ui.tag_add_title_blank_message
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
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

    DiaryTitleInputFocusEffect(state = scaffoldState.titleState)
    TagLinkAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = linkViewModel::link,
    )
    AddEffect(
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
                        linkedTagIdSet = linkViewModel.linkedTagIdSet.value,
                    )

                is TagAddScaffoldEvent.ClickLink -> navigateToDetail(event.id)

                is TagAddScaffoldEvent.ClickLinkAdd ->
                    if (tagPagingItems.isConfirmedEmpty()) {
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

@Composable
private fun AddEffect(
    clearLink: () -> Unit,
    addedResultRequestKey: Uuid?,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
    effect: Flow<TagAddEffect> = emptyFlow(),
    scaffoldState: TagFormState = rememberTagAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.tag_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.tag_add_title_blank_message)

    CollectEffect(effect) { value ->
        when (value) {
            is TagAddEffect.AddSucceeded -> {
                // 이 화면을 연 태그 입력에만 결과를 돌려주므로, 요청 키가 없으면 아무 곳에도 보내지 않는다.
                addedResultRequestKey?.let { requestKey ->
                    resultEventBus.sendResult(resultKey = tagAddedResultKey(requestKey = requestKey), result = TagAddedResult(id = value.id))
                }
                scaffoldState.emojiState.clearText()
                scaffoldState.titleState.clearText()
                scaffoldState.descriptionState.clearText()
                scaffoldState.titleState.requestFocus()
                clearLink()
                coroutineScope.launch { scaffoldState.colorState.animateTo(color = randomColor()) }
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = addSucceededMessage) }
            }

            is TagAddEffect.TitleBlank -> {
                scaffoldState.titleState.requestFocus()
                coroutineScope.launch { scaffoldState.hostState.showImmediate(message = titleBlankMessage) }
            }
        }
    }
}
