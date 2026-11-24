package io.github.taetae98coding.diary.app.shared.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.work.appaction.AppActionGraph
import io.github.taetae98coding.diary.work.fileupload.scheduler.FileUploadWorkInitializer
import io.github.taetae98coding.diary.work.spotlight.SpotlightWorkLauncher
import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncBackgroundTaskRegistrar

@DependencyGraph(AppScope::class)
public interface IosAppGraph :
    AppGraph,
    AppActionGraph {
    public val appDeepLink: AppDeepLink

    public val periodicSyncBackgroundTaskRegistrar: PeriodicSyncBackgroundTaskRegistrar

    public val fileUploadWorkInitializer: FileUploadWorkInitializer

    public val spotlightWorkLauncher: SpotlightWorkLauncher
}
