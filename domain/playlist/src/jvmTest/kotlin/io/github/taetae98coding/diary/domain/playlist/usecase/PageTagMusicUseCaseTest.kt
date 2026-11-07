package io.github.taetae98coding.diary.domain.playlist.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.repository.AccountTagMusicRepository
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

class PageTagMusicUseCaseTest :
    BehaviorSpec({
        Given("현재 계정과 대상 태그의 음악 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val sort = Arb.enum<ListSort>().next()
            val pagingData = PagingData.empty<Music>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagMusicRepository = mockk<AccountTagMusicRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountTagMusicRepository.page(account = account, tagId = tagId, scope = scope, sort = sort) } returns flowOf(pagingData)
            val useCase = PageTagMusicUseCase(getAccountUseCase = getAccountUseCase, accountTagMusicRepository = accountTagMusicRepository)

            When("대상 태그의 음악 페이지를 조회한다") {
                Then("현재 계정과 대상 태그·범위의 음악 페이지를 반환한다") {
                    useCase(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = scope, sort = sort)).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) { accountTagMusicRepository.page(account = account, tagId = tagId, scope = scope, sort = sort) }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val sort = Arb.enum<ListSort>().next()
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagMusicRepository = mockk<AccountTagMusicRepository>(relaxed = true)
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(failure))
            val useCase = PageTagMusicUseCase(getAccountUseCase = getAccountUseCase, accountTagMusicRepository = accountTagMusicRepository)

            When("대상 태그의 음악 페이지를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 페이지를 요청하지 않는다") {
                    useCase(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = scope, sort = sort)).test {
                        awaitItem().exceptionOrNull() shouldBeSameInstanceAs failure
                        awaitComplete()
                    }

                    verify(exactly = 0) { accountTagMusicRepository.page(account = any(), tagId = any(), scope = any(), sort = any()) }
                }
            }
        }

        Given("대상 태그의 음악 페이지 조회가 실패하도록 준비되어 있다") {
            val account = Arb.accountUser().next()
            val tagId = Arb.kotlinUuid().next()
            val scope = Arb.enum<TagScope>().next()
            val sort = Arb.enum<ListSort>().next()
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountTagMusicRepository = mockk<AccountTagMusicRepository>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            every { accountTagMusicRepository.page(account = account, tagId = tagId, scope = scope, sort = sort) } returns flow { throw failure }
            val useCase = PageTagMusicUseCase(getAccountUseCase = getAccountUseCase, accountTagMusicRepository = accountTagMusicRepository)

            When("대상 태그의 음악 페이지를 조회한다") {
                Then("저장소의 실패를 실패 결과로 전달한다") {
                    useCase(parameter = PageTagMusicUseCase.Parameter(tagId = tagId, scope = scope, sort = sort)).test {
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
