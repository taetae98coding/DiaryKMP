package io.github.taetae98coding.diary.feature.place.ui

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CoordinateTextMapperTest :
    FunSpec({
        test("좌표는 소수점 여섯째 자리까지 반올림하고 끝자리 0을 남긴 고정 자릿수로 표시한다") {
            val cases =
                listOf(
                    37.5665351 to "37.566535",
                    126.9779692 to "126.977969",
                    37.5 to "37.500000",
                    0.0 to "0.000000",
                    0.0001 to "0.000100",
                    0.0000004 to "0.000000",
                    -0.0001 to "-0.000100",
                    -33.8688197 to "-33.868820",
                    180.0 to "180.000000",
                    -180.0 to "-180.000000",
                )

            cases.forEach { (value, expected) ->
                value.toCoordinateText() shouldBe expected
            }
        }
    })
