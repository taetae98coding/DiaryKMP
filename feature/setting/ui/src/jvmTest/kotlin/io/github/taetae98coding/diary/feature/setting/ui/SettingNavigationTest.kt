package io.github.taetae98coding.diary.feature.setting.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingBrowserNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingDownloadNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class SettingNavigationTest :
    FunSpec({
        test("TC-SETTING-LIST-DETAIL-FEATURE-002 설정 상세로 이동하면 설정 목록 다음에 상세를 추가한다") {
            settingDetailList.forEach { destination ->
                val backStack = settingBackStack()

                backStack.navigateToSettingDetail(destination = destination)

                backStack shouldContainExactly listOf(MoreHomeNavKey, SettingHomeNavKey, destination)
            }
        }

        test("TC-SETTING-LIST-DETAIL-FEATURE-003 다른 설정 상세로 이동하면 현재 상세를 교체한다") {
            val detailCases =
                listOf(
                    SettingHolidayNavKey to SettingMapNavKey,
                    SettingMapNavKey to SettingHolidayNavKey,
                    SettingHolidayNavKey to SettingGeminiNavKey,
                    SettingGeminiNavKey to SettingMapNavKey,
                    SettingGeminiNavKey to SettingBrowserNavKey,
                    SettingGeminiNavKey to SettingDownloadNavKey,
                    SettingBrowserNavKey to SettingHolidayNavKey,
                    SettingBrowserNavKey to SettingDownloadNavKey,
                    SettingDownloadNavKey to SettingMapNavKey,
                )

            detailCases.forEach { (currentDetail, destination) ->
                val backStack = settingBackStack(currentDetail)

                backStack.navigateToSettingDetail(destination = destination)

                backStack shouldContainExactly listOf(MoreHomeNavKey, SettingHomeNavKey, destination)
            }
        }

        test("TC-SETTING-LIST-DETAIL-FEATURE-011 현재 설정 상세를 다시 선택하면 전환 이력을 유지한다") {
            settingDetailList.forEach { detail ->
                val backStack = settingBackStack(detail)

                backStack.navigateToSettingDetail(destination = detail)

                backStack shouldContainExactly listOf(MoreHomeNavKey, SettingHomeNavKey, detail)
            }
        }

        test("TC-SETTING-HOME-FEATURE-003 뒤로가기 동작을 선택하면 상세 표시 여부와 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf<ScreenNavKey?>(
                    null,
                    SettingHolidayNavKey,
                    SettingMapNavKey,
                    SettingGeminiNavKey,
                    SettingBrowserNavKey,
                    SettingDownloadNavKey,
                )

            detailCases.forEach { detail ->
                val backStack = settingBackStack(*listOfNotNull(detail).toTypedArray())

                backStack.navigateUpFromSettingHome()

                backStack shouldContainExactly listOf(MoreHomeNavKey)
            }
        }

        test("설정 목록이 없는 전환 이력에서는 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeNavKey)

            backStack.navigateUpFromSettingHome()

            backStack shouldContainExactly listOf(MoreHomeNavKey)
        }
    })

private val settingDetailList: List<ScreenNavKey> =
    listOf(
        SettingHolidayNavKey,
        SettingMapNavKey,
        SettingGeminiNavKey,
        SettingBrowserNavKey,
        SettingDownloadNavKey,
    )

private fun settingBackStack(vararg detail: ScreenNavKey): NavBackStack<ScreenNavKey> =
    NavBackStack(
        MoreHomeNavKey,
        SettingHomeNavKey,
        *detail,
    )
