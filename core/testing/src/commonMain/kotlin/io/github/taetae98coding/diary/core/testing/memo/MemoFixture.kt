package io.github.taetae98coding.diary.core.testing.memo

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.testing.finishedAndDeletedCaseList
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

// 대표 태그는 없을 수 있으므로 플래그 조합마다 있는 조건과 없는 조건을 함께 확인한다.
public fun memoCaseList(): List<MemoCase> =
    finishedAndDeletedCaseList.flatMap { (isFinished, isDeleted) ->
        listOf(null, Arb.kotlinUuid().next()).map { primaryTagId ->
            MemoCase(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)
        }
    }

public fun Arb.Companion.memo(
    isFinished: Boolean,
    isDeleted: Boolean,
    primaryTagId: Uuid?,
): Arb<Memo> = Arb.memo().map { memo -> memo.copy(primaryTagId = primaryTagId, isFinished = isFinished, isDeleted = isDeleted) }

public fun Arb.Companion.memo(title: String): Arb<Memo> = Arb.memo(title = title, dateTime = null)

public fun Arb.Companion.memo(
    title: String,
    dateTime: MemoDateTime?,
): Arb<Memo> =
    arbitrary {
        val memo = Arb.memo().bind()

        memo.copy(detail = Arb.memoDetail().bind().copy(title = title, dateTime = dateTime))
    }

public fun Arb.Companion.localMemo(
    isFinished: Boolean,
    isDeleted: Boolean,
    primaryTagId: Uuid?,
): Arb<MemoLocalEntity> = Arb.memoLocalEntity().map { memo -> memo.copy(primaryTagId = primaryTagId, isFinished = isFinished, isDeleted = isDeleted) }

public fun Arb.Companion.remoteMemo(
    isFinished: Boolean,
    isDeleted: Boolean,
    primaryTagId: Uuid?,
): Arb<MemoRemoteEntity> = Arb.memoRemoteEntity().map { memo -> memo.copy(primaryTagId = primaryTagId, isFinished = isFinished, isDeleted = isDeleted) }
