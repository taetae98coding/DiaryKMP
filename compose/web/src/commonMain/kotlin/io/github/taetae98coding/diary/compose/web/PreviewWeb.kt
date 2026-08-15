package io.github.taetae98coding.diary.compose.web

import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebPage
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewWeb(
    title: String,
    url: String,
): Web =
    Web(
        id = Uuid.random(),
        detail =
            WebDetail(
                title = title,
                description = "웹 설명",
                url = url,
                headerList = emptyList(),
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewWebPage(): WebPage =
    WebPage(
        baseUrl = "https://developer.android.com",
        body = "<html><body><h1>웹 페이지</h1></body></html>",
    )
