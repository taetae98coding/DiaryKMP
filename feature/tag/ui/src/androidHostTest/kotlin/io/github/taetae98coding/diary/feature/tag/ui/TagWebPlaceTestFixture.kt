package io.github.taetae98coding.diary.feature.tag.ui

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.time.Instant

private val webPlaceFixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun tagWeb(title: String): Web =
    webPlaceFixtureMonkey
        .giveMeKotlinBuilder<Web>()
        .setExp(
            Web::detail,
            webPlaceFixtureMonkey.giveMeOne<WebDetail>().copy(title = title, headerList = emptyList()),
        ).setExp(Web::isDeleted, false)
        .setExp(Web::updatedAt, instant())
        .setExp(Web::createdAt, instant())
        .sample()

internal fun tagPlace(title: String): Place =
    webPlaceFixtureMonkey
        .giveMeKotlinBuilder<Place>()
        .setExp(
            Place::detail,
            webPlaceFixtureMonkey.giveMeOne<PlaceDetail>().copy(title = title),
        ).setExp(Place::isDeleted, false)
        .setExp(Place::updatedAt, instant())
        .setExp(Place::createdAt, instant())
        .sample()

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

private fun instant(): Instant = Instant.fromEpochMilliseconds(webPlaceFixtureMonkey.giveMeOne<Long>())
