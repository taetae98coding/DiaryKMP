package io.github.taetae98coding.diary.compose.core.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DiaryEmojiInputStateTest :
    FunSpec({
        test("초기 텍스트는 사용자에게 보이는 마지막 문자만 남는다") {
            val state = DiaryEmojiInputState(initialText = RUNNER + SWIMMER)

            state.text shouldBe SWIMMER
        }

        test("setText는 사용자에게 보이는 마지막 문자만 남긴다") {
            val state = DiaryEmojiInputState(initialText = "")

            state.setText(text = SWIMMER + MAN_RUNNING)

            state.text shouldBe MAN_RUNNING
        }

        test("setText는 이미 문자가 있어도 새 문자로 대체한다") {
            val state = DiaryEmojiInputState(initialText = RUNNER)

            state.setText(text = SWIMMER)

            state.text shouldBe SWIMMER
        }

        test("setText는 이모지가 아닌 문자도 마지막 문자를 그대로 남긴다") {
            val state = DiaryEmojiInputState(initialText = "")

            state.setText(text = PLAIN_TEXT)

            state.text shouldBe PLAIN_TEXT.takeLast(1)
        }

        test("clearText는 입력된 문자를 비운다") {
            val state = DiaryEmojiInputState(initialText = RUNNER)

            state.clearText()

            state.text shouldBe ""
        }
    }) {
    public companion object {
        private const val RUNNER = "🏃"
        private const val SWIMMER = "🏊"
        private const val MAN_RUNNING = "🏃‍♂️"
        private const val PLAIN_TEXT = "abc"
    }
}
