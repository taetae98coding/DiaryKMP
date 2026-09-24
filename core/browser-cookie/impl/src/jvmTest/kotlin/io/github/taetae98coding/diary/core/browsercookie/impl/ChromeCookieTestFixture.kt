package io.github.taetae98coding.diary.core.browsercookie.impl

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Path
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random
import kotlin.time.Instant

internal const val CHROME_DATABASE_VERSION = 24L
private const val AES_KEY_LENGTH = 16
private const val IV_LENGTH = 16
private const val WINDOWS_EPOCH_OFFSET_MILLISECONDS = 11_644_473_600_000L
private const val MICROSECONDS_PER_MILLISECOND = 1_000L

// Chrome 쿠키 저장소의 cookies 테이블 열 정의. 읽는 쪽이 참조하는 열만 실제 형식과 맞추면 된다.
private val cookieColumnDefinitionList: List<String> =
    listOf(
        "creation_utc INTEGER NOT NULL",
        "host_key TEXT NOT NULL",
        "top_frame_site_key TEXT NOT NULL",
        "name TEXT NOT NULL",
        "value TEXT NOT NULL",
        "encrypted_value BLOB NOT NULL",
        "path TEXT NOT NULL",
        "expires_utc INTEGER NOT NULL",
        "is_secure INTEGER NOT NULL",
        "is_httponly INTEGER NOT NULL",
        "last_access_utc INTEGER NOT NULL",
        "has_expires INTEGER NOT NULL",
        "is_persistent INTEGER NOT NULL",
        "priority INTEGER NOT NULL",
        "samesite INTEGER NOT NULL",
        "source_scheme INTEGER NOT NULL",
        "source_port INTEGER NOT NULL",
        "last_update_utc INTEGER NOT NULL",
        "source_type INTEGER NOT NULL",
        "has_cross_site_ancestor INTEGER NOT NULL",
    )

internal fun randomAesKey(): ByteArray = Random.nextBytes(AES_KEY_LENGTH)

// Chrome이 macOS에서 쿠키 값을 저장하는 형식 그대로 host_key 해시를 앞에 붙여 AES-CBC로 암호화한다.
internal fun encryptChromeCookieValue(
    value: String,
    hostKey: String,
    key: ByteArray,
    withDomainHash: Boolean = true,
): ByteArray {
    val hash = if (withDomainHash) MessageDigest.getInstance("SHA-256").digest(hostKey.encodeToByteArray()) else ByteArray(0)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(ByteArray(IV_LENGTH) { ' '.code.toByte() }))

    return "v10".encodeToByteArray() + cipher.doFinal(hash + value.encodeToByteArray())
}

internal fun Instant.toChromeMicroseconds(): Long = (toEpochMilliseconds() + WINDOWS_EPOCH_OFFSET_MILLISECONDS) * MICROSECONDS_PER_MILLISECOND

internal data class ChromeCookieTestRow(
    val hostKey: String,
    val name: String,
    val value: String = "",
    val encryptedValue: ByteArray = ByteArray(0),
    val path: String = "/",
    val expiresAt: Instant? = null,
    val isSecure: Boolean = false,
    val isHttpOnly: Boolean = false,
    val sameSite: Long = -1L,
    val topFrameSiteKey: String = "",
)

internal fun createChromeCookieDatabase(
    path: Path,
    rowList: List<ChromeCookieTestRow>,
    version: Long = CHROME_DATABASE_VERSION,
) {
    val connection = BundledSQLiteDriver().open(path.toString())

    try {
        connection.execSQL("CREATE TABLE meta(key LONGVARCHAR NOT NULL UNIQUE PRIMARY KEY, value LONGVARCHAR)")
        connection.execSQL("INSERT INTO meta(key, value) VALUES ('version', $version)")
        connection.execSQL("CREATE TABLE cookies(${cookieColumnDefinitionList.joinToString()})")

        rowList.forEach { row -> connection.insert(row) }
    } finally {
        connection.close()
    }
}

private fun SQLiteConnection.insert(row: ChromeCookieTestRow) {
    val statement =
        prepare(
            "INSERT INTO cookies(creation_utc, host_key, top_frame_site_key, name, value, encrypted_value, path, expires_utc, " +
                "is_secure, is_httponly, last_access_utc, has_expires, is_persistent, priority, samesite, " +
                "source_scheme, source_port, last_update_utc, source_type, has_cross_site_ancestor) " +
                "VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 1, 1, ?, 2, 443, 0, 0, 0)",
        )

    try {
        statement.bindText(1, row.hostKey)
        statement.bindText(2, row.topFrameSiteKey)
        statement.bindText(3, row.name)
        statement.bindText(4, row.value)
        statement.bindBlob(5, row.encryptedValue)
        statement.bindText(6, row.path)
        statement.bindLong(7, row.expiresAt?.toChromeMicroseconds() ?: 0L)
        statement.bindLong(8, if (row.isSecure) 1L else 0L)
        statement.bindLong(9, if (row.isHttpOnly) 1L else 0L)
        statement.bindLong(10, if (row.expiresAt == null) 0L else 1L)
        statement.bindLong(11, row.sameSite)
        statement.step()
    } finally {
        statement.close()
    }
}
