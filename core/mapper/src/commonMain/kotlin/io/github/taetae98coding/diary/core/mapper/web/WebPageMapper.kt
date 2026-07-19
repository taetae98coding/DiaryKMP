package io.github.taetae98coding.diary.core.mapper.web

import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageRemoteEntity

public fun WebPageRemoteEntity.toDomain(): WebPage =
    WebPage(
        baseUrl = url,
        body = body,
    )
