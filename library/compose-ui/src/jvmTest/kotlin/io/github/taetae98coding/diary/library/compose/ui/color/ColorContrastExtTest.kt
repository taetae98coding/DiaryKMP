package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ColorContrastExtTest : FunSpec() {
    init {
        test("어두운 컬러 위에는 흰색을 올린다") {
            Color.Black.contentColor() shouldBe Color.White
            Color(color = 0xFF102030.toInt()).contentColor() shouldBe Color.White
        }

        test("밝은 컬러 위에는 검은색을 올린다") {
            Color.White.contentColor() shouldBe Color.Black
            Color(color = 0xFFF0E8D0.toInt()).contentColor() shouldBe Color.Black
        }
    }
}
