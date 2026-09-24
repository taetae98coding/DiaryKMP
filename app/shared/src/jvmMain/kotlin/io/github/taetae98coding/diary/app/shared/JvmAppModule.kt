package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDirectory
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDirectory
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsClientId
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
internal class JvmAppModule {
    @Factory
    @DiaryDatabaseDirectory
    fun providesDatabaseDirectory(): String = BuildKonfig.APP_DIRECTORY

    @Factory
    @AppFileDirectoryName
    fun providesAppFileDirectoryName(): String = BuildKonfig.APP_DIRECTORY

    @Factory
    @CalendarDatabaseDirectory
    fun providesCalendarDatabaseDirectory(): String = BuildKonfig.APP_DIRECTORY

    @Factory
    @DiarySettingDirectory
    fun providesDiarySettingDirectory(): String = BuildKonfig.APP_DIRECTORY

    @Factory
    @GoogleCredentialsClientId
    fun providesGoogleCredentialsClientId(): String = BuildKonfig.GOOGLE_CREDENTIALS_CLIENT_ID

    @Factory
    @GoogleCredentialsDispatcher
    fun providesGoogleCredentialsDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
