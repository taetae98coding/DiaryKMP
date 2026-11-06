package io.github.taetae98coding.diary.domain.tag.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.repository.AccountEntityTagRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock

// 종류마다 연결 저장·해제·조회를 같은 방식으로 다루고, 종류별 테스트 케이스 ID를 함께 둔다.
private data class EntityTagKindCase(
    val kind: EntityTagKind,
    val addTestCase: String,
    val removeTestCase: String,
    val syncTestCase: String,
    val getTestCase: String,
)

private val kindCaseList: List<EntityTagKindCase> =
    listOf(
        EntityTagKindCase(
            kind = EntityTagKind.WEB,
            addTestCase = "TC-db74bb38-a1d5-445f-9731-1a4585cc47ba",
            removeTestCase = "TC-4103b438-d654-41af-9549-c615fb7aba9b",
            syncTestCase = "TC-66a6f319-4748-437b-8fb2-4b65ec0d5c0a TC-51ef66dc-6d05-47c9-9de3-0344fa78c83d",
            getTestCase = "TC-0bc7baaf-dfbd-4bdc-81bb-8a77b3d1fab9",
        ),
        EntityTagKindCase(
            kind = EntityTagKind.PLACE,
            addTestCase = "TC-36040314-b2fa-4cd6-9b33-b1d5710bd002",
            removeTestCase = "TC-f380865f-40ed-4b8f-adea-05aafc8149b9",
            syncTestCase = "TC-b20c9c1d-25d9-4cb4-b8fb-82207b8e9772 TC-ac9a2e63-4625-45e5-ac14-107889410d99",
            getTestCase = "TC-ed9a3af3-c21a-417a-bb26-63211e4875c2",
        ),
        EntityTagKindCase(
            kind = EntityTagKind.CONTACT,
            addTestCase = "TC-7f6c44b9-1420-40bd-9fdc-57039b498d7d",
            removeTestCase = "TC-53ae5a58-14d2-4f12-9ca3-8eebe89ab6a9",
            syncTestCase = "TC-3e01120a-cc59-47fc-b540-e2acb8615781 TC-a8b8040e-c2f7-4839-876f-adb4181bfd80",
            getTestCase = "TC-27e308e2-1ca4-441a-9a0d-e8e142c9d5d3",
        ),
        EntityTagKindCase(
            kind = EntityTagKind.CHECKLIST,
            addTestCase = "TC-e6c08b62-d5cc-48b8-bf1b-3ddb529bf7fe",
            removeTestCase = "TC-c78affc9-cb70-4ddd-a9db-c9dee7f33e4f",
            syncTestCase = "TC-09652bea-8b0f-4d7e-8b6c-071cae7909ea TC-85998874-2154-4e91-84eb-05505b1c614a",
            getTestCase = "TC-d64ceb6e-9e7d-4b92-8df2-b556f9ec210a",
        ),
        EntityTagKindCase(
            kind = EntityTagKind.MUSIC,
            addTestCase = "TC-5f579e29-1b7d-426d-ae70-cbb8b9fcb481",
            removeTestCase = "TC-2fc7a25c-6d14-40ec-a22f-c130ec4af82f",
            syncTestCase = "TC-4360702d-1b39-44d2-bce2-cc561079726a TC-4a1bbe60-f9c6-4abb-a399-d283168ba3e0",
            getTestCase = "TC-92ad3a86-0168-4e77-815e-6749e160b070",
        ),
        EntityTagKindCase(
            kind = EntityTagKind.QR,
            addTestCase = "TC-05690781-039c-44d0-ab0a-ae300b6860cc",
            removeTestCase = "TC-575d6b08-b05c-421f-94b1-83a5195114c2",
            syncTestCase = "TC-1577840b-df73-4e2e-b0ee-ff80bda2b107 TC-9432f64b-4f31-468e-a411-6b4e3cfdac1f",
            getTestCase = "TC-1e4a39ac-0082-48a4-b07c-60031d3e426b",
        ),
    )

private const val SYNC_PROGRESS_TEST_CASE = "TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c"

class EntityTagUseCaseTest :
    BehaviorSpec({
        Given("테스트할 종류 목록이 준비되어 있다") {
            When("목록을 확인한다") {
                Then("모든 종류를 한 번씩 담는다") {
                    kindCaseList.map { kindCase -> kindCase.kind }.toSet() shouldBe EntityTagKind.entries.toSet()
                    kindCaseList.size shouldBe EntityTagKind.entries.size
                }
            }
        }

        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = Arb.accountUser().next()
            val now = Arb.secondPrecisionInstant().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            kindCaseList.forEach { kindCase ->
                val kind = kindCase.kind

                When("${kind.name} 항목에 태그를 연결한다") {
                    Then("${kindCase.addTestCase} 현재 계정의 연결로 저장한다") {
                        val entityId = Arb.kotlinUuid().next()
                        val tagId = Arb.kotlinUuid().next()
                        val repository = mockk<AccountEntityTagRepository>(relaxed = true)
                        val requestSyncUseCase = requestSyncUseCase()
                        val useCase = AddEntityTagUseCase(getAccountUseCase, requestSyncUseCase, repository, clock)

                        val result = useCase(parameter = AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId))

                        result.shouldBeSuccess()
                        coVerify(exactly = 1) {
                            repository.upsert(
                                account = account,
                                kind = kind,
                                entityId = entityId,
                                tagId = tagId,
                                isDeleted = false,
                                updatedAt = now,
                            )
                        }
                    }

                    Then("$SYNC_PROGRESS_TEST_CASE ${kindCase.syncTestCase} 저장한 뒤 동기화를 한 번 요청한다") {
                        val entityId = Arb.kotlinUuid().next()
                        val tagId = Arb.kotlinUuid().next()
                        val repository = mockk<AccountEntityTagRepository>(relaxed = true)
                        val requestSyncUseCase = requestSyncUseCase()
                        val useCase = AddEntityTagUseCase(getAccountUseCase, requestSyncUseCase, repository, clock)

                        useCase(parameter = AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId)).shouldBeSuccess()

                        coVerifyOrder {
                            repository.upsert(
                                account = account,
                                kind = kind,
                                entityId = entityId,
                                tagId = tagId,
                                isDeleted = false,
                                updatedAt = now,
                            )
                            requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { requestSyncUseCase(parameter = any()) }
                    }
                }

                When("${kind.name} 항목과 태그의 연결을 해제한다") {
                    Then("${kindCase.removeTestCase} 현재 계정의 연결을 해제 상태로 저장한다") {
                        val entityId = Arb.kotlinUuid().next()
                        val tagId = Arb.kotlinUuid().next()
                        val repository = mockk<AccountEntityTagRepository>(relaxed = true)
                        val useCase = RemoveEntityTagUseCase(getAccountUseCase, requestSyncUseCase(), repository, clock)

                        val result = useCase(parameter = RemoveEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId))

                        result.shouldBeSuccess()
                        coVerify(exactly = 1) {
                            repository.upsert(
                                account = account,
                                kind = kind,
                                entityId = entityId,
                                tagId = tagId,
                                isDeleted = true,
                                updatedAt = now,
                            )
                        }
                    }

                    Then("$SYNC_PROGRESS_TEST_CASE ${kindCase.syncTestCase} 해제를 저장한 뒤 동기화를 한 번 요청한다") {
                        val entityId = Arb.kotlinUuid().next()
                        val tagId = Arb.kotlinUuid().next()
                        val repository = mockk<AccountEntityTagRepository>(relaxed = true)
                        val requestSyncUseCase = requestSyncUseCase()
                        val useCase = RemoveEntityTagUseCase(getAccountUseCase, requestSyncUseCase, repository, clock)

                        useCase(parameter = RemoveEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId)).shouldBeSuccess()

                        coVerifyOrder {
                            repository.upsert(
                                account = account,
                                kind = kind,
                                entityId = entityId,
                                tagId = tagId,
                                isDeleted = true,
                                updatedAt = now,
                            )
                            requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { requestSyncUseCase(parameter = any()) }
                    }
                }

                When("${kind.name} 항목에 연결된 태그를 조회한다") {
                    Then("${kindCase.getTestCase} 현재 계정의 연결된 태그를 그대로 전달한다") {
                        val entityId = Arb.kotlinUuid().next()
                        val tagList = List(2) { tag() }
                        val repository = mockk<AccountEntityTagRepository>()
                        every { repository.getTagList(account = account, kind = kind, entityId = entityId) } returns flowOf(tagList)
                        val useCase = GetEntityTagUseCase(getAccountUseCase = getAccountUseCase, accountEntityTagRepository = repository)

                        useCase(parameter = GetEntityTagUseCase.Parameter(kind = kind, entityId = entityId)).first().shouldBeSuccess() shouldBe tagList
                    }
                }

                When("${kind.name} 항목에 연결할 수 있는 태그를 검색한다") {
                    Then("검색어 앞뒤 공백을 지우고 종류와 항목으로 조회한다") {
                        val entityId = Arb.kotlinUuid().next()
                        val pagingData = PagingData.empty<Tag>()
                        val repository = mockk<AccountEntityTagRepository>()
                        every {
                            repository.pageSelectableTag(account = account, kind = kind, entityId = entityId, query = "태그")
                        } returns flowOf(pagingData)
                        val useCase =
                            PageEntityTagSelectableTagUseCase(getAccountUseCase = getAccountUseCase, accountEntityTagRepository = repository)

                        val result =
                            useCase(parameter = PageEntityTagSelectableTagUseCase.Parameter(kind = kind, entityId = entityId, query = "  태그 "))
                                .first()

                        result.shouldBeSuccess() shouldBeSameInstanceAs pagingData
                        verify(exactly = 1) {
                            repository.pageSelectableTag(account = account, kind = kind, entityId = entityId, query = "태그")
                        }
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val clock = mockk<Clock>(relaxed = true)

            When("연결을 만든다") {
                Then("계정 조회 실패를 그대로 전달하고 저장하지 않는다") {
                    val repository = mockk<AccountEntityTagRepository>(relaxed = true)
                    val requestSyncUseCase = mockk<RequestSyncUseCase>()
                    val useCase = AddEntityTagUseCase(getAccountUseCase, requestSyncUseCase, repository, clock)

                    val result =
                        useCase(
                            parameter =
                                AddEntityTagUseCase.Parameter(
                                    kind = EntityTagKind.WEB,
                                    entityId = Arb.kotlinUuid().next(),
                                    tagId = Arb.kotlinUuid().next(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { repository.upsert(any(), any(), any(), any(), any(), any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }

            When("연결된 태그를 조회한다") {
                Then("계정 조회 실패를 그대로 전달한다") {
                    val useCase = GetEntityTagUseCase(getAccountUseCase = getAccountUseCase, accountEntityTagRepository = mockk())

                    useCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.PLACE, entityId = Arb.kotlinUuid().next()))
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private fun requestSyncUseCase(): RequestSyncUseCase =
            mockk<RequestSyncUseCase>().apply {
                coEvery { this@apply(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            }

        private fun tag(): Tag =
            Arb
                .tag()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
    }
}
