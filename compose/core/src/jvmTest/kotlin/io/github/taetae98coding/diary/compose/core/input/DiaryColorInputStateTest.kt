package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class DiaryColorInputStateTest : FunSpec() {
    init {
        test("초기 컬러를 노출한다") {
            val initialColor = randomColor()

            val state = DiaryColorInputState(initialColor = initialColor)

            state.color shouldBe initialColor
        }

        test("Saver로 저장하고 복원하면 컬러가 보존된다") {
            val color = randomColor()
            val state = DiaryColorInputState(initialColor = color)

            val saved = with(DiaryColorInputState.Saver) { SaverScope { true }.save(state) }
            val restored = DiaryColorInputState.Saver.restore(saved.shouldNotBeNull())

            restored.shouldNotBeNull().color shouldBe color
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun randomColor(): Color = Color(color = 0xFF000000.toInt() or (fixtureMonkey.giveMeOne<Int>() and 0xFFFFFF))
    }
}
