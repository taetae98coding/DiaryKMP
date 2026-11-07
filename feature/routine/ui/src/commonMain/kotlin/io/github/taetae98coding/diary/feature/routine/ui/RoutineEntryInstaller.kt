package io.github.taetae98coding.diary.feature.routine.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class RoutineEntryInstaller : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        routineEntry(
            backStack = navigator.backStack,
            homeReselectEvent = navigator.reselectEventOf(RoutineHomeNavKey),
        )
    }
}
