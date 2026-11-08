package io.github.taetae98coding.diary.work.appaction.core

public data class AppActionNumberedPage<T>(
    val items: List<T>,
    val nextPageNumber: Int?,
)

// iOS는 위치 대신 1부터 세는 페이지 번호로 목록을 고르므로, 번호를 위치로 바꿔 읽고 다음 위치가 있으면 다음 번호로 알린다.
public suspend fun <T> readNumberedPage(
    pageNumber: Int,
    read: suspend (cursor: String?) -> AppActionPage<T>,
): AppActionNumberedPage<T> {
    val page = read(appActionPageCursor(pageNumber = pageNumber))

    return AppActionNumberedPage(
        items = page.items,
        nextPageNumber = (pageNumber + 1).takeIf { page.hasNextPage },
    )
}
