package io.github.taetae98coding.diary.data.holiday.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.data.holiday.HolidayDataTestKoinApplication
import io.github.taetae98coding.diary.data.holiday.mapper.toDomain
import io.github.taetae98coding.diary.data.holiday.mapper.toLocal
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayCountryOptionUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.koin.core.Koin
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayCountryDataTest :
    FunSpec({
        test("TC-HOLIDAY-FETCH-DOMAIN-008 한 국가가 실패해도 다른 국가의 저장된 공휴일은 원격 공휴일로 교체되고 연도 동기화는 실패한다") {
            val year = randomYear()
            val unitedStatesRemoteList = listOf(remoteHoliday(year = year), remoteHoliday(year = year))
            val context = holidayCountryDataTestContext(optionSet = BOTH_OPTION_SET)
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } throws
                IllegalStateException(fixtureMonkey.giveMeOne<String>())
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.UNITED_STATES, year = year) } returns unitedStatesRemoteList

            context.koin
                .get<FetchHolidayUseCase>()(parameter = year)
                .shouldBeFailure()

            coVerify(exactly = 1) {
                context.transaction.upsert(
                    country = HolidayCountryLocalEntity.UNITED_STATES,
                    year = year,
                    holidayList = unitedStatesRemoteList.map { remote -> remote.toLocal(country = HolidayCountry.UNITED_STATES, year = year) },
                )
            }
            coVerify(exactly = 0) { context.transaction.upsert(country = HolidayCountryLocalEntity.KOREA, year = any(), holidayList = any()) }
        }

        test("TC-HOLIDAY-FETCH-DATA-014 한 국가가 실패한 연도를 다시 요청하면 실패한 국가만 다시 원격 조회한다") {
            val year = randomYear()
            val context = holidayCountryDataTestContext(optionSet = BOTH_OPTION_SET)
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } throws
                IllegalStateException(fixtureMonkey.giveMeOne<String>()) andThen listOf(remoteHoliday(year = year))
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.UNITED_STATES, year = year) } returns listOf(remoteHoliday(year = year))
            every { context.localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.UNITED_STATES), year = year) } returns
                MutableStateFlow(listOf(localHoliday(year = year, country = HolidayCountryLocalEntity.UNITED_STATES)))
            val useCase = context.koin.get<FetchHolidayUseCase>()
            useCase(parameter = year).shouldBeFailure()

            useCase(parameter = year).shouldBeSuccess()

            coVerify(exactly = 2) { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.UNITED_STATES, year = year) }
        }

        test("TC-HOLIDAY-COUNTRY-DATA-005 국가 설정을 바꿔도 기기에 저장된 공휴일은 바뀌지 않는다") {
            val year = randomYear()
            val storedHolidayList = listOf(localHoliday(year = year), localHoliday(year = year))
            val context = holidayCountryDataTestContext(optionSet = setOf(HolidayCountryOptionLocalEntity.KOREA))
            every { context.localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.KOREA), year = year) } returns MutableStateFlow(storedHolidayList)

            context.koin
                .get<ToggleHolidayCountryOptionUseCase>()(parameter = HolidayCountryOption.UNITED_STATES)
                .shouldBeSuccess()

            context.optionSetFlow.value shouldBe setOf(HolidayCountryOptionLocalEntity.KOREA, HolidayCountryOptionLocalEntity.UNITED_STATES)
            context.koin
                .get<HolidayRepository>()
                .get(countrySet = setOf(HolidayCountry.KOREA), year = year)
                .first() shouldBe storedHolidayList.map { local -> local.toDomain() }
            coVerify(exactly = 0) { context.transaction.upsert(country = any(), year = any(), holidayList = any()) }
        }
    })

private val BOTH_OPTION_SET: Set<HolidayCountryOptionLocalEntity> =
    setOf(HolidayCountryOptionLocalEntity.KOREA, HolidayCountryOptionLocalEntity.UNITED_STATES)

private class HolidayCountryDataTestContext(
    val koin: Koin,
    val remoteDataSource: HolidayRemoteDataSource,
    val localDataSource: HolidayLocalDataSource,
    val transaction: HolidayTransaction,
    val optionSetFlow: MutableStateFlow<Set<HolidayCountryOptionLocalEntity>>,
)

private fun holidayCountryDataTestContext(optionSet: Set<HolidayCountryOptionLocalEntity>): HolidayCountryDataTestContext {
    val remoteDataSource = mockk<HolidayRemoteDataSource>()
    val localDataSource = mockk<HolidayLocalDataSource>()
    val transaction = mockk<HolidayTransaction>()
    coEvery { transaction.upsert(country = any(), year = any(), holidayList = any()) } just Runs
    val optionSetFlow = MutableStateFlow(optionSet)
    val settingLocalDataSource =
        mockk<HolidaySettingLocalDataSource>().also { dataSource ->
            every { dataSource.getHiddenKeySet() } returns MutableStateFlow(emptySet())
            every { dataSource.getCountryOptionSet() } returns optionSetFlow
            coEvery { dataSource.addCountryOption(option = any()) } coAnswers {
                optionSetFlow.value = optionSetFlow.value + firstArg<HolidayCountryOptionLocalEntity>()
            }
            coEvery { dataSource.removeCountryOption(option = any()) } coAnswers {
                optionSetFlow.value = optionSetFlow.value - firstArg<HolidayCountryOptionLocalEntity>()
            }
        }
    val koin =
        koinApplication<HolidayDataTestKoinApplication> {
            modules(
                module {
                    single<HolidayRemoteDataSource> { remoteDataSource }
                    single<HolidayLocalDataSource> { localDataSource }
                    single<HolidayTransaction> { transaction }
                    single<HolidaySettingLocalDataSource> { settingLocalDataSource }
                },
            )
        }.koin

    return HolidayCountryDataTestContext(
        koin = koin,
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        transaction = transaction,
        optionSetFlow = optionSetFlow,
    )
}

private fun remoteHoliday(year: Int): HolidayRemoteEntity {
    val start = randomDate(year = year)

    return HolidayRemoteEntity(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = fixtureMonkey.giveMeOne(),
        start = start,
        endInclusive = start,
    )
}

private fun localHoliday(
    year: Int,
    country: HolidayCountryLocalEntity = HolidayCountryLocalEntity.KOREA,
): HolidayLocalEntity {
    val start = randomDate(year = year)

    return HolidayLocalEntity(
        country = country,
        year = year,
        name = fixtureMonkey.giveMeOne(),
        isHoliday = fixtureMonkey.giveMeOne(),
        start = start,
        endInclusive = start,
    )
}

private fun randomDate(year: Int): LocalDate =
    LocalDate(
        year = year,
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )

private fun randomYear(): Int = 2_000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 1_000u).toInt()
