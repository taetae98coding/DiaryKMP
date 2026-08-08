package io.github.taetae98coding.diary.data.holiday.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.holiday.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.mapper.holiday.toDomain
import io.github.taetae98coding.diary.data.holiday.HolidayDataTestKoinApplication
import io.github.taetae98coding.diary.data.holiday.datasource.HolidayDirtyDataSource
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

private const val YEAR = 2026
private const val CONSTITUTION_DAY = "제헌절"
private const val MIDSUMMER_DAY = "초복"
private const val SECOND_MIDSUMMER_DAY = "중복"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class HolidayVisibilityDataTest :
    FunSpec({
        test("TC-HOLIDAY-VISIBILITY-DATA-001 숨김으로 고른 공휴일은 이후 조회에서 제외된다") {
            val context = holidayVisibilityTestContext(holidayList = standardHolidayList())

            context.settingRepository.addHiddenKey(key = MIDSUMMER_DAY)

            context
                .getCalendarHolidayUseCase(parameter = YEAR)
                .first()
                .getOrThrow()
                .map { holiday -> holiday.name } shouldBe listOf(CONSTITUTION_DAY)
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-002 노출로 되돌린 공휴일은 다시 제공된다") {
            val context =
                holidayVisibilityTestContext(
                    holidayList = standardHolidayList(),
                    hiddenKeySet = setOf(MIDSUMMER_DAY),
                )

            context.settingRepository.removeHiddenKey(key = MIDSUMMER_DAY)

            context
                .getCalendarHolidayUseCase(parameter = YEAR)
                .first()
                .getOrThrow()
                .map { holiday -> holiday.name } shouldBe listOf(CONSTITUTION_DAY, MIDSUMMER_DAY)
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-003 여러 key를 숨기면 각각 제외된다") {
            val context =
                holidayVisibilityTestContext(
                    holidayList =
                        listOf(
                            holiday(name = CONSTITUTION_DAY, day = 17),
                            holiday(name = MIDSUMMER_DAY, day = 20),
                            holiday(name = SECOND_MIDSUMMER_DAY, day = 30),
                        ),
                )

            context.settingRepository.submitHiddenKeySet(
                hiddenKeySet = setOf(MIDSUMMER_DAY, SECOND_MIDSUMMER_DAY),
            )

            context
                .getCalendarHolidayUseCase(parameter = YEAR)
                .first()
                .getOrThrow()
                .map { holiday -> holiday.name } shouldBe listOf(CONSTITUTION_DAY)
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-004 조회 중에 노출 설정이 바뀌면 이어서 새 결과를 제공한다") {
            val context = holidayVisibilityTestContext(holidayList = standardHolidayList())

            context.getCalendarHolidayUseCase(parameter = YEAR).test {
                awaitItem().getOrThrow().map { holiday -> holiday.name } shouldBe
                    listOf(CONSTITUTION_DAY, MIDSUMMER_DAY)

                context.settingRepository.addHiddenKey(key = MIDSUMMER_DAY)

                awaitItem().getOrThrow().map { holiday -> holiday.name } shouldBe listOf(CONSTITUTION_DAY)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-007 어느 공휴일에도 없는 key를 숨겨도 조회 결과가 달라지지 않는다") {
            val context =
                holidayVisibilityTestContext(
                    holidayList = listOf(holiday(name = CONSTITUTION_DAY, day = 17)),
                )

            context.getCalendarHolidayUseCase(parameter = YEAR).test {
                awaitItem().getOrThrow().map { holiday -> holiday.name } shouldBe listOf(CONSTITUTION_DAY)

                context.settingRepository.addHiddenKey(key = MIDSUMMER_DAY)

                awaitItem().getOrThrow().map { holiday -> holiday.name } shouldBe listOf(CONSTITUTION_DAY)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-009 노출 설정을 바꿔도 저장된 공휴일은 바뀌지 않는다") {
            val holidayList = standardHolidayList()
            val context = holidayVisibilityTestContext(holidayList = holidayList)

            context.settingRepository.addHiddenKey(key = MIDSUMMER_DAY)

            context.holidayRepository
                .get(year = YEAR)
                .first() shouldBe holidayList.map { local -> local.toDomain() }
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-010 저장된 공휴일이 교체되어도 노출 설정은 유지된다") {
            val context =
                holidayVisibilityTestContext(
                    holidayList = standardHolidayList(),
                    hiddenKeySet = setOf(MIDSUMMER_DAY),
                )

            context.settingRepository.getHiddenKeySet().test {
                awaitItem() shouldBe setOf(MIDSUMMER_DAY)

                context.holidayListFlow.value =
                    listOf(
                        holiday(name = CONSTITUTION_DAY, day = 17),
                        holiday(name = SECOND_MIDSUMMER_DAY, day = 30),
                    )

                expectNoEvents()
                context.settingRepository.getHiddenKeySet().first() shouldBe setOf(MIDSUMMER_DAY)
                cancelAndIgnoreRemainingEvents()
            }
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-011 숨김 key 집합을 교체하면 기존 값과 중간 상태가 남지 않는다") {
            val currentHiddenKey = MIDSUMMER_DAY
            val staleHiddenKey = uniqueKey(label = "stale")
            val initialHiddenKeySet = setOf(currentHiddenKey, staleHiddenKey)
            val newHiddenKeySet =
                setOf(
                    uniqueKey(label = "new-first"),
                    uniqueKey(label = "new-second"),
                    uniqueKey(label = "new-third"),
                )
            val context =
                holidayVisibilityTestContext(
                    holidayList = standardHolidayList(),
                    hiddenKeySet = initialHiddenKeySet,
                )

            context.settingRepository.getHiddenKeySet().test {
                awaitItem() shouldBe initialHiddenKeySet

                context.settingRepository.submitHiddenKeySet(
                    hiddenKeySet = newHiddenKeySet,
                )

                awaitItem() shouldBe newHiddenKeySet
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) {
                context.settingLocalDataSource.submitHiddenKeySet(
                    hiddenKeySet = newHiddenKeySet,
                )
            }
        }

        test("TC-HOLIDAY-VISIBILITY-DATA-013 변경 대상이 아닌 숨김 key는 유지한다") {
            val absentKey = "캐시에없는이름"
            val context =
                holidayVisibilityTestContext(
                    holidayList = standardHolidayList(),
                    hiddenKeySet = setOf(absentKey, MIDSUMMER_DAY),
                )

            context.settingRepository.addHiddenKey(key = SECOND_MIDSUMMER_DAY)

            context.settingRepository.getHiddenKeySet().first() shouldBe
                setOf(absentKey, MIDSUMMER_DAY, SECOND_MIDSUMMER_DAY)
        }
    })

private class HolidayVisibilityTestContext(
    val holidayListFlow: MutableStateFlow<List<HolidayLocalEntity>>,
    val settingLocalDataSource: HolidaySettingLocalDataSource,
    val holidayRepository: HolidayRepositoryImpl,
    val settingRepository: HolidaySettingRepositoryImpl,
    val getCalendarHolidayUseCase: GetCalendarHolidayUseCase,
)

private fun holidayVisibilityTestContext(
    holidayList: List<HolidayLocalEntity>,
    hiddenKeySet: Set<String> = emptySet(),
): HolidayVisibilityTestContext {
    val holidayListFlow = MutableStateFlow(holidayList)
    val holidayLocalDataSource =
        mockk<HolidayLocalDataSource>().also { dataSource ->
            every { dataSource.get(year = YEAR) } returns holidayListFlow
        }
    val holidayRemoteDataSource = mockk<HolidayRemoteDataSource>()
    val holidayTransaction = mockk<HolidayTransaction>()
    val holidayRepository =
        HolidayRepositoryImpl(
            holidayRemoteDataSource = holidayRemoteDataSource,
            holidayLocalDataSource = holidayLocalDataSource,
            holidayTransaction = holidayTransaction,
            holidayDirtyDataSource = HolidayDirtyDataSource(),
        )
    val hiddenKeySetFlow = MutableStateFlow(hiddenKeySet)
    val settingLocalDataSource =
        mockk<HolidaySettingLocalDataSource>().also { dataSource ->
            every { dataSource.getHiddenKeySet() } returns hiddenKeySetFlow
            coEvery { dataSource.addHiddenKey(key = any()) } coAnswers {
                hiddenKeySetFlow.value = hiddenKeySetFlow.value + firstArg<String>()
            }
            coEvery { dataSource.removeHiddenKey(key = any()) } coAnswers {
                hiddenKeySetFlow.value = hiddenKeySetFlow.value - firstArg<String>()
            }
            coEvery {
                dataSource.submitHiddenKeySet(
                    hiddenKeySet = any(),
                )
            } coAnswers {
                hiddenKeySetFlow.value = firstArg()
            }
        }
    val settingRepository =
        HolidaySettingRepositoryImpl(
            holidaySettingLocalDataSource = settingLocalDataSource,
        )
    val getCalendarHolidayUseCase =
        koinApplication<HolidayDataTestKoinApplication> {
            modules(
                module {
                    single<HolidayRepository> { holidayRepository }
                    single<HolidaySettingRepository> { settingRepository }
                    // 진입점이 data:holiday 자체 모듈까지 함께 조립하므로 그 의존성도 채운다.
                    single<HolidayRemoteDataSource> { holidayRemoteDataSource }
                    single<HolidayLocalDataSource> { holidayLocalDataSource }
                    single<HolidayTransaction> { holidayTransaction }
                    single<HolidaySettingLocalDataSource> { settingLocalDataSource }
                },
            )
        }.koin.get<GetCalendarHolidayUseCase>()

    return HolidayVisibilityTestContext(
        holidayListFlow = holidayListFlow,
        settingLocalDataSource = settingLocalDataSource,
        holidayRepository = holidayRepository,
        settingRepository = settingRepository,
        getCalendarHolidayUseCase = getCalendarHolidayUseCase,
    )
}

private fun standardHolidayList(): List<HolidayLocalEntity> =
    listOf(
        holiday(name = CONSTITUTION_DAY, day = 17),
        holiday(name = MIDSUMMER_DAY, day = 20),
    )

private fun uniqueKey(label: String): String = "${fixtureMonkey.giveMeOne<String>()}:$label"

private fun holiday(
    name: String,
    day: Int,
): HolidayLocalEntity =
    HolidayLocalEntity(
        year = YEAR,
        name = name,
        isHoliday = true,
        start = LocalDate(YEAR, 7, day),
        endInclusive = LocalDate(YEAR, 7, day),
    )
