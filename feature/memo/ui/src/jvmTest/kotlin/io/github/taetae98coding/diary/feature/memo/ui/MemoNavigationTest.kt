package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.navigateToMemoDetail
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class MemoNavigationTest :
    FunSpec({
        test("TC-MEMO-LIST-DETAIL-FEATURE-018 다른 메모를 선택하면 앞 메모 상세의 기록 없이 새 메모 상세를 놓는다") {
            val selectedId = fixtureMonkey.giveMeOne<Uuid>()
            val previousDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val addKey = MemoAddNavKey()
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(previousDetailKey),
                    listOf(addKey, previousDetailKey),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, *detailKeyList.toTypedArray())
                val expected =
                    listOf(MemoHomeNavKey) +
                        detailKeyList.filterNot { key -> key == previousDetailKey } +
                        MemoDetailNavKey(id = selectedId)

                backStack.navigateToMemoDetail(selectedId)

                backStack shouldContainExactly expected
            }
        }

        test("TC-MEMO-LIST-DETAIL-FEATURE-019 메모를 복사하면 원본 상세의 기록 없이 복사한 메모 상세를 놓는다") {
            val copiedId = fixtureMonkey.giveMeOne<Uuid>()
            val addKey = MemoAddNavKey()
            val backStack =
                NavBackStack<ScreenNavKey>(
                    MemoHomeNavKey,
                    addKey,
                    MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStack.navigateToCopiedMemo(copiedId)

            backStack shouldContainExactly listOf(MemoHomeNavKey, addKey, MemoDetailNavKey(id = copiedId))
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-019 완료된 메모 목록의 상세 영역에서 복사하면 원본 상세의 기록 없이 복사한 메모 상세를 상세 영역에 놓는다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val copiedId = fixtureMonkey.giveMeOne<Uuid>()
            val listKey = TagMemoFinishedListNavKey(tagId = tagId)
            val backStack =
                NavBackStack<ScreenNavKey>(
                    TagDetailNavKey(id = tagId),
                    listKey,
                    MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStack.navigateToCopiedMemo(copiedId)

            backStack shouldContainExactly listOf(TagDetailNavKey(id = tagId), listKey, MemoDetailNavKey(id = copiedId))
            backStack.memoDetailPaneSceneKey(backStack.last()) shouldBe listKey
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-009 복사한 메모 상세에서 뒤로가면 목록을 유지하고 선택 전 상태가 된다") {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val listKey = TagMemoFinishedListNavKey(tagId = tagId)
            val backStack =
                NavBackStack<ScreenNavKey>(
                    TagDetailNavKey(id = tagId),
                    listKey,
                    MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStack.navigateToCopiedMemo(fixtureMonkey.giveMeOne<Uuid>())
            backStack.removeLastOrNull()

            backStack shouldContainExactly listOf(TagDetailNavKey(id = tagId), listKey)
        }

        test("TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-005 완료된 메모 목록의 상세 영역에서 복사하면 원본 상세의 기록 없이 복사한 메모 상세를 상세 영역에 놓는다") {
            val copiedId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack =
                NavBackStack<ScreenNavKey>(
                    MemoHomeNavKey,
                    MemoFinishedListNavKey,
                    MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStack.navigateToCopiedMemo(copiedId)

            backStack shouldContainExactly listOf(MemoHomeNavKey, MemoFinishedListNavKey, MemoDetailNavKey(id = copiedId))
            backStack.memoDetailPaneSceneKey(backStack.last()) shouldBe MemoFinishedListNavKey
        }

        test("TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-008 복사한 메모 상세에서 뒤로가면 목록을 유지하고 선택 전 상태가 된다") {
            val backStack =
                NavBackStack<ScreenNavKey>(
                    MemoHomeNavKey,
                    MemoFinishedListNavKey,
                    MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStack.navigateToCopiedMemo(fixtureMonkey.giveMeOne<Uuid>())
            backStack.removeLastOrNull()

            backStack shouldContainExactly listOf(MemoHomeNavKey, MemoFinishedListNavKey)
        }

        test("TC-MEMO-FINISHED-LIST-DETAIL-FEATURE-012 완료된 메모 목록의 뒤로가기 동작은 상세 표시 여부와 관계없이 MemoHome 목록으로 돌아간다") {
            val homeDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val homeBackStackCases: List<List<ScreenNavKey>> = listOf(listOf(MemoHomeNavKey), listOf(MemoHomeNavKey, homeDetailKey))
            val finishedListDetailCases: List<List<ScreenNavKey>> = listOf(emptyList(), listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())))

            homeBackStackCases.forEach { homeBackStack ->
                finishedListDetailCases.forEach { detailKeyList ->
                    val backStack = NavBackStack<ScreenNavKey>(*homeBackStack.toTypedArray(), MemoFinishedListNavKey, *detailKeyList.toTypedArray())

                    backStack.navigateUpFromMemoFinishedList()

                    backStack shouldContainExactly homeBackStack
                }
            }
        }

        test("TC-MEMO-FINISHED-LIST-DETAIL-DOMAIN-001 목록 화면을 떠났다가 다시 진입하면 상세가 선택 전 상태로 돌아간다") {
            val detailCases =
                listOf(
                    listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoFinishedListNavKey, *detailKeyList.toTypedArray())

                backStack.navigateUpFromMemoFinishedList()
                backStack.add(MemoFinishedListNavKey)

                backStack shouldContainExactly listOf(MemoHomeNavKey, MemoFinishedListNavKey)
            }
        }

        test("TC-MEMO-LIST-DETAIL-DOMAIN-005 완료된 메모 목록에 다녀와도 상세 영역과 뒤로가기 기록이 그대로다") {
            val homeDetailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val addKey = MemoAddNavKey()
            val cases: List<Pair<List<ScreenNavKey>, List<ScreenNavKey>>> =
                listOf(
                    listOf(homeDetailKey) to emptyList(),
                    listOf(homeDetailKey, addKey) to listOf(homeDetailKey),
                )

            cases.forEach { (homeDetailList, expectedAfterBack) ->
                val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, *homeDetailList.toTypedArray())

                backStack.add(MemoFinishedListNavKey)
                backStack.navigateToMemoDetail(fixtureMonkey.giveMeOne<Uuid>())
                backStack.navigateUpFromMemoFinishedList()

                backStack shouldContainExactly listOf(MemoHomeNavKey) + homeDetailList
                backStack.memoDetailPaneSceneKey(backStack.last()) shouldBe MemoHomeNavKey

                backStack.removeLastOrNull()

                backStack shouldContainExactly listOf(MemoHomeNavKey) + expectedAfterBack
            }
        }

        test("완료된 메모 목록이 없는 전환 이력에서는 완료된 메모 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, detailKey)

            backStack.navigateUpFromMemoFinishedList()

            backStack shouldContainExactly listOf(MemoHomeNavKey, detailKey)
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-019 항목 완료 메모 목록의 상세 영역에서 복사하면 원본 상세의 기록 없이 복사한 메모 상세를 상세 영역에 놓는다") {
            entityFinishedListBackStackCases().forEach { (entityDetailKey, listKey) ->
                val copiedId = fixtureMonkey.giveMeOne<Uuid>()
                val backStack =
                    NavBackStack<ScreenNavKey>(entityDetailKey, listKey, MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))

                backStack.navigateToCopiedMemo(copiedId)

                backStack shouldContainExactly listOf(entityDetailKey, listKey, MemoDetailNavKey(id = copiedId))
                backStack.memoDetailPaneSceneKey(backStack.last()) shouldBe listKey
            }
        }

        test("TC-ENTITY-MEMO-FINISHED-LIST-DETAIL-FEATURE-009 복사한 메모 상세에서 뒤로가면 목록을 유지하고 선택 전 상태가 된다") {
            entityFinishedListBackStackCases().forEach { (entityDetailKey, listKey) ->
                val backStack =
                    NavBackStack<ScreenNavKey>(entityDetailKey, listKey, MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))

                backStack.navigateToCopiedMemo(fixtureMonkey.giveMeOne<Uuid>())
                backStack.removeLastOrNull()

                backStack shouldContainExactly listOf(entityDetailKey, listKey)
            }
        }

        test("TC-MEMO-DETAIL-FEATURE-076 복사한 뒤 뒤로가면 원본 상세가 아니라 진입 화면으로 돌아간다") {
            val entryKeyList =
                listOf(
                    MemoHomeNavKey,
                    TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                    ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            entryKeyList.forEach { entryKey ->
                val backStack = NavBackStack(entryKey, MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))

                backStack.navigateToCopiedMemo(fixtureMonkey.giveMeOne<Uuid>())
                backStack.removeLastOrNull()

                backStack shouldContainExactly listOf(entryKey)
            }
        }

        test("TC-MEMO-HOME-FEATURE-047 검색을 선택하면 메모 결과부터 보여 주는 SearchHome 화면으로 이동한다") {
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey)

            backStack.navigateToSearchFromMemoHome()

            backStack shouldContainExactly listOf(MemoHomeNavKey, SearchHomeNavKey(initialType = SearchHomeType.MEMO))
        }

        test("TC-MEMO-HOME-FEATURE-064 태그 추가를 누르면 필터를 닫고 TagAdd 화면으로 이동한다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, detailKey, MemoHomeFilterNavKey)

            backStack.navigateToTagAddFromFilter()

            backStack shouldContainExactly listOf(MemoHomeNavKey, detailKey, TagAddNavKey())
        }

        test("TC-MEMO-HOME-FEATURE-070 TagAdd 화면에서 돌아오면 필터가 다시 열리지 않고 추가한 태그를 선택으로 돌려받지 않는다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, detailKey, MemoHomeFilterNavKey)

            backStack.navigateToTagAddFromFilter()
            val tagAddKey = backStack.last() as TagAddNavKey
            backStack.removeLastOrNull()

            tagAddKey.requestKey.shouldBeNull()
            backStack shouldContainExactly listOf(MemoHomeNavKey, detailKey)
        }

        test("TC-WEB-ADD-FEATURE-017 메모 웹 입력에서 웹 추가를 선택하면 초기 태그 없는 웹 추가로 이동한다") {
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, MemoAddNavKey())
            val requestKey = fixtureMonkey.giveMeOne<Uuid>()

            backStack.navigateToWebAddFromMemoWebInput(requestKey = requestKey)

            backStack shouldContainExactly listOf(MemoHomeNavKey, MemoAddNavKey(), WebAddNavKey(requestKey = requestKey))
            (backStack.last() as WebAddNavKey).initialTagId.shouldBeNull()
        }
    }) {
    companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

private fun entityFinishedListBackStackCases(): List<Pair<ScreenNavKey, ScreenNavKey>> {
    val contactId = Uuid.random()
    val placeId = Uuid.random()
    val webId = Uuid.random()

    return listOf(
        ContactDetailNavKey(id = contactId) to ContactMemoFinishedListNavKey(contactId = contactId),
        PlaceDetailNavKey(id = placeId) to PlaceMemoFinishedListNavKey(placeId = placeId),
        WebDetailNavKey(id = webId) to WebMemoFinishedListNavKey(webId = webId),
    )
}
