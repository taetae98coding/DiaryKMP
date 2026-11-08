package io.github.taetae98coding.diary.domain.dday.preset

import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DDayPresetValueTest :
    FunSpec({
        test("TC-f16ae9d6-9f63-4d20-8024-fe6497a12920 새 디데이의 기본값은 켬, 반복 간격 없음, 종료일 없음, 말일로 당김이다") {
            DDayPreset.CUSTOM.value() shouldBe
                DDayPresetValue(
                    countsBaseDay = true,
                    repeatList = emptyList(),
                    endDate = null,
                    overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
                )
        }

        // 추가 테스트: 프리셋은 기본값에서 반복 간격만 바꾼다.
        test("프리셋은 기본값에서 반복 간격만 바꾼다") {
            val default = DDayPreset.CUSTOM.value()

            DDayPreset.EXAM.value() shouldBe default
            DDayPreset.COUPLE.value() shouldBe
                default.copy(
                    repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR)),
                )
            DDayPreset.BABY.value() shouldBe
                default.copy(
                    repeatList =
                        listOf(
                            DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY),
                            DDayRepeatInput(number = 1, unit = DDayRepeatUnit.MONTH),
                            DDayRepeatInput(number = 1, unit = DDayRepeatUnit.YEAR),
                        ),
                )
        }
    })
