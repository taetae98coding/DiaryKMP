package io.github.taetae98coding.diary.domain.checklist.usecase

import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistTitleBlankException
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class AddChecklistUseCaseTest :
    BehaviorSpec({
        Given("현재 계정이 확인되고 현재 시각이 고정되어 있으며 제목, 설명과 넣어 둔 체크 항목이 있다") {
            val context = AddContext(account = fixtureMonkey.giveMeOne<Account.User>())
            val detail = ChecklistDetail(title = " 여행 준비 ", description = "여름\n휴가 ")
            val contentList = listOf(itemContent(), " 충전기 ", itemContent())

            When("체크리스트 추가를 요청한다") {
                val id = context.useCase(parameter = AddChecklistUseCase.Parameter(detail = detail, itemContentList = contentList)).shouldBeSuccess()

                Then("TC-CHECKLIST-ADD-DOMAIN-007 TC-CHECKLIST-ADD-DOMAIN-010 제목과 설명을 입력한 그대로, 미삭제 상태와 같은 생성·수정 시각으로 기록한다") {
                    context.savedChecklistList.single() shouldBe
                        Checklist(id = id, detail = detail, isDeleted = false, updatedAt = context.now, createdAt = context.now)
                }

                Then("TC-CHECKLIST-ADD-DOMAIN-008 넣어 둔 항목을 새 체크리스트에 속한 체크되지 않고 삭제되지 않은 새 항목으로 만든다") {
                    val itemList = context.savedItemListList.single()
                    itemList.map { item -> item.content } shouldBe contentList
                    itemList.forEach { item ->
                        item.checklistId shouldBe id
                        item.isChecked shouldBe false
                        item.isDeleted shouldBe false
                    }
                    itemList.map { item -> item.id }.toSet().size shouldBe contentList.size
                }

                Then("TC-CHECKLIST-ADD-DOMAIN-011 넣어 둔 순서대로 생성 시각이 늦어지고 수정 시각은 생성 시각과 같다") {
                    val itemList = context.savedItemListList.single()
                    itemList.map { item -> item.createdAt } shouldBe contentList.indices.map { index -> context.now + index.milliseconds }
                    itemList.forEach { item -> item.updatedAt shouldBe item.createdAt }
                }

                Then("TC-CHECKLIST-ADD-DOMAIN-012 체크리스트와 체크 항목을 현재 계정으로 함께 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = checkNotNull(context.account), checklist = any(), itemList = any()) }
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-CHECKLIST-ADD-DATA-004 TC-CHECKLIST-ADD-DATA-005 기기에 저장한 결과로 성공을 판단하고 동기화를 한 번 요청한다") {
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        listOf(
            itemContent() to true,
            "" to false,
            "   " to false,
        ).forEach { (leftover, isLeftoverKept) ->
            Given("넣어 둔 체크 항목 뒤에 항목 입력 '$leftover' 가 함께 전달된다") {
                val context = AddContext(account = fixtureMonkey.giveMeOne<Account.User>())
                val content = itemContent()
                val expected = if (isLeftoverKept) listOf(content, leftover) else listOf(content)

                When("체크리스트 추가를 요청한다") {
                    context
                        .useCase(
                            parameter = AddChecklistUseCase.Parameter(detail = fixtureMonkey.checklistDetail(), itemContentList = listOf(content, leftover)),
                        ).shouldBeSuccess()

                    Then("TC-CHECKLIST-ADD-DOMAIN-009 비어 있지 않은 항목 입력만 맨 끝 항목으로 만든다") {
                        context.savedItemListList.single().map { item -> item.content } shouldBe expected
                        context.savedItemListList.single().map { item -> item.createdAt } shouldBe
                            expected.indices.map { index -> context.now + index.milliseconds }
                    }
                }
            }
        }

        Given("제목만 있고 설명과 넣어 둔 체크 항목이 없다") {
            val context = AddContext(account = fixtureMonkey.giveMeOne<Account.User>())
            val detail = fixtureMonkey.checklistDetail(description = "")

            When("체크리스트 추가를 요청한다") {
                context
                    .useCase(
                        parameter = AddChecklistUseCase.Parameter(detail = detail, itemContentList = emptyList()),
                    ).shouldBeSuccess()

                Then("TC-CHECKLIST-ADD-DOMAIN-005 빈 설명의 체크리스트를 체크 항목 없이 저장한다") {
                    context.savedChecklistList.single().detail shouldBe detail
                    context.savedItemListList.single().shouldBeEmpty()
                }
            }
        }

        Given("같은 제목, 설명과 체크 항목으로 체크리스트를 이미 추가했다") {
            val context = AddContext(account = fixtureMonkey.giveMeOne<Account.User>())
            val parameter = AddChecklistUseCase.Parameter(detail = fixtureMonkey.checklistDetail(), itemContentList = listOf(itemContent(), itemContent()))
            val firstId = context.useCase(parameter = parameter).shouldBeSuccess()

            When("같은 내용으로 체크리스트 추가를 요청한다") {
                val secondId = context.useCase(parameter = parameter).shouldBeSuccess()

                Then("TC-CHECKLIST-ADD-DOMAIN-006 다른 식별 정보를 가진 새 체크리스트와 새 체크 항목으로 저장한다") {
                    secondId shouldNotBe firstId
                    context.savedItemListList[1].map { item -> item.id } shouldNotContainAnyOf context.savedItemListList[0].map { item -> item.id }
                    context.savedItemListList[1].map { item -> item.checklistId }.toSet() shouldBe setOf(secondId)
                }
            }
        }

        listOf("", "   ").forEach { title ->
            Given("제목이 '$title' 이다") {
                val context = AddContext(account = fixtureMonkey.giveMeOne<Account.User>())

                When("체크리스트 추가를 요청한다") {
                    val result =
                        context.useCase(
                            parameter = AddChecklistUseCase.Parameter(detail = fixtureMonkey.checklistDetail(title = title), itemContentList = listOf(itemContent())),
                        )

                    Then("TC-CHECKLIST-ADD-DOMAIN-004 체크리스트와 체크 항목을 저장하지 않고 제목 미입력으로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<ChecklistTitleBlankException>()
                        context.verifyNothingSaved()
                        coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val context = AddContext(account = null, accountFailure = throwable)

            When("체크리스트 추가를 요청한다") {
                val result = context.useCase(parameter = AddChecklistUseCase.Parameter(detail = fixtureMonkey.checklistDetail(), itemContentList = listOf(itemContent())))

                Then("TC-CHECKLIST-ADD-DOMAIN-013 체크리스트와 체크 항목을 저장하지 않고 실패를 전달한다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    context.verifyNothingSaved()
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = AddContext(account = fixtureMonkey.giveMeOne<Account.Guest>())

            When("체크리스트 추가를 요청한다") {
                context.useCase(parameter = AddChecklistUseCase.Parameter(detail = fixtureMonkey.checklistDetail(), itemContentList = listOf(itemContent()))).shouldBeSuccess()

                Then("TC-CHECKLIST-ADD-DATA-006 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = checkNotNull(context.account), checklist = any(), itemList = any()) }
                }
            }
        }

        Given("기기 저장이 실패하도록 설정되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val context = AddContext(account = fixtureMonkey.giveMeOne<Account.User>())
            coEvery { context.repository.upsert(account = any(), checklist = any(), itemList = any()) } throws throwable

            When("체크리스트 추가를 요청한다") {
                val result = context.useCase(parameter = AddChecklistUseCase.Parameter(detail = fixtureMonkey.checklistDetail(), itemContentList = listOf(itemContent())))

                Then("TC-CHECKLIST-ADD-DATA-002 추가를 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }
    })

private class AddContext(
    val account: Account?,
    accountFailure: Throwable? = null,
) {
    val now: Instant = fixtureMonkey.giveMeOne()
    val requestSyncUseCase: RequestSyncUseCase = requestSyncUseCase()
    val repository: AccountChecklistRepository = mockk()
    val savedChecklistList = mutableListOf<Checklist>()
    val savedItemListList = mutableListOf<List<ChecklistItem>>()
    val useCase: AddChecklistUseCase =
        AddChecklistUseCase(
            getAccountUseCase =
                if (account != null) getAccountUseCase(account = account) else failingGetAccountUseCase(throwable = checkNotNull(accountFailure)),
            requestSyncUseCase = requestSyncUseCase,
            accountChecklistRepository = repository,
            clock = clock(now = now),
        )

    init {
        coEvery { repository.upsert(account = any(), checklist = capture(savedChecklistList), itemList = capture(savedItemListList)) } just Runs
    }

    fun verifyNothingSaved() {
        coVerify(exactly = 0) { repository.upsert(account = any(), checklist = any(), itemList = any()) }
    }
}
