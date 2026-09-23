@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class PlaylistEntryTest :
    FunSpec({
        test("TC-PLAYLIST-LIST-DETAIL-DOMAIN-002 곡 목록에서 이어 진입한 곡 추가는 목록·상세 배치의 상세 pane이다") {
            val backStack = listOf(OtherNavKey, PlaylistHomeNavKey, MusicAddNavKey)

            metadataOf(backStack = backStack, key = MusicAddNavKey).keys shouldBe detailPaneMetadataKeys
        }

        test("TC-PLAYLIST-LIST-DETAIL-DOMAIN-001 곡 목록에서 진입하지 않은 곡 추가는 목록·상세 배치에 참여하지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherNavKey, MusicAddNavKey),
                    listOf(PlaylistHomeNavKey, OtherNavKey, MusicAddNavKey),
                )

            backStackCases.forEach { backStack ->
                metadataOf(backStack = backStack, key = MusicAddNavKey).shouldBeEmpty()
            }
        }

        test("TC-PLAYLIST-LIST-DETAIL-DOMAIN-003 곡 목록에서 이어 진입한 곡 상세는 목록·상세 배치의 상세 pane이다") {
            val key = MusicDetailNavKey(id = Uuid.random())
            val backStack = listOf(OtherNavKey, PlaylistHomeNavKey, MusicAddNavKey, key)

            metadataOf(backStack = backStack, key = key).keys shouldBe detailPaneMetadataKeys
        }

        test("PlaylistHome 화면은 목록·상세 배치의 목록 pane이다") {
            val backStack = listOf(OtherNavKey, PlaylistHomeNavKey)

            metadataOf(backStack = backStack, key = PlaylistHomeNavKey).keys shouldBe listPaneMetadataKeys
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
            val provider = entryProvider<ScreenNavKey> { playlistEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 곡 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
