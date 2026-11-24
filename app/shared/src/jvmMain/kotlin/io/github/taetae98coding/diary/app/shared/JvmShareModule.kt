package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.share.JvmContentSharerFactory

@BindingContainer
@ContributesTo(AppScope::class)
internal object JvmShareModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesContentSharerFactory(): ContentSharerFactory = JvmContentSharerFactory
}
