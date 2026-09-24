package io.github.taetae98coding.diary.core.calendar.network.api.datasource

import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity

public interface LunarRemoteDataSource {
    public suspend fun get(year: Int): List<LunarDateRemoteEntity>
}
