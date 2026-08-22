package io.github.taetae98coding.diary.feature.setting.ui

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting

internal fun previewHolidaySettingList(): List<HolidaySetting> =
    listOf(
        HolidaySetting(key = "신정", name = "신정", isHoliday = true, isVisible = true),
        HolidaySetting(key = "초복", name = "초복", isHoliday = false, isVisible = false),
    )

internal fun previewGeminiSetting(): GeminiSetting =
    GeminiSetting(
        apiKey = "AIzaSyExampleApiKeyValue",
        model = "models/gemini-3.6-flash",
        systemPrompt = "사용자의 요청을 메모 한 건으로 정리한다.",
    )
