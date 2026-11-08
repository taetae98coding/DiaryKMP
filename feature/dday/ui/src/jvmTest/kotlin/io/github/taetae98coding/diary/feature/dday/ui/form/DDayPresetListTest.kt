package io.github.taetae98coding.diary.feature.dday.ui.form

import io.github.taetae98coding.diary.domain.dday.preset.DDayPreset
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class DDayPresetListTest :
    FunSpec({
        test("연인, 시험, 아기, 직접 입력 순서로 둔다") {
            ddayPresetList shouldContainExactly
                listOf(
                    DDayPreset.COUPLE,
                    DDayPreset.EXAM,
                    DDayPreset.BABY,
                    DDayPreset.CUSTOM,
                )
        }

        test("모든 프리셋을 한 번씩만 담는다") {
            ddayPresetList shouldContainExactlyInAnyOrder DDayPreset.entries
        }
    })
