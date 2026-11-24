package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.compose.core.share.AndroidContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
internal object AndroidShareModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesContentSharerFactory(): ContentSharerFactory = AndroidContentSharerFactory(dispatcher = Dispatchers.IO)
}
