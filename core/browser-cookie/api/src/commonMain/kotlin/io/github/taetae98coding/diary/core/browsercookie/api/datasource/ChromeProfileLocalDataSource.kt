package io.github.taetae98coding.diary.core.browsercookie.api.datasource

import io.github.taetae98coding.diary.core.browsercookie.api.entity.ChromeProfileLocalEntity

public interface ChromeProfileLocalDataSource {
    public suspend fun findAll(): List<ChromeProfileLocalEntity>
}
