package io.github.taetae98coding.diary.feature.playlist.ui

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.isPlaylistAddOnDetailPane
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class PlaylistListDetailPaneTest :
    FunSpec({
        test("TC-PLAYLIST-LIST-DETAIL-FEATURE-003 선택한 곡이 없거나 곡 추가로 이동했으면 상세 영역에 곡 추가가 놓인다") {
            val backStackCases =
                listOf(
                    listOf(PlaylistHomeNavKey),
                    listOf(OtherScreenNavKey, PlaylistHomeNavKey),
                    listOf(OtherScreenNavKey, PlaylistHomeNavKey, MusicAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isPlaylistAddOnDetailPane() shouldBe true
            }
        }

        test("TC-PLAYLIST-LIST-DETAIL-FEATURE-004 곡 상세로 이동했으면 상세 영역에 곡 추가가 놓이지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherScreenNavKey, PlaylistHomeNavKey, MusicDetailNavKey(id = Uuid.random())),
                    listOf(OtherScreenNavKey, PlaylistHomeNavKey, MusicAddNavKey, MusicDetailNavKey(id = Uuid.random())),
                )

            backStackCases.forEach { backStack ->
                backStack.isPlaylistAddOnDetailPane() shouldBe false
            }
        }

        test("곡 목록에서 진입하지 않은 곡 추가는 상세 영역에 놓인 것으로 보지 않는다") {
            val backStackCases =
                listOf(
                    emptyList(),
                    listOf(OtherScreenNavKey),
                    listOf(OtherScreenNavKey, MusicAddNavKey),
                    listOf(PlaylistHomeNavKey, OtherScreenNavKey, MusicAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isPlaylistAddOnDetailPane() shouldBe false
            }
        }
    })

// 곡 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherScreenNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
