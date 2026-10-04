package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageHeaderRemoteEntity

internal fun WebHeader.toRemote(): WebPageHeaderRemoteEntity =
    WebPageHeaderRemoteEntity(
        name = name,
        value = value,
    )
