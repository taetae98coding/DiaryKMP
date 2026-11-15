package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.WasmContentSharerFactory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal class WasmShareModule {
    @Single
    fun providesContentSharerFactory(): ContentSharerFactory = WasmContentSharerFactory
}
