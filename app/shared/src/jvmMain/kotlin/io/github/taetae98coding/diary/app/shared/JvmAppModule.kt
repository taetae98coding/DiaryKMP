package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDirectory
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDirectory
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePickerDispatcher
import io.github.taetae98coding.diary.feature.login.ui.credential.CredentialsDispatcher
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsClientId
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPickerDispatcher
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanCameraDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

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
    @CredentialsDispatcher
    fun providesCredentialsDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Factory
    @FilePickerDispatcher
    fun providesFilePickerDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Factory
    @PhotoPickerDispatcher
    fun providesPhotoPickerDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Single
    fun providesMapHttpServer(): MapHttpServer = MapHttpServer()

    @Factory
    @QrScanCameraDispatcher
    fun providesQrScanCameraDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
