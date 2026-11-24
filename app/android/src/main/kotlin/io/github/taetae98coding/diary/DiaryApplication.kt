package io.github.taetae98coding.diary

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import io.github.taetae98coding.diary.app.shared.di.AndroidAppGraph
import io.github.taetae98coding.diary.app.shared.di.createAndroidAppGraph
import io.github.taetae98coding.diary.work.appaction.AppActionGraph
import io.github.taetae98coding.diary.work.appfunction.AppActionGraphOwner

internal class DiaryApplication :
    Application(),
    AppActionGraphOwner,
    Configuration.Provider {
    val appGraph: AndroidAppGraph by lazy { createAndroidAppGraph(context = this) }

    override val appActionGraph: AppActionGraph
        get() = appGraph

    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setWorkerFactory(appGraph.workerFactory)
                .build()
}

internal val Context.appGraph: AndroidAppGraph
    get() = (applicationContext as DiaryApplication).appGraph
