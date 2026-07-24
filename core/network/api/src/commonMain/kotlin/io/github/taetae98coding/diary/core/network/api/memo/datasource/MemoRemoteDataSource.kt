package io.github.taetae98coding.diary.core.network.api.memo.datasource

import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity

public interface MemoRemoteDataSource {
    public suspend fun push(memoList: List<MemoRemoteEntity>)

    public suspend fun pull(usn: Long): List<MemoPullRemoteEntity>
}
