package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.impl.di.BrowserCookieDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Chrome이 macOS에서 쿠키 암호화 키를 만드는 방식 그대로 키체인 비밀번호를 PBKDF2로 늘린다.
private const val KEYCHAIN_SERVICE = "Chrome Safe Storage"
private const val KEYCHAIN_ACCOUNT = "Chrome"
private const val SALT = "saltysalt"
private const val ITERATION_COUNT = 1_003
private const val KEY_LENGTH_BITS = 128
private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA1"

// 키체인 접근 허용을 묻는 대화상자에 사용자가 응답할 시간이다.
private const val KEYCHAIN_TIMEOUT_SECONDS = 30L

/**
 * 시스템 키체인에서 Chrome의 암호화 비밀번호를 읽어 쿠키 복호화 키를 만든다.
 * 키는 프로세스가 사는 동안만 기억해 키체인 접근 허용을 매번 묻지 않게 한다.
 */
@Single
internal class KeychainChromeCookieKeyProvider(
    @BrowserCookieDispatcher
    private val dispatcher: CoroutineDispatcher,
) : ChromeCookieKeyProvider {
    private val mutex = Mutex()
    private var key: ByteArray? = null

    override suspend fun getKey(): ByteArray =
        mutex.withLock {
            key ?: deriveKey(password = readPassword()).also { derived -> key = derived }
        }

    private suspend fun readPassword(): String =
        withContext(dispatcher) {
            val process =
                ProcessBuilder("security", "find-generic-password", "-s", KEYCHAIN_SERVICE, "-a", KEYCHAIN_ACCOUNT, "-w")
                    .start()

            if (!process.waitFor(KEYCHAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                error("Timed out while reading $KEYCHAIN_SERVICE from the keychain.")
            }

            check(process.exitValue() == 0) { "security exited with ${process.exitValue()} while reading $KEYCHAIN_SERVICE." }

            process.inputStream
                .bufferedReader()
                .use { reader -> reader.readText() }
                .trim()
        }

    private fun deriveKey(password: String): ByteArray =
        SecretKeyFactory
            .getInstance(KEY_ALGORITHM)
            .generateSecret(PBEKeySpec(password.toCharArray(), SALT.encodeToByteArray(), ITERATION_COUNT, KEY_LENGTH_BITS))
            .encoded
}
