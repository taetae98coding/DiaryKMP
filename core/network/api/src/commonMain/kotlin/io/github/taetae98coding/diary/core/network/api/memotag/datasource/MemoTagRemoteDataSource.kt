package io.github.taetae98coding.diary.core.network.api.memotag.datasource

import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity

public interface MemoTagRemoteDataSource {
    public suspend fun push(memoTagList: List<MemoTagRemoteEntity>)

    public suspend fun pull(usn: Long): List<MemoTagPullRemoteEntity>
}
