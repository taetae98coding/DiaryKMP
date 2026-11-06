package io.github.taetae98coding.diary.domain.qr.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.qr.repository.AccountTagQrRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class PageTagQrUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 대상 태그의 QR 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val pagingData = PagingData.empty<Qr>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagQrRepository = mockk<AccountTagQrRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountTagQrRepository.page(account = account, tagId = tagId, scope = scope) } returns flowOf(pagingData)
            val useCase = PageTagQrUseCase(getAccountUseCase = getAccountUseCase, accountTagQrRepository = accountTagQrRepository)

            When("대상 태그의 QR 페이지를 조회한다") {
                Then("현재 계정과 대상 태그·범위의 QR 페이지를 반환한다") {
                    useCase(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = scope)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) { accountTagQrRepository.page(account = account, tagId = tagId, scope = scope) }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagQrRepository = mockk<AccountTagQrRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase = PageTagQrUseCase(getAccountUseCase = getAccountUseCase, accountTagQrRepository = accountTagQrRepository)

            When("대상 태그의 QR 페이지를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 페이지를 요청하지 않는다") {
                    useCase(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = scope)).test {
                        awaitItem().exceptionOrNull() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    verify(exactly = 0) { accountTagQrRepository.page(account = any(), tagId = any(), scope = any()) }
                }
            }
        }

        Given("대상 태그의 QR 페이지 조회가 실패하도록 준비되어 있다") {
            val account = Arb.accountUser().next()
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagQrRepository = mockk<AccountTagQrRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountTagQrRepository.page(account = account, tagId = tagId, scope = scope) } returns flow { throw failure }
            val useCase = PageTagQrUseCase(getAccountUseCase = getAccountUseCase, accountTagQrRepository = accountTagQrRepository)

            When("대상 태그의 QR 페이지를 조회한다") {
                Then("저장소의 실패를 실패 결과로 전달한다") {
                    useCase(parameter = PageTagQrUseCase.Parameter(tagId = tagId, scope = scope)).test {
                        awaitItem()
                            .shouldBeFailure()
                            .shouldBeInstanceOf<IllegalStateException>()
                            .message shouldBe failure.message
                        awaitComplete()
                    }
                }
            }
        }
    })
