package io.github.taetae98coding.diary.core.data.testing.memo

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import kotlin.uuid.Uuid

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
