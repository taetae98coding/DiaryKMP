package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsConfig
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.apple.JvmAppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.JvmGoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.JvmDiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.core.calendar.database.impl.di.CalendarDatabaseDirectory
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDirectory
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.QrActionDispatcher
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanCameraDispatcher
import io.github.taetae98coding.diary.work.mcp.di.McpServerPort
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

    @Single
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory =
        JvmGoogleCredentialsManagerFactory(
            clientId = BuildKonfig.GOOGLE_CREDENTIALS_CLIENT_ID,
            coroutineDispatcher = Dispatchers.IO,
        )

    @Single
    fun providesAppleCredentialsManagerFactory(config: AppleCredentialsConfig): AppleCredentialsManagerFactory =
        JvmAppleCredentialsManagerFactory(
            config = config,
            coroutineDispatcher = Dispatchers.IO,
        )

    @Factory
    @McpServerPort
    fun providesMcpServerPort(): Int = BuildKonfig.MCP_SERVER_PORT

    @Single
    fun providesMapHttpServer(): MapHttpServer = MapHttpServer()

    @Single
    fun providesDiaryMapContentOverride(mapHttpServer: MapHttpServer): DiaryMapContentOverride = JvmDiaryMapContentOverride(httpServer = mapHttpServer)

    @Factory
    @QrScanCameraDispatcher
    fun providesQrScanCameraDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Factory
    @QrActionDispatcher
    fun providesQrActionDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
