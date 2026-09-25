@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.web.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class WebEntryTest :
    FunSpec({
        test("TC-WEB-LIST-DETAIL-DOMAIN-002 웹 목록에서 이어 진입한 웹 추가는 목록·상세 배치의 상세 pane이다") {
            val backStack = listOf(OtherNavKey, WebHomeNavKey, WebAddNavKey())

            metadataOf(backStack = backStack, key = WebAddNavKey()).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-WEB-LIST-DETAIL-DOMAIN-001 웹 목록에서 진입하지 않은 웹 추가는 목록·상세 배치에 참여하지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherNavKey, WebAddNavKey()),
                    listOf(WebHomeNavKey, OtherNavKey, WebAddNavKey()),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = WebAddNavKey()).shouldBeEmpty()
            }
        }

        test("TC-WEB-LIST-DETAIL-DOMAIN-003 웹 상세는 목록·상세 배치에 참여하지 않는다") {
            val key = WebDetailNavKey(id = Uuid.NIL)
            val backStack = listOf(OtherNavKey, WebHomeNavKey, key)

            metadataOf(backStack = backStack, key = key).shouldBeEmpty()
        }

        test("TC-TAG-DETAIL-WEB-FEATURE-019 TagDetail 웹 탭에서 이어진 웹 추가와 웹 상세는 목록·상세 배치에 참여하지 않는다") {
            val tagDetailKey = TagDetailNavKey(id = Uuid.random())
            val tagDetailBackStack = listOf(OtherNavKey, TagHomeNavKey, tagDetailKey)
            val addKey = WebAddNavKey(initialTagId = tagDetailKey.id)
            val detailKey = WebDetailNavKey(id = Uuid.random())

            metadataOf(backStack = tagDetailBackStack + addKey, key = addKey).shouldBeEmpty()
            metadataOf(backStack = tagDetailBackStack + detailKey, key = detailKey).shouldBeEmpty()
        }

        test("WebHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherNavKey, WebHomeNavKey)

            metadataOf(backStack = backStack, key = WebHomeNavKey).keys shouldBe listPaneMetadataKeys
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
            val provider = entryProvider<ScreenNavKey> { webEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 웹 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
