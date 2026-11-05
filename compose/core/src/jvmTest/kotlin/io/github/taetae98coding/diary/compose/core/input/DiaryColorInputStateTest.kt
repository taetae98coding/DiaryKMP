package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.saveable.SaverScope
import io.github.taetae98coding.diary.compose.core.opaqueColor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class DiaryColorInputStateTest : FunSpec() {
    init {
        test("초기 컬러를 노출한다") {
            val initialColor = Arb.opaqueColor().next()

            val state = DiaryColorInputState(initialColor = initialColor)

            state.color shouldBe initialColor
        }

        test("Saver로 저장하고 복원하면 컬러가 보존된다") {
            val color = Arb.opaqueColor().next()
            val state = DiaryColorInputState(initialColor = color)

            val saved = with(DiaryColorInputState.Saver) { SaverScope { true }.save(state) }
            val restored = DiaryColorInputState.Saver.restore(saved.shouldNotBeNull())

            restored.shouldNotBeNull().color shouldBe color
        }
    }
}
