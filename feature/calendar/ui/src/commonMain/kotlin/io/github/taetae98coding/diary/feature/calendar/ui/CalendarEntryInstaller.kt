package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class CalendarEntryInstaller : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        calendarEntry(
            backStack = navigator.backStack,
            homeReselectEvent = navigator.reselectEventOf(CalendarHomeNavKey),
        )
    }
}
