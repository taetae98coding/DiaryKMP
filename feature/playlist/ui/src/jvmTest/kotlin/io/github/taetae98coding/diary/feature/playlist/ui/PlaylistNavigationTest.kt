package io.github.taetae98coding.diary.feature.playlist.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.uuid.Uuid

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

        test("TC-PLAYLIST-LIST-DETAIL-FEATURE-014 곡 상세가 놓여 있으면 새 곡의 상세로 교체한다") {
            val first = MusicDetailNavKey(id = Uuid.random())
            val second = MusicDetailNavKey(id = Uuid.random())
            val backStack = playlistBackStack(detailKeyList = listOf(first))

            backStack.navigateToMusicDetail(id = second.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, PlaylistHomeNavKey, second)
        }

        test("곡 상세가 놓여 있지 않으면 곡 상세를 이어서 놓는다") {
            val detail = MusicDetailNavKey(id = Uuid.random())
            listOf(emptyList(), listOf<ScreenNavKey>(MusicAddNavKey)).forEach { detailKeyList ->
                val backStack = playlistBackStack(detailKeyList = detailKeyList)

                backStack.navigateToMusicDetail(id = detail.id)

                backStack shouldContainExactly listOf(MoreHomeStubNavKey, PlaylistHomeNavKey) + detailKeyList + detail
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
