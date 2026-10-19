@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.contact.api.isContactListDetailPane
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class ContactMemoFinishedListNavigationTest :
    FunSpec({
        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-001 TC-CONTACT-DETAIL-MEMO-FEATURE-031 ContactMemoFinishedList 화면은 연락처 배치에 참여하지 않고 목록·상세 배치의 목록 pane이다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val finishedListKey = ContactMemoFinishedListNavKey(contactId = contactId)
            val backStack = listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId), finishedListKey)

            metadataOf(backStack = backStack, key = finishedListKey).keys shouldBe listPaneMetadataKeys
            backStack.isContactListDetailPane(finishedListKey) shouldBe false
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-FEATURE-011 TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-017 완료된 메모 목록의 뒤로가기 동작은 상세 표시 여부와 관계없이 ContactDetail로 돌아간다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = finishedListBackStack(contactId = contactId, finishedDetailKeyList = detailKeyList)

                backStack.navigateUpFromContactMemoFinishedList()

                backStack shouldContainExactly listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId))
            }
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-DOMAIN-001 목록 화면을 떠났다가 다시 진입하면 상세가 선택 전 상태로 돌아간다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val openCases =
                listOf(
                    listOf(fixtureMonkey.giveMeOne<Uuid>()),
                    listOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>()),
                )

            openCases.forEach { memoIdList ->
                val backStack = finishedListBackStack(contactId = contactId, finishedDetailKeyList = emptyList())
                memoIdList.forEach { memoId -> backStack.navigateToMemoDetail(memoId) }

                backStack.navigateUpFromContactMemoFinishedList()
                backStack.add(ContactMemoFinishedListNavKey(contactId = contactId))

                backStack.toList() shouldContainExactly
                    listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId))
            }
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-003 메모를 선택하면 목록을 유지한 채 그 메모의 상세가 놓인다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack = finishedListBackStack(contactId = contactId, finishedDetailKeyList = emptyList())

            backStack.navigateToMemoDetail(memoId)

            backStack.toList() shouldContainExactly
                listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId), MemoDetailNavKey(id = memoId))
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-004 다른 메모를 선택하면 이전 상세를 쌓지 않고 교체한다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val selectedId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack =
                finishedListBackStack(
                    contactId = contactId,
                    finishedDetailKeyList = listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            backStack.navigateToMemoDetail(selectedId)

            backStack.toList() shouldContainExactly
                listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId), MemoDetailNavKey(id = selectedId))
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-009 상세가 선택된 상태에서 뒤로가면 목록을 유지하고 선택 전 상태가 된다") {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val openCases =
                listOf(
                    listOf(fixtureMonkey.giveMeOne<Uuid>()),
                    listOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>()),
                )

            openCases.forEach { memoIdList ->
                val backStack = finishedListBackStack(contactId = contactId, finishedDetailKeyList = emptyList())
                memoIdList.forEach { memoId -> backStack.navigateToMemoDetail(memoId) }

                backStack.removeLastOrNull()

                backStack.toList() shouldContainExactly
                    listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId))
            }
        }

        test("완료된 메모 목록이 없는 전환 이력에서는 완료된 메모 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(ContactHomeNavKey, ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
            val expected = backStack.toList()

            backStack.navigateUpFromContactMemoFinishedList()

            backStack shouldContainExactly expected
        }
    }) {
    companion object {
        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { contactEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private fun finishedListBackStack(
    contactId: Uuid,
    finishedDetailKeyList: List<ScreenNavKey>,
): NavBackStack<ScreenNavKey> =
    NavBackStack(
        ContactHomeNavKey,
        ContactDetailNavKey(id = contactId),
        ContactMemoFinishedListNavKey(contactId = contactId),
        *finishedDetailKeyList.toTypedArray(),
    )
