package io.github.taetae98coding.diary.core.network.api.memoweb.datasource

import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebRemoteEntity

public interface MemoWebRemoteDataSource {
    public suspend fun push(memoWebList: List<MemoWebRemoteEntity>)

    public suspend fun pull(usn: Long): List<MemoWebPullRemoteEntity>
}
