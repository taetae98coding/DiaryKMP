package io.github.taetae98coding.diary.domain.holiday.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

// 2026년 2월 1일은 일요일이므로 2월 7일과 14일은 토요일, 2월 8일과 15일은 일요일이다.
class GetGoldenHolidayUseCaseTest :
    BehaviorSpec({
        Given("TC-HOLIDAY-HOME-DOMAIN-001 금요일 하루가 공휴일이고 연차 개수가 0이다") {
            val holiday = holiday(start = february(day = 6))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(holiday))

                Then("금요일부터 일요일까지의 황금연휴 하나가 나오고 사용 연차는 없다") {
                    goldenHolidayList shouldBe
                        listOf(
                            GoldenHoliday(
                                dateRange = february(day = 6)..february(day = 8),
                                holidayList = listOf(holiday),
                                annualLeaveDateRangeList = emptyList(),
                            ),
                        )
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-002 목요일 하루가 공휴일이고 연차 개수가 0이다") {
            val holiday = holiday(start = february(day = 5))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(holiday))

                Then("황금연휴가 하나도 나오지 않는다") {
                    goldenHolidayList shouldBe emptyList()
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-003 목요일 하루가 공휴일이고 연차 개수가 1이다") {
            val holiday = holiday(start = february(day = 5))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 1, holidayList = listOf(holiday))

                Then("금요일을 연차로 메운 목요일부터 일요일까지의 황금연휴 하나가 나온다") {
                    goldenHolidayList shouldBe
                        listOf(
                            GoldenHoliday(
                                dateRange = february(day = 5)..february(day = 8),
                                holidayList = listOf(holiday),
                                annualLeaveDateRangeList = listOf(february(day = 6)..february(day = 6)),
                            ),
                        )
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-004 저장된 공휴일이 없다") {
            When("연차 개수를 바꿔 가며 2026년의 황금연휴를 구한다") {
                Then("연차를 모두 써도 황금연휴가 하나도 나오지 않는다") {
                    listOf(1, 5).forEach { annualLeaveCount ->
                        goldenHolidayList(annualLeaveCount = annualLeaveCount, holidayList = emptyList()) shouldBe emptyList()
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-005 수요일 하루가 공휴일이고 연차 개수가 1이다") {
            val holiday = holiday(start = february(day = 4))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 1, holidayList = listOf(holiday))

                Then("황금연휴가 하나도 나오지 않는다") {
                    goldenHolidayList shouldBe emptyList()
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-006 목요일 하루가 공휴일이고 연차 개수가 2다") {
            val holiday = holiday(start = february(day = 5))

            When("2026년의 황금연휴를 구한다") {
                val group = goldenHolidayGroupList(annualLeaveCount = 2, holidayList = listOf(holiday)).single()

                Then("금요일을 메우고 남은 하루를 앞이나 뒤에 붙여 연차 2일을 모두 사용한다") {
                    group.optionList.map { option -> option.dateRange } shouldBe
                        listOf(
                            february(day = 4)..february(day = 8),
                            february(day = 5)..february(day = 9),
                        )
                    group.optionList.forEach { option -> option.annualLeaveCount shouldBe 2 }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-007 목요일부터 금요일까지가 공휴일이고 연차 개수가 0이다") {
            val holiday = holiday(start = february(day = 12), endInclusive = february(day = 13))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(holiday))

                Then("이어지는 쉬는 날을 하나로 묶은 목요일부터 일요일까지의 황금연휴 하나가 나온다") {
                    goldenHolidayList shouldBe
                        listOf(
                            GoldenHoliday(
                                dateRange = february(day = 12)..february(day = 15),
                                holidayList = listOf(holiday),
                                annualLeaveDateRangeList = emptyList(),
                            ),
                        )
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-008 설날이 저장되어 있고 연차 개수가 3이다") {
            val holiday = holiday(start = february(day = 16), endInclusive = february(day = 18))

            When("2026년의 황금연휴를 구한다") {
                val groupList = goldenHolidayGroupList(annualLeaveCount = 3, holidayList = listOf(holiday))

                Then("남는 연차를 앞뒤에 붙인 대안이 시작일 순으로 한 항목에 묶인다") {
                    groupList.size shouldBe 1
                    groupList.single().optionList.map { option -> option.dateRange } shouldBe
                        listOf(
                            february(day = 13)..february(day = 22),
                            february(day = 14)..february(day = 23),
                        )
                    groupList.single().optionList.forEach { option ->
                        option.dateRange.size shouldBe 10
                        option.annualLeaveCount shouldBe 3
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-016 가장 긴 대안이 여러 개인 항목이 나온다") {
            val holiday = holiday(start = february(day = 16), endInclusive = february(day = 18))

            When("2026년의 황금연휴를 구한다") {
                val group = goldenHolidayGroupList(annualLeaveCount = 5, holidayList = listOf(holiday)).single()

                Then("가장 긴 12일 대안만 남고 모두 연차 5일을 쓰며 첫 번째 대안의 시작일이 가장 이르다") {
                    group.optionList.forEach { option ->
                        option.dateRange.size shouldBe 12
                        option.annualLeaveCount shouldBe 5
                    }
                    group.optionList.first().dateRange shouldBe february(day = 7)..february(day = 18)
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-017 날짜를 공유하지 않는 공휴일이 두 개 저장되어 있다") {
            val februaryHoliday = holiday(start = february(day = 6))
            val septemberHoliday = holiday(start = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 25))

            When("2026년의 황금연휴를 구한다") {
                val groupList =
                    goldenHolidayGroupList(
                        annualLeaveCount = 0,
                        holidayList = listOf(februaryHoliday, septemberHoliday),
                    )

                Then("서로 다른 항목이 되고 각 항목의 대안은 하나씩이다") {
                    groupList.size shouldBe 2
                    groupList.forEach { group -> group.optionList.size shouldBe 1 }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-018 전국동시지방선거가 수요일, 현충일이 그 주 토요일이고 연차 개수가 2다") {
            val election = holiday(start = june(day = 3))
            val memorialDay = holiday(start = june(day = 6))

            When("2026년의 황금연휴를 구한다") {
                val groupList = goldenHolidayGroupList(annualLeaveCount = 2, holidayList = listOf(election, memorialDay))

                Then("같은 일수와 같은 연차로 공휴일을 하나만 담는 안은 제외되고 두 공휴일을 담는 안만 남는다") {
                    groupList.size shouldBe 1
                    groupList.single().optionList.map { option -> option.dateRange } shouldBe
                        listOf(june(day = 3)..june(day = 7))
                    groupList
                        .single()
                        .optionList
                        .single()
                        .holidayList shouldBe listOf(election, memorialDay)
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-019 전국동시지방선거와 현충일이 저장되어 있고 연차 개수가 3이다") {
            val election = holiday(start = june(day = 3))
            val memorialDay = holiday(start = june(day = 6))

            When("2026년의 황금연휴를 구한다") {
                val group = goldenHolidayGroupList(annualLeaveCount = 3, holidayList = listOf(election, memorialDay)).single()

                Then("두 공휴일을 모두 담는 대안만 남고 서로를 밀어내지 못하므로 둘 다 유지된다") {
                    group.optionList.map { option -> option.dateRange } shouldBe
                        listOf(
                            june(day = 2)..june(day = 7),
                            june(day = 3)..june(day = 8),
                        )
                    group.optionList.forEach { option ->
                        option.holidayList shouldBe listOf(election, memorialDay)
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-009 9월 공휴일이 2월 공휴일보다 먼저 저장되어 있고 연차 개수가 0이다") {
            val septemberHoliday = holiday(start = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 25))
            val februaryHoliday = holiday(start = february(day = 6))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList =
                    goldenHolidayList(
                        annualLeaveCount = 0,
                        holidayList = listOf(septemberHoliday, februaryHoliday),
                    )

                Then("시작일이 이른 2월 황금연휴가 먼저 나온다") {
                    goldenHolidayList.map { goldenHoliday -> goldenHoliday.dateRange } shouldBe
                        listOf(
                            february(day = 6)..february(day = 8),
                            LocalDate(year = 2026, month = Month.SEPTEMBER, day = 25)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 27),
                        )
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-010 2026년 12월 31일과 2027년 1월 1일이 공휴일이고 연차 개수가 0이다") {
            val endOfYearHoliday = holiday(start = LocalDate(year = 2026, month = Month.DECEMBER, day = 31))
            val newYearHoliday = holiday(start = LocalDate(year = 2027, month = Month.JANUARY, day = 1))
            val holidayList = listOf(endOfYearHoliday, newYearHoliday)
            val expectedDateRange =
                LocalDate(year = 2026, month = Month.DECEMBER, day = 31)..LocalDate(year = 2027, month = Month.JANUARY, day = 3)

            When("2026년과 2027년의 황금연휴를 각각 구한다") {
                val goldenHolidayListOf2026 = goldenHolidayList(year = 2026, annualLeaveCount = 0, holidayList = holidayList)
                val goldenHolidayListOf2027 = goldenHolidayList(year = 2027, annualLeaveCount = 0, holidayList = holidayList)

                Then("두 년도 모두에서 년도 경계를 넘는 황금연휴가 나온다") {
                    goldenHolidayListOf2026.map { goldenHoliday -> goldenHoliday.dateRange } shouldBe listOf(expectedDateRange)
                    goldenHolidayListOf2027.map { goldenHoliday -> goldenHoliday.dateRange } shouldBe listOf(expectedDateRange)
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-011 금요일 하루가 공휴일 여부가 거짓으로 저장되어 있고 연차 개수가 0이다") {
            val anniversary = holiday(start = february(day = 6), isHoliday = false)

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(anniversary))

                Then("황금연휴가 하나도 나오지 않는다") {
                    goldenHolidayList shouldBe emptyList()
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-012 화요일부터 목요일까지가 공휴일이고 연차 개수가 0이다") {
            val holiday = holiday(start = february(day = 10), endInclusive = february(day = 12))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList = goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(holiday))

                Then("기간의 모든 날을 쉬는 날로 본 화요일부터 목요일까지의 황금연휴 하나가 나온다") {
                    goldenHolidayList shouldBe
                        listOf(
                            GoldenHoliday(
                                dateRange = february(day = 10)..february(day = 12),
                                holidayList = listOf(holiday),
                                annualLeaveDateRangeList = emptyList(),
                            ),
                        )
                }
            }
        }

        Given("수요일 하루가 공휴일이고 연차 개수가 다섯이다") {
            val holiday = holiday(start = february(day = 11))

            When("2026년의 황금연휴를 구한다") {
                val group = goldenHolidayGroupList(annualLeaveCount = 5, holidayList = listOf(holiday)).single()

                Then("앞뒤 주말을 연차로 이어 붙여 연차 다섯 날을 모두 쓴 열 날 연휴가 나온다") {
                    group.optionList.map { option -> option.dateRange } shouldBe
                        listOf(
                            february(day = 6)..february(day = 15),
                            february(day = 7)..february(day = 16),
                        )
                    group.optionList.forEach { option ->
                        option.dateRange.size shouldBe 10
                        option.annualLeaveCount shouldBe 5
                    }
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-020 2025년 기독탄신일과 2026년 신정이 저장되어 있고 연차 개수가 4다") {
            val christmas = holiday(start = LocalDate(year = 2025, month = Month.DECEMBER, day = 25))
            val newYear = holiday(start = LocalDate(year = 2026, month = Month.JANUARY, day = 1))

            When("2026년의 황금연휴를 구한다") {
                val groupList = goldenHolidayGroupList(annualLeaveCount = 4, holidayList = listOf(christmas, newYear))

                Then("담은 공휴일이 다른 안은 서로 다른 항목이 되고, 2026년에 걸치지 않는 항목은 목록에 없다") {
                    groupList.map { group -> group.optionList.map { option -> option.holidayList } } shouldBe
                        listOf(
                            listOf(listOf(christmas, newYear)),
                            listOf(listOf(newYear)),
                        )
                    groupList.map { group -> group.optionList.single().dateRange } shouldBe
                        listOf(
                            LocalDate(year = 2025, month = Month.DECEMBER, day = 25)..LocalDate(year = 2026, month = Month.JANUARY, day = 1),
                            LocalDate(year = 2025, month = Month.DECEMBER, day = 27)..LocalDate(year = 2026, month = Month.JANUARY, day = 4),
                        )
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-014 공휴일이 아닌 이름과 공휴일이 함께 저장되어 있다") {
            val holiday = holiday(start = february(day = 6))
            val anniversary = holiday(start = february(day = 7), isHoliday = false)

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList =
                    goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(holiday, anniversary))

                Then("연휴의 공휴일에는 공휴일 여부가 참인 항목만 담긴다") {
                    goldenHolidayList.single().holidayList shouldBe listOf(holiday)
                }
            }
        }

        Given("TC-HOLIDAY-HOME-DOMAIN-015 연휴 기간과 겹치지 않는 공휴일이 함께 저장되어 있다") {
            val holiday = holiday(start = february(day = 6))
            val otherHoliday = holiday(start = february(day = 25))

            When("2026년의 황금연휴를 구한다") {
                val goldenHolidayList =
                    goldenHolidayList(annualLeaveCount = 0, holidayList = listOf(holiday, otherHoliday))

                Then("연휴의 공휴일에는 그 기간에 걸치는 공휴일만 담긴다") {
                    goldenHolidayList
                        .single { goldenHoliday -> goldenHoliday.dateRange.start == february(day = 6) }
                        .holidayList shouldBe listOf(holiday)
                }
            }
        }
    })

// 대안이 하나뿐인 규칙 검증에서는 각 항목의 유일한 대안을 그대로 본다.
private suspend fun goldenHolidayList(
    year: Int = 2026,
    annualLeaveCount: Int,
    holidayList: List<Holiday>,
): List<GoldenHoliday> =
    goldenHolidayGroupList(year = year, annualLeaveCount = annualLeaveCount, holidayList = holidayList)
        .map { group -> group.optionList.single() }

private suspend fun goldenHolidayGroupList(
    year: Int = 2026,
    annualLeaveCount: Int,
    holidayList: List<Holiday>,
): List<GoldenHolidayGroup> =
    getGoldenHolidayUseCase(holidayRepository = holidayRepository(holidayList = holidayList))(
        parameter = GetGoldenHolidayUseCase.Parameter(year = year, annualLeaveCount = annualLeaveCount),
    ).first()
        .shouldBeSuccess()

private fun getGoldenHolidayUseCase(holidayRepository: HolidayRepository): GetGoldenHolidayUseCase =
    GetGoldenHolidayUseCase(
        getHolidayUseCase = GetHolidayUseCase(holidayRepository = holidayRepository),
    )

// 저장된 공휴일을 시작일의 년도별로 나누어 돌려주는 저장소다.
private fun holidayRepository(holidayList: List<Holiday>): HolidayRepository =
    mockk<HolidayRepository>().also { repository ->
        every { repository.get(year = any()) } answers {
            val year = firstArg<Int>()
            flowOf(holidayList.filter { holiday -> holiday.dateRange.start.year == year })
        }
    }

private fun holiday(
    start: LocalDate,
    endInclusive: LocalDate = start,
    isHoliday: Boolean = true,
): Holiday =
    Holiday(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = isHoliday,
        dateRange = LocalDateRange(start = start, endInclusive = endInclusive),
    )

private fun february(day: Int): LocalDate = LocalDate(year = 2026, month = Month.FEBRUARY, day = day)

// 2026년 6월 1일은 월요일이므로 6월 3일은 수요일, 6월 6일은 토요일, 6월 7일은 일요일이다.
private fun june(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JUNE, day = day)
