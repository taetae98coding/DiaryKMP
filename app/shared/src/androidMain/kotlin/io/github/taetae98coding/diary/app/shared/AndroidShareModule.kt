package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.core.share.AndroidContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal class AndroidShareModule {
    @Single
    fun providesContentSharerFactory(): ContentSharerFactory = AndroidContentSharerFactory(dispatcher = Dispatchers.IO)
}
