package io.github.taetae98coding.diary.app.shared.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.app.shared.di.DiaryKoinApplication
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import org.koin.plugin.module.dsl.koinApplication

private val appEntryInstallerList: List<ScreenEntryInstaller> by lazy {
    koinApplication<DiaryKoinApplication>().koin.getAll()
}

internal fun appEntryProvider(backStack: NavBackStack<ScreenNavKey>): (ScreenNavKey) -> NavEntry<ScreenNavKey> {
    val navigator =
        mockk<ScreenNavigator> {
            every { this@mockk.backStack } returns backStack
            every { reselectEventOf(any()) } returns emptyFlow()
        }

    return entryProvider {
        appEntryInstallerList.forEach { installer -> with(installer) { install(navigator = navigator) } }
    }
}
