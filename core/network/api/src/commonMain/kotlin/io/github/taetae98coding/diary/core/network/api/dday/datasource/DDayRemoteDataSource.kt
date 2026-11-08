package io.github.taetae98coding.diary.core.network.api.dday.datasource

import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity

public interface DDayRemoteDataSource {
    public suspend fun push(ddayList: List<DDayRemoteEntity>)

    public suspend fun pull(usn: Long): List<DDayPullRemoteEntity>
}
