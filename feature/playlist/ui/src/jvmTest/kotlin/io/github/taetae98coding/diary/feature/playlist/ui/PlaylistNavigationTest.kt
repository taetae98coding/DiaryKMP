package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class PlaylistNavigationTest :
    FunSpec({
        test("TC-PLAYLIST-HOME-FEATURE-014 곡 목록의 뒤로가기 동작은 상세 영역의 화면과 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(MusicAddNavKey),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = playlistBackStack(detailKeyList = detailKeyList)

                backStack.navigateUpFromPlaylistHome()

                backStack shouldContainExactly listOf(MoreHomeStubNavKey)
            }
        }

        test("곡 목록이 없는 전환 이력에서는 곡 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeStubNavKey, MusicAddNavKey)
            val expected = backStack.toList()

            backStack.navigateUpFromPlaylistHome()

            backStack shouldContainExactly expected
        }
    })

// 곡 목록으로 진입하는 `더보기` 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreHomeStubNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStub"
}

private fun playlistBackStack(detailKeyList: List<ScreenNavKey>): NavBackStack<ScreenNavKey> =
    NavBackStack(
        MoreHomeStubNavKey,
        PlaylistHomeNavKey,
        *detailKeyList.toTypedArray(),
    )
