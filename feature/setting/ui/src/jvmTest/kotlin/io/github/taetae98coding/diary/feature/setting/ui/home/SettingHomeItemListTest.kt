package io.github.taetae98coding.diary.feature.setting.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class SettingHomeItemListTest :
    FunSpec({
        test("설정 항목은 공휴일, 지도, Gemini 순서다") {
            settingHomeItemList shouldContainExactly
                listOf(
                    SettingHomeItem.HOLIDAY,
                    SettingHomeItem.MAP,
                    SettingHomeItem.GEMINI,
                )
        }

        test("설정 항목 목록은 모든 항목을 한 번씩만 담는다") {
            settingHomeItemList.toSet() shouldBe SettingHomeItem.entries.toSet()
            settingHomeItemList.size shouldBe SettingHomeItem.entries.size
        }
    })
