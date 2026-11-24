package io.github.taetae98coding.diary.work.appfunction.core

import android.app.Application
import io.github.taetae98coding.diary.work.appaction.AppActionGraph
import io.github.taetae98coding.diary.work.appfunction.AppActionGraphOwner

internal class TestAppFunctionApplication :
    Application(),
    AppActionGraphOwner {
    override lateinit var appActionGraph: AppActionGraph
}
