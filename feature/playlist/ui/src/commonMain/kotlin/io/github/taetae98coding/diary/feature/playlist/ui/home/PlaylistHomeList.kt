package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.PlaylistIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadState
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.music.SwipeToDeleteMusicCard
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_empty_description
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_empty_title
import io.github.taetae98coding.diary.feature.playlist.ui.previewMusic
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val PLAYLIST_HOME_LIST_TEST_TAG: String = "PlaylistHomeList"

@Composable
internal fun PlaylistHomeList(
    onEvent: (PlaylistHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    musicPagingItems: LazyPagingItems<Music> = remember { flowOf(PagingData.empty<Music>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    downloadStateProvider: (Music) -> MusicDownloadState? = { null },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { musicPagingItems.itemSnapshotList.items },
    )

    DiaryCrossfade(
        targetState = musicPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(PlaylistHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.playlist_home_empty_title),
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    description = stringResource(Res.string.playlist_home_empty_description),
                    icon = { PlaylistIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = { onEvent(PlaylistHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = PLAYLIST_HOME_LIST_TEST_TAG,
            ) {
                items(
                    count = musicPagingItems.itemCount,
                    key = musicPagingItems.itemKey { music -> music.id },
                ) { index ->
                    val music = musicPagingItems[index]

                    SwipeToDeleteMusicCard(
                        onClick = { music?.let { value -> onEvent(PlaylistHomeScaffoldEvent.ClickMusic(id = value.id)) } },
                        onDelete = { music?.let { value -> onEvent(PlaylistHomeScaffoldEvent.DeleteMusic(id = value.id)) } },
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        music = music,
                        downloadStateProvider = { music?.let(downloadStateProvider) },
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
            modifier = Modifier.fillMaxSize(),
            musicPagingItems = musicPagingData.collectAsLazyPagingItems(),
        )
    }
}
