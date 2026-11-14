package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.PlaylistIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableEmptyBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.music.MusicListEvent
import io.github.taetae98coding.diary.compose.music.SwipeMusicCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_empty_description
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_empty_title
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaylistHomeList(
    onEvent: (PlaylistHomeScaffoldEvent) -> Unit,
    onMusicListEvent: (MusicListEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    musicPagingItems: LazyPagingItems<Music> = remember { flowOf(PagingData.empty<Music>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    isAddButtonVisibleProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { musicPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { musicPagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryCrossfade(
        targetState = musicPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryRefreshableEmptyBox(
                onRefresh = { onEvent(PlaylistHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                isRefreshingProvider = isRefreshingProvider,
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.playlist_home_empty_title),
                    description = stringResource(Res.string.playlist_home_empty_description),
                    icon = { PlaylistIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableStaggeredGrid(
                onRefresh = { onEvent(PlaylistHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                bottomPadding = if (isAddButtonVisibleProvider()) DiaryTheme.dimens.floatingActionButtonClearance else DiaryTheme.dimens.screenVerticalPadding,
            ) {
                items(
                    count = musicPagingItems.itemCount,
                    key = musicPagingItems.itemKey { music -> music.id },
                ) { index ->
                    val music = musicPagingItems[index]

                    SwipeMusicCard(
                        onEvent = onMusicListEvent,
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        music = music,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun PlaylistHomeListPreview() {
    val musicList =
        remember {
            listOf(
                previewMusic(title = "Ditto", artist = "NewJeans"),
                previewMusic(title = "Love Lee", artist = "AKMU"),
            )
        }
    val musicPagingData = remember(musicList) { flowOf(PagingData.from(musicList)) }

    DiaryTheme {
        PlaylistHomeList(
            onEvent = {},
            onMusicListEvent = {},
            modifier = Modifier.fillMaxSize(),
            musicPagingItems = musicPagingData.collectAsLazyPagingItems(),
        )
    }
}
