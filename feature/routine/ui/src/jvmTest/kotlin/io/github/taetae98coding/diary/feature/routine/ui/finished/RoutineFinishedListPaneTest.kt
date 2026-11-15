@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.feature.routine.ui.navigateUpFromRoutineFinishedList
import io.github.taetae98coding.diary.feature.routine.ui.routineDetailPaneSceneKey
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.emptyFlow

class RoutineFinishedListPaneTest :
    FunSpec({
        test("TC-773e10fe-a17f-4f5b-a9ad-c52430350dc2 완료된 루틴 목록은 목록 pane이고 그 위의 루틴 상세는 같은 배치의 상세 pane이다") {
            val detail = detailKey()
            val backStack = finishedListBackStack + detail

            metadataOf(backStack = backStack, key = RoutineFinishedListNavKey).keys shouldBe listPaneMetadataKeys
            metadataOf(backStack = backStack, key = detail).keys shouldBe detailPaneMetadataKeys
            backStack.routineDetailPaneSceneKey(detail) shouldBe RoutineFinishedListNavKey
            // RoutineHome의 루틴 목록은 이 배치와 다른 배치의 목록이다.
            backStack.routineDetailPaneSceneKey(RoutineHomeNavKey) shouldBe null
        }

        test("TC-8950c241-b6e3-4548-ae3e-57edb4b41d89 RoutineFinishedList에서 진입하지 않은 화면은 이 배치에 참여하지 않는다") {
            val detail = detailKey()
            val standaloneDetail = detail.copy(isStandalone = true)
            val homeAdd = RoutineAddNavKey()
            val tagDetail = TagDetailNavKey(id = Arb.kotlinUuid().next())
            val tagFinishedList = TagRoutineFinishedListNavKey(tagId = tagDetail.id)
            val cases =
                listOf(
                    // RoutineHome의 루틴 목록과 달력 보기에서 고른 루틴 상세는 같은 화면 위에 놓인다.
                    listOf(FinishedListDefaultNavKey, RoutineHomeNavKey, detail) to detail,
                    listOf(FinishedListDefaultNavKey, RoutineHomeNavKey, homeAdd) to homeAdd,
                    listOf(FinishedListDefaultNavKey, tagDetail, standaloneDetail) to standaloneDetail,
                    listOf(FinishedListDefaultNavKey, tagDetail, tagFinishedList, detail) to detail,
                    listOf(FinishedListDefaultNavKey, FinishedListSearchNavKey, standaloneDetail) to standaloneDetail,
                )

            cases.forEach { (backStack, key) ->
                backStack.routineDetailPaneSceneKey(key) shouldNotBe RoutineFinishedListNavKey
            }
        }

        test("TC-77e6ff56-f13f-4323-b7f0-9d43de132efc 루틴 상세가 놓여 있으면 새로 고른 루틴의 상세로 교체한다") {
            val first = detailKey()
            val second = detailKey()
            val backStack = NavBackStack(*(finishedListBackStack + first).toTypedArray())

            backStack.navigateToRoutineDetail(id = second.id)

            backStack shouldContainExactly finishedListBackStack + second
        }

        test("TC-c9f4ab3f-e8e1-440d-8d31-2e7d29c01b9b 목록 화면의 뒤로가기는 상세 선택 여부와 관계없이 완료된 루틴 목록과 상세를 함께 닫는다") {
            listOf(emptyList(), listOf(detailKey())).forEach { detailList ->
                val backStack = NavBackStack(*(finishedListBackStack + detailList).toTypedArray())

                backStack.navigateUpFromRoutineFinishedList()

                backStack shouldContainExactly listOf(FinishedListDefaultNavKey, RoutineHomeNavKey)
            }
        }

        test("루틴 목록의 상세 영역에 루틴 상세를 둔 채 진입했다가 돌아오면 그 상세가 그대로 남는다") {
            val homeDetail = detailKey()
            val backStack = NavBackStack(FinishedListDefaultNavKey, RoutineHomeNavKey, homeDetail, RoutineFinishedListNavKey, detailKey())

            backStack.navigateUpFromRoutineFinishedList()

            backStack shouldContainExactly listOf(FinishedListDefaultNavKey, RoutineHomeNavKey, homeDetail)
        }
    }) {
    private companion object {
        val finishedListBackStack: List<ScreenNavKey> = listOf(FinishedListDefaultNavKey, RoutineHomeNavKey, RoutineFinishedListNavKey)

        val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        fun detailKey(): RoutineDetailNavKey = RoutineDetailNavKey(id = Arb.kotlinUuid().next())

        fun metadataOf(
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

// 캘린더 홈처럼 루틴이 아닌 진입 화면을 대신한다. 그 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object FinishedListDefaultNavKey : ScreenNavKey {
    override val screenName: String
        get() = "FinishedListDefault"
}

// SearchHome을 대신한다.
private data object FinishedListSearchNavKey : ScreenNavKey {
    override val screenName: String
        get() = "FinishedListSearch"
}
