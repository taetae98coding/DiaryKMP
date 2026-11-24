package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.core.navigation.DeepLinkNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller

@Inject
@SingleIn(AppScope::class)
public class AppDependencies(
    internal val appDeepLink: AppDeepLink,
    deepLinkNavigationSet: Set<DeepLinkNavigation>,
    entryInstallerSet: Set<ScreenEntryInstaller>,
    internal val mapContentOverride: DiaryMapContentOverride,
    internal val viewModelFactory: MetroViewModelFactory,
) {
    internal val deepLinkNavigationList: List<DeepLinkNavigation> = deepLinkNavigationSet.toList()
    internal val entryInstallerList: List<ScreenEntryInstaller> = entryInstallerSet.toList()
}
