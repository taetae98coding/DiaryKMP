package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence

internal fun MemoExistenceFilterLocalEntity.toDomain(): MemoExistenceFilter =
    MemoExistenceFilter(
        date = hasDate.toMemoFilterExistence(),
        tag = hasTag.toMemoFilterExistence(),
        place = hasPlace.toMemoFilterExistence(),
    )

internal fun MemoFilterExistence.toLocal(): Boolean? =
    when (this) {
        MemoFilterExistence.ALL -> null
        MemoFilterExistence.EXIST -> true
        MemoFilterExistence.NOT_EXIST -> false
    }

private fun Boolean?.toMemoFilterExistence(): MemoFilterExistence =
    when (this) {
        null -> MemoFilterExistence.ALL
        true -> MemoFilterExistence.EXIST
        false -> MemoFilterExistence.NOT_EXIST
    }
