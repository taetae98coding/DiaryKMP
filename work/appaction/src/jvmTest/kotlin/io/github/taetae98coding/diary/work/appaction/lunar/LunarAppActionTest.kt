package io.github.taetae98coding.diary.work.appaction.lunar

import io.github.taetae98coding.diary.core.testing.lunar.lunarDate
import io.github.taetae98coding.diary.domain.lunar.usecase.GetLunarUseCase
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class LunarAppActionTest :
    FunSpec({
        test("음력 날짜는 받은 날짜 범위로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val dateRange = Arb.localDateRange().next()
                val lunarDateList = Arb.list(Arb.lunarDate(), 1..3).next()
                val getLunarUseCase = mockk<GetLunarUseCase>()
                every { getLunarUseCase(parameter = any()) } returns flowOf(Result.success(lunarDateList))
                val action = LunarAppAction(getLunarUseCase = getLunarUseCase)

                action.list(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString()) shouldBe
                    lunarDateList.map { lunarDate ->
                        AppActionLunarDate(
                            solarDate = lunarDate.solar.toString(),
                            lunarYear = lunarDate.year,
                            lunarMonth = lunarDate.month,
                            lunarDay = lunarDate.day,
                            isLeapMonth = lunarDate.isLeapMonth,
                        )
                    }
                verify(exactly = 1) { getLunarUseCase(parameter = dateRange) }
            }
        }

        test("시작일보다 앞선 마지막 날짜는 조회하지 않고 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val start = Arb.localDateRange().next().start
                val getLunarUseCase = mockk<GetLunarUseCase>()
                val action = LunarAppAction(getLunarUseCase = getLunarUseCase)

                shouldThrow<AppActionException.InvalidArgument> {
                    action.list(startDate = start.toString(), endDate = LocalDate.fromEpochDays(start.toEpochDays() - 1).toString())
                }.message shouldBe "'endDate' must not be before 'startDate'."
                verify(exactly = 0) { getLunarUseCase(parameter = any()) }
            }
        }

        test("음력 조회가 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val dateRange = Arb.localDateRange().next()
                val getLunarUseCase = mockk<GetLunarUseCase>()
                every { getLunarUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = LunarAppAction(getLunarUseCase = getLunarUseCase)

                shouldThrow<AppActionException.Failure> {
                    action.list(startDate = dateRange.start.toString(), endDate = dateRange.endInclusive.toString())
                }.message shouldBe "IllegalStateException: $message"
            }
        }
    })
