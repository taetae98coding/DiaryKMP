package io.github.taetae98coding.diary.domain.lunar.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
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

private val MAX_SOLAR_DATE: LocalDate = LocalDate(2100, 1, 1)

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FetchLunarUseCaseTest :
    BehaviorSpec({
        Given("음력 자료 동기화가 성공하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val lunarDateList = listOf(lunarDate())
            val repository = mockk<LunarRepository>()
            coEvery { repository.fetch(year = year) } returns lunarDateList
            val useCase = FetchLunarUseCase(lunarRepository = repository)

            When("특정 연도의 음력 자료 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("TC-e6f699a2-bd9c-4a3a-9d54-dccbcb829194 동기화한 음력 날짜를 성공 결과로 반환하고 요청한 연도를 전달한다") {
                    result.shouldBeSuccess() shouldBe lunarDateList
                    coVerify(exactly = 1) { repository.fetch(year = year) }
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 음력 자료 동기화가 특정 원인으로 실패하도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository = mockk<LunarRepository>()
            coEvery { repository.fetch(year = year) } throws failure
            val useCase = FetchLunarUseCase(lunarRepository = repository)
            val reportList = recordCrashlyticsLog()

            When("특정 연도의 음력 자료 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("TC-c6e531c4-1558-43a1-879e-c9e73f55d26b 오류 보고는 남지 않는다") {
                    reportList.shouldBeEmpty()
                }

                Then("TC-16f767f7-bf9a-4111-ba16-fb3e808cd5b5 실패 결과로 끝나 제공하지 않는 연도와 구분된다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs failure
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 원격이 음력 자료를 제공하지 않도록 준비되어 있다") {
            val year = fixtureMonkey.giveMeOne<Int>()
            val repository = mockk<LunarRepository>()
            coEvery { repository.fetch(year = year) } returns emptyList()
            val useCase = FetchLunarUseCase(lunarRepository = repository)
            val reportList = recordCrashlyticsLog()

            When("특정 연도의 음력 자료 동기화를 요청한다") {
                val result = useCase(parameter = year)

                Then("TC-16f767f7-bf9a-4111-ba16-fb3e808cd5b5 빈 음력 날짜로 성공하고 오류 보고는 남지 않는다") {
                    result.shouldBeSuccess().shouldBeEmpty()
                    reportList.shouldBeEmpty()
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

private fun lunarDate(): LunarDate =
    LunarDate(
        solar = LocalDate.fromEpochDays(fixtureMonkey.giveMeOne<Long>().mod(MAX_SOLAR_DATE.toEpochDays())),
        year = fixtureMonkey.giveMeOne(),
        month = fixtureMonkey.giveMeOne(),
        day = fixtureMonkey.giveMeOne(),
        isLeapMonth = fixtureMonkey.giveMeOne(),
    )
