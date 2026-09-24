package io.github.taetae98coding.diary.feature.setting.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class SettingHomeItemListTest :
    FunSpec({
        test("TC-SETTING-HOME-DOMAIN-001 설정 항목은 공휴일, 지도, Gemini, 브라우저, 다운로드 순서다") {
            settingHomeItemList shouldContainExactly
                listOf(
                    SettingHomeItem.HOLIDAY,
                    SettingHomeItem.MAP,
                    SettingHomeItem.GEMINI,
                    SettingHomeItem.BROWSER,
                    SettingHomeItem.DOWNLOAD,
                )
        }

        test("설정 항목 목록은 모든 항목을 한 번씩만 담는다") {
            settingHomeItemList.toSet() shouldBe SettingHomeItem.entries.toSet()
            settingHomeItemList.size shouldBe SettingHomeItem.entries.size
        }
    })
