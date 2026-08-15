package io.github.taetae98coding.diary.domain.web.repository

import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage

public interface WebPageRepository {
    public suspend fun fetch(
        url: String,
        headerList: List<WebHeader>,
    ): WebPage
}
