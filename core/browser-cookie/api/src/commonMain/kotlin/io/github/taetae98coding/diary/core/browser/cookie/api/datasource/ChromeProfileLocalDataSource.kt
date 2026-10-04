package io.github.taetae98coding.diary.core.browser.cookie.api.datasource

import io.github.taetae98coding.diary.core.browser.cookie.api.entity.ChromeProfileLocalEntity

public interface ChromeProfileLocalDataSource {
    public suspend fun readProfileList(): List<ChromeProfileLocalEntity>
}
