package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ColorLongExtTest : FunSpec() {
    init {
        test("컬러와 Long 변환은 왕복 보존된다") {
            repeat(times = 10) {
                val color = Color(color = fixtureMonkey.giveMeOne<Int>())

                color.toColorLong().toColor() shouldBe color
            }
        }

        test("알파를 포함한 ARGB 값을 그대로 옮긴다") {
            Color(color = 0xFF3A7BD5.toInt()).toColorLong() shouldBe 0xFF3A7BD5.toInt().toLong()
            0xFF3A7BD5.toInt().toLong().toColor() shouldBe Color(color = 0xFF3A7BD5.toInt())
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
