package io.github.taetae98coding.diary.core.holiday.database.impl.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.database.impl.HolidayDatabase
import org.koin.core.annotation.Factory

@Factory
internal class HolidayTransactionImpl(
    private val database: HolidayDatabase,
) : HolidayTransaction {
    override suspend fun upsert(
        country: HolidayCountryLocalEntity,
        year: Int,
        holidayList: List<HolidayLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.holidayDao().delete(country = country, year = year)
            database.holidayDao().upsert(holidayList)
        }
    }
}
