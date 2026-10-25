package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebTagRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class WebTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountWebTagRepository = mockk<AccountWebTagRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("웹 항목에 태그를 연결한다") {
                Then("TC-db74bb38-a1d5-445f-9731-1a4585cc47ba 현재 계정의 연결로 저장한다") {
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountWebTagRepository = accountWebTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddWebTagUseCase.Parameter(webId = webId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountWebTagRepository.upsert(
                            account = account,
                            webId = webId,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-66a6f319-4748-437b-8fb2-4b65ec0d5c0a TC-51ef66dc-6d05-47c9-9de3-0344fa78c83d 저장한 뒤 동기화를 한 번 요청한다") {
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val syncRepository = mockk<AccountWebTagRepository>(relaxed = true)
                    val useCase =
                        AddWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountWebTagRepository = syncRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddWebTagUseCase.Parameter(webId = webId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        syncRepository.upsert(
                            account = account,
                            webId = webId,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("연결을 해제한다") {
                Then("TC-4103b438-d654-41af-9549-c615fb7aba9b 현재 계정의 연결을 해제 상태로 저장한다") {
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountWebTagRepository = accountWebTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveWebTagUseCase.Parameter(webId = webId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountWebTagRepository.upsert(
                            account = account,
                            webId = webId,
                            tagId = tagId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-66a6f319-4748-437b-8fb2-4b65ec0d5c0a TC-51ef66dc-6d05-47c9-9de3-0344fa78c83d 해제를 저장한 뒤 동기화를 한 번 요청한다") {
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val syncRepository = mockk<AccountWebTagRepository>(relaxed = true)
                    val useCase =
                        RemoveWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountWebTagRepository = syncRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveWebTagUseCase.Parameter(webId = webId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        syncRepository.upsert(
                            account = account,
                            webId = webId,
                            tagId = tagId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("연결된 태그를 조회한다") {
                Then("TC-0bc7baaf-dfbd-4bdc-81bb-8a77b3d1fab9 현재 계정의 연결된 태그를 그대로 전달한다") {
                    val webId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagList = List(2) { tag() }
                    every {
                        accountWebTagRepository.getTagList(account = account, webId = webId)
                    } returns flowOf(tagList)
                    val useCase =
                        GetWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountWebTagRepository = accountWebTagRepository,
                        )

                    useCase(parameter = webId).first().shouldBeSuccess() shouldBe tagList
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountWebTagRepository = mockk<AccountWebTagRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)

            When("연결을 만든다") {
                Then("계정 조회 실패를 그대로 전달하고 저장하지 않는다") {
                    val useCase =
                        AddWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountWebTagRepository = accountWebTagRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                AddWebTagUseCase.Parameter(
                                    webId = fixtureMonkey.giveMeOne<Uuid>(),
                                    tagId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) {
                        accountWebTagRepository.upsert(
                            account = any(),
                            webId = any(),
                            tagId = any(),
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
                        GetWebTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountWebTagRepository = accountWebTagRepository,
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
                .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
