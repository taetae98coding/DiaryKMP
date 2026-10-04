package io.github.taetae98coding.diary.feature.tag.ui.detail.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import io.github.taetae98coding.diary.compose.core.button.ListEntryButton
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoContent
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_finished_list_action_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailMemoContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val memoViewModel = koinViewModel<TagDetailMemoViewModel> { parametersOf(id) }
        val scopeUiState by memoViewModel.scopeUiState.collectAsStateWithLifecycle()

        TagDetailScopeEffect(
            onSelect = { scope -> memoViewModel.select(scope = scope) },
            state = scopeState,
        )
        EntityDetailMemoContent(
            memoViewModel = memoViewModel,
            syncViewModel = koinViewModel(),
            navigateToMemoDetail = navigateToMemoDetail,
            emptyTitle = stringResource(Res.string.tag_detail_memo_empty_title),
            emptyDescription = stringResource(Res.string.tag_detail_memo_empty_description),
            modifier = modifier,
            snackbarHostState = snackbarHostState,
            filterProvider = { scopeUiState.scope },
            trailing = {
                ListEntryButton(
                    onClick = navigateToMemoFinishedList,
                    label = stringResource(Res.string.tag_detail_memo_finished_list_action_label),
                )
            },
        )
    }
}
