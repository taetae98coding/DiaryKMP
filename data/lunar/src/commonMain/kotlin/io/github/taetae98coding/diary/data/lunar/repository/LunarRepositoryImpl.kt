package io.github.taetae98coding.diary.data.lunar.repository

import io.github.taetae98coding.diary.core.calendar.database.api.datasource.LunarLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.LunarTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.LunarRemoteDataSource
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.data.lunar.datasource.LunarDirtyDataSource
import io.github.taetae98coding.diary.data.lunar.mapper.toDomain
import io.github.taetae98coding.diary.data.lunar.mapper.toLocal
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import org.koin.core.annotation.Factory

@Factory
internal class LunarRepositoryImpl(
    private val lunarRemoteDataSource: LunarRemoteDataSource,
    private val lunarLocalDataSource: LunarLocalDataSource,
    private val lunarTransaction: LunarTransaction,
    private val lunarDirtyDataSource: LunarDirtyDataSource,
) : LunarRepository {
    override suspend fun fetch(year: Int): List<LunarDate> {
        if (!lunarDirtyDataSource.isDirty(year = year)) return get(dateRange = year.solarDateRange()).first()

        val lunarDateList =
            lunarRemoteDataSource
                .get(year = year)
                .map { remote -> remote.toLocal(solarYear = year) }

        lunarTransaction.upsert(
            solarYear = year,
            lunarDateList = lunarDateList,
        )

        lunarDirtyDataSource.clean(year = year)

        return lunarDateList.map { local -> local.toDomain() }
    }

    override fun get(dateRange: LocalDateRange): Flow<List<LunarDate>> =
        lunarLocalDataSource
            .get(dateRange = dateRange)
            .map { lunarDateList -> lunarDateList.map { local -> local.toDomain() } }
}

private fun Int.solarDateRange(): LocalDateRange = LocalDate(year = this, month = Month.JANUARY, day = 1)..LocalDate(year = this, month = Month.DECEMBER, day = 31)
