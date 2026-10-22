package io.github.taetae98coding.diary.domain.place.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceTagRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
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

class PlaceTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountPlaceTagRepository = mockk<AccountPlaceTagRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("장소에 태그를 연결한다") {
                Then("TC-36040314-b2fa-4cd6-9b33-b1d5710bd002 현재 계정의 연결로 저장한다") {
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddPlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountPlaceTagRepository = accountPlaceTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddPlaceTagUseCase.Parameter(placeId = placeId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountPlaceTagRepository.upsert(
                            account = account,
                            placeId = placeId,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-b20c9c1d-25d9-4cb4-b8fb-82207b8e9772 TC-ac9a2e63-4625-45e5-ac14-107889410d99 저장한 뒤 동기화를 한 번 요청한다") {
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val syncRepository = mockk<AccountPlaceTagRepository>(relaxed = true)
                    val useCase =
                        AddPlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountPlaceTagRepository = syncRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddPlaceTagUseCase.Parameter(placeId = placeId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        syncRepository.upsert(
                            account = account,
                            placeId = placeId,
                            tagId = tagId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("연결을 해제한다") {
                Then("TC-f380865f-40ed-4b8f-adea-05aafc8149b9 현재 계정의 연결을 해제 상태로 저장한다") {
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemovePlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountPlaceTagRepository = accountPlaceTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemovePlaceTagUseCase.Parameter(placeId = placeId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountPlaceTagRepository.upsert(
                            account = account,
                            placeId = placeId,
                            tagId = tagId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-b20c9c1d-25d9-4cb4-b8fb-82207b8e9772 TC-ac9a2e63-4625-45e5-ac14-107889410d99 해제를 저장한 뒤 동기화를 한 번 요청한다") {
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val syncRepository = mockk<AccountPlaceTagRepository>(relaxed = true)
                    val useCase =
                        RemovePlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountPlaceTagRepository = syncRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemovePlaceTagUseCase.Parameter(placeId = placeId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        syncRepository.upsert(
                            account = account,
                            placeId = placeId,
                            tagId = tagId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("연결된 태그를 조회한다") {
                Then("TC-ed9a3af3-c21a-417a-bb26-63211e4875c2 현재 계정의 연결된 태그를 그대로 전달한다") {
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagList = List(2) { tag() }
                    every {
                        accountPlaceTagRepository.getTagList(account = account, placeId = placeId)
                    } returns flowOf(tagList)
                    val useCase =
                        GetPlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountPlaceTagRepository = accountPlaceTagRepository,
                        )

                    useCase(parameter = placeId).first().shouldBeSuccess() shouldBe tagList
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountPlaceTagRepository = mockk<AccountPlaceTagRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)

            When("연결을 만든다") {
                Then("계정 조회 실패를 그대로 전달하고 저장하지 않는다") {
                    val useCase =
                        AddPlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountPlaceTagRepository = accountPlaceTagRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                AddPlaceTagUseCase.Parameter(
                                    placeId = fixtureMonkey.giveMeOne<Uuid>(),
                                    tagId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) {
                        accountPlaceTagRepository.upsert(
                            account = any(),
                            placeId = any(),
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
                        GetPlaceTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountPlaceTagRepository = accountPlaceTagRepository,
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
