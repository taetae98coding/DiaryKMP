package io.github.taetae98coding.diary.domain.holiday.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

private const val CONSTITUTION_DAY_NAME = "제헌절"
private const val MIDSUMMER_DAY_NAME = "초복"
private const val INDEPENDENCE_MOVEMENT_DAY_NAME = "삼일절"
private const val SUBSTITUTE_INDEPENDENCE_MOVEMENT_DAY_NAME = "대체공휴일(삼일절)"
private const val LUNAR_NEW_YEAR_NAME = "설날"
private const val SUBSTITUTE_HOLIDAY_NAME = "대체공휴일"
private const val LIBERATION_DAY_NAME = "광복절"
private const val NATIONAL_FOUNDATION_DAY_NAME = "개천절"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetCalendarHolidayUseCaseTest :
    BehaviorSpec({
        Given("TC-HOLIDAY-VISIBILITY-DOMAIN-001 노출 설정을 한 번도 고르지 않았다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val constitutionDay = holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true)
            val midsummerDay = holiday(name = MIDSUMMER_DAY_NAME, isHoliday = false)
            val useCase =
                getCalendarHolidayUseCase(
                    holidayRepository = holidayRepository(year to listOf(constitutionDay, midsummerDay)),
                    hiddenKeySet = emptySet(),
                )

            When("해당 연도의 캘린더용 공휴일을 조회한다") {
                Then("두 공휴일이 모두 제공된다") {
                    useCase(parameter = year).first().shouldBeSuccess() shouldBe listOf(constitutionDay, midsummerDay)
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DOMAIN-002 두 연도에 같은 이름의 공휴일이 저장되어 있고 그 이름을 숨김으로 골랐다") {
            val previousYearMidsummerDay = holiday(name = MIDSUMMER_DAY_NAME, start = july(year = 2025, day = 20))
            val midsummerDay = holiday(name = MIDSUMMER_DAY_NAME, start = july(year = 2026, day = 20))
            val useCase =
                getCalendarHolidayUseCase(
                    holidayRepository =
                        holidayRepository(
                            2025 to listOf(previousYearMidsummerDay),
                            2026 to listOf(midsummerDay),
                        ),
                    hiddenKeySet = setOf(MIDSUMMER_DAY_NAME),
                )

            When("두 연도의 캘린더용 공휴일을 각각 조회한다") {
                Then("두 연도 모두 제공되지 않는다") {
                    useCase(parameter = 2025).first().shouldBeSuccess() shouldBe emptyList()
                    useCase(parameter = 2026).first().shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DOMAIN-003 이름이 다른 두 공휴일 중 하나만 숨김으로 골랐다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val independenceMovementDay = holiday(name = INDEPENDENCE_MOVEMENT_DAY_NAME)
            val substituteDay = holiday(name = SUBSTITUTE_INDEPENDENCE_MOVEMENT_DAY_NAME)
            val useCase =
                getCalendarHolidayUseCase(
                    holidayRepository = holidayRepository(year to listOf(independenceMovementDay, substituteDay)),
                    hiddenKeySet = setOf(INDEPENDENCE_MOVEMENT_DAY_NAME),
                )

            When("해당 연도의 캘린더용 공휴일을 조회한다") {
                Then("숨김으로 고르지 않은 공휴일만 제공된다") {
                    useCase(parameter = year).first().shouldBeSuccess() shouldBe listOf(substituteDay)
                }
            }
        }

        Given("공백만 다른 이름의 공휴일이 서로 다른 연도에 저장되어 있다") {
            val previousYearHoliday =
                holiday(
                    name = "대체 공휴일",
                    start = july(year = 2025, day = 17),
                )
            val holiday =
                holiday(
                    name = SUBSTITUTE_HOLIDAY_NAME,
                    start = july(year = 2026, day = 17),
                )
            val useCase =
                getCalendarHolidayUseCase(
                    holidayRepository =
                        holidayRepository(
                            2025 to listOf(previousYearHoliday),
                            2026 to listOf(holiday),
                        ),
                    hiddenKeySet = setOf(SUBSTITUTE_HOLIDAY_NAME),
                )

            When("한쪽 이름만 숨긴 채 두 연도의 캘린더용 공휴일을 각각 조회한다") {
                Then("이름을 가공하지 않으므로 공백이 다른 이름의 공휴일은 그대로 제공된다") {
                    useCase(parameter = 2025).first().shouldBeSuccess() shouldBe listOf(previousYearHoliday)
                    useCase(parameter = 2026).first().shouldBeSuccess() shouldBe emptyList()
                }
            }
        }

        Given("TC-HOLIDAY-VISIBILITY-DATA-017 연도별 조회가 이름 순이 아닌 순서로 공휴일을 제공하고 그중 하나를 숨김으로 골랐다") {
            val midsummerDay = holiday(name = MIDSUMMER_DAY_NAME, start = july(year = 2026, day = 15))
            val constitutionDay = holiday(name = CONSTITUTION_DAY_NAME, start = july(year = 2026, day = 17))
            val liberationDay = holiday(name = LIBERATION_DAY_NAME, start = LocalDate(year = 2026, month = Month.AUGUST, day = 15))
            val foundationDay = holiday(name = NATIONAL_FOUNDATION_DAY_NAME, start = LocalDate(year = 2026, month = Month.OCTOBER, day = 3))
            val useCase =
                getCalendarHolidayUseCase(
                    holidayRepository = holidayRepository(2026 to listOf(midsummerDay, constitutionDay, liberationDay, foundationDay)),
                    hiddenKeySet = setOf(LIBERATION_DAY_NAME),
                )

            When("2026년의 캘린더용 공휴일을 조회한다") {
                Then("숨긴 공휴일만 빠지고 연도별 조회 순서 그대로 제공된다") {
                    useCase(parameter = 2026).first().shouldBeSuccess() shouldBe listOf(midsummerDay, constitutionDay, foundationDay)
                }
            }
        }

        // 2026년 2월 6일은 금요일이므로 금요일부터 일요일까지 쉬는 날 사흘이 이어져 황금연휴가 된다.
        Given("TC-HOLIDAY-VISIBILITY-DOMAIN-005 황금연휴에 포함되는 공휴일을 숨김으로 골랐다") {
            val lunarNewYear = holiday(name = LUNAR_NEW_YEAR_NAME, start = LocalDate(year = 2026, month = Month.FEBRUARY, day = 6))
            val holidayRepository = holidayRepositoryByStartYear(listOf(lunarNewYear))
            val calendarHolidayUseCase =
                getCalendarHolidayUseCase(
                    holidayRepository = holidayRepository,
                    hiddenKeySet = setOf(LUNAR_NEW_YEAR_NAME),
                )
            val goldenHolidayUseCase =
                GetGoldenHolidayUseCase(
                    getHolidayUseCase = GetHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = holidayRepository),
                )

            When("캘린더용 공휴일과 황금연휴를 각각 조회한다") {
                Then("캘린더용 공휴일에는 제공되지 않지만 황금연휴에는 그대로 사용된다") {
                    calendarHolidayUseCase(parameter = 2026).first().shouldBeSuccess() shouldBe emptyList()

                    val goldenHolidayGroupList =
                        goldenHolidayUseCase(
                            parameter = GetGoldenHolidayUseCase.Parameter(year = 2026, annualLeaveCount = 0),
                        ).first()
                            .shouldBeSuccess()

                    goldenHolidayGroupList
                        .flatMap { group -> group.optionList }
                        .flatMap { option -> option.holidayList } shouldBe listOf(lunarNewYear)
                }
            }
        }
    })

private fun getCalendarHolidayUseCase(
    holidayRepository: HolidayRepository,
    hiddenKeySet: Set<String>,
): GetCalendarHolidayUseCase =
    GetCalendarHolidayUseCase(
        getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(),
        holidayRepository = holidayRepository,
        holidaySettingRepository =
            mockk<HolidaySettingRepository>().also { repository ->
                every { repository.getHiddenKeySet() } returns flowOf(hiddenKeySet)
            },
    )

private fun holidayRepository(vararg holidayListByYear: Pair<Int, List<Holiday>>): HolidayRepository {
    val holidayListMap = holidayListByYear.toMap()

    return mockk<HolidayRepository>().also { repository ->
        every { repository.get(countrySet = KOREA_COUNTRY_SET, year = any()) } answers {
            flowOf(holidayListMap[secondArg<Int>()].orEmpty())
        }
    }
}

private fun holidayRepositoryByStartYear(holidayList: List<Holiday>): HolidayRepository =
    mockk<HolidayRepository>().also { repository ->
        every { repository.get(countrySet = KOREA_COUNTRY_SET, year = any()) } answers {
            val year = secondArg<Int>()
            flowOf(holidayList.filter { holiday -> holiday.dateRange.start.year == year })
        }
    }

private fun holiday(
    name: String,
    isHoliday: Boolean = true,
    start: LocalDate = randomDate(),
): Holiday =
    Holiday(
        name = name,
        isHoliday = isHoliday,
        dateRange = start..start,
    )

private fun july(
    year: Int,
    day: Int,
): LocalDate = LocalDate(year = year, month = Month.JULY, day = day)

private fun randomDate(): LocalDate =
    LocalDate(
        year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
        month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
        day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
    )
