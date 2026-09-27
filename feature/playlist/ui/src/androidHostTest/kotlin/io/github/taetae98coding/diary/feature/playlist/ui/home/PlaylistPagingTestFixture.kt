package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadTarget
import io.github.taetae98coding.diary.domain.playlist.link.toMusicDownloadTargetOrNull
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

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

internal fun testMusic(
    title: String,
    artist: String = "가수-${fixtureMonkey.giveMeOne<Int>()}",
    link: String = "https://youtu.be/dQw4w9WgXcQ",
): Music =
    Music(
        id = Uuid.random(),
        detail = MusicDetail(title = title, artist = artist, link = link),
        isDeleted = false,
        updatedAt = fixtureMonkey.giveMeOne<Instant>(),
        createdAt = fixtureMonkey.giveMeOne<Instant>(),
    )

internal fun Music.downloadTarget(): MusicDownloadTarget = checkNotNull(toMusicDownloadTargetOrNull())
