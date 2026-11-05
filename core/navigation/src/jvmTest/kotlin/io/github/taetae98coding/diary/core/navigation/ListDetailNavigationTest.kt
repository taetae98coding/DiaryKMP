package io.github.taetae98coding.diary.core.navigation

import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ListDetailNavigationTest :
    FunSpec({
        val homeKey = TestKey.Home
        val detailKeyA = TestKey.Detail("A-${Arb.nonBlankString().next()}")
        val detailKeyB = TestKey.Detail("B-${Arb.nonBlankString().next()}")
        val otherKey = TestKey.Other
        val isDetailPaneKey: (ScreenNavKey) -> Boolean = { key -> key is TestKey.Detail }

        context("상세로 이동") {
            test("상세 영역에 상세가 놓여 있지 않으면 새 상세를 쌓는다") {
                val backStack: MutableList<ScreenNavKey> = mutableListOf(homeKey)

                backStack.navigateToDetail(detailKeyA, isDetailPaneKey)

                backStack shouldBe listOf(homeKey, detailKeyA)
            }

            test("상세 영역에 상세가 놓여 있으면 앞 상세를 쌓아 두지 않고 새 상세로 교체한다") {
                val backStack: MutableList<ScreenNavKey> = mutableListOf(homeKey, detailKeyA)

                backStack.navigateToDetail(detailKeyB, isDetailPaneKey)

                backStack shouldBe listOf(homeKey, detailKeyB)
            }

            test("상세가 여러 겹 쌓여 있어도 모두 걷어내고 새 상세로 교체한다") {
                val backStack: MutableList<ScreenNavKey> = mutableListOf(homeKey, detailKeyA, detailKeyB)

                backStack.navigateToDetail(detailKeyA, isDetailPaneKey)

                backStack shouldBe listOf(homeKey, detailKeyA)
            }

            test("상세가 아닌 화면은 걷어내지 않는다") {
                val backStack: MutableList<ScreenNavKey> = mutableListOf(homeKey, otherKey)

                backStack.navigateToDetail(detailKeyA, isDetailPaneKey)

                backStack shouldBe listOf(homeKey, otherKey, detailKeyA)
            }
        }

        context("배치에 참여하는 상세 판정") {
            test("목록 바로 위에 놓인 상세는 배치에 참여한다") {
                val backStack = listOf(homeKey, detailKeyA)

                backStack.isListDetailPane(detailKeyA, homeKey, isDetailPaneKey) shouldBe true
            }

            test("목록 위에 상세가 이어 놓여도 배치에 참여한다") {
                val backStack = listOf(homeKey, detailKeyA, detailKeyB)

                backStack.isListDetailPane(detailKeyB, homeKey, isDetailPaneKey) shouldBe true
            }

            test("그 목록이 아닌 곳에서 진입한 상세는 배치에 참여하지 않는다") {
                val backStack = listOf(homeKey, otherKey, detailKeyA)

                backStack.isListDetailPane(detailKeyA, homeKey, isDetailPaneKey) shouldBe false
            }

            test("back stack에 없는 키는 배치에 참여하지 않는다") {
                val backStack = listOf(homeKey)

                backStack.isListDetailPane(detailKeyA, homeKey, isDetailPaneKey) shouldBe false
            }

            test("상세 영역 키가 아닌 화면은 배치에 참여하지 않는다") {
                val backStack = listOf(homeKey, otherKey)

                backStack.isListDetailPane(otherKey, homeKey, isDetailPaneKey) shouldBe false
            }
        }

        context("배치 떠나기") {
            test("목록과 그 위의 화면을 모두 걷어낸다") {
                val backStack: MutableList<ScreenNavKey> = mutableListOf(homeKey, detailKeyA, otherKey)

                backStack.navigateUpTo(homeKey)

                backStack shouldBe listOf<ScreenNavKey>()
            }

            test("목록이 back stack에 없으면 그대로 둔다") {
                val backStack: MutableList<ScreenNavKey> = mutableListOf(otherKey, detailKeyA)

                backStack.navigateUpTo(homeKey)

                backStack shouldBe listOf(otherKey, detailKeyA)
            }
        }
    })

private sealed interface TestKey : ScreenNavKey {
    data object Home : TestKey {
        override val screenName: String get() = "Home"
    }

    data class Detail(
        val id: String,
    ) : TestKey {
        override val screenName: String get() = "Detail"
    }

    data object Other : TestKey {
        override val screenName: String get() = "Other"
    }
}
