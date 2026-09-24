package io.github.taetae98coding.diary.domain.holiday.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

private const val CONSTITUTION_DAY_NAME = "제헌절"
private const val MIDSUMMER_DAY_NAME = "초복"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetCalendarHolidayUseCaseSettingTest :
    BehaviorSpec({
        Given("TC-HOLIDAY-VISIBILITY-DATA-005 한 이름을 숨긴 상태로 캘린더용 공휴일을 계속 조회하고 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val midsummerDay = holiday(name = MIDSUMMER_DAY_NAME)
            val changedMidsummerDay = holiday(name = MIDSUMMER_DAY_NAME)
            val constitutionDay = holiday(name = CONSTITUTION_DAY_NAME)
            val holidayFlow = MutableStateFlow(listOf(midsummerDay))
            val useCase =
                getCalendarHolidayUseCase(
                    holidayFlow = holidayFlow,
                    year = year,
                    hiddenKeySetFlow = MutableStateFlow(setOf(MIDSUMMER_DAY_NAME)),
                )

            When("저장된 공휴일이 같은 이름을 포함한 다른 목록으로 교체된다") {
                Then("교체된 목록에서 그 이름을 뺀 결과를 이어서 제공한다") {
                    useCase(parameter = year).test {
                        awaitItem().shouldBeSuccess() shouldBe emptyList()

                        holidayFlow.value = listOf(constitutionDay, changedMidsummerDay)

                        awaitItem().shouldBeSuccess() shouldBe listOf(constitutionDay)
                    }
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DATA-008 한 이름을 숨겼고 저장된 공휴일에는 그 이름이 없다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val constitutionDay = holiday(name = CONSTITUTION_DAY_NAME)
            val midsummerDay = holiday(name = MIDSUMMER_DAY_NAME)
            val holidayFlow = MutableStateFlow(listOf(constitutionDay))
            val useCase =
                getCalendarHolidayUseCase(
                    holidayFlow = holidayFlow,
                    year = year,
                    hiddenKeySetFlow = MutableStateFlow(setOf(MIDSUMMER_DAY_NAME)),
                )

            When("그 이름을 포함한 목록으로 교체된 뒤 캘린더용 공휴일을 조회한다") {
                holidayFlow.value = listOf(constitutionDay, midsummerDay)

                Then("이전에 고른 숨김이 그대로 적용된다") {
                    useCase(parameter = year).first().shouldBeSuccess() shouldBe listOf(constitutionDay)
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DATA-012 두 공휴일이 저장되어 있고 보관된 노출 설정을 읽을 수 없다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failureMessage = fixtureMonkey.giveMeOne<String>()
            val useCase =
                GetCalendarHolidayUseCase(
                    getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
                    holidayRepository =
                        mockk<HolidayRepository>().also { repository ->
                            every { repository.get(countrySet = KOREA_COUNTRY_SET, year = year) } returns
                                flowOf(listOf(holiday(name = CONSTITUTION_DAY_NAME), holiday(name = MIDSUMMER_DAY_NAME)))
                        },
                    holidaySettingRepository =
                        mockk<HolidaySettingRepository>().also { repository ->
                            every { repository.getHiddenKeySet() } returns flow { throw IllegalStateException(failureMessage) }
                        },
                )

            When("해당 연도의 캘린더용 공휴일을 조회한다") {
                Then("읽지 못한 원인을 담은 실패가 제공된다") {
                    useCase(parameter = year).test {
                        val throwable = awaitItem().shouldBeFailure()

                        throwable.shouldBeInstanceOf<IllegalStateException>()
                        throwable.message shouldBe failureMessage

                        awaitComplete()
                    }
                }
            }
        }

        Given("보관된 노출 설정이 바뀐 상태로 캘린더용 공휴일을 계속 조회하고 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val constitutionDay = holiday(name = CONSTITUTION_DAY_NAME)
            val midsummerDay = holiday(name = MIDSUMMER_DAY_NAME)
            val hiddenKeySetFlow = MutableStateFlow<Set<String>>(emptySet())
            val useCase =
                getCalendarHolidayUseCase(
                    holidayFlow = MutableStateFlow(listOf(constitutionDay, midsummerDay)),
                    year = year,
                    hiddenKeySetFlow = hiddenKeySetFlow,
                )

            When("보관된 숨김 key가 바뀐다") {
                Then("기존 조회 결과에 이어 바뀐 기준의 결과를 제공한다") {
                    useCase(parameter = year).test {
                        awaitItem().shouldBeSuccess() shouldBe listOf(constitutionDay, midsummerDay)

                        hiddenKeySetFlow.value = setOf(MIDSUMMER_DAY_NAME)

                        awaitItem().shouldBeSuccess() shouldBe listOf(constitutionDay)

                        hiddenKeySetFlow.value = emptySet()

                        awaitItem().shouldBeSuccess() shouldBe listOf(constitutionDay, midsummerDay)
                    }
                }
            }
        }
    })

private fun getCalendarHolidayUseCase(
    holidayFlow: Flow<List<Holiday>>,
    year: Int,
    hiddenKeySetFlow: Flow<Set<String>>,
): GetCalendarHolidayUseCase =
    GetCalendarHolidayUseCase(
        getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
        holidayRepository =
            mockk<HolidayRepository>().also { repository ->
                every { repository.get(countrySet = KOREA_COUNTRY_SET, year = year) } returns holidayFlow
            },
        holidaySettingRepository =
            mockk<HolidaySettingRepository>().also { repository ->
                every { repository.getHiddenKeySet() } returns hiddenKeySetFlow
            },
    )

private fun holiday(name: String): Holiday {
    val start = randomDate()

    return Holiday(
        name = name,
        isHoliday = fixtureMonkey.giveMeOne(),
        dateRange = start..start,
    )
}

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
