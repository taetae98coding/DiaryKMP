package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.focus.FocusRequester
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val FETCHED_TITLE: String = "FetchedMusicTitle"
private const val FETCHED_ARTIST: String = "FetchedMusicArtist"
private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
private const val YOUTUBE_THUMBNAIL: String = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"
private const val OTHER_YOUTUBE_LINK: String = "https://youtu.be/ArmDp-zijuc"
private const val OTHER_YOUTUBE_THUMBNAIL: String = "https://i.ytimg.com/vi/ArmDp-zijuc/hqdefault.jpg"
private const val YOUTUBE_CHANNEL_LINK: String = "https://www.youtube.com/@channel"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MusicFormStateTest :
    FunSpec({
        test("TC-MUSIC-ADD-FEATURE-016 불러오기에 성공하면 제목과 가수를 채우고 링크와 썸네일은 그대로 둔다") {
            val state = formState(link = YOUTUBE_LINK)

            state.fill(link = state.link, title = FETCHED_TITLE, artist = FETCHED_ARTIST)

            state.detail.title shouldBe FETCHED_TITLE
            state.detail.artist shouldBe FETCHED_ARTIST
            state.detail.link shouldBe YOUTUBE_LINK
            state.thumbnail shouldBe YOUTUBE_THUMBNAIL
        }

        listOf(
            "" to "빈 값",
            "   " to "공백 문자로만 이루어진 값",
        ).forEach { (blank, label) ->
            test("TC-MUSIC-ADD-FEATURE-024 제목과 가수가 $label 이면 불러온 값으로 채운다") {
                val state = formState(title = blank, artist = blank)

                state.fill(link = state.link, title = FETCHED_TITLE, artist = FETCHED_ARTIST)

                state.detail.title shouldBe FETCHED_TITLE
                state.detail.artist shouldBe FETCHED_ARTIST
            }
        }

        test("TC-MUSIC-ADD-FEATURE-024 이미 채워진 제목과 가수도 불러온 값으로 덮어쓴다") {
            val state =
                formState(
                    title = "title-${fixtureMonkey.giveMeOne<String>()}",
                    artist = "artist-${fixtureMonkey.giveMeOne<String>()}",
                )

            state.fill(link = state.link, title = FETCHED_TITLE, artist = FETCHED_ARTIST)

            state.detail.title shouldBe FETCHED_TITLE
            state.detail.artist shouldBe FETCHED_ARTIST
        }

        listOf(
            OTHER_YOUTUBE_LINK to "다른 영상의 링크로 바꾼",
            "" to "지운",
        ).forEach { (changedLink, label) ->
            test("TC-MUSIC-ADD-FEATURE-035 TC-MUSIC-DETAIL-FEATURE-033 불러오는 동안 링크를 $label 뒤 도착한 결과는 채우지 않는다") {
                val title = "title-${fixtureMonkey.giveMeOne<String>()}"
                val artist = "artist-${fixtureMonkey.giveMeOne<String>()}"
                val state = formState(title = title, artist = artist, link = YOUTUBE_LINK)
                state.linkState.textFieldState.setTextAndPlaceCursorAtEnd(changedLink)

                state.fill(link = YOUTUBE_LINK, title = FETCHED_TITLE, artist = FETCHED_ARTIST)

                state.detail.title shouldBe title
                state.detail.artist shouldBe artist
                state.detail.link shouldBe changedLink
            }
        }

        test("TC-MUSIC-ADD-FEATURE-035 TC-MUSIC-DETAIL-FEATURE-033 링크를 바꿨다가 실행한 때의 링크로 되돌리면 도착한 결과를 채운다") {
            val state = formState(link = YOUTUBE_LINK)
            state.linkState.textFieldState.setTextAndPlaceCursorAtEnd(OTHER_YOUTUBE_LINK)
            state.linkState.textFieldState.setTextAndPlaceCursorAtEnd(YOUTUBE_LINK)

            state.fill(link = YOUTUBE_LINK, title = FETCHED_TITLE, artist = FETCHED_ARTIST)

            state.detail.title shouldBe FETCHED_TITLE
            state.detail.artist shouldBe FETCHED_ARTIST
        }

        listOf(
            YOUTUBE_LINK to YOUTUBE_THUMBNAIL,
            OTHER_YOUTUBE_LINK to OTHER_YOUTUBE_THUMBNAIL,
            "" to "",
            YOUTUBE_CHANNEL_LINK to "",
            "https://example.com/watch?v=dQw4w9WgXcQ" to "",
        ).forEach { (link, thumbnail) ->
            test("TC-MUSIC-ADD-FEATURE-028 TC-MUSIC-DETAIL-FEATURE-028 링크에서 썸네일이 정해진다: '$link'") {
                formState(link = link).thumbnail shouldBe thumbnail
            }
        }

        test("TC-MUSIC-ADD-FEATURE-030 TC-MUSIC-DETAIL-FEATURE-029 링크를 다른 영상으로 바꾸면 썸네일도 그 영상으로 바뀐다") {
            val state = formState(link = YOUTUBE_LINK)

            state.linkState.textFieldState.setTextAndPlaceCursorAtEnd(OTHER_YOUTUBE_LINK)

            state.thumbnail shouldBe OTHER_YOUTUBE_THUMBNAIL
        }

        test("TC-MUSIC-ADD-FEATURE-030 TC-MUSIC-DETAIL-FEATURE-029 링크를 지우거나 영상이 아닌 주소로 바꾸면 썸네일이 사라진다") {
            val state = formState(link = YOUTUBE_LINK)

            state.linkState.clearText()
            state.thumbnail shouldBe ""

            state.linkState.textFieldState.setTextAndPlaceCursorAtEnd(YOUTUBE_CHANNEL_LINK)
            state.thumbnail shouldBe ""
        }

        test("TC-MUSIC-ADD-FEATURE-004 작성 내용을 비우면 썸네일도 함께 사라진다") {
            val state =
                formState(
                    title = "title-${fixtureMonkey.giveMeOne<String>()}",
                    artist = "artist-${fixtureMonkey.giveMeOne<String>()}",
                    link = YOUTUBE_LINK,
                )

            state.clearText()

            state.detail.title shouldBe ""
            state.detail.artist shouldBe ""
            state.detail.link shouldBe ""
            state.thumbnail shouldBe ""
        }
    }) {
    public companion object {
        private fun formState(
            title: String = "",
            artist: String = "",
            link: String = "",
        ): MusicFormState =
            MusicFormState(
                titleState =
                    DiaryTitleInputState(
                        textFieldState = TextFieldState(initialText = title),
                        focusRequester = FocusRequester(),
                    ),
                artistState =
                    MusicArtistInputState(
                        textFieldState = TextFieldState(initialText = artist),
                        focusRequester = FocusRequester(),
                    ),
                linkState =
                    MusicLinkInputState(
                        textFieldState = TextFieldState(initialText = link),
                        focusRequester = FocusRequester(),
                    ),
                hostState = SnackbarHostState(),
            )
    }
}
