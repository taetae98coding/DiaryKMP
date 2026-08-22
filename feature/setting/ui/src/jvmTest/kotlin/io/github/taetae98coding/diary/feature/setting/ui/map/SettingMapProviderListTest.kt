package io.github.taetae98coding.diary.feature.setting.ui.map

import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class SettingMapProviderListTest :
    FunSpec({
        test("고를 수 있는 지도는 네이버, Google 순서다") {
            settingMapProviderList shouldContainExactly
                listOf(
                    MapProvider.NAVER,
                    MapProvider.GOOGLE,
                )
        }

        test("지도 목록은 모든 지도를 한 번씩만 담는다") {
            settingMapProviderList.toSet() shouldBe MapProvider.entries.toSet()
            settingMapProviderList.size shouldBe MapProvider.entries.size
        }
    })
