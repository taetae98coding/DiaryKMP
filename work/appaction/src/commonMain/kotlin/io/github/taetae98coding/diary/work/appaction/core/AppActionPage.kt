package io.github.taetae98coding.diary.work.appaction.core

import androidx.paging.PagingData
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.first

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

internal suspend fun <T : Any> PagingData<T>.readPage(
    offset: Int,
    size: Int,
): AppActionPage<T> {
    val snapshot = readSnapshot(endExclusive = offset + size + 1)
    val endExclusive = minOf(offset + size, snapshot.size)
    val itemList = (offset until endExclusive).mapNotNull { index -> snapshot[index] }

    return AppActionPage(
        items = itemList,
        nextCursor = (offset + size).takeIf { nextOffset -> snapshot.size > nextOffset }?.toString(),
    )
}
