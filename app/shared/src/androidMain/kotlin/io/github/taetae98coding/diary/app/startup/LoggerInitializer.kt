package io.github.taetae98coding.diary.app.startup

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.startup.Initializer
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.app.initializer.LoggerInitializer as CommonLoggerInitializer

internal class LoggerInitializer : Initializer<DiaryLogger> {
    override fun create(context: Context): DiaryLogger {
        val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        CommonLoggerInitializer.initialize(isDebug = isDebug)

        return DiaryLogger
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(KoinInitializer::class.java)
}
