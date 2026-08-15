package io.github.taetae98coding.diary.feature.web.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

/**
 * 전달한 웹 항목이 모두 준비된 상태의 웹 목록을 만든다.
 */
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

// FixtureMonkey가 Instant를 생성하지 못하므로 웹 항목은 직접 만든다.
internal fun testWeb(
    title: String,
    url: String = "https://example.com/${fixtureMonkey.giveMeOne<Int>()}",
    description: String = "설명-${fixtureMonkey.giveMeOne<String>()}",
): Web {
    val detail =
        fixtureMonkey
            .giveMeKotlinBuilder<WebDetail>()
            .setExp(WebDetail::title, title)
            .setExp(WebDetail::description, description)
            .setExp(WebDetail::url, url)
            .sample()

    return Web(
        id = Uuid.random(),
        detail = detail,
        isDeleted = false,
        updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
        createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
    )
}
