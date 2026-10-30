package io.github.taetae98coding.diary.core.webcookie.impl

import android.webkit.CookieManager
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@ComponentScan
@Configuration
public class AndroidWebCookieModule {
    @Factory
    internal fun providesCookieManager(): CookieManager = CookieManager.getInstance()
}
