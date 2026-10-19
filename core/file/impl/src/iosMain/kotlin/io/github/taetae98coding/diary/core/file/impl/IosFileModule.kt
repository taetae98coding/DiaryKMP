package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.FileDownloadDefaultDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath
import io.github.taetae98coding.diary.library.applicationsupport.documentDirectoryPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class IosFileModule {
    @Factory
    @AppFileDirectory
    internal fun providesAppFileDirectory(): String = applicationSupportDirectoryPath()

    // 파일 앱에 이 앱의 폴더로 보이는 곳이다.
    @Factory
    @FileDownloadDefaultDirectory
    internal fun providesFileDownloadDefaultDirectory(): String = documentDirectoryPath()
}
