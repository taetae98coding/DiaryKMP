@file:OptIn(ExperimentalPathApi::class)

package io.github.taetae98coding.diary.core.browsercookie.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.name

private const val SNAPSHOT_DIRECTORY_PREFIX = "diary-chrome-cookies"

// WAL 모드의 SQLite는 아직 본 파일에 합쳐지지 않은 쓰기를 이 두 파일에 두므로 함께 복사해야 최신 값이 읽힌다.
private val SIDECAR_SUFFIX_LIST = listOf("-wal", "-shm")

// Chrome이 실행 중이면 쿠키 저장소 파일이 잠겨 있어 직접 열 수 없으므로 복사본을 읽는다.

internal class ChromeCookieSnapshot private constructor(
    private val directory: Path,
    val databasePath: Path,
) {
    fun delete() {
        directory.deleteRecursively()
    }

    companion object {
        fun create(cookiesPath: Path): ChromeCookieSnapshot {
            check(cookiesPath.exists()) { "Chrome cookies database does not exist: $cookiesPath" }

            val directory = Files.createTempDirectory(SNAPSHOT_DIRECTORY_PREFIX)
            val databasePath = directory.resolve(cookiesPath.name)

            try {
                Files.copy(cookiesPath, databasePath, StandardCopyOption.REPLACE_EXISTING)

                SIDECAR_SUFFIX_LIST.forEach { suffix ->
                    val sidecar = cookiesPath.resolveSibling(cookiesPath.name + suffix)

                    if (sidecar.exists()) {
                        Files.copy(sidecar, directory.resolve(sidecar.name), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            } catch (throwable: Throwable) {
                directory.deleteRecursively()
                throw throwable
            }

            return ChromeCookieSnapshot(directory = directory, databasePath = databasePath)
        }
    }
}
