package io.github.taetae98coding.diary.data.setting.mapper

import io.github.taetae98coding.diary.core.browsercookie.api.entity.ChromeProfileLocalEntity
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile

internal fun ChromeProfileLocalEntity.toDomain(): ChromeProfile =
    ChromeProfile(
        directory = directory,
        name = name,
    )
