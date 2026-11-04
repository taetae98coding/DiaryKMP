package io.github.taetae98coding.diary.feature.tag.ui

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.time.Instant

internal fun tagWeb(title: String): Web =
    Arb
        .web()
        .next()
        .copy(
            detail = Arb.webDetail().next().copy(title = title, headerList = emptyList()),
            isDeleted = false,
            updatedAt = instant(),
            createdAt = instant(),
        )

internal fun tagPlace(title: String): Place =
    Arb
        .place()
        .next()
        .copy(
            detail = Arb.placeDetail().next().copy(title = title),
            isDeleted = false,
            updatedAt = instant(),
            createdAt = instant(),
        )

internal fun <T : Any> tagEntityPagingData(
    itemList: List<T>,
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = true),
): PagingData<T> =
    PagingData.from(
        data = itemList,
        sourceLoadStates =
            LoadStates(
                refresh = refresh,
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = append,
            ),
    )

internal fun <T : Any> tagEntityLoadingPagingData(): PagingData<T> =
    PagingData.from(
        data = emptyList<T>(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

private fun instant(): Instant = Arb.secondPrecisionInstant().next()
