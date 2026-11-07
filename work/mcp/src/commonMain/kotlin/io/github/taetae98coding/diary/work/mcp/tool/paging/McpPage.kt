package io.github.taetae98coding.diary.work.mcp.tool.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.EmptyCoroutineContext

internal const val MCP_PAGE_SIZE: Int = 100

private const val CURSOR_PARAMETER = "cursor"

internal val mcpPageCursorParameter: McpToolParameter =
    McpToolParameter(
        name = CURSOR_PARAMETER,
        type = McpToolParameterType.STRING,
        description = "The nextCursor value from the previous page. Omit it to read the first page.",
    )

internal data class McpPage<T>(
    val itemList: List<T>,
    val nextOffset: Int?,
)

internal fun McpToolArguments.pageOffset(): Int {
    val cursor = stringOrNull(CURSOR_PARAMETER) ?: return 0

    return cursor.toIntOrNull()?.takeIf { offset -> offset >= 0 } ?: throw McpToolArgumentException(message = "'$CURSOR_PARAMETER' must be a nextCursor value from a previous page.")
}

internal fun <T> McpPage<T>.toJson(encode: (T) -> JsonElement): JsonObject =
    buildJsonObject {
        put("items", JsonArray(itemList.map(encode)))
        nextOffset?.let { offset -> put("nextCursor", offset.toString()) }
    }

internal suspend fun <Parameter, T : Any> FlowUseCase<Parameter, PagingData<T>>.readPage(
    parameter: Parameter,
    offset: Int,
): McpPage<T> =
    invoke(parameter = parameter)
        .first()
        .getOrThrow()
        .readPage(offset = offset, size = MCP_PAGE_SIZE)

// PagingData는 화면이 끝에 다가갈 때마다 다음 페이지를 불러오도록 만들어져 있으므로, 화면 대신 원하는 위치까지 차례로 불러오기를 요청한다.
internal suspend fun <T : Any> PagingData<T>.readPage(
    offset: Int,
    size: Int,
): McpPage<T> =
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

            McpPage(
                itemList = itemList,
                nextOffset = (offset + size).takeIf { nextOffset -> snapshot.size > nextOffset },
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
