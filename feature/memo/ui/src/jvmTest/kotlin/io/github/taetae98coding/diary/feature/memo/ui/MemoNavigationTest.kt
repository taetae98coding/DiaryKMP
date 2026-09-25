package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
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

                backStack.navigateToMemoDetailFromHome(selectedId)

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

        test("TC-MEMO-HOME-FEATURE-064 태그 추가를 누르면 필터를 닫고 TagAdd 화면으로 이동한다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, detailKey, MemoHomeFilterNavKey)

            backStack.navigateToTagAddFromMemoHomeFilter()

            backStack shouldContainExactly listOf(MemoHomeNavKey, detailKey, TagAddNavKey())
        }

        test("TC-MEMO-HOME-FEATURE-070 TagAdd 화면에서 돌아오면 필터가 다시 열리지 않고 추가한 태그를 선택으로 돌려받지 않는다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = NavBackStack<ScreenNavKey>(MemoHomeNavKey, detailKey, MemoHomeFilterNavKey)

            backStack.navigateToTagAddFromMemoHomeFilter()
            val tagAddKey = backStack.last() as TagAddNavKey
            backStack.removeLastOrNull()

            tagAddKey.requestKey.shouldBeNull()
            backStack shouldContainExactly listOf(MemoHomeNavKey, detailKey)
        }
    }) {
    companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}
