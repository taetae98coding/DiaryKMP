package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import kotlin.uuid.Uuid

// 한 묶음을 보낸 뒤 바로 보류를 지워, 중간에 실패해도 이미 보낸 묶음은 다시 보내지 않는다.
internal suspend fun <T> pushPending(
    pendingList: List<T>,
    push: suspend (chunk: List<T>) -> Unit,
    clearPending: suspend (chunk: List<T>) -> Unit,
) {
    pendingList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
        push(chunk)
        clearPending(chunk)
    }
}

// 받은 묶음과 커서를 한 트랜잭션으로 저장하고, 커서가 더 나아가지 않으면 멈춘다.
internal suspend fun <T> SyncCursorLocalDataSource.pullUntilExhausted(
    accountId: Uuid,
    kind: SyncKindLocalEntity,
    pull: suspend (cursor: Long) -> List<T>,
    usn: (T) -> Long,
    upsert: suspend (pullList: List<T>, cursor: Long) -> Unit,
) {
    var cursor = read(accountId = accountId, kind = kind)

    while (true) {
        val pullList = pull(cursor)
        val nextCursor = pullList.maxOfOrNull(usn)

        if (nextCursor == null || nextCursor <= cursor) return

        upsert(pullList, nextCursor)
        cursor = nextCursor
    }
}
