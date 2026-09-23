package io.github.taetae98coding.diary.feature.playlist.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val FETCHED_TITLE: String = "FetchedMusicTitle"
private const val FETCHED_ARTIST: String = "FetchedMusicArtist"
private const val FETCHED_THUMBNAIL: String = "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class MusicFormStateTest :
    FunSpec({
        test("TC-MUSIC-ADD-FEATURE-016 불러오기에 성공하면 제목과 가수와 썸네일을 채운다") {
            val state = formState()

            state.fill(title = FETCHED_TITLE, artist = FETCHED_ARTIST, thumbnail = FETCHED_THUMBNAIL)

            state.detail.title shouldBe FETCHED_TITLE
            state.detail.artist shouldBe FETCHED_ARTIST
            state.detail.thumbnail shouldBe FETCHED_THUMBNAIL
        }

        listOf(
            "" to "빈 값",
            "   " to "공백 문자로만 이루어진 값",
        ).forEach { (blank, label) ->
            test("TC-MUSIC-ADD-FEATURE-024 제목과 가수가 $label 이면 불러온 값으로 채운다") {
                val state = formState(title = blank, artist = blank)

                state.fill(title = FETCHED_TITLE, artist = FETCHED_ARTIST, thumbnail = FETCHED_THUMBNAIL)

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

            state.fill(title = FETCHED_TITLE, artist = FETCHED_ARTIST, thumbnail = FETCHED_THUMBNAIL)

            state.detail.title shouldBe FETCHED_TITLE
            state.detail.artist shouldBe FETCHED_ARTIST
        }

        test("TC-MUSIC-ADD-FEATURE-025 이미 불러온 썸네일도 새로 불러온 주소로 덮어쓴다") {
            val state = formState(thumbnail = "https://i.ytimg.com/vi/${fixtureMonkey.giveMeOne<String>()}/hqdefault.jpg")

            state.fill(title = FETCHED_TITLE, artist = FETCHED_ARTIST, thumbnail = FETCHED_THUMBNAIL)

            state.detail.thumbnail shouldBe FETCHED_THUMBNAIL
        }

        test("TC-MUSIC-ADD-FEATURE-004 작성 내용을 비우면 썸네일도 함께 비워진다") {
            val state =
                formState(
                    link = "https://youtu.be/${fixtureMonkey.giveMeOne<String>()}",
                    title = "title-${fixtureMonkey.giveMeOne<String>()}",
                    artist = "artist-${fixtureMonkey.giveMeOne<String>()}",
                    thumbnail = FETCHED_THUMBNAIL,
                )

            state.clearText()

            state.detail.link shouldBe ""
            state.detail.title shouldBe ""
            state.detail.artist shouldBe ""
            state.detail.thumbnail shouldBe ""
        }
    }) {
    public companion object {
        private fun formState(
            link: String = "",
            title: String = "",
            artist: String = "",
            thumbnail: String = "",
        ): MusicFormState =
            MusicFormState(
                linkState =
                    MusicLinkInputState(
                        textFieldState = TextFieldState(initialText = link),
                        focusRequester = FocusRequester(),
                    ),
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
                hostState = SnackbarHostState(),
                thumbnailState = mutableStateOf(thumbnail),
            )
    }
}
