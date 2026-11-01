package io.github.taetae98coding.diary.library.applicationsupport

import java.nio.file.Path
import java.nio.file.Paths

private const val APPLICATION_SUPPORT_RELATIVE_PATH = "Library/Application Support"
private const val USER_HOME_PROPERTY = "user.home"
private const val SANDBOX_CONTAINER_ID_ENV = "APP_SANDBOX_CONTAINER_ID"
private const val HOME_ENV = "HOME"

// 샌드박스 앱이 쓸 수 있는 홈은 운영체제가 HOME으로 알려 주는 컨테이너이고, user.home이 같은 곳을 가리킨다는 보장이 없다.
public fun userHomeDirectory(
    environment: Map<String, String> = System.getenv(),
    userHomeProperty: String = System.getProperty(USER_HOME_PROPERTY),
): Path {
    val sandboxHome = environment[HOME_ENV]?.takeIf { SANDBOX_CONTAINER_ID_ENV in environment }

    return Paths.get(sandboxHome ?: userHomeProperty)
}

public fun applicationSupportDirectory(
    directoryName: String,
    userHome: Path = userHomeDirectory(),
): Path =
    userHome
        .resolve(APPLICATION_SUPPORT_RELATIVE_PATH)
        .resolve(directoryName)
