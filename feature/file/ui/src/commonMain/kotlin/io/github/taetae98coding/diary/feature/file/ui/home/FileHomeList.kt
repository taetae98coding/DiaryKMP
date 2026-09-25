package io.github.taetae98coding.diary.feature.file.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FileIcon
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.feature.file.ui.Res
import io.github.taetae98coding.diary.feature.file.ui.file.FileListItem
import io.github.taetae98coding.diary.feature.file.ui.file_home_empty_description
import io.github.taetae98coding.diary.feature.file.ui.file_home_empty_title
import io.github.taetae98coding.diary.feature.file.ui.file_home_load_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_load_more_failed_message
import io.github.taetae98coding.diary.feature.file.ui.file_home_retry
import io.github.taetae98coding.diary.feature.file.ui.previewDiaryFile
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val FILE_HOME_LIST_TEST_TAG: String = "FileHomeList"
internal const val FILE_HOME_APPEND_LOADING_TEST_TAG: String = "FileHomeAppendLoading"

private enum class FileHomeListContent {
    LOADING,
    FAILED,
    EMPTY,
    LIST,
}

@Composable
internal fun FileHomeList(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filePagingItems: LazyPagingItems<DiaryFile> = remember { flowOf(PagingData.empty<DiaryFile>()) }.collectAsLazyPagingItems(),
) {
    DiaryCrossfade(
        targetState = filePagingItems.content(),
        modifier = modifier,
    ) { content ->
        when (content) {
            FileHomeListContent.LOADING -> {
                DiaryLoadingBox(modifier = Modifier.fillMaxSize())
            }

            FileHomeListContent.FAILED -> {
                FileHomeLoadFailed(
                    onRetry = { onEvent(FileHomeScaffoldEvent.ClickRetry) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            FileHomeListContent.EMPTY -> {
                DiaryEmptyBox(
                    title = stringResource(Res.string.file_home_empty_title),
                    modifier = Modifier.fillMaxSize(),
                    description = stringResource(Res.string.file_home_empty_description),
                    icon = { FileIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }

            FileHomeListContent.LIST -> {
                FileHomeLazyColumn(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    listState = listState,
                    filePagingItems = filePagingItems,
                )
            }
        }
    }
}

@Composable
private fun FileHomeLazyColumn(
    onEvent: (FileHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filePagingItems: LazyPagingItems<DiaryFile> = remember { flowOf(PagingData.empty<DiaryFile>()) }.collectAsLazyPagingItems(),
) {
    LazyColumn(
        modifier = modifier.testTag(FILE_HOME_LIST_TEST_TAG),
        state = listState,
        contentPadding = PaddingValues(vertical = DiaryTheme.dimens.screenVerticalPadding),
    ) {
        items(
            count = filePagingItems.itemCount,
            key = filePagingItems.itemKey { file -> file.id },
        ) { index ->
            filePagingItems[index]?.let { file ->
                FileListItem(
                    file = file,
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                )
            }
        }

        when (filePagingItems.loadState.append) {
            is LoadState.Loading -> {
                item {
                    FileHomeAppendLoading(modifier = Modifier.fillMaxWidth())
                }
            }

            is LoadState.Error -> {
                item {
                    FileHomeAppendFailed(
                        onRetry = { onEvent(FileHomeScaffoldEvent.ClickRetry) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun FileHomeLoadFailed(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(DiaryTheme.dimens.screenPaddingValues),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.file_home_load_failed_message),
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.file_home_retry))
        }
    }
}

@Composable
private fun FileHomeAppendLoading(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .padding(vertical = DiaryTheme.dimens.screenVerticalPadding)
                .testTag(FILE_HOME_APPEND_LOADING_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator(modifier = Modifier.size(DiaryTheme.dimens.inProgressIndicatorSize))
    }
}

@Composable
private fun FileHomeAppendFailed(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = DiaryTheme.dimens.screenVerticalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.file_home_load_more_failed_message),
            color = DiaryTheme.colorScheme.onSurfaceVariant,
            style = DiaryTheme.typography.bodyMedium,
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.file_home_retry))
        }
    }
}

private fun LazyPagingItems<DiaryFile>.content(): FileHomeListContent =
    when {
        itemCount > 0 -> FileHomeListContent.LIST
        loadState.refresh is LoadState.Loading -> FileHomeListContent.LOADING
        loadState.refresh is LoadState.Error -> FileHomeListContent.FAILED
        else -> FileHomeListContent.EMPTY
    }

@ScreenPreview
@Composable
private fun FileHomeListPreview() {
    val fileList =
        remember {
            listOf(
                previewDiaryFile(name = "보고서.pdf", size = 24_536_679),
                previewDiaryFile(name = "memo.txt", size = 512),
            )
        }
    val filePagingData = remember(fileList) { flowOf(PagingData.from(fileList)) }

    DiaryTheme {
        Surface {
            FileHomeList(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                filePagingItems = filePagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
