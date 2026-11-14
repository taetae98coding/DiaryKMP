package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import org.koin.core.annotation.Single

@Single
public class AppDependencies(
    internal val appDeepLink: AppDeepLink,
    internal val deepLinkNavigationList: List<DeepLinkNavigation>,
    internal val entryInstallerList: List<ScreenEntryInstaller>,
    internal val mapContentOverride: DiaryMapContentOverride,
)
