package io.github.taetae98coding.diary.core.testing.memo

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.github.taetae98coding.diary.core.testing.finishedAndDeletedCaseList
import kotlin.time.Instant
import kotlin.uuid.Uuid

// 대표 태그는 없을 수 있으므로 플래그 조합마다 있는 조건과 없는 조건을 함께 확인한다.
public fun FixtureMonkey.memoCaseList(): List<MemoCase> =
    finishedAndDeletedCaseList.flatMap { (isFinished, isDeleted) ->
        listOf(null, giveMeOne<Uuid>()).map { primaryTagId ->
            MemoCase(isFinished = isFinished, isDeleted = isDeleted, primaryTagId = primaryTagId)
        }
    }

public fun FixtureMonkey.memo(
    isFinished: Boolean,
    isDeleted: Boolean,
    primaryTagId: Uuid?,
): Memo =
    giveMeKotlinBuilder<Memo>()
        .setExp(Memo::isFinished, isFinished)
        .setExp(Memo::isDeleted, isDeleted)
        .sample()
        .copy(primaryTagId = primaryTagId)

public fun FixtureMonkey.memo(title: String): Memo =
    giveMeKotlinBuilder<Memo>()
        .setExp(Memo::detail, giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
        .setExp(Memo::updatedAt, giveMeOne<Instant>())
        .setExp(Memo::createdAt, giveMeOne<Instant>())
        .sample()

public fun FixtureMonkey.localMemo(
    isFinished: Boolean,
    isDeleted: Boolean,
    primaryTagId: Uuid?,
): MemoLocalEntity =
    giveMeKotlinBuilder<MemoLocalEntity>()
        .setExp(MemoLocalEntity::isFinished, isFinished)
        .setExp(MemoLocalEntity::isDeleted, isDeleted)
        .sample()
        .copy(primaryTagId = primaryTagId)

public fun FixtureMonkey.remoteMemo(
    isFinished: Boolean,
    isDeleted: Boolean,
    primaryTagId: Uuid?,
): MemoRemoteEntity =
    giveMeKotlinBuilder<MemoRemoteEntity>()
        .setExp(MemoRemoteEntity::isFinished, isFinished)
        .setExp(MemoRemoteEntity::isDeleted, isDeleted)
        .sample()
        .copy(primaryTagId = primaryTagId)
