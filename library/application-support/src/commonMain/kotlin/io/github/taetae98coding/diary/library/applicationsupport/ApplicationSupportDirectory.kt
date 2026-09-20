package io.github.taetae98coding.diary.library.applicationsupport

import java.nio.file.Path
import java.nio.file.Paths

private const val APPLICATION_SUPPORT_RELATIVE_PATH = "Library/Application Support"

public fun applicationSupportDirectory(
    directoryName: String,
    userHome: Path = Paths.get(System.getProperty("user.home")),
): Path =
    userHome
        .resolve(APPLICATION_SUPPORT_RELATIVE_PATH)
        .resolve(directoryName)
