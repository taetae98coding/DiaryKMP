package io.github.taetae98coding.diary.library.compose.ui.color

import androidx.compose.ui.graphics.Color
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class ColorExtTest : FunSpec() {
    init {
        test("컬러를 # 대문자 6자리 Hex 문자열로 변환한다") {
            Color(color = 0xFF3A7BD5.toInt()).toHexString() shouldBe "#3A7BD5"
            Color(color = 0xFF000000.toInt()).toHexString() shouldBe "#000000"
            Color(color = 0xFFFFFFFF.toInt()).toHexString() shouldBe "#FFFFFF"
            Color(color = 0xFF000A0B.toInt()).toHexString() shouldBe "#000A0B"
        }

        test("컬러를 RGB 채널의 10진수 문자열로 변환한다") {
            Color(color = 0xFF3A7BD5.toInt()).toRgbString() shouldBe "RGB(58, 123, 213)"
            Color(color = 0xFF000000.toInt()).toRgbString() shouldBe "RGB(0, 0, 0)"
            Color(color = 0xFFFFFFFF.toInt()).toRgbString() shouldBe "RGB(255, 255, 255)"
        }

        test("유효한 Hex 문자열을 컬러로 변환한다") {
            parseHexColorOrNull("#3A7BD5") shouldBe Color(color = 0xFF3A7BD5.toInt())
            parseHexColorOrNull("3A7BD5") shouldBe Color(color = 0xFF3A7BD5.toInt())
            parseHexColorOrNull("#3a7bd5") shouldBe Color(color = 0xFF3A7BD5.toInt())
        }

        test("유효하지 않은 Hex 문자열은 null을 반환한다") {
            listOf(
                "",
                "#",
                "#3A7BD",
                "#3A7BD5F",
                "#GGGGGG",
                "+23456",
                "-23456",
                " 3A7BD5",
                "##3A7BD5",
            ).forEach { input ->
                parseHexColorOrNull(input).shouldBeNull()
            }
        }

        test("무작위 컬러는 완전 불투명한 컬러다") {
            repeat(times = 100) {
                randomColor().alpha shouldBe 1F
            }
        }

        test("같은 시드로 생성한 무작위 컬러는 동일하다") {
            val seed = fixtureMonkey.giveMeOne<Long>()

            randomColor(random = Random(seed = seed)) shouldBe randomColor(random = Random(seed = seed))
        }

        test("임의 컬러의 Hex 변환과 파싱은 왕복 보존된다") {
            repeat(times = 10) {
                val color = Color(color = 0xFF000000.toInt() or (fixtureMonkey.giveMeOne<Int>() and 0xFFFFFF))

                parseHexColorOrNull(color.toHexString()) shouldBe color
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
