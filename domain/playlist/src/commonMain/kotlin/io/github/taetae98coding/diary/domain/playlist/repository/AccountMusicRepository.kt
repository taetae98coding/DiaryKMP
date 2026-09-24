package io.github.taetae98coding.diary.domain.playlist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMusicRepository {
    public fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Music>>

    public suspend fun findList(
        account: Account,
        sort: ListSort,
    ): List<Music>

    public fun find(
        account: Account,
        musicId: Uuid,
    ): Flow<Music?>

    public suspend fun upsert(
        account: Account,
        music: Music,
    )

    public suspend fun updateDetail(
        account: Account,
        musicId: Uuid,
        detail: MusicDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        musicId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
