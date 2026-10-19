@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.library.applicationsupport

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

// Documents는 파일 앱에 보이는 내려받기 폴더라, 사용자가 보거나 지우면 안 되는 앱 자료는 이곳에 둔다.
public fun applicationSupportDirectoryPath(): String {
    val applicationSupportDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )

    return requireNotNull(applicationSupportDirectory?.path)
}
