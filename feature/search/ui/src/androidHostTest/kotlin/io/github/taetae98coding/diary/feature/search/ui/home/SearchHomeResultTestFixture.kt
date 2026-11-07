package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.Uuid

internal const val MEMO_TAB_LABEL: String = "Memo"
internal const val TAG_TAB_LABEL: String = "Tag"
internal const val PLACE_TAB_LABEL: String = "Place"
internal const val WEB_TAB_LABEL: String = "Web"
internal const val RESULT_EMOJI: String = "✈️"

internal fun resultTitle(): String = "제목-${Arb.nonBlankString().next()}"

internal fun resultAddress(): String = "주소-${Arb.nonBlankString().next()}"

internal fun resultUrl(): String = "https://example.com/${Arb.string().next()}"

internal fun resultMemo(
    title: String = resultTitle(),
    dateTime: MemoDateTime? = null,
    isFinished: Boolean = false,
): Memo {
    val detail =
        Arb
            .memoDetail()
            .next()
            .copy(
                title = title,
            ).copy(dateTime = dateTime)

    return Memo(
        id = Uuid.random(),
        detail = detail,
        primaryTagId = null,
        isFinished = isFinished,
        isDeleted = false,
        updatedAt = Arb.secondPrecisionInstant().next(),
        createdAt = Arb.secondPrecisionInstant().next(),
    )
}

internal fun resultTag(
    emoji: String = "",
    title: String = resultTitle(),
    isFinished: Boolean = false,
): Tag {
    val detail =
        Arb
            .tagDetail()
            .next()
            .copy(
                emoji = emoji,
                title = title,
            )

    return Tag(
        id = Uuid.random(),
        detail = detail,
        isFinished = isFinished,
        isDeleted = false,
        updatedAt = Arb.secondPrecisionInstant().next(),
        createdAt = Arb.secondPrecisionInstant().next(),
    )
}

internal fun resultPlace(
    title: String = resultTitle(),
    address: String = "",
): Place {
    val detail =
        Arb
            .placeDetail()
            .next()
            .copy(
                title = title,
                address = address,
            )

    return Place(
        id = Uuid.random(),
        detail = detail,
        isDeleted = false,
        updatedAt = Arb.secondPrecisionInstant().next(),
        createdAt = Arb.secondPrecisionInstant().next(),
    )
}

internal fun resultWeb(
    title: String = resultTitle(),
    url: String = resultUrl(),
): Web {
    val detail =
        Arb
            .webDetail()
            .next()
            .copy(
                title = title,
                url = url,
            ).copy(headerList = emptyList())

    return Web(
        id = Uuid.random(),
        detail = detail,
        isDeleted = false,
        updatedAt = Arb.secondPrecisionInstant().next(),
        createdAt = Arb.secondPrecisionInstant().next(),
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
