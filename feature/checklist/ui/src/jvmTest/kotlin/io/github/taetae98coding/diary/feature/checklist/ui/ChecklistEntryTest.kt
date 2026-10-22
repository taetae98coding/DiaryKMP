@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class ChecklistEntryTest :
    FunSpec({
        test("TC-320d4e24-62cc-4e2a-bc7c-8479b43a8971 체크리스트 목록에서 이어 진입한 체크리스트 추가는 목록·상세 배치의 상세 pane이다") {
            val backStack = listOf(OtherNavKey, ChecklistHomeNavKey, ChecklistAddNavKey)

            metadataOf(backStack = backStack, key = ChecklistAddNavKey).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-2825146d-f2d4-4196-bdb0-b59a14efb2d9 체크리스트 목록에서 진입하지 않은 체크리스트 추가는 목록·상세 배치에 참여하지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherNavKey, ChecklistAddNavKey),
                    listOf(ChecklistHomeNavKey, OtherNavKey, ChecklistAddNavKey),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = ChecklistAddNavKey).shouldBeEmpty()
            }
        }

        test("TC-3943e3e4-b520-4a11-ad55-8b92ef622f9a 체크리스트 목록에서 이어 진입한 체크리스트 상세는 목록·상세 배치의 상세 pane이다") {
            val key = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = listOf(OtherNavKey, ChecklistHomeNavKey, ChecklistAddNavKey, key)

            metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
        }

        test("체크리스트 목록에서 진입하지 않은 체크리스트 상세는 목록·상세 배치에 참여하지 않는다") {
            val key = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = listOf(ChecklistHomeNavKey, OtherNavKey, key)

            metadataOf(backStack = backStack, key = key).shouldBeEmpty()
        }

        test("ChecklistHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherNavKey, ChecklistHomeNavKey)

            metadataOf(backStack = backStack, key = ChecklistHomeNavKey).keys shouldBe listPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { checklistEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 체크리스트 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
