package io.github.taetae98coding.diary.feature.contact.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class ContactEntryInstaller(
    private val contentSharerFactory: ContentSharerFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        contactEntry(backStack = navigator.backStack, contentSharerFactory = contentSharerFactory)
    }
}
