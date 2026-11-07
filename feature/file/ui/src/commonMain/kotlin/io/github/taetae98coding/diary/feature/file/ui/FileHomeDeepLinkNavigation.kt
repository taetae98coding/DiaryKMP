package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.toDeepLinkNavigation
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.isFileListDetailPane
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import org.koin.core.annotation.Single

@Single(binds = [DeepLinkNavigation::class])
internal class FileHomeDeepLinkNavigation :
    DeepLinkNavigation by UriDeepLinkMatcher(DeepLinkUri(ScreenDeepLink.FILE_HOME), FileHomeNavKey.serializer())
        .toDeepLinkNavigation({ result -> backStack.openFileHome(topLevelBackStackOf(MoreHomeNavKey) + result.key) })

private fun NavBackStack<ScreenNavKey>.openFileHome(navKeyList: List<ScreenNavKey>) {
    if (isFileScreenVisible()) return

    clear()
    addAll(navKeyList)
}

private fun List<ScreenNavKey>.isFileScreenVisible(): Boolean =
    when (lastOrNull()) {
        FileHomeNavKey -> true
        FileAddNavKey -> isFileListDetailPane(key = FileAddNavKey)
        else -> false
    }
