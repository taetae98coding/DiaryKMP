package io.github.taetae98coding.diary.app.startup

import android.content.Context
import androidx.startup.Initializer
import io.github.taetae98coding.diary.app.initializer.KoinInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinApplication

internal class KoinStartupInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication =
        KoinInitializer.initialize {
            androidContext(context.applicationContext)
            workManagerFactory()
        }

    // WorkerFactory 설치가 WorkManager를 초기화하면 대기 중이던 작업이 곧바로 실행될 수 있고, 그 작업의 실패 로그는
    // 등록된 기록 수단에만 남으므로 기록 수단 등록이 먼저 끝나야 한다.
    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(LoggerStartupInitializer::class.java)
}
