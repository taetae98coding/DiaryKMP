package io.github.taetae98coding.diary.domain.tag.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.exception.TagLinkSelfException
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagLinkRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TagLinkUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountTagLinkRepository = mockk<AccountTagLinkRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("다른 태그를 향하는 연결을 만든다") {
                Then("TC-TAG-LINK-DOMAIN-001 현재 계정의 연결로 저장한다") {
                    val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddTagLinkUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountTagLinkRepository = accountTagLinkRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = fromTagId, toTagId = toTagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountTagLinkRepository.upsert(
                            account = account,
                            fromTagId = fromTagId,
                            toTagId = toTagId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-TAG-LINK-DATA-007 저장한 뒤 동기화를 한 번 요청한다") {
                    val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val syncRepository = mockk<AccountTagLinkRepository>(relaxed = true)
                    val useCase =
                        AddTagLinkUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountTagLinkRepository = syncRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = fromTagId, toTagId = toTagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        syncRepository.upsert(
                            account = account,
                            fromTagId = fromTagId,
                            toTagId = toTagId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("연결을 해제한다") {
                Then("TC-TAG-LINK-DOMAIN-011 현재 계정의 연결을 해제 상태로 저장한다") {
                    val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveTagLinkUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountTagLinkRepository = accountTagLinkRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = fromTagId, toTagId = toTagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountTagLinkRepository.upsert(
                            account = account,
                            fromTagId = fromTagId,
                            toTagId = toTagId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-TAG-LINK-DATA-007 해제를 저장한 뒤 동기화를 한 번 요청한다") {
                    val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val toTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val syncRepository = mockk<AccountTagLinkRepository>(relaxed = true)
                    val useCase =
                        RemoveTagLinkUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountTagLinkRepository = syncRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveTagLinkUseCase.Parameter(fromTagId = fromTagId, toTagId = toTagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        syncRepository.upsert(
                            account = account,
                            fromTagId = fromTagId,
                            toTagId = toTagId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("같은 태그를 출발 태그와 도착 태그로 지정해 연결을 만든다") {
                Then("TC-TAG-LINK-DOMAIN-006 실패로 끝나고 저장하지 않는다") {
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val selfLinkRepository = mockk<AccountTagLinkRepository>(relaxed = true)
                    val selfLinkSyncUseCase = mockk<RequestSyncUseCase>()
                    val useCase =
                        AddTagLinkUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = selfLinkSyncUseCase,
                            accountTagLinkRepository = selfLinkRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddTagLinkUseCase.Parameter(fromTagId = tagId, toTagId = tagId))

                    result.shouldBeFailure().shouldBeInstanceOf<TagLinkSelfException>()
                    coVerify(exactly = 0) {
                        selfLinkRepository.upsert(
                            account = any(),
                            fromTagId = any(),
                            toTagId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                    coVerify(exactly = 0) { selfLinkSyncUseCase(parameter = any()) }
                }
            }

            When("연결된 태그를 조회한다") {
                Then("TC-TAG-LINK-DOMAIN-015 현재 계정의 연결된 태그를 그대로 전달한다") {
                    val fromTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagList = List(2) { tag() }
                    every {
                        accountTagLinkRepository.getTagList(account = account, fromTagId = fromTagId)
                    } returns flowOf(tagList)
                    val useCase =
                        GetLinkedTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountTagLinkRepository = accountTagLinkRepository,
                        )

                    useCase(parameter = fromTagId).first().shouldBeSuccess() shouldBe tagList
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountTagLinkRepository = mockk<AccountTagLinkRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)

            When("연결을 만든다") {
                Then("계정 조회 실패를 그대로 전달하고 저장하지 않는다") {
                    val useCase =
                        AddTagLinkUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountTagLinkRepository = accountTagLinkRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                AddTagLinkUseCase.Parameter(
                                    fromTagId = fixtureMonkey.giveMeOne<Uuid>(),
                                    toTagId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) {
                        accountTagLinkRepository.upsert(
                            account = any(),
                            fromTagId = any(),
                            toTagId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }

            When("연결된 태그를 조회한다") {
                Then("계정 조회 실패를 그대로 전달한다") {
                    val useCase =
                        GetLinkedTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountTagLinkRepository = accountTagLinkRepository,
                        )

                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
