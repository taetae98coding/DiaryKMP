package io.github.taetae98coding.diary.library.applicationsupport

import java.nio.file.Path
import java.nio.file.Paths

private const val APPLICATION_SUPPORT_RELATIVE_PATH = "Library/Application Support"
private const val USER_HOME_PROPERTY = "user.home"

public fun userHomeDirectory(): Path = Paths.get(System.getProperty(USER_HOME_PROPERTY))

public fun applicationSupportDirectory(
    directoryName: String,
    userHome: Path = userHomeDirectory(),
): Path =
    userHome
        .resolve(APPLICATION_SUPPORT_RELATIVE_PATH)
        .resolve(directoryName)
