@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class MemoEntryTest :
    FunSpec({
        test("TC-MEMO-LIST-DETAIL-FEATURE-017 메모 목록에서 이어 진입한 상세 화면은 목록·상세 배치의 상세 pane이다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val addKey = MemoAddNavKey()
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey) to addKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, detailKey) to detailKey,
                    // 목록 pane까지 스캔이 이어지도록 최상단이 아닌 상세 화면도 상세 pane을 유지해야 한다.
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey, detailKey) to addKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, addKey) to addKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, addKey) to detailKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey, MemoHomeFilterNavKey) to detailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
            }
        }

        test(
            "TC-MEMO-LIST-DETAIL-FEATURE-016 TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-011 " +
                "메모 목록이나 완료된 메모 목록에서 진입하지 않은 상세 화면은 목록·상세 배치에 참여하지 않는다",
        ) {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val calendarAddKey =
                MemoAddNavKey(
                    initialDateRange =
                        MemoAddNavKey.InitialDateRange(
                            start = LocalDate(2026, 8, 3),
                            endInclusive = LocalDate(2026, 8, 4),
                        ),
                )
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, detailKey) to detailKey,
                    listOf(OtherTopLevelNavKey, calendarAddKey) to calendarAddKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, MemoFinishedListNavKey, detailKey) to detailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).shouldBeEmpty()
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-010 TagMemoFinishedList에서 이어 진입한 상세 화면은 목록·상세 배치의 상세 pane이다") {
            val tagId = Uuid.random()
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = tagId)
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val backStackCases =
                listOf(
                    listOf(OtherTopLevelNavKey, tagMemoFinishedListKey, detailKey) to detailKey,
                    listOf(
                        OtherTopLevelNavKey,
                        TagDetailNavKey(id = tagId),
                        tagMemoFinishedListKey,
                        detailKey,
                    ) to detailKey,
                )

            backStackCases.forEach { (backStack, key) ->
                metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
            }
        }

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-011 상세 pane의 scene 키는 진입한 목록 pane마다 다르다") {
            val tagId = Uuid.random()
            val tagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = tagId)
            val otherTagMemoFinishedListKey = TagMemoFinishedListNavKey(tagId = Uuid.random())
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val addKey = MemoAddNavKey()
            val sceneKeyCases =
                listOf(
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, detailKey) to MemoHomeNavKey,
                    listOf(OtherTopLevelNavKey, MemoHomeNavKey, addKey) to MemoHomeNavKey,
                    listOf(OtherTopLevelNavKey, tagMemoFinishedListKey, detailKey) to tagMemoFinishedListKey,
                    listOf(OtherTopLevelNavKey, otherTagMemoFinishedListKey, detailKey) to otherTagMemoFinishedListKey,
                    // TagHome 목록·상세 배치를 지나 진입해도 scene 키는 바로 위의 완료된 메모 목록이다.
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagDetailNavKey(id = tagId), tagMemoFinishedListKey, detailKey) to
                        tagMemoFinishedListKey,
                    // TagDetail 메모 탭에서 진입한 상세는 목록·상세 배치에 참여하지 않는다.
                    listOf(OtherTopLevelNavKey, TagHomeNavKey, TagDetailNavKey(id = tagId), detailKey) to null,
                )

            sceneKeyCases.forEach { (backStack, expectedSceneKey) ->
                backStack.memoDetailPaneSceneKey(key = backStack.last()) shouldBe expectedSceneKey
            }
        }

        test("목록·상세 배치에 참여하지 않는 상세 화면에는 scene 키가 없다") {
            val detailKey = MemoDetailNavKey(id = Uuid.random())
            val backStack = listOf(OtherTopLevelNavKey, detailKey)

            backStack.memoDetailPaneSceneKey(key = detailKey).shouldBeNull()
        }
    }) {
    companion object {
        private val detailPaneMetadataKeys: Set<String> =
            (ListDetailSceneStrategy.detailPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)).keys

        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { memoEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 캘린더 홈처럼 메모 목록이 아닌 진입 화면을 대신한다. 캘린더 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "OtherTopLevel"
}
