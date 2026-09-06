package io.github.taetae98coding.diary.app.startup

import android.content.Context
import androidx.startup.Initializer
import io.github.taetae98coding.diary.app.di.startKoin
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinApplication

internal class KoinInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication =
        startKoin {
            androidContext(context.applicationContext)
            workManagerFactory()
        }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
