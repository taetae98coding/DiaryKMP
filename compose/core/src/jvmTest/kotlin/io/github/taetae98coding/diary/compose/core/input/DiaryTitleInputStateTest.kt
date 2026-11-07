package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

class DiaryTitleInputStateTest :
    FunSpec({
        test("text는 TextFieldState의 현재 텍스트를 반영한다") {
            val value = Arb.string().next()

            val state = titleInputState(initialText = value)

            state.text.toString() shouldBe value
        }

        test("clearText는 입력된 텍스트를 비운다") {
            val value = Arb.string().next()
            val state = titleInputState(initialText = value)

            state.clearText()

            state.text.toString() shouldBe ""
        }

        test("setText는 기존 텍스트를 새 텍스트로 대체한다") {
            val initialText = Arb.string().next()
            val nextText = Arb.string().next()
            val state = titleInputState(initialText = initialText)

            state.setText(nextText)

            state.text.toString() shouldBe nextText
        }

        test("setText는 커서를 텍스트 끝에 놓는다") {
            val value = Arb.string().next()
            val state = titleInputState()

            state.setText(value)

            state.textFieldState.selection shouldBe TextRange(value.length)
        }
    }) {
    public companion object {
        private fun titleInputState(initialText: String = ""): DiaryTitleInputState =
            DiaryTitleInputState(
                textFieldState = TextFieldState(initialText = initialText),
                focusRequester = FocusRequester(),
            )
    }
}
