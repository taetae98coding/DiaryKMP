package io.github.taetae98coding.diary.core.calendar.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import org.koin.core.annotation.Factory

@Factory
internal class LunarTransactionImpl(
    private val database: CalendarDatabase,
) : LunarTransaction {
    override suspend fun upsert(
        solarYear: Int,
        lunarDateList: List<LunarDateLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.lunarDateDao().delete(solarYear = solarYear)
            database.lunarDateDao().upsert(lunarDateList)
        }
    }
}
