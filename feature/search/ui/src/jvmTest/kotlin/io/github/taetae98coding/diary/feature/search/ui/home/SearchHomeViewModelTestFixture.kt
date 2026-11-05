@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.place.placeDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.core.testing.tag.tagDetail
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.core.testing.web.webDetail
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultUiState
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle

internal const val QUERY: String = "여행"
internal const val OTHER_QUERY: String = "회의"

internal fun <T : Any> successPagingDataFlowOf(itemList: List<T>): Flow<Result<PagingData<T>>> = flowOf(Result.success(PagingData.from(itemList)))

internal fun <T : Any> failurePagingDataFlow(): Flow<Result<PagingData<T>>> = flowOf(Result.failure(IllegalStateException()))

internal fun searchMemo(): Memo = searchMemo(title = Arb.nonBlankString().next())

internal fun searchMemo(title: String): Memo =
    Arb
        .memo()
        .next()
        .copy(
            detail = Arb.memoDetail().next().copy(title = title, dateTime = null),
            isDeleted = false,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

internal fun searchTag(): Tag = searchTag(title = Arb.nonBlankString().next())

internal fun searchTag(title: String): Tag =
    Arb
        .tag()
        .next()
        .copy(
            detail = Arb.tagDetail().next().copy(title = title),
            isDeleted = false,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

internal fun searchPlace(): Place = searchPlace(title = Arb.nonBlankString().next())

internal fun searchPlace(title: String): Place =
    Arb
        .place()
        .next()
        .copy(
            detail = Arb.placeDetail().next().copy(title = title),
            isDeleted = false,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

internal fun searchWeb(): Web = searchWeb(title = Arb.nonBlankString().next())

internal fun searchWeb(title: String): Web =
    Arb
        .web()
        .next()
        .copy(
            detail = Arb.webDetail().next().copy(title = title, headerList = emptyList()),
            isDeleted = false,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

// uiState는 구독하는 동안에만 갱신되므로, 잠시 구독해 지금 값을 읽는다.
internal fun TestScope.currentUiState(viewModel: SearchHomeResultViewModel<*>): SearchHomeResultUiState {
    val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    advanceUntilIdle()
    val uiState = viewModel.uiState.value
    job.cancel()

    return uiState
}
