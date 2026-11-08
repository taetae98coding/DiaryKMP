package io.github.taetae98coding.diary.work.appaction.core

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.coroutines.EmptyCoroutineContext

// 데스크톱 MCP 연결도 이 값을 쓴다.
public const val APP_ACTION_PAGE_SIZE: Int = 100

private const val CURSOR = "cursor"

public data class AppActionPage<T>(
    val items: List<T>,
    val nextCursor: String?,
) {
    val hasNextPage: Boolean
        get() = nextCursor != null
}

// iOS는 위치 대신 1부터 세는 페이지 번호로 목록을 고르므로, 같은 크기의 페이지 번호를 위치로 바꿔 같은 목록 동작을 쓴다.
public fun appActionPageCursor(pageNumber: Int): String? {
    if (pageNumber < 1) invalidArgument(message = "'page' must be 1 or greater.")

    return if (pageNumber == 1) null else ((pageNumber - 1) * APP_ACTION_PAGE_SIZE).toString()
}

internal fun String?.toPageOffset(): Int {
    val cursor = this ?: return 0

    return cursor.toIntOrNull()?.takeIf { offset -> offset >= 0 } ?: invalidArgument(message = "'$CURSOR' must be a nextCursor value from a previous page.")
}

internal fun <T, R> AppActionPage<T>.map(transform: (T) -> R): AppActionPage<R> = AppActionPage(items = items.map(transform), nextCursor = nextCursor)

internal suspend fun <Parameter, T : Any> FlowUseCase<Parameter, PagingData<T>>.readPage(
    parameter: Parameter,
    offset: Int,
): AppActionPage<T> =
    invoke(parameter = parameter)
        .first()
        .getOrThrow()
        .readPage(offset = offset, size = APP_ACTION_PAGE_SIZE)

// PagingData는 화면이 끝에 다가갈 때마다 다음 페이지를 불러오도록 만들어져 있으므로, 화면 대신 원하는 위치까지 차례로 불러오기를 요청한다.
internal suspend fun <T : Any> PagingData<T>.readPage(
    offset: Int,
    size: Int,
): AppActionPage<T> =
    coroutineScope {
        val presenter = SilentPagingDataPresenter<T>()
        val firstPresentation = async(start = CoroutineStart.UNDISPATCHED) { presenter.awaitFirstPresentation() }
        val collectJob = launch { presenter.collectFrom(this@readPage) }

        try {
            firstPresentation.await()
            presenter.loadUntil(endExclusive = offset + size + 1)

            val snapshot = presenter.snapshot()
            val endExclusive = minOf(offset + size, snapshot.size)
            val itemList = (offset until endExclusive).mapNotNull { index -> snapshot[index] }

            AppActionPage(
                items = itemList,
                nextCursor = (offset + size).takeIf { nextOffset -> snapshot.size > nextOffset }?.toString(),
            )
        } finally {
            collectJob.cancel()
        }
    }

// 고정된 목록으로 만든 PagingData는 불러오기 상태를 알리지 않으므로, 목록이 처음 반영되는 것도 함께 기다린다.
private suspend fun <T : Any> PagingDataPresenter<T>.awaitFirstPresentation() {
    coroutineScope {
        val pagesUpdated = async(start = CoroutineStart.UNDISPATCHED) { onPagesUpdatedFlow.first() }
        val loadSettled = async(start = CoroutineStart.UNDISPATCHED) { loadStateFlow.filterNotNull().first { loadState -> loadState.isIdle() } }

        select {
            pagesUpdated.onAwait { }
            loadSettled.onAwait { }
        }
        pagesUpdated.cancel()
        loadSettled.cancel()
    }

    if (loadStateFlow.value != null) awaitNotLoading()
}

private suspend fun <T : Any> PagingDataPresenter<T>.loadUntil(endExclusive: Int) {
    while (true) {
        val snapshot = snapshot()
        val missingIndex = (0 until minOf(endExclusive, snapshot.size)).firstOrNull { index -> snapshot[index] == null }
        val isAppendable = loadStateFlow.value?.append?.endOfPaginationReached == false

        val hintIndex =
            when {
                missingIndex != null -> missingIndex
                snapshot.size < endExclusive && isAppendable && snapshot.isNotEmpty() -> snapshot.size - 1
                else -> return
            }

        awaitUpdate { get(hintIndex) }
    }
}

private suspend fun <T : Any> PagingDataPresenter<T>.awaitUpdate(request: () -> Unit) {
    coroutineScope {
        val pagesUpdated = async(start = CoroutineStart.UNDISPATCHED) { onPagesUpdatedFlow.first() }
        val loadSettled =
            async(start = CoroutineStart.UNDISPATCHED) {
                loadStateFlow
                    .drop(1)
                    .filterNotNull()
                    .first { loadState -> loadState.isIdle() }
            }

        request()

        select {
            pagesUpdated.onAwait { }
            loadSettled.onAwait { }
        }
        pagesUpdated.cancel()
        loadSettled.cancel()
    }

    awaitNotLoading()
}

private suspend fun <T : Any> PagingDataPresenter<T>.awaitNotLoading() {
    val loadState = loadStateFlow.filterNotNull().first { loadState -> loadState.isIdle() }

    loadState.firstError()?.let { error -> throw error }
}

private fun CombinedLoadStates.isIdle(): Boolean = refresh !is LoadState.Loading && prepend !is LoadState.Loading && append !is LoadState.Loading

private fun CombinedLoadStates.firstError(): Throwable? = listOf(refresh, prepend, append).firstNotNullOfOrNull { loadState -> (loadState as? LoadState.Error)?.error }

private class SilentPagingDataPresenter<T : Any> : PagingDataPresenter<T>(mainContext = EmptyCoroutineContext) {
    override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) = Unit
}
