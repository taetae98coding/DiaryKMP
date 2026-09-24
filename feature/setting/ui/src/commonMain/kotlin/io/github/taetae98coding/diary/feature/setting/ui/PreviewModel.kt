package io.github.taetae98coding.diary.feature.setting.ui

import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountrySetting
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting

internal fun previewHolidaySettingList(): List<HolidaySetting> =
    listOf(
        HolidaySetting(name = "신정", isHoliday = true, isVisible = true),
        HolidaySetting(name = "초복", isHoliday = false, isVisible = false),
    )

internal fun previewHolidayCountrySetting(): HolidayCountrySetting =
    HolidayCountrySetting(
        selectedOptionSet = setOf(HolidayCountryOption.DEVICE),
        deviceCountry = HolidayCountry.KOREA,
    )

internal fun previewGeminiSetting(): GeminiSetting =
    GeminiSetting(
        apiKey = "AIzaSyExampleApiKeyValue",
        model = "models/gemini-3.6-flash",
        systemPrompt = "사용자의 요청을 메모 한 건으로 정리한다.",
    )

internal fun previewChromeProfileList(): List<ChromeProfile> =
    listOf(
        ChromeProfile(directory = "Default", name = "TaeJong"),
        ChromeProfile(directory = "Profile 1", name = "Work"),
    )

internal fun previewMusicDownloadProxyAddressList(): List<String> =
    listOf(
        "http://192.168.0.10:27180",
        "http://10.0.0.5:27180",
    )

internal fun previewMusicDownloadProxySetting(): MusicDownloadProxySetting = MusicDownloadProxySetting(address = "http://192.168.0.10:27180")
