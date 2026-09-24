package io.github.taetae98coding.diary.data.holiday.repository

import io.github.taetae98coding.diary.core.holiday.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.data.holiday.datasource.HolidayDirtyDataSource
import io.github.taetae98coding.diary.data.holiday.mapper.toDomain
import io.github.taetae98coding.diary.data.holiday.mapper.toLocal
import io.github.taetae98coding.diary.data.holiday.mapper.toRemote
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
    override suspend fun fetch(
        country: HolidayCountry,
        year: Int,
    ): List<Holiday> {
        if (!holidayDirtyDataSource.isDirty(country = country, year = year)) return get(countrySet = setOf(country), year = year).first()

        val holidayList =
            holidayRemoteDataSource
                .get(country = country.toRemote(), year = year)
                .map { remote -> remote.toLocal(country = country, year = year) }

        holidayTransaction.upsert(
            country = country.toLocal(),
            year = year,
            holidayList = holidayList,
        )

        holidayDirtyDataSource.clean(country = country, year = year)

        return holidayList.map { local -> local.toDomain() }
    }

    override fun get(countrySet: Set<HolidayCountry>): Flow<List<Holiday>> =
        holidayLocalDataSource
            .get(countrySet = countrySet.mapTo(mutableSetOf()) { country -> country.toLocal() })
            .map { holidayList -> holidayList.map { local -> local.toDomain() } }

    override fun get(
        countrySet: Set<HolidayCountry>,
        year: Int,
    ): Flow<List<Holiday>> =
        holidayLocalDataSource
            .get(countrySet = countrySet.mapTo(mutableSetOf()) { country -> country.toLocal() }, year = year)
            .map { holidayList -> holidayList.map { local -> local.toDomain() } }
}
