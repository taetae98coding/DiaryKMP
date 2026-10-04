package io.github.taetae98coding.diary.domain.browser.repository

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile

public interface ChromeProfileRepository {
    public suspend fun readProfileList(): List<ChromeProfile>
}
