package io.github.taetae98coding.diary.core.browser.cookie.impl

import io.github.taetae98coding.diary.core.browser.cookie.impl.di.BrowserCookieDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class JvmBrowserCookieModule {
    @Factory
    @BrowserCookieDispatcher
    internal fun providesBrowserCookieDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Single
    internal fun providesChromeCookieLocation(): ChromeCookieLocation = ChromeCookieLocation.current()
}
