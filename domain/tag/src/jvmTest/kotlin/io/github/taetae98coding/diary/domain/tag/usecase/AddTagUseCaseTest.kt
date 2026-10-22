package io.github.taetae98coding.diary.domain.tag.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.exception.TagTitleBlankException
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AddTagUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
                val useCase =
                    AddTagUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        accountTagRepository = accountTagRepository,
                        clock = Clock.System,
                    )

                When("태그를 추가한다") {
                    Then("TC-e22777d8-af82-4f13-940c-e2d3c9f04fee 제목 공백 예외로 실패하고 태그를 저장하지 않는다") {
                        val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = blankTitle)

                        val result = useCase(parameter = AddTagUseCase.Parameter(detail = detail))

                        result.shouldBeFailure().shouldBeInstanceOf<TagTitleBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountTagRepository.upsert(account = any(), tag = any(), linkedTagIdSet = any()) }
                        coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                    }
                }
            }
        }

        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagSlot = slot<Tag>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery { accountTagRepository.upsert(account = account, tag = capture(tagSlot), linkedTagIdSet = any()) } just Runs
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 태그를 추가한다") {
                Then("TC-6b189131-ffb1-40b3-af1f-3a299eddae0e 현재 계정과 연결된 고유한 태그로 저장한다") {
                    val firstDetail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())
                    val secondDetail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())

                    val firstId = useCase(parameter = AddTagUseCase.Parameter(detail = firstDetail)).shouldBeSuccess()
                    val secondId = useCase(parameter = AddTagUseCase.Parameter(detail = secondDetail)).shouldBeSuccess()

                    firstId shouldNotBe Uuid.NIL
                    secondId shouldNotBe Uuid.NIL
                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountTagRepository.upsert(account = account, tag = any(), linkedTagIdSet = any()) }
                }

                Then("TC-a16e1dca-972f-4c12-af5e-8a2539ac5d9e 입력한 이모지, 제목, 설명, 컬러를 그대로 저장한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())

                    val result = useCase(parameter = AddTagUseCase.Parameter(detail = detail))

                    result.shouldBeSuccess(tagSlot.captured.id)
                    tagSlot.captured.detail shouldBe detail
                }

                Then("TC-54afbe51-7aa0-43a8-b26f-4655006c1a24 이모지를 입력하지 않아도 이모지가 빈 태그로 저장한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = nonBlankTitle())

                    val result = useCase(parameter = AddTagUseCase.Parameter(detail = detail))

                    result.shouldBeSuccess(tagSlot.captured.id)
                    tagSlot.captured.detail.emoji shouldBe ""
                    tagSlot.captured.detail shouldBe detail
                }

                Then("TC-8218e34f-cf4d-4e9c-b8f4-e2ff033dd1bb 설명을 입력하지 않아도 설명이 빈 태그로 저장한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle(), description = "")

                    val result = useCase(parameter = AddTagUseCase.Parameter(detail = detail))

                    result.shouldBeSuccess(tagSlot.captured.id)
                    tagSlot.captured.detail.description shouldBe ""
                    tagSlot.captured.detail shouldBe detail
                }

                Then("TC-f9dcded9-62bf-4629-93c1-3ffffb06725e 미완료·미삭제 상태와 추가 시각을 저장한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())

                    useCase(parameter = AddTagUseCase.Parameter(detail = detail)).shouldBeSuccess()

                    tagSlot.captured.isFinished shouldBe false
                    tagSlot.captured.isDeleted shouldBe false
                    tagSlot.captured.createdAt shouldBe now
                    tagSlot.captured.updatedAt shouldBe now
                }
            }
        }

        Given("연결할 태그를 고를 수 있는 상태로 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagSlot = slot<Tag>()
            val linkedTagIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery {
                accountTagRepository.upsert(
                    account = account,
                    tag = capture(tagSlot),
                    linkedTagIdSet = capture(linkedTagIdSetSlot),
                )
            } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns fixtureMonkey.giveMeOne<Instant>()
            val useCase =
                AddTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("고른 태그와 함께 태그를 추가한다") {
                Then("TC-1e53dccb-f364-43cc-9777-8428a3247b7c 고른 태그를 도착 태그로 하는 연결을 새 태그와 함께 저장한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())
                    val linkedTagIdSet = List(2) { fixtureMonkey.giveMeOne<Uuid>() }.toSet()

                    val result =
                        useCase(
                            parameter = AddTagUseCase.Parameter(detail = detail, linkedTagIdSet = linkedTagIdSet),
                        )

                    result.shouldBeSuccess(tagSlot.captured.id)
                    linkedTagIdSetSlot.captured shouldBe linkedTagIdSet
                }
            }

            When("태그를 하나도 고르지 않고 태그를 추가한다") {
                Then("TC-371e7073-94f7-479b-b893-d95ed346f26d 연결 없이 태그를 저장한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())

                    val result = useCase(parameter = AddTagUseCase.Parameter(detail = detail))

                    result.shouldBeSuccess(tagSlot.captured.id)
                    linkedTagIdSetSlot.captured.shouldBeEmpty()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            val useCase =
                AddTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목으로 태그를 추가한다") {
                Then("TC-8b2ffaae-1e63-44a2-a189-983f26c2ea3b 계정 조회 실패를 전달하고 태그를 저장하지 않는다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())

                    val result = useCase(parameter = AddTagUseCase.Parameter(detail = detail))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountTagRepository.upsert(account = any(), tag = any(), linkedTagIdSet = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("태그 저장과 동기화 요청이 성공하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery { accountTagRepository.upsert(account = account, tag = any(), linkedTagIdSet = any()) } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns fixtureMonkey.giveMeOne<Instant>()
            val useCase =
                AddTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 태그를 추가한다") {
                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-fc0efa55-9c70-44bb-ae31-8eeaec53668f 태그를 기기에 저장한 뒤 동기화를 한 번 요청한다") {
                    val result = useCase(parameter = AddTagUseCase.Parameter(detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = nonBlankTitle())))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountTagRepository.upsert(account = account, tag = any(), linkedTagIdSet = any())
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun nonBlankTitle(): String = "title-${fixtureMonkey.giveMeOne<String>()}"
    }
}
