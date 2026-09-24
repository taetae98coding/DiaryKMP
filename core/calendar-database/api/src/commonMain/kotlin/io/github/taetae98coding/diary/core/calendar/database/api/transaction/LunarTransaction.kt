package io.github.taetae98coding.diary.core.calendar.database.api.transaction

import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity

public interface LunarTransaction {
    public suspend fun upsert(
        solarYear: Int,
        lunarDateList: List<LunarDateLocalEntity>,
    )
}
