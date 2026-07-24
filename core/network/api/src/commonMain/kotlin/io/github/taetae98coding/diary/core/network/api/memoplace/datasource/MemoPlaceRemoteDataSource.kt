package io.github.taetae98coding.diary.core.network.api.memoplace.datasource

import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlacePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlaceRemoteEntity

public interface MemoPlaceRemoteDataSource {
    public suspend fun push(memoPlaceList: List<MemoPlaceRemoteEntity>)

    public suspend fun pull(usn: Long): List<MemoPlacePullRemoteEntity>
}
