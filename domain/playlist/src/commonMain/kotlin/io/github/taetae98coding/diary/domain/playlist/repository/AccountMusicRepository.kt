package io.github.taetae98coding.diary.domain.playlist.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import kotlinx.coroutines.flow.Flow

public interface AccountMusicRepository {
    public fun page(
        account: Account,
        sort: ListSort,
    ): Flow<PagingData<Music>>

    public suspend fun upsert(
        account: Account,
        music: Music,
    )
}
