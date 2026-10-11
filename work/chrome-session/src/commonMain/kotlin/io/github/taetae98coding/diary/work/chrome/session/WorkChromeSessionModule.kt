package io.github.taetae98coding.diary.work.chrome.session

import io.github.taetae98coding.diary.library.coroutines.scope.workCoroutineScope
import io.github.taetae98coding.diary.work.chrome.session.di.ChromeSessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
public class WorkChromeSessionModule {
    @Single
    @ChromeSessionScope
    internal fun providesChromeSessionCoroutineScope(): CoroutineScope = workCoroutineScope(dispatcher = Dispatchers.Default)
}
