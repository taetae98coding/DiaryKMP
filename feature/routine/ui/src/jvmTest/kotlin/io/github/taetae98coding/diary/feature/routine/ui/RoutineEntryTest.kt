@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

class RoutineEntryTest :
    FunSpec({
        test("TC-ROUTINE-LIST-DETAIL-DOMAIN-002 루틴 목록에서 이어 진입한 루틴 추가는 목록·상세 배치의 상세 pane이다") {
            val backStack = listOf(OtherTopLevelNavKey, RoutineHomeNavKey, RoutineAddNavKey)

            metadataOf(backStack = backStack, key = RoutineAddNavKey).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-ROUTINE-LIST-DETAIL-DOMAIN-001 루틴 목록에서 진입하지 않은 루틴 추가는 목록·상세 배치에 참여하지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, RoutineAddNavKey),
                    listOf(RoutineHomeNavKey, OtherTopLevelNavKey, RoutineAddNavKey),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = RoutineAddNavKey).shouldBeEmpty()
            }
        }

        test("RoutineHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherTopLevelNavKey, RoutineHomeNavKey)

            metadataOf(backStack = backStack, key = RoutineHomeNavKey).keys shouldBe listPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<NavKey>,
            key: NavKey,
        ): Map<String, Any> {
            val provider = entryProvider<NavKey> { routineEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 캘린더 홈처럼 루틴 목록이 아닌 진입 화면을 대신한다. 캘린더 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : NavKey
