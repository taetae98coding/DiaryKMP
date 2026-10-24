package io.github.taetae98coding.diary.feature.file.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail

@Composable
internal fun FileDetailScreen(
    navigateUp: () -> Unit,
    detailViewModel: FileDetailViewModel,
    accountViewModel: FileDetailAccountViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState as? FileDetailUiState.Content

    FetchFileEffect(detailViewModel = detailViewModel)
    FileDetailAccountEffect(
        accountViewModel = accountViewModel,
        navigateUp = navigateUp,
    )

    // 불러오기 전에는 채울 값이 없으므로, 처음 불러온 정보로 입력을 새로 만든다. 그 뒤 고쳐 저장해도 입력은 다시 만들지 않는다.
    key(content?.file?.id) {
        val state = rememberFileDetailFormState(initialDetail = content?.file?.toDetail() ?: DiaryFileDetail(name = "", title = "", description = ""))

        FileDetailScreenEffect(
            effect = detailViewModel.effect,
            state = state,
        )

        FileDetailScaffold(
            onEvent = { event ->
                when (event) {
                    is FileDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
                    is FileDetailScaffoldEvent.ClickRetry -> detailViewModel.retry()
                    is FileDetailScaffoldEvent.ClickUpdate -> detailViewModel.update(detail = state.detail)
                    is FileDetailScaffoldEvent.ClickDownload -> detailViewModel.download()
                }
            },
            modifier = modifier,
            state = state,
            uiStateProvider = { uiState },
        )
    }
}

@Composable
private fun FetchFileEffect(detailViewModel: FileDetailViewModel) {
    LaunchedEffect(detailViewModel) {
        detailViewModel.fetch()
    }
}

@Composable
private fun FileDetailAccountEffect(
    accountViewModel: FileDetailAccountViewModel,
    navigateUp: () -> Unit,
) {
    CollectEffect(accountViewModel.uiState) { uiState ->
        if (uiState.isAccountChanged) navigateUp()
    }
}
