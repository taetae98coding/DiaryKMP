@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.library.applicationsupport

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

public fun documentDirectoryPath(): String {
    val documentDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )

    return requireNotNull(documentDirectory?.path)
}
