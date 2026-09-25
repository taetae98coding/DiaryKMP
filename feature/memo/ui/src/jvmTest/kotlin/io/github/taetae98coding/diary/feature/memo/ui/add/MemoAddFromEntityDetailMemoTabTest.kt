@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.add

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.testing.place.place
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.domain.contact.usecase.GetSelectedContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.AddMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableContactUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageMemoSelectableWebUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetSelectedPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetSelectedTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.domain.web.usecase.GetSelectedWebUseCase
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class MemoAddFromEntityDetailMemoTabTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        placeTests()
        webTests()
        contactTests()
    }

    private fun placeTests() {
        test("TC-PLACE-DETAIL-MEMO-FEATURE-010 대상 장소를 장소 카드에 선택하고 시작하며 저장 전까지 해제하거나 더 선택할 수 있다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.place(isDeleted = false)
                val other = fixtureMonkey.place(isDeleted = false)
                val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialPlaceId = target.id), placeList = listOf(target, other))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.place.uiState.value.selectedPlaceList shouldBe listOf(target)
                memoAdd.tag.uiState.value.selectedTagList
                    .shouldBeEmpty()
                memoAdd.web.uiState.value.selectedWebList
                    .shouldBeEmpty()
                memoAdd.contact.uiState.value.selectedContactList
                    .shouldBeEmpty()

                memoAdd.place.selectPlace(id = other.id)
                advanceUntilIdle()
                memoAdd.place.uiState.value.selectedPlaceList shouldContainExactlyInAnyOrder listOf(target, other)

                memoAdd.place.unselectPlace(id = target.id)
                advanceUntilIdle()
                memoAdd.place.uiState.value.selectedPlaceList shouldBe listOf(other)
            }
        }

        test("TC-PLACE-DETAIL-MEMO-FEATURE-011 삭제된 대상 장소는 장소 카드에 선택된 것으로 표시하지 않는다") {
            runTest(mainDispatcher) {
                val deletedTarget = fixtureMonkey.place(isDeleted = false)
                // 삭제된 장소는 선택한 장소로 조회되지 않는다.
                val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialPlaceId = deletedTarget.id), placeList = listOf(fixtureMonkey.place(isDeleted = false)))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.place.uiState.value.selectedPlaceList
                    .shouldBeEmpty()
            }
        }

        test("TC-PLACE-DETAIL-MEMO-DOMAIN-002 대상 장소 하나만 선택하고 태그·웹·연락처·기간과 대표 태그는 없이 시작한다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.place(isDeleted = false)
                val key = MemoAddNavKey(initialPlaceId = target.id)
                val memoAdd = MemoAddSelection(key = key, placeList = listOf(target))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.assertOnlyTargetSelected(key = key)
                memoAdd.place.placeIdSet.value shouldBe setOf(target.id)
            }
        }

        test("TC-PLACE-DETAIL-MEMO-DATA-005 저장 시점의 장소 선택을 그대로 저장 요청에 담는다") {
            runTest(mainDispatcher) {
                val placeA = fixtureMonkey.place(isDeleted = false)
                val placeB = fixtureMonkey.place(isDeleted = false)
                val finalSelectionList = listOf(setOf(placeA.id), setOf(placeA.id, placeB.id), setOf(placeB.id), emptySet())

                finalSelectionList.forEach { finalSelection ->
                    val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialPlaceId = placeA.id), placeList = listOf(placeA, placeB))
                    (finalSelection - placeA.id).forEach { id -> memoAdd.place.selectPlace(id = id) }
                    if (placeA.id !in finalSelection) memoAdd.place.unselectPlace(id = placeA.id)

                    memoAdd.save()
                    advanceUntilIdle()

                    coVerify(exactly = 1) { memoAdd.addMemoUseCase(match<AddMemoUseCase.Parameter> { parameter -> parameter.placeIdSet == finalSelection }) }
                }
            }
        }
    }

    private fun webTests() {
        test("TC-WEB-DETAIL-MEMO-FEATURE-010 대상 웹 항목을 웹 입력에 선택하고 시작하며 저장 전까지 해제하거나 더 선택할 수 있다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.web(isDeleted = false)
                val other = fixtureMonkey.web(isDeleted = false)
                val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialWebId = target.id), webList = listOf(target, other))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.web.uiState.value.selectedWebList shouldBe listOf(target)
                memoAdd.tag.uiState.value.selectedTagList
                    .shouldBeEmpty()
                memoAdd.contact.uiState.value.selectedContactList
                    .shouldBeEmpty()
                memoAdd.place.uiState.value.selectedPlaceList
                    .shouldBeEmpty()

                memoAdd.web.selectWeb(id = other.id)
                advanceUntilIdle()
                memoAdd.web.uiState.value.selectedWebList shouldContainExactlyInAnyOrder listOf(target, other)

                memoAdd.web.unselectWeb(id = target.id)
                advanceUntilIdle()
                memoAdd.web.uiState.value.selectedWebList shouldBe listOf(other)
            }
        }

        test("TC-WEB-DETAIL-MEMO-FEATURE-011 삭제된 대상 웹 항목은 웹 입력에 선택된 것으로 표시하지 않는다") {
            runTest(mainDispatcher) {
                val deletedTarget = fixtureMonkey.web(isDeleted = false)
                val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialWebId = deletedTarget.id), webList = listOf(fixtureMonkey.web(isDeleted = false)))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.web.uiState.value.selectedWebList
                    .shouldBeEmpty()
            }
        }

        test("TC-WEB-DETAIL-MEMO-DOMAIN-002 대상 웹 항목 하나만 선택하고 태그·연락처·장소·기간과 대표 태그는 없이 시작한다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.web(isDeleted = false)
                val key = MemoAddNavKey(initialWebId = target.id)
                val memoAdd = MemoAddSelection(key = key, webList = listOf(target))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.assertOnlyTargetSelected(key = key)
                memoAdd.web.webIdSet.value shouldBe setOf(target.id)
            }
        }

        test("TC-WEB-DETAIL-MEMO-DATA-005 저장 시점의 웹 항목 선택을 그대로 저장 요청에 담는다") {
            runTest(mainDispatcher) {
                val webA = fixtureMonkey.web(isDeleted = false)
                val webB = fixtureMonkey.web(isDeleted = false)
                val finalSelectionList = listOf(setOf(webA.id), setOf(webA.id, webB.id), setOf(webB.id), emptySet())

                finalSelectionList.forEach { finalSelection ->
                    val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialWebId = webA.id), webList = listOf(webA, webB))
                    (finalSelection - webA.id).forEach { id -> memoAdd.web.selectWeb(id = id) }
                    if (webA.id !in finalSelection) memoAdd.web.unselectWeb(id = webA.id)

                    memoAdd.save()
                    advanceUntilIdle()

                    coVerify(exactly = 1) { memoAdd.addMemoUseCase(match<AddMemoUseCase.Parameter> { parameter -> parameter.webIdSet == finalSelection }) }
                }
            }
        }
    }

    private fun contactTests() {
        test("TC-CONTACT-DETAIL-MEMO-FEATURE-010 대상 연락처를 연락처 입력에 선택하고 시작하며 저장 전까지 해제하거나 더 선택할 수 있다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)
                val other = fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)
                val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialContactId = target.id), contactList = listOf(target, other))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.contact.uiState.value.selectedContactList shouldBe listOf(target)
                memoAdd.tag.uiState.value.selectedTagList
                    .shouldBeEmpty()
                memoAdd.web.uiState.value.selectedWebList
                    .shouldBeEmpty()
                memoAdd.place.uiState.value.selectedPlaceList
                    .shouldBeEmpty()

                memoAdd.contact.selectContact(id = other.id)
                advanceUntilIdle()
                memoAdd.contact.uiState.value.selectedContactList shouldContainExactlyInAnyOrder listOf(target, other)

                memoAdd.contact.unselectContact(id = target.id)
                advanceUntilIdle()
                memoAdd.contact.uiState.value.selectedContactList shouldBe listOf(other)
            }
        }

        test("TC-CONTACT-DETAIL-MEMO-FEATURE-011 삭제된 대상 연락처는 연락처 입력에 선택된 것으로 표시하지 않는다") {
            runTest(mainDispatcher) {
                val deletedTarget = fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)
                val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialContactId = deletedTarget.id), contactList = listOf(fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.contact.uiState.value.selectedContactList
                    .shouldBeEmpty()
            }
        }

        test("TC-CONTACT-DETAIL-MEMO-DOMAIN-002 대상 연락처 하나만 선택하고 태그·웹·장소·기간과 대표 태그는 없이 시작한다") {
            runTest(mainDispatcher) {
                val target = fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)
                val key = MemoAddNavKey(initialContactId = target.id)
                val memoAdd = MemoAddSelection(key = key, contactList = listOf(target))
                memoAdd.collect(this)
                advanceUntilIdle()

                memoAdd.assertOnlyTargetSelected(key = key)
                memoAdd.contact.contactIdSet.value shouldBe setOf(target.id)
            }
        }

        test("TC-CONTACT-DETAIL-MEMO-DATA-005 저장 시점의 연락처 선택을 그대로 저장 요청에 담는다") {
            runTest(mainDispatcher) {
                val contactA = fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)
                val contactB = fixtureMonkey.giveMeOne<Contact>().copy(isDeleted = false)
                val finalSelectionList = listOf(setOf(contactA.id), setOf(contactA.id, contactB.id), setOf(contactB.id), emptySet())

                finalSelectionList.forEach { finalSelection ->
                    val memoAdd = MemoAddSelection(key = MemoAddNavKey(initialContactId = contactA.id), contactList = listOf(contactA, contactB))
                    (finalSelection - contactA.id).forEach { id -> memoAdd.contact.selectContact(id = id) }
                    if (contactA.id !in finalSelection) memoAdd.contact.unselectContact(id = contactA.id)

                    memoAdd.save()
                    advanceUntilIdle()

                    coVerify(exactly = 1) { memoAdd.addMemoUseCase(match<AddMemoUseCase.Parameter> { parameter -> parameter.contactIdSet == finalSelection }) }
                }
            }
        }
    }

    /**
     * MemoAdd 화면이 화면 키로 만드는 선택 입력들과 저장 요청을 한데 묶는다. 선택한 항목 조회는 선택할 수 있는 항목 가운데 요청한 ID의 항목만 돌려준다.
     */
    private class MemoAddSelection(
        val key: MemoAddNavKey,
        placeList: List<Place> = emptyList(),
        webList: List<Web> = emptyList(),
        contactList: List<Contact> = emptyList(),
    ) {
        val addMemoUseCase: AddMemoUseCase =
            mockk<AddMemoUseCase>().apply {
                coEvery { this@apply(any<AddMemoUseCase.Parameter>()) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
            }

        val add: MemoAddViewModel = MemoAddViewModel(addMemoUseCase = addMemoUseCase)

        val tag: MemoAddTagViewModel =
            MemoAddTagViewModel(
                initialPrimaryTagId = key.primaryTagId,
                pageTagUseCase = mockk<PageTagUseCase>().apply { every { this@apply(parameter = any()) } returns emptyFlow() },
                getSelectedTagUseCase =
                    mockk<GetSelectedTagUseCase>().apply {
                        every { this@apply(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    },
            )

        val place: MemoAddPlaceViewModel =
            MemoAddPlaceViewModel(
                initialPlaceId = key.initialPlaceId,
                pagePlaceUseCase = mockk<PagePlaceUseCase>().apply { every { this@apply(parameter = any()) } returns emptyFlow() },
                getSelectedPlaceUseCase =
                    mockk<GetSelectedPlaceUseCase>().apply {
                        every { this@apply(parameter = any()) } answers {
                            val idSet = firstArg<Set<Uuid>>()
                            flowOf(Result.success(placeList.filter { value -> value.id in idSet }))
                        }
                    },
            )

        val web: MemoAddWebViewModel =
            MemoAddWebViewModel(
                initialWebId = key.initialWebId,
                pageMemoSelectableWebUseCase = mockk<PageMemoSelectableWebUseCase>().apply { every { this@apply(parameter = any()) } returns emptyFlow() },
                getSelectedWebUseCase =
                    mockk<GetSelectedWebUseCase>().apply {
                        every { this@apply(parameter = any()) } answers {
                            val idSet = firstArg<Set<Uuid>>()
                            flowOf(Result.success(webList.filter { value -> value.id in idSet }))
                        }
                    },
            )

        val contact: MemoAddContactViewModel =
            MemoAddContactViewModel(
                initialContactId = key.initialContactId,
                pageMemoSelectableContactUseCase = mockk<PageMemoSelectableContactUseCase>().apply { every { this@apply(parameter = any()) } returns emptyFlow() },
                getSelectedContactUseCase =
                    mockk<GetSelectedContactUseCase>().apply {
                        every { this@apply(parameter = any()) } answers {
                            val idSet = firstArg<Set<Uuid>>()
                            flowOf(Result.success(contactList.filter { value -> value.id in idSet }))
                        }
                    },
            )

        // 선택 상태는 구독자가 있을 때 만들어지므로 검증 동안 수집을 유지한다.
        fun collect(scope: TestScope) {
            scope.backgroundScope.launch { tag.uiState.collect() }
            scope.backgroundScope.launch { place.uiState.collect() }
            scope.backgroundScope.launch { web.uiState.collect() }
            scope.backgroundScope.launch { contact.uiState.collect() }
        }

        // MemoAdd 화면의 저장은 각 선택 입력이 보관한 선택을 모아 한 번에 요청한다.
        fun save() {
            add.add(
                detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}"),
                tagSelection = tag.selection.value,
                webIdSet = web.webIdSet.value,
                contactIdSet = contact.contactIdSet.value,
                placeIdSet = place.placeIdSet.value,
            )
        }

        fun assertOnlyTargetSelected(key: MemoAddNavKey) {
            tag.uiState.value.selectedTagList
                .shouldBeEmpty()
            tag.uiState.value.primaryTagId
                .shouldBeNull()
            tag.selection.value.tagIdSet
                .shouldBeEmpty()
            key.initialDateRange.shouldBeNull()
            listOf(place.placeIdSet.value, web.webIdSet.value, contact.contactIdSet.value).sumOf { idSet -> idSet.size } shouldBe 1
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
