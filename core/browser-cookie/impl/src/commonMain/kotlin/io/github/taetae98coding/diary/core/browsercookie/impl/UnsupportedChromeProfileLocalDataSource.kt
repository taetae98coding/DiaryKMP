package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeProfileLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.ChromeProfileLocalEntity

internal object UnsupportedChromeProfileLocalDataSource : ChromeProfileLocalDataSource {
    override suspend fun findAll(): List<ChromeProfileLocalEntity> = error("Chrome profiles are not supported on this platform.")
}
