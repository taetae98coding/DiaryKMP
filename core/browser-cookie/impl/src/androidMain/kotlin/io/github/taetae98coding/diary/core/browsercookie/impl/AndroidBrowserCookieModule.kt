package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeProfileLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.datasource.InAppBrowserCookieLocalDataSource
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidBrowserCookieModule {
    @Factory
    internal fun providesChromeCookieLocalDataSource(): ChromeCookieLocalDataSource = UnsupportedChromeCookieLocalDataSource

    @Factory
    internal fun providesChromeProfileLocalDataSource(): ChromeProfileLocalDataSource = UnsupportedChromeProfileLocalDataSource

    @Factory
    internal fun providesInAppBrowserCookieLocalDataSource(): InAppBrowserCookieLocalDataSource = UnsupportedInAppBrowserCookieLocalDataSource
}
