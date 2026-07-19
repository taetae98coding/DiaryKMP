package io.github.taetae98coding.diary.core.mapper.memofilter

import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence

public fun MemoExistenceFilterLocalEntity.toDomain(): MemoExistenceFilter =
    MemoExistenceFilter(
        date = hasDate.toMemoFilterExistence(),
        tag = hasTag.toMemoFilterExistence(),
        place = hasPlace.toMemoFilterExistence(),
    )

public fun MemoFilterExistence.toLocal(): Boolean? =
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
