package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.focus.FocusRequester
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DiaryDescriptionInputStateTest :
    FunSpec({
        test("text는 현재 입력된 텍스트를 반환한다") {
            val initialText = fixtureMonkey.giveMeOne<String>()
            val state = diaryDescriptionInputState(initialText = initialText)

            state.text.toString() shouldBe initialText
        }

        test("setText는 텍스트를 새 값으로 바꾼다") {
            val newText = fixtureMonkey.giveMeOne<String>()
            val state = diaryDescriptionInputState(initialText = fixtureMonkey.giveMeOne<String>())

            state.setText(newText)

            state.text.toString() shouldBe newText
        }

        test("입력 페이지이면 focusTarget은 입력 칸의 FocusRequester를 반환한다") {
            val focusRequester = FocusRequester()
            val state = diaryDescriptionInputState(page = DiaryDescriptionInputPage.Input, focusRequester = focusRequester)

            state.focusTarget shouldBe focusRequester
        }

        test("미리보기 페이지이면 focusTarget은 FocusRequester.Default를 반환한다") {
            val state = diaryDescriptionInputState(page = DiaryDescriptionInputPage.Preview)

            state.focusTarget shouldBe FocusRequester.Default
        }

        test("clearText는 텍스트를 모두 지운다") {
            val state = diaryDescriptionInputState(initialText = fixtureMonkey.giveMeOne<String>())

            state.clearText()

            state.text.toString() shouldBe ""
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun diaryDescriptionInputState(
            initialText: String = "",
            page: DiaryDescriptionInputPage = DiaryDescriptionInputPage.Input,
            focusRequester: FocusRequester = FocusRequester(),
        ): DiaryDescriptionInputState =
            DiaryDescriptionInputState(
                textFieldState = TextFieldState(initialText = initialText),
                swipeState = AnchoredDraggableState(initialValue = page),
                focusRequester = focusRequester,
            )
    }
}
