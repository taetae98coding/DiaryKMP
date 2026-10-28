@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.feature.memo.ui.contact.MemoContactInputUiState
import io.github.taetae98coding.diary.library.coroutines.flow.INPUT_IDLE_DELAY
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoAddContactViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-f6951ae2-267d-4a58-8689-c2562dd77d1f 복원 뒤 새로 만든 화면이 되살린 검색어를 알리면 입력 정지 대기 시간 뒤 그 검색어로 좁힌 목록을 보여 준다") {
            runTest(mainDispatcher) {
                val query = "Query${fixtureMonkey.giveMeOne<String>().filter(Char::isLetterOrDigit)}"
                val allList = List(2) { contact() }
                val matchedList = listOf(allList.first())
                val useCase = mockk<PageMemoSelectableContactUseCase>()
                every { useCase(parameter = "") } returns flowOf(Result.success(PagingData.from(allList)))
                every { useCase(parameter = query) } returns flowOf(Result.success(PagingData.from(matchedList)))
                val viewModel = viewModel(pageMemoSelectableContactUseCase = useCase)

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe allList

                    // 복원된 화면은 목록을 다시 열면서 되살린 검색어를 알려 준다.
                    viewModel.updateQuery(query)
                    advanceTimeBy(INPUT_IDLE_DELAY / 2)
                    expectNoEvents()

                    advanceUntilIdle()
                    flowOf(awaitItem()).asSnapshot() shouldBe matchedList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("선택한 연락처 조회에 실패하면 선택한 연락처가 없는 상태를 유지한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val viewModel = viewModel(savedContactListFlow = flowOf(Result.failure(IllegalStateException("contact error"))))

                viewModel.selectContact(id = contact.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe MemoContactInputUiState()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("연락처 선택 목록은 페이지 조회 결과를 그대로 전달한다") {
            runTest(mainDispatcher) {
                val contactList = listOf(contact(), contact())
                val viewModel = viewModel(contactPagingFlow = flowOf(Result.success(PagingData.from(contactList))))

                val itemList = flowOf(viewModel.contactPagingData.first()).asSnapshot()
                viewModel.viewModelScope.cancel()
                advanceUntilIdle()

                itemList shouldBe contactList
            }
        }

        test("연락처 선택 목록 페이지 조회에 실패해도 선택 상태는 그대로 표시한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val viewModel =
                    viewModel(
                        contactPagingFlow = flowOf(Result.failure(IllegalStateException("contact error"))),
                        savedContactListFlow = flowOf(Result.success(listOf(contact))),
                    )
                viewModel.selectContact(id = contact.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(contact)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-8bc7e7d0-9705-4bf2-ad5b-c7233fa2d634 ContactDetail 메모 탭에서 진입하면 그 연락처만 선택된 상태로 시작한다") {
            runTest(mainDispatcher) {
                val target = contact()
                val other = contact()
                val viewModel =
                    viewModel(
                        initialContactId = target.id,
                        contactPagingFlow = flowOf(Result.success(PagingData.from(listOf(target, other)))),
                        savedContactListFlow = flowOf(Result.success(listOf(target, other))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(target)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-8a446cb3-f4b7-4dde-aed0-222187c3edba ContactDetail 메모 탭에서 진입해도 초기 선택을 해제하거나 더 선택할 수 있다") {
            runTest(mainDispatcher) {
                val target = contact()
                val other = contact()
                val viewModel =
                    viewModel(
                        initialContactId = target.id,
                        savedContactListFlow = flowOf(Result.success(listOf(target, other))),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem().selectedContactList shouldBe listOf(target)

                    viewModel.unselectContact(id = target.id)
                    viewModel.selectContact(id = other.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(other)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-e3ab310f-da8b-4f95-9484-ce8580f19ba6 삭제된 대상 연락처는 선택한 것으로 보이지 않지만 선택에 남는다") {
            runTest(mainDispatcher) {
                val deletedTarget = contact()
                val viewModel =
                    viewModel(
                        initialContactId = deletedTarget.id,
                        savedContactListFlow = flowOf(Result.success(emptyList())),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
                viewModel.selectionUiState.value shouldBe MemoAddContactSelectionUiState(contactIdSet = setOf(deletedTarget.id))
            }
        }

        test("TC-0d27e1ff-ec0c-40d1-9c9b-74dd1b465ad2 진입하면 선택한 연락처가 없는 상태로 시작한다") {
            runTest(mainDispatcher) {
                val contactList = listOf(contact(), contact())
                val viewModel =
                    viewModel(
                        contactPagingFlow = flowOf(Result.success(PagingData.from(contactList))),
                        savedContactListFlow = flowOf(Result.success(contactList)),
                    )

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-d8a15eeb-4fbb-4b78-b513-cce7e71db76b TC-58e3a6fc-fec5-48dd-ae63-a332532ae678 선택한 연락처를 즉시 노출하고 해제하면 즉시 제외한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val viewModel = viewModel(savedContactListFlow = flowOf(Result.success(listOf(contact))))

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList.shouldBeEmpty()

                    viewModel.selectContact(id = contact.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(contact)

                    viewModel.unselectContact(id = contact.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-6e297fd3-569e-4470-a09c-1942b49371ae 한 번에 여러 연락처를 선택할 수 있다") {
            runTest(mainDispatcher) {
                val firstContact = contact()
                val secondContact = contact()
                val viewModel = viewModel(savedContactListFlow = flowOf(Result.success(listOf(firstContact, secondContact))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectContact(id = firstContact.id)
                    viewModel.selectContact(id = secondContact.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(firstContact, secondContact)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-d2f46b63-2ba8-4962-9a0e-816a91fcf3f6 화면 구성이 변경되어도 선택한 연락처를 유지한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val viewModel = viewModel(savedContactListFlow = flowOf(Result.success(listOf(contact))))

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectContact(id = contact.id)
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(contact)
                    cancelAndIgnoreRemainingEvents()
                }

                // 화면 구성 변경으로 UI가 재생성되어 같은 ViewModel을 새로 구독해도 선택이 유지된다.
                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(contact)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-2bef38d2-4eb0-43b3-94df-a8cfd1f5647e 선택한 연락처가 삭제되면 선택에서 제외된 것으로 표시된다") {
            runTest(mainDispatcher) {
                val remainingContact = contact()
                val removedContact = contact()
                val savedContactListFlow = MutableStateFlow(Result.success(listOf(remainingContact, removedContact)))
                val viewModel = viewModel(savedContactListFlow = savedContactListFlow)

                viewModel.selectContact(id = removedContact.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList shouldBe listOf(removedContact)

                    savedContactListFlow.value = Result.success(listOf(remainingContact))
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-9a7affe5-2910-408a-83fe-444074531be9 선택한 뒤 삭제된 연락처도 선택으로 남는다") {
            runTest(mainDispatcher) {
                val remainingContact = contact()
                val deletedContact = contact()
                val savedContactListFlow = MutableStateFlow(Result.success(listOf(remainingContact, deletedContact)))
                val viewModel = viewModel(savedContactListFlow = savedContactListFlow)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    viewModel.selectContact(id = remainingContact.id)
                    viewModel.selectContact(id = deletedContact.id)
                    advanceUntilIdle()

                    savedContactListFlow.value = Result.success(listOf(remainingContact))
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.selectionUiState.value.contactIdSet shouldBe setOf(remainingContact.id, deletedContact.id)
            }
        }

        test("TC-3c6a9cc3-e5bc-4f92-9402-cde2099232a8 자동 선택된 연락처의 선택을 해제하면 다시 선택되지 않는다") {
            runTest(mainDispatcher) {
                val addedContact = contact()
                val viewModel = viewModel(savedContactListFlow = flowOf(Result.success(listOf(addedContact))))

                // ContactAdd 화면에서 돌아와 자동 선택된 상태를 만든다.
                viewModel.selectContact(id = addedContact.id)
                viewModel.unselectContact(id = addedContact.id)

                viewModel.uiState.test {
                    advanceUntilIdle()

                    expectMostRecentItem().selectedContactList.shouldBeEmpty()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.selectionUiState.value.contactIdSet
                    .shouldBeEmpty()
            }
        }

        test("TC-5cb7fa61-7251-43d0-b0d9-b66a9afb557f 검색어가 바뀌면 새 검색어 기준으로 선택 목록을 다시 조회한다") {
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

        test("TC-d8c8b522-d00e-4be8-a134-efb66f75449d 검색어를 바꿔도 연락처 입력에 표시하는 연락처는 그대로다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val viewModel = viewModel(savedContactListFlow = flowOf(Result.success(listOf(contact))))
                viewModel.selectContact(id = contact.id)

                viewModel.uiState.test {
                    advanceUntilIdle()
                    expectMostRecentItem() shouldBe MemoContactInputUiState(selectedContactList = listOf(contact))

                    viewModel.updateQuery(SEARCH_QUERY)
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
        private const val SEARCH_QUERY = "Kim"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun contact(): Contact =
            fixtureMonkey
                .giveMeKotlinBuilder<Contact>()
                .setExp(Contact::isDeleted, false)
                .setExp(Contact::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Contact::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()

        private fun viewModel(
            initialContactId: Uuid? = null,
            contactPagingFlow: Flow<Result<PagingData<Contact>>> = emptyFlow(),
            savedContactListFlow: Flow<Result<List<Contact>>> = flowOf(Result.success(emptyList())),
            pageMemoSelectableContactUseCase: PageMemoSelectableContactUseCase =
                mockk<PageMemoSelectableContactUseCase>().apply {
                    every { this@apply(parameter = any()) } returns contactPagingFlow
                },
        ): MemoAddContactViewModel {
            // 선택한 식별자로 저장소를 조회하는 동작을 저장된 연락처 목록에서 골라내는 방식으로 대신한다.
            val getSelectedContactUseCase = mockk<GetSelectedContactUseCase>()
            every { getSelectedContactUseCase(parameter = any()) } answers {
                val contactIdSet = firstArg<Set<Uuid>>()

                if (contactIdSet.isEmpty()) {
                    flowOf(Result.success(emptyList()))
                } else {
                    savedContactListFlow.map { result -> result.map { contactList -> contactList.filter { contact -> contact.id in contactIdSet } } }
                }
            }

            return MemoAddContactViewModel(
                initialContactId = initialContactId,
                pageMemoSelectableContactUseCase = pageMemoSelectableContactUseCase,
                getSelectedContactUseCase = getSelectedContactUseCase,
            )
        }
    }
}
