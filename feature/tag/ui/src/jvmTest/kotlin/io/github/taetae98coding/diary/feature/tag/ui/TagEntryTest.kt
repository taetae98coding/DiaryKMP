@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.emptyFlow
import kotlin.uuid.Uuid

class TagEntryTest :
    FunSpec({
        test("TC-TAG-LIST-DETAIL-FEATURE-016 태그 목록에서 이어 진입한 상세 화면은 목록·상세 배치의 상세 pane이다") {
            val detailKey = TagDetailNavKey(id = Uuid.random())
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagAddNavKey()) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagAddNavKey(), detailKey) to detailKey,
                    // 목록 pane까지 스캔이 이어지도록 최상단이 아닌 상세 화면도 상세 pane을 유지해야 한다.
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagAddNavKey(), detailKey) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, detailKey, TagAddNavKey()) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, detailKey, TagAddNavKey()) to detailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
            }
        }

        test("TC-TAG-LIST-DETAIL-FEATURE-015 태그 목록에서 진입하지 않은 상세 화면은 목록·상세 배치에 참여하지 않는다") {
            val memoDetailKey = MemoDetailNavKey(id = Uuid.random())
            val tagDetailKey = TagDetailNavKey(id = Uuid.random())
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, MemoHomeFilterNavKey, TagAddNavKey()) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, memoDetailKey, TagAddNavKey()) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, TagAddNavKey()) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, TagAddNavKey()) to TagAddNavKey(),
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, memoDetailKey, tagDetailKey) to tagDetailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).shouldBeEmpty()
            }
        }

        test("TC-TAG-HOME-FEATURE-027 태그 필터는 목록 위에 겹치는 Bottom Sheet로 표시한다") {
            val backStack = listOf(OtherTopLevelNavKey, TagHomeNavKey, TagHomeFilterNavKey)

            metadataOf(backStack = backStack, key = TagHomeFilterNavKey).keys shouldBe bottomSheetMetadataKeys
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-001 TagMemoFinishedList 화면은 목록·상세 배치의 목록 pane이다") {
            val tagId = Uuid.random()
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = tagId)
            val backStack =
                listOf(
                    OtherTopLevelNavKey,
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                    tagMemoFinishedListKey,
                )

            metadataOf(backStack = backStack, key = tagMemoFinishedListKey).keys shouldBe listPaneMetadataKeys
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private val bottomSheetMetadataKeys: Set<String> = BottomSheetSceneStrategy.bottomSheet().keys

        private val listPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.listPane(detailPlaceholder = {}) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider =
                entryProvider<ScreenNavKey> {
                    tagEntry(
                        backStack = NavBackStack(*backStack.toTypedArray()),
                        homeReselectEvent = emptyFlow(),
                    )
                }

            return provider(key).metadata
        }
    }
}

// 캘린더 홈처럼 태그 목록이 아닌 진입 화면을 대신한다. 캘린더 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "OtherTopLevel"
}
