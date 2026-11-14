package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.IosContentSharerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal class IosShareModule {
    @Single
    fun providesContentSharerFactory(): ContentSharerFactory = IosContentSharerFactory(dispatcher = Dispatchers.IO)
}
