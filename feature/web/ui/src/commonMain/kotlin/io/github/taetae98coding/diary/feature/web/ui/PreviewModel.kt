package io.github.taetae98coding.diary.feature.web.ui

import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader

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
