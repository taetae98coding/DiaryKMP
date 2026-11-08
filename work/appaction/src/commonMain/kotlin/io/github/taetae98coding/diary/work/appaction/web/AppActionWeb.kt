package io.github.taetae98coding.diary.work.appaction.web

import io.github.taetae98coding.diary.core.model.web.Web

public data class AppActionWeb(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val createdAt: String,
    val updatedAt: String,
)

internal fun Web.toAppActionWeb(): AppActionWeb =
    AppActionWeb(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        url = detail.url,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
