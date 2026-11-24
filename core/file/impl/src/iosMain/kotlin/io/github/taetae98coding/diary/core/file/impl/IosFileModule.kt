package io.github.taetae98coding.diary.core.file.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.FileDownloadDefaultDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath
import io.github.taetae98coding.diary.library.applicationsupport.documentDirectoryPath

@BindingContainer
@ContributesTo(AppScope::class)
public object IosFileModule {
    @Provides
    @AppFileDirectory
    internal fun providesAppFileDirectory(): String = applicationSupportDirectoryPath()

    // 파일 앱에 이 앱의 폴더로 보이는 곳이다.
    @Provides
    @FileDownloadDefaultDirectory
    internal fun providesFileDownloadDefaultDirectory(): String = documentDirectoryPath()
}
