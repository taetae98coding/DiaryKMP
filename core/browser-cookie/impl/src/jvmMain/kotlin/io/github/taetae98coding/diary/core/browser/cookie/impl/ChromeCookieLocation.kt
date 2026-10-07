package io.github.taetae98coding.diary.core.browser.cookie.impl

import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import io.github.taetae98coding.diary.library.applicationsupport.userHomeDirectory
import java.nio.file.Path
import kotlin.io.path.Path

private const val CHROME_DIRECTORY_NAME = "Google/Chrome"
private const val COOKIES_FILE_NAME = "Cookies"
private const val LOCAL_STATE_FILE_NAME = "Local State"
private const val MAC_OS_NAME_KEYWORD = "mac"

internal data class ChromeCookieLocation(
    val isSupported: Boolean,
    val userDataDirectory: Path,
    val snapshotParentDirectory: Path,
) {
    val localStatePath: Path
        get() = userDataDirectory.resolve(LOCAL_STATE_FILE_NAME)

    fun cookiesPath(profileDirectory: String): Path = userDataDirectory.resolve(profileDirectory).resolve(COOKIES_FILE_NAME)

    companion object {
        fun current(
            osName: String = System.getProperty("os.name").orEmpty(),
            userHome: Path = userHomeDirectory(),
        ): ChromeCookieLocation =
            ChromeCookieLocation(
                isSupported = osName.lowercase().contains(MAC_OS_NAME_KEYWORD),
                userDataDirectory = applicationSupportDirectory(directoryName = CHROME_DIRECTORY_NAME, userHome = userHome),
                snapshotParentDirectory = Path(System.getProperty("java.io.tmpdir")),
            )
    }
}
