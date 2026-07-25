package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.text.input.TextFieldState
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

        test("clearText는 텍스트를 모두 지운다") {
            val state = diaryDescriptionInputState(initialText = fixtureMonkey.giveMeOne<String>())

            state.clearText()

            state.text.toString() shouldBe ""
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun diaryDescriptionInputState(initialText: String = ""): DiaryDescriptionInputState =
            DiaryDescriptionInputState(
                textFieldState = TextFieldState(initialText = initialText),
                swipeState = AnchoredDraggableState(initialValue = DiaryDescriptionInputPage.Input),
            )
    }
}
