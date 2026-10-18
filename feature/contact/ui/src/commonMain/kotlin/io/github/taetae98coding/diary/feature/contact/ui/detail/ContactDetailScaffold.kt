package io.github.taetae98coding.diary.feature.contact.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_memo_empty_title
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTabRow
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.rememberContactDetailTabState
import io.github.taetae98coding.diary.feature.contact.ui.form.ContactFormState
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactDetailFormState
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoTab
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailScaffold(
    onEvent: (ContactDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: ContactFormState = rememberContactDetailFormState(),
    tabState: ContactDetailTabState = rememberContactDetailTabState(),
    uiStateProvider: () -> ContactDetailUiState = { ContactDetailUiState.Loading },
    componentVisibleProvider: () -> ContactDetailScaffoldComponentVisible = { ContactDetailScaffoldComponentVisible() },
    tabFloatingActionButton: @Composable (ContactDetailTab) -> Unit,
    tabContent: @Composable (ContactDetailTab) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ContactDetailTopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
                componentVisibleProvider = componentVisibleProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = { tabFloatingActionButton(tabState.tab) },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            ContactDetailTabRow(
                modifier = Modifier.fillMaxWidth(),
                state = tabState,
            )
            ContactDetailPager(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1F),
                state = tabState,
                tabContent = tabContent,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun ContactDetailScaffoldPreview() {
    val uiState = ContactDetailUiState.Content(id = Uuid.NIL, detail = ContactDetail.EMPTY.copy(name = "김철수"))
    val state = rememberContactDetailFormState(initialDetail = uiState.detail)

    DiaryTheme {
        ContactDetailScaffold(
            onEvent = {},
            state = state,
            uiStateProvider = { uiState },
            tabFloatingActionButton = { tab ->
                ContactDetailTabFloatingActionButton(
                    tab = tab,
                    onUpdate = {},
                    onMemoAdd = {},
                )
            },
        ) { tab ->
            when (tab) {
                ContactDetailTab.DETAIL ->
                    ContactDetailScaffoldContent(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        uiStateProvider = { uiState },
                    )

                ContactDetailTab.MEMO -> EntityDetailMemoTab(emptyTitle = stringResource(Res.string.contact_detail_memo_empty_title), emptyDescription = stringResource(Res.string.contact_detail_memo_empty_description), onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
