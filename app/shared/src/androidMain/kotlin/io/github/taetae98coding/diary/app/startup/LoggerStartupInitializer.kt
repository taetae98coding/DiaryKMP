package io.github.taetae98coding.diary.app.startup

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.startup.Initializer
import io.github.taetae98coding.diary.app.initializer.LoggerInitializer
import io.github.taetae98coding.diary.logger.core.DiaryLogger

internal class LoggerStartupInitializer : Initializer<DiaryLogger> {
    override fun create(context: Context): DiaryLogger {
        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        return LoggerInitializer.initialize(isDebug = isDebug)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
