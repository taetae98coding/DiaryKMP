package io.github.taetae98coding.diary.app.shared.navigation

import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.calendarNavKeys
import io.github.taetae98coding.diary.feature.checklist.api.checklistNavKeys
import io.github.taetae98coding.diary.feature.contact.api.contactNavKeys
import io.github.taetae98coding.diary.feature.dday.api.dDayNavKeys
import io.github.taetae98coding.diary.feature.file.api.fileNavKeys
import io.github.taetae98coding.diary.feature.holiday.api.holidayNavKeys
import io.github.taetae98coding.diary.feature.login.api.loginNavKeys
import io.github.taetae98coding.diary.feature.memo.api.memoNavKeys
import io.github.taetae98coding.diary.feature.more.api.moreNavKeys
import io.github.taetae98coding.diary.feature.place.api.placeNavKeys
import io.github.taetae98coding.diary.feature.playlist.api.playlistNavKeys
import io.github.taetae98coding.diary.feature.qr.api.qrNavKeys
import io.github.taetae98coding.diary.feature.routine.api.routineNavKeys
import io.github.taetae98coding.diary.feature.search.api.searchNavKeys
import io.github.taetae98coding.diary.feature.setting.api.settingNavKeys
import io.github.taetae98coding.diary.feature.tag.api.tagNavKeys
import io.github.taetae98coding.diary.feature.web.api.webNavKeys
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val AppNavKeySavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(ScreenNavKey::class) {
                    calendarNavKeys()
                    checklistNavKeys()
                    contactNavKeys()
                    dDayNavKeys()
                    fileNavKeys()
                    holidayNavKeys()
                    loginNavKeys()
                    placeNavKeys()
                    playlistNavKeys()
                    memoNavKeys()
                    moreNavKeys()
                    qrNavKeys()
                    routineNavKeys()
                    searchNavKeys()
                    settingNavKeys()
                    tagNavKeys()
                    webNavKeys()
                }
            }
    }
