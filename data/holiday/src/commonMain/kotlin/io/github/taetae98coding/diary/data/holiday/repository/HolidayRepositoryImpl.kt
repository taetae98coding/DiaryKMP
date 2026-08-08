package io.github.taetae98coding.diary.data.holiday.repository

import io.github.taetae98coding.diary.core.holiday.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.mapper.holiday.toDomain
import io.github.taetae98coding.diary.core.mapper.holiday.toLocal
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.data.holiday.datasource.HolidayDirtyDataSource
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class HolidayRepositoryImpl(
    private val holidayRemoteDataSource: HolidayRemoteDataSource,
    private val holidayLocalDataSource: HolidayLocalDataSource,
    private val holidayTransaction: HolidayTransaction,
    private val holidayDirtyDataSource: HolidayDirtyDataSource,
) : HolidayRepository {
    override suspend fun fetch(year: Int): List<Holiday> {
        if (!holidayDirtyDataSource.isDirty(year = year)) return get(year = year).first()

        val holidayList =
            holidayRemoteDataSource
                .get(year = year)
                .map { remote -> remote.toLocal(year = year) }

        holidayTransaction.upsert(
            year = year,
            holidayList = holidayList,
        )

        holidayDirtyDataSource.clean(year = year)

        return holidayList.map { local -> local.toDomain() }
    }

    override fun get(): Flow<List<Holiday>> =
        holidayLocalDataSource
            .get()
            .map { holidayList -> holidayList.map { local -> local.toDomain() } }

    override fun get(year: Int): Flow<List<Holiday>> =
        holidayLocalDataSource
            .get(year = year)
            .map { holidayList -> holidayList.map { local -> local.toDomain() } }
}
