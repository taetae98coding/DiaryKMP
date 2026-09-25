package io.github.taetae98coding.diary.domain.holiday.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchHolidayUseCaseTest :
    BehaviorSpec({
        Given("공휴일 동기화가 성공하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<HolidayRepository>()
            val holidayList = listOf(holiday())
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } returns holidayList
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            When("특정 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("TC-HOLIDAY-FETCH-DOMAIN-005 동기화한 공휴일을 성공 결과로 반환하고 요청한 연도를 전달한다") {
                    result.shouldBeSuccess() shouldBe holidayList
                    coVerify(exactly = 1) { repository.fetch(country = HolidayCountry.KOREA, year = year) }
                }
            }
        }

        Given("공휴일 동기화가 실패하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<HolidayRepository>()
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } throws failure
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            When("특정 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("실패 원인을 그대로 반환한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 공휴일 동기화가 특정 원인으로 실패하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<HolidayRepository>()
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } throws failure
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("특정 연도의 공휴일 동기화를 요청한다") {
                useCase(parameter = year)

                Then("TC-HOLIDAY-FETCH-DOMAIN-001 TC-USECASE-FAILURE-LOGGING-DOMAIN-009 그 원인을 담은 오류 보고가 한 번 남는다") {
                    val report = reportList.single()

                    report.throwable shouldBeSameInstanceAs failure
                    report.message shouldContain "FetchHolidayUseCase"
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 공휴일 동기화가 성공하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<HolidayRepository>()
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } returns listOf(holiday())
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("특정 연도의 공휴일 동기화를 요청한다") {
                useCase(parameter = year)

                Then("TC-HOLIDAY-FETCH-DOMAIN-002 오류 보고가 남지 않는다") {
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 원격이 공휴일을 제공하지 않도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<HolidayRepository>()
            coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } returns emptyList()
            val useCase = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("특정 연도의 공휴일 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("TC-HOLIDAY-FETCH-DOMAIN-003 빈 공휴일 목록으로 성공하고 오류 보고는 남지 않는다") {
                    result.shouldBeSuccess().shouldBeEmpty()
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("원격 조회 결과가 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())

            When("받아오지 못해 실패한다") {
                val repository = mockk<HolidayRepository>()
                coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } throws failure
                val result = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)(parameter = year)

                Then("TC-HOLIDAY-FETCH-DOMAIN-004 실패 결과로 끝나 제공하지 않는 연도와 구분된다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }

            When("원격이 공휴일을 제공하지 않는다") {
                val repository = mockk<HolidayRepository>()
                coEvery { repository.fetch(country = HolidayCountry.KOREA, year = year) } returns emptyList()
                val result = FetchHolidayUseCase(getHolidayCountrySettingUseCase = koreaCountrySettingUseCase(), holidayRepository = repository)(parameter = year)

                Then("TC-HOLIDAY-FETCH-DOMAIN-004 빈 공휴일 목록을 담은 성공 결과로 끝나 실패와 구분된다") {
                    result.shouldBeSuccess().shouldBeEmpty()
                }
            }
        }
    })

private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
    val reportList = mutableListOf<CrashlyticsLog>()
    val delegate = mockk<DiaryLoggerDelegate>()

    every { delegate.log(log = any()) } answers {
        val log = firstArg<DiaryLog>()
        if (log is CrashlyticsLog) {
            reportList += log
        }
    }

    DiaryLogger.add(delegate = delegate)

    return reportList
}

private fun holiday(): Holiday {
    val start =
        LocalDate(
            year = 2000 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 100u).toInt(),
            month = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 12u).toInt(),
            day = 1 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 28u).toInt(),
        )

    return Holiday(
        name = fixtureMonkey.giveMeOne(),
        isHoliday = fixtureMonkey.giveMeOne(),
        dateRange = start..start,
    )
}
