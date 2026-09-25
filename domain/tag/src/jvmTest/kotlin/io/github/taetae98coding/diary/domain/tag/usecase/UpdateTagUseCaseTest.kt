package io.github.taetae98coding.diary.domain.tag.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UpdateTagUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 으로 수정하고 기존 태그에 저장된 제목이 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
                val tagId = fixtureMonkey.giveMeOne<Uuid>()
                val storedTag =
                    fixtureMonkey
                        .giveMeKotlinBuilder<Tag>()
                        .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                        .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                        .sample()
                val detailSlot = slot<TagDetail>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
                val findTagUseCase = mockk<FindTagUseCase>()
                every { findTagUseCase(parameter = tagId) } returns flowOf(Result.success(storedTag))
                val accountTagRepository = mockk<AccountTagRepository>()
                coEvery {
                    accountTagRepository.updateDetail(account = account, tagId = tagId, detail = capture(detailSlot), updatedAt = any())
                } returns 1
                val clock = mockk<Clock>()
                every { clock.now() } returns now
                val useCase =
                    UpdateTagUseCase(
                        getAccountUseCase = getAccountUseCase,
                        findTagUseCase = findTagUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        accountTagRepository = accountTagRepository,
                        clock = clock,
                    )

                When("이모지, 설명, 컬러만 바꿔 수정한다") {
                    Then("TC-TAG-DETAIL-DOMAIN-001 기존 제목을 유지하고 이모지, 설명, 컬러는 수정한 내용으로 저장한다") {
                        val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = blankTitle)

                        val result = useCase(parameter = UpdateTagUseCase.Parameter(id = tagId, detail = detail))

                        result.shouldBeSuccess(1)
                        detailSlot.captured.title shouldBe storedTag.detail.title
                        detailSlot.captured.emoji shouldBe detail.emoji
                        detailSlot.captured.description shouldBe detail.description
                        detailSlot.captured.color shouldBe detail.color
                    }
                }
            }
        }

        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val detailSlot = slot<TagDetail>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val findTagUseCase = mockk<FindTagUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery {
                accountTagRepository.updateDetail(
                    account = account,
                    tagId = tagId,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                UpdateTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findTagUseCase = findTagUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 태그를 수정한다") {
                Then("TC-TAG-DETAIL-DATA-001 이모지, 제목, 설명, 컬러와 저장 시각으로 현재 계정의 태그를 갱신한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")

                    val result = useCase(parameter = UpdateTagUseCase.Parameter(id = tagId, detail = detail))

                    result.shouldBeSuccess(1)
                    coVerify(exactly = 1) {
                        accountTagRepository.updateDetail(account = account, tagId = tagId, detail = any(), updatedAt = any())
                    }
                    detailSlot.captured shouldBe detail
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-TAG-DETAIL-DOMAIN-004 이모지를 비우면 빈 이모지로 갱신한다") {
                    val detail =
                        fixtureMonkey
                            .giveMeOne<TagDetail>()
                            .copy(emoji = "", title = "title-${fixtureMonkey.giveMeOne<String>()}")

                    val result = useCase(parameter = UpdateTagUseCase.Parameter(id = tagId, detail = detail))

                    result.shouldBeSuccess(1)
                    detailSlot.captured.emoji shouldBe ""
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val findTagUseCase = mockk<FindTagUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                UpdateTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findTagUseCase = findTagUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 태그를 수정한다") {
                Then("계정 조회 실패를 전달하고 수정하지 않는다") {
                    val parameter =
                        UpdateTagUseCase.Parameter(
                            id = fixtureMonkey.giveMeOne<Uuid>(),
                            detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}"),
                        )

                    val result = useCase(parameter = parameter)

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) {
                        accountTagRepository.updateDetail(account = any(), tagId = any(), detail = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("태그 수정과 동기화 요청이 성공하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val findTagUseCase = mockk<FindTagUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery {
                accountTagRepository.updateDetail(account = account, tagId = tagId, detail = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                UpdateTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findTagUseCase = findTagUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 태그를 수정한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 태그 갱신 후 동기화를 한 번 요청한다") {
                    val detail = fixtureMonkey.giveMeOne<TagDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")

                    val result = useCase(parameter = UpdateTagUseCase.Parameter(id = tagId, detail = detail))

                    result.shouldBeSuccess(1)
                    coVerifyOrder {
                        accountTagRepository.updateDetail(account = account, tagId = tagId, detail = any(), updatedAt = any())
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
    }
}
