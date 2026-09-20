package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageRemoteEntity

internal fun WebPageRemoteEntity.toDomain(): WebPage =
    WebPage(
        baseUrl = url,
        body = body,
    )
