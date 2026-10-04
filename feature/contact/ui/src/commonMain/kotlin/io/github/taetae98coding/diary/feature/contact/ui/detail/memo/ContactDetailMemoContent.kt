package io.github.taetae98coding.diary.feature.contact.ui.detail.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import io.github.taetae98coding.diary.feature.contact.ui.Res
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.contact.ui.contact_detail_memo_empty_title
import io.github.taetae98coding.diary.feature.contact.ui.detail.tab.ContactDetailTab
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoContent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun ContactDetailMemoContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = ContactDetailTab.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        EntityDetailMemoContent(
            memoViewModel = koinViewModel<ContactDetailMemoViewModel> { parametersOf(id) },
            syncViewModel = koinViewModel(),
            navigateToMemoDetail = navigateToMemoDetail,
            emptyTitle = stringResource(Res.string.contact_detail_memo_empty_title),
            emptyDescription = stringResource(Res.string.contact_detail_memo_empty_description),
            modifier = modifier,
            snackbarHostState = snackbarHostState,
        )
    }
}
