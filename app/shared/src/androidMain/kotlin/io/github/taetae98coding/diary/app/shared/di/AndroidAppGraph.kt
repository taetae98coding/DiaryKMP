package io.github.taetae98coding.diary.app.shared.di

import android.content.Context
import androidx.work.WorkerFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink
import io.github.taetae98coding.diary.app.shared.share.ShareStreamReader
import io.github.taetae98coding.diary.work.appaction.AppActionGraph

@DependencyGraph(AppScope::class)
public interface AndroidAppGraph :
    AppGraph,
    AppActionGraph {
    public val appDeepLink: AppDeepLink

    public val shareStreamReader: ShareStreamReader

    public val workerFactory: WorkerFactory

    @DependencyGraph.Factory
    public fun interface Factory {
        public fun create(
            @Provides context: Context,
        ): AndroidAppGraph
    }
}

public fun createAndroidAppGraph(context: Context): AndroidAppGraph = createGraphFactory<AndroidAppGraph.Factory>().create(context = context)
