package io.github.taetae98coding.diary.app.shared

import android.content.Context
import androidx.work.DelegatingWorkerFactory
import androidx.work.WorkerFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.app.shared.di.ShareDispatcher
import io.github.taetae98coding.diary.app.shared.share.ShareStreamReader
import io.github.taetae98coding.diary.compose.credential.apple.AndroidAppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsConfig
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.AndroidGoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.PlatformDiaryMapContentOverride
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
internal object AndroidAppModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesWorkerFactory(workerFactorySet: Set<WorkerFactory>): WorkerFactory = DelegatingWorkerFactory().apply { workerFactorySet.forEach(::addFactory) }

    @Provides
    @SingleIn(AppScope::class)
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory = AndroidGoogleCredentialsManagerFactory(serverClientId = BuildKonfig.GOOGLE_CREDENTIALS_SERVER_CLIENT_ID)

    @Provides
    @SingleIn(AppScope::class)
    fun providesAppleCredentialsManagerFactory(config: AppleCredentialsConfig): AppleCredentialsManagerFactory = AndroidAppleCredentialsManagerFactory(config = config)

    @Provides
    @SingleIn(AppScope::class)
    fun providesDiaryMapContentOverride(): DiaryMapContentOverride = PlatformDiaryMapContentOverride

    @Provides
    @ShareDispatcher
    fun providesShareDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun providesShareStreamReader(
        context: Context,
        @ShareDispatcher dispatcher: CoroutineDispatcher,
    ): ShareStreamReader = ShareStreamReader(context = context, dispatcher = dispatcher)
}
