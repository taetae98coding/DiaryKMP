package io.github.taetae98coding.diary.domain.setting.repository

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile

public interface ChromeProfileRepository {
    public suspend fun findAll(): List<ChromeProfile>
}
