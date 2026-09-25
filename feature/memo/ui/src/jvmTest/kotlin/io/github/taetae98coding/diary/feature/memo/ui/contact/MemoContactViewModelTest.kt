@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetMemoContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RemoveMemoContactUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoContactViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MEMO-DETAIL-FEATURE-066 연락처 입력에 저장된 연락처 연결이 표시된다") {
            runTest(mainDispatcher) {
                val connectedContact = contact()
                val otherContact = contact()
                val viewModel =
                    viewModel(
                        contactPagingFlow = flowOf(Result.success(PagingData.from(listOf(connectedContact, otherContact)))),
                        memoContactFlow = flowOf(Result.success(listOf(connectedContact))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(connectedContact))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-067 다른 경로로 저장된 연락처 연결이 바뀌면 연락처 입력에 반영된다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val memoContactFlow = MutableStateFlow(Result.success(emptyList<Contact>()))
                val viewModel =
                    viewModel(
                        contactPagingFlow = flowOf(Result.success(PagingData.from(listOf(contact)))),
                        memoContactFlow = memoContactFlow,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    // 연결된 연락처가 없는 조회 결과는 초기 상태와 같으므로 새 항목이 방출되지 않는다.
                    viewModel.uiState.value shouldBe MemoContactInputUiState(selectedContactList = emptyList())

                    memoContactFlow.value = Result.success(listOf(contact))
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(contact))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-068 연락처 연결 변경에 실패하면 별도 안내 없이 저장된 연결을 그대로 표시한다") {
            runTest(mainDispatcher) {
                val connectedContact = contact()
                val otherContact = contact()
                val addMemoContactUseCase = mockk<AddMemoContactUseCase>()
                coEvery { addMemoContactUseCase(any<AddMemoContactUseCase.Parameter>()) } returns Result.failure(IllegalStateException("save error"))
                val removeMemoContactUseCase = mockk<RemoveMemoContactUseCase>()
                coEvery { removeMemoContactUseCase(any<RemoveMemoContactUseCase.Parameter>()) } returns Result.failure(IllegalStateException("save error"))
                val viewModel =
                    viewModel(
                        contactPagingFlow = flowOf(Result.success(PagingData.from(listOf(connectedContact, otherContact)))),
                        memoContactFlow = flowOf(Result.success(listOf(connectedContact))),
                        addMemoContactUseCase = addMemoContactUseCase,
                        removeMemoContactUseCase = removeMemoContactUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    val savedUiState = expectMostRecentItem()
                    savedUiState shouldBe MemoContactInputUiState(selectedContactList = listOf(connectedContact))

                    viewModel.selectContact(contactId = otherContact.id)
                    viewModel.unselectContact(contactId = connectedContact.id)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe savedUiState
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-006 삭제된 연락처는 선택한 것으로 표시하지 않는다") {
            runTest(mainDispatcher) {
                val deletedContact = contact()
                val memoContactFlow = MutableStateFlow(Result.success(listOf(deletedContact)))
                val viewModel = viewModel(memoContactFlow = memoContactFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(deletedContact))

                    memoContactFlow.value = Result.success(emptyList())
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = emptyList())
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-007 연락처가 복구되면 유지되어 있던 선택이 다시 나타난다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val memoContactFlow = MutableStateFlow(Result.success(emptyList<Contact>()))
                val viewModel = viewModel(memoContactFlow = memoContactFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe MemoContactInputUiState(selectedContactList = emptyList())

                    memoContactFlow.value = Result.success(listOf(contact))
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(contact))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-DATA-002 TC-MEMO-CONTACT-INPUT-DOMAIN-008 선택 목록이 준비되지 않아도 선택한 연락처를 표시한다") {
            runTest(mainDispatcher) {
                val connectedContact = contact()
                val viewModel =
                    viewModel(
                        contactPagingFlow = emptyFlow(),
                        memoContactFlow = flowOf(Result.success(listOf(connectedContact))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(connectedContact))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("연락처 선택 목록 조회 결과를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val contactList = List(2) { contact() }
                val viewModel = viewModel(contactPagingFlow = flowOf(Result.success(PagingData.from(contactList))))

                val itemList = flowOf(viewModel.contactPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe contactList
            }
        }

        test("TC-MEMO-CONTACT-INPUT-FEATURE-020 연락처 선택 목록 조회에 실패하면 선택 상태만 그대로 표시한다") {
            runTest(mainDispatcher) {
                val connectedContact = contact()
                val viewModel =
                    viewModel(
                        contactPagingFlow = flowOf(Result.failure(IllegalStateException("contact error"))),
                        memoContactFlow = flowOf(Result.success(listOf(connectedContact))),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(connectedContact))
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("저장된 연락처 연결 조회에 실패하면 선택한 연락처가 없는 상태를 유지한다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(memoContactFlow = flowOf(Result.failure(IllegalStateException("memo contact error"))))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()

                    viewModel.uiState.value.selectedContactList
                        .shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-FEATURE-011 연락처를 선택하면 그 메모와 연락처의 연결 추가를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val contactId = fixtureMonkey.giveMeOne<Uuid>()
                val addMemoContactUseCase = mockk<AddMemoContactUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, addMemoContactUseCase = addMemoContactUseCase)

                viewModel.selectContact(contactId = contactId)
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoContactUseCase(AddMemoContactUseCase.Parameter(memoId = id, contactId = contactId)) }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-FEATURE-012 연락처 선택을 해제하면 그 메모와 연락처의 연결 제거를 요청한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val contactId = fixtureMonkey.giveMeOne<Uuid>()
                val removeMemoContactUseCase = mockk<RemoveMemoContactUseCase>(relaxed = true)
                val viewModel = viewModel(id = id, removeMemoContactUseCase = removeMemoContactUseCase)

                viewModel.unselectContact(contactId = contactId)
                advanceUntilIdle()

                coVerify(exactly = 1) { removeMemoContactUseCase(RemoveMemoContactUseCase.Parameter(memoId = id, contactId = contactId)) }
            }
        }

        test("TC-MEMO-DETAIL-DOMAIN-014 앞선 연락처 연결 변경이 처리 중이어도 뒤이은 연락처 연결 변경을 처리한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val firstContactId = fixtureMonkey.giveMeOne<Uuid>()
                val secondContactId = fixtureMonkey.giveMeOne<Uuid>()
                val completion = CompletableDeferred<Result<Unit>>()
                val addMemoContactUseCase = mockk<AddMemoContactUseCase>()
                coEvery { addMemoContactUseCase(any()) } coAnswers { completion.await() }
                val viewModel = viewModel(id = id, addMemoContactUseCase = addMemoContactUseCase)

                viewModel.selectContact(contactId = firstContactId)
                runCurrent()
                viewModel.selectContact(contactId = secondContactId)
                runCurrent()
                completion.complete(Result.success(Unit))
                advanceUntilIdle()

                coVerify(exactly = 1) { addMemoContactUseCase(AddMemoContactUseCase.Parameter(memoId = id, contactId = firstContactId)) }
                coVerify(exactly = 1) { addMemoContactUseCase(AddMemoContactUseCase.Parameter(memoId = id, contactId = secondContactId)) }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-DATA-004 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val allContactList = List(2) { contact() }
                val matchedContactList = listOf(allContactList.first())
                val pageMemoSelectableContactUseCase = mockk<PageMemoSelectableContactUseCase>()
                every { pageMemoSelectableContactUseCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allContactList)))
                every { pageMemoSelectableContactUseCase(parameter = SEARCH_QUERY) } returns flowOf(Result.success(PagingData.from(matchedContactList)))
                val viewModel = viewModel(pageMemoSelectableContactUseCase = pageMemoSelectableContactUseCase)

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allContactList

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    flowOf(awaitItem()).asSnapshot() shouldBe matchedContactList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-MEMO-CONTACT-INPUT-DOMAIN-012 검색어를 바꿔도 연락처 입력에 표시하는 연락처는 그대로다") {
            runTest(mainDispatcher) {
                val connectedContactList = List(2) { contact() }
                val viewModel = viewModel(memoContactFlow = flowOf(Result.success(connectedContactList)))

                viewModel.uiState.test {
                    awaitItem() shouldBe MemoContactInputUiState()
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = connectedContactList)

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    public companion object {
        private const val SEARCH_QUERY = "Wiki"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun contact(): Contact =
            fixtureMonkey
                .giveMeKotlinBuilder<Contact>()
                .setExp(Contact::isDeleted, false)
                .setExp(Contact::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Contact::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()

        private fun viewModel(
            id: Uuid = fixtureMonkey.giveMeOne<Uuid>(),
            contactPagingFlow: Flow<Result<PagingData<Contact>>> = emptyFlow(),
            memoContactFlow: Flow<Result<List<Contact>>> = emptyFlow(),
            addMemoContactUseCase: AddMemoContactUseCase = mockk(relaxed = true),
            removeMemoContactUseCase: RemoveMemoContactUseCase = mockk(relaxed = true),
            pageMemoSelectableContactUseCase: PageMemoSelectableContactUseCase =
                mockk<PageMemoSelectableContactUseCase>().apply {
                    every { this@apply(parameter = any()) } returns contactPagingFlow
                },
            isListOpened: Boolean = true,
        ): MemoContactViewModel {
            val getMemoContactUseCase = mockk<GetMemoContactUseCase>()
            every { getMemoContactUseCase(any()) } returns memoContactFlow

            return MemoContactViewModel(
                id = id,
                pageMemoSelectableContactUseCase = pageMemoSelectableContactUseCase,
                getMemoContactUseCase = getMemoContactUseCase,
                addMemoContactUseCase = addMemoContactUseCase,
                removeMemoContactUseCase = removeMemoContactUseCase,
            ).apply {
                // 화면은 선택 목록을 열 때 검색어를 알려 주므로, 목록이 열린 상태를 만든다.
                if (isListOpened) updateQuery(query = "")
            }
        }
    }
}
