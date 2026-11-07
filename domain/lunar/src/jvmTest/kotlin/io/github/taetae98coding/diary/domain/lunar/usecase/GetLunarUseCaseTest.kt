package io.github.taetae98coding.diary.domain.lunar.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.core.testing.lunar.lunarDate
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class GetLunarUseCaseTest :
    BehaviorSpec({
        Given("기기에 저장한 음력 날짜가 있다") {
            val dateRange = Arb.localDateRange().next()
            val lunarDateFlow = MutableStateFlow(listOf(Arb.lunarDate().next()))
            val repository = mockk<LunarRepository>()
            every { repository.get(dateRange = dateRange) } returns lunarDateFlow
            val useCase = GetLunarUseCase(lunarRepository = repository)

            When("기간의 음력 날짜를 구독한다") {
                Then("저장된 음력 날짜와 바뀐 음력 날짜를 차례로 성공 결과로 받고 요청한 기간을 전달한다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeSuccess() shouldBe lunarDateFlow.value

                        val changed = listOf(Arb.lunarDate().next(), Arb.lunarDate().next())
                        lunarDateFlow.value = changed

                        awaitItem().shouldBeSuccess() shouldBe changed
                    }
                    verify(exactly = 1) { repository.get(dateRange = dateRange) }
                }
            }
        }

        Given("기기의 음력 날짜를 읽지 못한다") {
            val dateRange = Arb.localDateRange().next()
            val failure = IllegalStateException(Arb.string().next())
            val repository = mockk<LunarRepository>()
            every { repository.get(dateRange = dateRange) } returns flow<List<LunarDate>> { throw failure }
            val useCase = GetLunarUseCase(lunarRepository = repository)

            When("기간의 음력 날짜를 구독한다") {
                Then("실패 결과를 받고 흐름이 끝난다") {
                    useCase(parameter = dateRange).test {
                        awaitItem().shouldBeFailure() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }
                }
            }
        }
    })
