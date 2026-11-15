package io.github.taetae98coding.diary.data.holiday.repository

import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountryOption
import io.github.taetae98coding.diary.core.testing.holiday.holidayLocalEntity
import io.github.taetae98coding.diary.core.testing.holiday.holidayRemoteEntity
import io.github.taetae98coding.diary.data.holiday.HolidayDataTestKoinApplication
import io.github.taetae98coding.diary.data.holiday.mapper.toDomain
import io.github.taetae98coding.diary.data.holiday.mapper.toLocal
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayCountryOptionUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.koin.core.Koin
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

class HolidayCountryDataTest :
    FunSpec({
        test("TC-7182d3ff-07ce-496b-aa7d-3030eb9ad9e6 한 국가가 실패해도 다른 국가의 저장된 공휴일은 원격 공휴일로 교체되고 연도 동기화는 실패한다") {
            val year = Arb.int(YEAR_RANGE).next()
            val unitedStatesRemoteList = listOf(Arb.holidayRemoteEntity().next(), Arb.holidayRemoteEntity().next())
            val context = holidayCountryDataTestContext(optionSet = BOTH_OPTION_SET)
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } throws
                IllegalStateException(Arb.string().next())
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

        test("TC-5bcf3e88-0091-49ed-a823-f72e9cb68b74 한 국가가 실패한 연도를 다시 요청하면 실패한 국가만 다시 원격 조회한다") {
            val year = Arb.int(YEAR_RANGE).next()
            val context = holidayCountryDataTestContext(optionSet = BOTH_OPTION_SET)
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) } throws
                IllegalStateException(Arb.string().next()) andThen listOf(Arb.holidayRemoteEntity().next())
            coEvery { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.UNITED_STATES, year = year) } returns listOf(Arb.holidayRemoteEntity().next())
            every { context.localDataSource.get(countrySet = setOf(HolidayCountryLocalEntity.UNITED_STATES), year = year) } returns
                MutableStateFlow(listOf(Arb.holidayLocalEntity().next().copy(year = year, country = HolidayCountryLocalEntity.UNITED_STATES)))
            val useCase = context.koin.get<FetchHolidayUseCase>()
            useCase(parameter = year).shouldBeFailure()

            useCase(parameter = year).shouldBeSuccess()

            coVerify(exactly = 2) { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.KOREA, year = year) }
            coVerify(exactly = 1) { context.remoteDataSource.get(country = HolidayCountryRemoteEntity.UNITED_STATES, year = year) }
        }

        test("TC-0d734f0f-dd9e-4adc-9cbd-d6095ee05e47 국가 설정을 바꿔도 기기에 저장된 공휴일은 바뀌지 않는다") {
            val year = Arb.int(YEAR_RANGE).next()
            val storedHolidayList = listOf(Arb.holidayLocalEntity().next().copy(year = year, country = HolidayCountryLocalEntity.KOREA), Arb.holidayLocalEntity().next().copy(year = year, country = HolidayCountryLocalEntity.KOREA))
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

private val YEAR_RANGE = 2_000..2_999
