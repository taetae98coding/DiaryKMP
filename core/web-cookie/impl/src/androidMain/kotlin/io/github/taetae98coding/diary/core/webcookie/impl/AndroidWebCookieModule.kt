package io.github.taetae98coding.diary.core.webcookie.impl

import android.webkit.CookieManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.webcookie.impl.di.WebCookieDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
public object AndroidWebCookieModule {
    @Provides
    internal fun providesCookieManager(): CookieManager = CookieManager.getInstance()

    @Provides
    @WebCookieDispatcher
    internal fun providesWebCookieDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
