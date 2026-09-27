package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val MEMO_TAB_LABEL: String = "Memo"
internal const val TAG_TAB_LABEL: String = "Tag"
internal const val PLACE_TAB_LABEL: String = "Place"
internal const val WEB_TAB_LABEL: String = "Web"
internal const val RESULT_EMOJI: String = "✈️"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

// FixtureMonkey는 빈 문자열도 생성하므로 표시 여부 검증에 쓰는 값은 비어 있지 않게 접두사를 붙인다.
internal fun resultTitle(): String = "제목-${fixtureMonkey.giveMeOne<String>()}"

internal fun resultAddress(): String = "주소-${fixtureMonkey.giveMeOne<String>()}"

internal fun resultUrl(): String = "https://example.com/${fixtureMonkey.giveMeOne<String>()}"

internal fun resultMemo(
    title: String = resultTitle(),
    dateTime: MemoDateTime? = null,
    isFinished: Boolean = false,
): Memo {
    val detail =
        fixtureMonkey
            .giveMeKotlinBuilder<MemoDetail>()
            .setExp(MemoDetail::title, title)
            .sample()
            .copy(dateTime = dateTime)

    return Memo(
        id = Uuid.random(),
        detail = detail,
        primaryTagId = null,
        isFinished = isFinished,
        isDeleted = false,
        updatedAt = instant(),
        createdAt = instant(),
    )
}

internal fun resultTag(
    emoji: String = "",
    title: String = resultTitle(),
    isFinished: Boolean = false,
): Tag {
    val detail =
        fixtureMonkey
            .giveMeKotlinBuilder<TagDetail>()
            .setExp(TagDetail::emoji, emoji)
            .setExp(TagDetail::title, title)
            .sample()

    return Tag(
        id = Uuid.random(),
        detail = detail,
        isFinished = isFinished,
        isDeleted = false,
        updatedAt = instant(),
        createdAt = instant(),
    )
}

internal fun resultPlace(
    title: String = resultTitle(),
    address: String = "",
): Place {
    val detail =
        fixtureMonkey
            .giveMeKotlinBuilder<PlaceDetail>()
            .setExp(PlaceDetail::title, title)
            .setExp(PlaceDetail::address, address)
            .sample()

    return Place(
        id = Uuid.random(),
        detail = detail,
        isDeleted = false,
        updatedAt = instant(),
        createdAt = instant(),
    )
}

internal fun resultWeb(
    title: String = resultTitle(),
    url: String = resultUrl(),
): Web {
    val detail =
        fixtureMonkey
            .giveMeKotlinBuilder<WebDetail>()
            .setExp(WebDetail::title, title)
            .setExp(WebDetail::url, url)
            .sample()
            .copy(headerList = emptyList())

    return Web(
        id = Uuid.random(),
        detail = detail,
        isDeleted = false,
        updatedAt = instant(),
        createdAt = instant(),
    )
}

internal fun <T : Any> loadingPagingDataFlowOf(): MutableStateFlow<PagingData<T>> =
    MutableStateFlow(
        PagingData.from(
            data = emptyList(),
            sourceLoadStates =
                LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
        ),
    )

internal fun <T : Any> pagingDataFlowOf(itemList: List<T>): MutableStateFlow<PagingData<T>> = MutableStateFlow(pagingDataOf(itemList))

internal fun <T : Any> pagingDataOf(itemList: List<T>): PagingData<T> =
    PagingData.from(
        data = itemList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

private fun instant(): Instant = fixtureMonkey.giveMeOne<Instant>()
