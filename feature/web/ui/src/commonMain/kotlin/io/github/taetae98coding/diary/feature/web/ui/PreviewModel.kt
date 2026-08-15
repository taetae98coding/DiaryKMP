package io.github.taetae98coding.diary.feature.web.ui

import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
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

internal fun previewWebDetail(): WebDetail =
    WebDetail(
        title = "안드로이드 개발자",
        description = "공식 안드로이드 문서",
        url = "https://developer.android.com",
        headerList =
            listOf(
                WebHeader(name = "Authorization", value = "Bearer preview-token"),
                WebHeader(name = "Accept-Language", value = "ko-KR"),
            ),
    )

internal fun previewWebPage(): WebPage =
    WebPage(
        baseUrl = "https://developer.android.com",
        body = "<html><body><h1>안드로이드 개발자</h1></body></html>",
    )
