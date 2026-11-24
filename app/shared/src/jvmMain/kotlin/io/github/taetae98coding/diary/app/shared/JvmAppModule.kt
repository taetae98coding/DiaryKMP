package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
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

@BindingContainer
@ContributesTo(AppScope::class)
internal object JvmAppModule {
    @Provides
    @DiaryDatabaseDirectory
    fun providesDatabaseDirectory(): String = BuildKonfig.APP_DIRECTORY

    @Provides
    @AppFileDirectoryName
    fun providesAppFileDirectoryName(): String = BuildKonfig.APP_DIRECTORY

    @Provides
    @CalendarDatabaseDirectory
    fun providesCalendarDatabaseDirectory(): String = BuildKonfig.APP_DIRECTORY

    @Provides
    @DiarySettingDirectory
    fun providesDiarySettingDirectory(): String = BuildKonfig.APP_DIRECTORY

    @Provides
    @SingleIn(AppScope::class)
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory =
        JvmGoogleCredentialsManagerFactory(
            clientId = BuildKonfig.GOOGLE_CREDENTIALS_CLIENT_ID,
            coroutineDispatcher = Dispatchers.IO,
        )

    @Provides
    @SingleIn(AppScope::class)
    fun providesAppleCredentialsManagerFactory(config: AppleCredentialsConfig): AppleCredentialsManagerFactory =
        JvmAppleCredentialsManagerFactory(
            config = config,
            coroutineDispatcher = Dispatchers.IO,
        )

    @Provides
    @McpServerPort
    fun providesMcpServerPort(): Int = BuildKonfig.MCP_SERVER_PORT

    @Provides
    @SingleIn(AppScope::class)
    fun providesMapHttpServer(): MapHttpServer = MapHttpServer()

    @Provides
    @SingleIn(AppScope::class)
    fun providesDiaryMapContentOverride(mapHttpServer: MapHttpServer): DiaryMapContentOverride = JvmDiaryMapContentOverride(httpServer = mapHttpServer)

    @Provides
    @QrScanCameraDispatcher
    fun providesQrScanCameraDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @QrActionDispatcher
    fun providesQrActionDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
