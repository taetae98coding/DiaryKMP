package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class NonWasmDataStoreModule {
    @Factory
    @DiarySettingDispatcher
    internal fun providesDiarySettingDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
