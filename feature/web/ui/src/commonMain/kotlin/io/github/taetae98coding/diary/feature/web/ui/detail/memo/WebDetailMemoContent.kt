package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoContent
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_empty_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailMemoContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = WebDetailTab.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        EntityDetailMemoContent(
            memoViewModel = koinViewModel<WebDetailMemoViewModel> { parametersOf(id) },
            syncViewModel = koinViewModel(),
            navigateToMemoDetail = navigateToMemoDetail,
            emptyTitle = stringResource(Res.string.web_detail_memo_empty_title),
            emptyDescription = stringResource(Res.string.web_detail_memo_empty_description),
            modifier = modifier,
            snackbarHostState = snackbarHostState,
        )
    }
}
