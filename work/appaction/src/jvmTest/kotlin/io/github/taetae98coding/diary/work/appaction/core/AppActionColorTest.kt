package io.github.taetae98coding.diary.work.appaction.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next

class AppActionColorTest :
    FunSpec({
        test("투명도가 있는 색도 RGB만 16진수로 바꾼다") {
            val color = Arb.long().next()

            color.toColorHex() shouldBe "#" + (color.toInt() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()
        }
    })
