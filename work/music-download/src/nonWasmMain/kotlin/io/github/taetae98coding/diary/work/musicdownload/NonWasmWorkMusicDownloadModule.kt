package io.github.taetae98coding.diary.work.musicdownload

import io.github.taetae98coding.diary.work.musicdownload.di.MusicDownloadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class NonWasmWorkMusicDownloadModule {
    @Factory
    @MusicDownloadDispatcher
    internal fun providesMusicDownloadDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
