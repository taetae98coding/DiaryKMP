package io.github.taetae98coding.diary.core.network.api.memocontact.datasource

import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity

public interface MemoContactRemoteDataSource {
    public suspend fun push(memoContactList: List<MemoContactRemoteEntity>)

    public suspend fun pull(usn: Long): List<MemoContactPullRemoteEntity>
}
