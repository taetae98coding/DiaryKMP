package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun webPagingDataOf(webList: List<Web>): PagingData<Web> =
    PagingData.from(
        data = webList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingWebPagingData(): PagingData<Web> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedWebPagingData(): PagingData<Web> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException("웹 목록을 조회하지 못했습니다.")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun testWeb(
    title: String,
    url: String = "https://example.com/${Arb.int().next()}",
    description: String = "설명-${Arb.string().next()}",
): Web {
    val detail =
        Arb
            .webDetail()
            .next()
            .copy(
                title = title,
                description = description,
                url = url,
            )

    return Web(
        id = Uuid.random(),
        detail = detail,
        isDeleted = false,
        updatedAt = Arb.secondPrecisionInstant().next(),
        createdAt = Arb.secondPrecisionInstant().next(),
    )
}
