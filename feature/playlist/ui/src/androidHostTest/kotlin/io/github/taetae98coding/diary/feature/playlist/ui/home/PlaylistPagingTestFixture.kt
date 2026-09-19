package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

/**
 * 전달한 곡이 모두 준비된 상태의 곡 목록을 만든다.
 */
internal fun musicPagingDataOf(musicList: List<Music>): PagingData<Music> =
    PagingData.from(
        data = musicList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingMusicPagingData(): PagingData<Music> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedMusicPagingData(): PagingData<Music> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException("곡 목록을 조회하지 못했습니다.")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

// FixtureMonkey가 Instant를 생성하지 못하므로 곡은 직접 만든다.
internal fun testMusic(
    title: String,
    artist: String = "가수-${fixtureMonkey.giveMeOne<Int>()}",
    link: String = "https://youtu.be/${fixtureMonkey.giveMeOne<Int>()}",
    thumbnail: String = "https://i.ytimg.com/vi/${fixtureMonkey.giveMeOne<Int>()}/hqdefault.jpg",
): Music =
    Music(
        id = Uuid.random(),
        detail = MusicDetail(link = link, title = title, artist = artist, thumbnail = thumbnail),
        isDeleted = false,
        updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
        createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
    )
