package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.PlaylistIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.music.MusicListEvent
import io.github.taetae98coding.diary.compose.music.SwipeMusicCard
import io.github.taetae98coding.diary.compose.music.previewMusic
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_music_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_music_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailMusicTab(
    onEvent: (TagDetailMusicContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    musicPagingItems: LazyPagingItems<Music> = remember { flowOf(PagingData.empty<Music>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        filterProvider = scopeProvider,
        itemListProvider = { musicPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { musicPagingItems.loadState.refresh is LoadState.Loading },
    )

    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(TagDetailMusicContentEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { musicPagingItems.itemCount > 0 },
        )

        DiaryCrossfade(
            targetState = musicPagingItems.isLoadedEmpty(),
            modifier = Modifier.fillMaxSize(),
        ) { isEmpty ->
            if (isEmpty) {
                Empty(
                    onEvent = onEvent,
                    isRefreshingProvider = isRefreshingProvider,
                )
            } else {
                MusicGrid(
                    onEvent = onEvent,
                    gridState = gridState,
                    musicPagingItems = musicPagingItems,
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagDetailMusicContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@Composable
private fun MusicGrid(
    onEvent: (TagDetailMusicContentEvent) -> Unit,
    gridState: LazyStaggeredGridState,
    musicPagingItems: LazyPagingItems<Music>,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableStaggeredGrid(
        onRefresh = { onEvent(TagDetailMusicContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        state = gridState,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        items(
            count = musicPagingItems.itemCount,
            key = musicPagingItems.itemKey { item -> item.id },
        ) { index ->
            val music = musicPagingItems[index]

            SwipeMusicCard(
                onEvent = { event ->
                    when (event) {
                        is MusicListEvent.ClickMusic -> onEvent(TagDetailMusicContentEvent.ClickMusic(id = event.id))
                        is MusicListEvent.SwipeDelete -> onEvent(TagDetailMusicContentEvent.DeleteMusic(id = event.id))
                    }
                },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                music = music,
            )
        }
    }
}

@Composable
private fun Empty(
    onEvent: (TagDetailMusicContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = { onEvent(TagDetailMusicContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_music_empty_title),
            // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            description = stringResource(Res.string.tag_detail_music_empty_description),
            icon = { PlaylistIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailMusicTabPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewMusic(title = "곡 제목", artist = "가수")))) }

    DiaryTheme {
        Surface {
            TagDetailMusicTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                musicPagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
