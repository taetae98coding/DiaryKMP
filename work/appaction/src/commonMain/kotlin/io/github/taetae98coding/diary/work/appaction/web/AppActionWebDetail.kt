package io.github.taetae98coding.diary.work.appaction.web

import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag

private const val HEADERS = "headers"

public data class AppActionWebHeader(
    val name: String,
    val value: String,
)

public data class AppActionWebWithHeaders(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val createdAt: String,
    val updatedAt: String,
    val headers: List<AppActionWebHeader>,
)

public data class AppActionWebDetail(
    val web: AppActionWebWithHeaders,
    val tags: List<AppActionTag>,
)

internal fun Web.toAppActionWebWithHeaders(): AppActionWebWithHeaders =
    AppActionWebWithHeaders(
        id = id.toString(),
        title = detail.title,
        description = detail.description,
        url = detail.url,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        headers = detail.headerList.map { header -> AppActionWebHeader(name = header.name, value = header.value) },
    )

// 헤더는 순서를 지키고 같은 이름도 각각 기록하므로, 이름을 키로 쓰는 객체 대신 "이름: 값" 문자열 목록으로 받는다.
internal fun List<String>.toWebHeaderListArgument(): List<WebHeader> =
    map { text ->
        if (!text.contains(':')) invalidArgument(message = "Each '$HEADERS' item must be \"Name: value\".")

        WebHeader(
            name = text.substringBefore(':'),
            value = text.substringAfter(':').removePrefix(" "),
        )
    }
