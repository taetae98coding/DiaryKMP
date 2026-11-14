@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.isRoutineAddOnDetailPane
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.emptyFlow

class RoutineListDetailPaneTest :
    FunSpec({
        test("TC-411ef686-22d2-4e3e-842c-b27f4635f248 상세 영역에 루틴 상세가 놓이면 목록의 추가 수단을 둔다") {
            val backStackCases =
                listOf(
                    listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, detailKey()),
                    listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, detailKey(), RoutineHomeFilterNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isRoutineAddOnDetailPane() shouldBe false
            }
        }

        test("TC-a2f32751-6f4b-4dfe-bdfa-a0c65b2cc024 RoutineHome에서 이어 진입한 루틴 상세는 상세 pane이다") {
            val detail = detailKey()
            val detailBackStack = listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, detail)

            metadataOf(backStack = detailBackStack, key = detail).keys shouldBe detailPaneMetadataKeys
            metadataOf(backStack = detailBackStack, key = RoutineHomeNavKey).keys shouldBe listPaneMetadataKeys
        }

        test("TC-12247261-c406-4b46-bb2c-329f4b536428 다른 화면에서 진입한 루틴 상세와 추가는 배치에 참여하지 않는다") {
            val detail = detailKey()
            val standaloneDetail = detail.copy(isStandalone = true)
            val tagAddKey = RoutineAddNavKey(initialTagId = Arb.kotlinUuid().next())
            val cases =
                listOf(
                    // TagDetail 루틴 탭과 SearchHome에서 고른 루틴 상세는 그 화면 위에 놓인다.
                    listOf(ListDetailDefaultNavKey, ListDetailTagDetailNavKey, detail) to detail,
                    listOf(ListDetailDefaultNavKey, ListDetailSearchNavKey, detail) to detail,
                    // Spotlight로 여는 루틴 상세는 루틴 목록 바로 위에 놓여도 목록과 함께 표시하지 않는다.
                    listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, standaloneDetail) to standaloneDetail,
                    listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, detailKey(), standaloneDetail) to standaloneDetail,
                    listOf(ListDetailDefaultNavKey, ListDetailTagDetailNavKey, tagAddKey) to tagAddKey,
                    // 루틴 목록 위에서 다른 화면을 거쳐 진입해도 목록과 함께 표시하지 않는다.
                    listOf(RoutineHomeNavKey, ListDetailSearchNavKey, detail) to detail,
                    listOf(RoutineHomeNavKey, tagAddKey) to tagAddKey,
                )

            cases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).shouldBeEmpty()
            }
        }

        test("TC-fb696550-5cf8-4c1a-97bb-00fb3b090fb6 루틴 상세가 놓여 있으면 새 루틴의 상세로 교체한다") {
            val first = detailKey()
            val second = detailKey()
            val backStack = NavBackStack(ListDetailDefaultNavKey, RoutineHomeNavKey, first)

            backStack.navigateToRoutineDetail(id = second.id)

            backStack shouldContainExactly listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, second)
        }

        test("TC-80127a90-7c34-49f6-b302-7eb992d4c38f 루틴을 고르면 쌓인 상세와 루틴 추가를 걷어내고 그 상세로 바꾼다") {
            val detailCases =
                listOf(
                    listOf(RoutineAddNavKey()),
                    listOf(detailKey(), RoutineAddNavKey()),
                )

            detailCases.forEach { detailKeyList ->
                val selected = detailKey()
                val backStack = NavBackStack(ListDetailDefaultNavKey, RoutineHomeNavKey, *detailKeyList.toTypedArray())

                backStack.navigateToRoutineDetail(id = selected.id)

                backStack shouldContainExactly listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, selected)
            }
        }

        test("TC-ff826665-c13f-4602-9374-e01a487c2e01 목록의 루틴 추가는 초기 태그 없는 새 루틴 추가를 상세 위에 놓는다") {
            val detail = detailKey()
            val backStack = NavBackStack(ListDetailDefaultNavKey, RoutineHomeNavKey, detail)

            backStack.navigateToRoutineAddFromHome()

            backStack shouldContainExactly listOf(ListDetailDefaultNavKey, RoutineHomeNavKey, detail, RoutineAddNavKey())
            (backStack.last() as RoutineAddNavKey).initialTagId.shouldBeNull()
            metadataOf(backStack = backStack, key = backStack.last()).keys shouldBe detailPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun detailKey(): RoutineDetailNavKey = RoutineDetailNavKey(id = Arb.kotlinUuid().next())

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider =
                entryProvider<ScreenNavKey> {
                    routineEntry(
                        backStack = NavBackStack(*backStack.toTypedArray()),
                        homeReselectEvent = emptyFlow(),
                    )
                }

            return provider(key).metadata
        }
    }
}

// 기본 목적지인 캘린더 홈과 TagDetail, SearchHome을 대신한다. 그 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object ListDetailDefaultNavKey : ScreenNavKey {
    override val screenName: String
        get() = "DefaultStub"
}

private data object ListDetailTagDetailNavKey : ScreenNavKey {
    override val screenName: String
        get() = "TagDetailStub"
}

private data object ListDetailSearchNavKey : ScreenNavKey {
    override val screenName: String
        get() = "SearchStub"
}
