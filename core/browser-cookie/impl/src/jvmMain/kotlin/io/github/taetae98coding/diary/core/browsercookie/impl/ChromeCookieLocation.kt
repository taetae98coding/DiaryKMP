package io.github.taetae98coding.diary.core.browsercookie.impl

import java.nio.file.Path
import java.nio.file.Paths

private const val CHROME_USER_DATA_RELATIVE_PATH = "Library/Application Support/Google/Chrome"
private const val COOKIES_FILE_NAME = "Cookies"
private const val LOCAL_STATE_FILE_NAME = "Local State"
private const val MAC_OS_NAME_KEYWORD = "mac"

internal data class ChromeCookieLocation(
    val isSupported: Boolean,
    val userDataDirectory: Path,
) {
    val localStatePath: Path
        get() = userDataDirectory.resolve(LOCAL_STATE_FILE_NAME)

    fun cookiesPath(profileDirectory: String): Path = userDataDirectory.resolve(profileDirectory).resolve(COOKIES_FILE_NAME)

    companion object {
        fun current(
            osName: String = System.getProperty("os.name").orEmpty(),
            userHome: Path = Paths.get(System.getProperty("user.home")),
        ): ChromeCookieLocation =
            ChromeCookieLocation(
                isSupported = osName.lowercase().contains(MAC_OS_NAME_KEYWORD),
                userDataDirectory = userHome.resolve(CHROME_USER_DATA_RELATIVE_PATH),
            )
    }
}
