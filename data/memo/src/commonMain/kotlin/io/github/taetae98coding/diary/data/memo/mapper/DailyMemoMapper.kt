package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.DailyMemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.DailyMemo

internal fun DailyMemoLocalEntity.toDomain(): DailyMemo =
    DailyMemo(
        id = id,
        title = title,
    )
