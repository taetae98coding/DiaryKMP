package io.github.taetae98coding.diary.work.appaction.place

import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateOrNaN
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.take

class AppActionPlaceCoordinateNumberTest :
    FunSpec({
        test("좌표 숫자를 지수 표기 없는 소수 글로 바꾼다") {
            listOf(
                37.5665 to "37.5665",
                -126.978 to "-126.978",
                0.00001 to "0.00001",
                -0.000000001 to "-0.000000001",
                180.0 to "180",
                -0.0 to "0",
            ).forEach { (value, text) -> appActionCoordinateText(value = value) shouldBe text }
        }

        test("바꾼 글은 좌표 글로 다시 읽으면 같은 좌표가 된다") {
            Arb.numericDouble(min = -180.0, max = 180.0).take(SAMPLE_COUNT).forEach { value ->
                appActionCoordinateText(value = value).toPlaceCoordinateOrNaN() shouldBe (value plusOrMinus TOLERANCE)
            }
        }

        test("숫자가 아니거나 범위를 크게 벗어난 값은 좌표 글로 읽히지 않는다") {
            listOf(Double.NaN, Double.POSITIVE_INFINITY, 1.0e20).forEach { value ->
                appActionCoordinateText(value = value).toPlaceCoordinateOrNaN().isNaN() shouldBe true
            }
        }
    })

private const val SAMPLE_COUNT = 100
private const val TOLERANCE = 1.0e-9
