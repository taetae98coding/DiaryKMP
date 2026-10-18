package io.github.taetae98coding.diary.data.browser.mapper

import io.github.taetae98coding.diary.core.browser.cookie.api.entity.ChromeProfileLocalEntity
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile

internal fun ChromeProfileLocalEntity.toDomain(): ChromeProfile =
    ChromeProfile(
        directory = directory,
        name = name,
    )
