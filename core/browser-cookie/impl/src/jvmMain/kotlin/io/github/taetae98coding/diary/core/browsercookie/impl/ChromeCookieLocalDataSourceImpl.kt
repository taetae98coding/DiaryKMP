package io.github.taetae98coding.diary.core.browsercookie.impl

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.core.browsercookie.impl.di.BrowserCookieDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import kotlin.time.Instant

// Chrome은 쿠키 시각을 1601-01-01 기준 마이크로초로 저장한다.
private const val WINDOWS_EPOCH_OFFSET_MICROSECONDS = 11_644_473_600_000_000L
private const val MICROSECONDS_PER_MILLISECOND = 1_000L

// 이 버전부터 복호화한 쿠키 값 앞에 host_key의 SHA-256이 붙는다.
private const val HASHED_DOMAIN_DATABASE_VERSION = 24L

private const val SAME_SITE_NONE = 0L
private const val SAME_SITE_LAX = 1L
private const val SAME_SITE_STRICT = 2L

private const val TRUE_FLAG = 1L

private const val HOST_KEY_COLUMN = 0
private const val NAME_COLUMN = 1
private const val VALUE_COLUMN = 2
private const val ENCRYPTED_VALUE_COLUMN = 3
private const val PATH_COLUMN = 4
private const val HAS_EXPIRES_COLUMN = 5
private const val EXPIRES_UTC_COLUMN = 6
private const val IS_SECURE_COLUMN = 7
private const val IS_HTTP_ONLY_COLUMN = 8
private const val SAME_SITE_COLUMN = 9

@Factory
internal class ChromeCookieLocalDataSourceImpl(
    private val location: ChromeCookieLocation,
    private val keyProvider: ChromeCookieKeyProvider,
    @BrowserCookieDispatcher
    private val dispatcher: CoroutineDispatcher,
) : ChromeCookieLocalDataSource {
    override val isSupported: Boolean
        get() = location.isSupported

    override suspend fun findAll(profileDirectory: String): List<BrowserCookieLocalEntity> {
        val (databaseVersion, rowList) =
            withContext(dispatcher) {
                val snapshot = ChromeCookieSnapshot.create(cookiesPath = location.cookiesPath(profileDirectory = profileDirectory))

                try {
                    BundledSQLiteDriver().open(snapshot.databasePath.toString()).use { connection ->
                        connection.readDatabaseVersion() to connection.readRowList()
                    }
                } finally {
                    snapshot.delete()
                }
            }

        val key = if (rowList.any { row -> row.encryptedValue.isNotEmpty() }) keyProvider.getKey() else null

        return rowList.mapNotNull { row -> row.toEntity(databaseVersion = databaseVersion, key = key) }
    }

    private fun SQLiteConnection.readDatabaseVersion(): Long =
        prepare("SELECT value FROM meta WHERE key = 'version'").use { statement ->
            if (statement.step()) statement.getLong(0) else 0L
        }

    private fun SQLiteConnection.readRowList(): List<ChromeCookieRow> {
        val sql =
            "SELECT host_key, name, value, encrypted_value, path, has_expires, expires_utc, is_secure, is_httponly, samesite " +
                "FROM cookies WHERE top_frame_site_key = ''"

        return prepare(sql).use { statement ->
            buildList {
                while (statement.step()) {
                    add(
                        ChromeCookieRow(
                            hostKey = statement.getText(HOST_KEY_COLUMN),
                            name = statement.getText(NAME_COLUMN),
                            value = statement.getText(VALUE_COLUMN),
                            encryptedValue = if (statement.isNull(ENCRYPTED_VALUE_COLUMN)) ByteArray(0) else statement.getBlob(ENCRYPTED_VALUE_COLUMN),
                            path = statement.getText(PATH_COLUMN),
                            hasExpires = statement.getLong(HAS_EXPIRES_COLUMN) == TRUE_FLAG,
                            expiresUtc = statement.getLong(EXPIRES_UTC_COLUMN),
                            isSecure = statement.getLong(IS_SECURE_COLUMN) == TRUE_FLAG,
                            isHttpOnly = statement.getLong(IS_HTTP_ONLY_COLUMN) == TRUE_FLAG,
                            sameSite = statement.getLong(SAME_SITE_COLUMN),
                        ),
                    )
                }
            }
        }
    }

    private fun ChromeCookieRow.toEntity(
        databaseVersion: Long,
        key: ByteArray?,
    ): BrowserCookieLocalEntity? {
        val value =
            if (encryptedValue.isEmpty()) {
                this.value
            } else {
                val decrypted = ChromeCookieDecryptor.decrypt(encryptedValue = encryptedValue, key = requireNotNull(key))
                val plain =
                    if (databaseVersion >= HASHED_DOMAIN_DATABASE_VERSION) {
                        ChromeCookieDecryptor.stripDomainHash(decryptedValue = decrypted, hostKey = hostKey) ?: return null
                    } else {
                        decrypted
                    }

                plain.decodeToString()
            }

        return BrowserCookieLocalEntity(
            name = name,
            value = value,
            domain = hostKey,
            path = path,
            expiresAt = expiresAt(),
            isSecure = isSecure,
            isHttpOnly = isHttpOnly,
            sameSite = sameSite.toSameSiteEntity(),
        )
    }

    private fun ChromeCookieRow.expiresAt(): Instant? {
        if (!hasExpires || expiresUtc <= 0L) return null

        return Instant.fromEpochMilliseconds((expiresUtc - WINDOWS_EPOCH_OFFSET_MICROSECONDS) / MICROSECONDS_PER_MILLISECOND)
    }

    private fun Long.toSameSiteEntity(): BrowserCookieSameSiteLocalEntity =
        when (this) {
            SAME_SITE_NONE -> BrowserCookieSameSiteLocalEntity.NONE
            SAME_SITE_LAX -> BrowserCookieSameSiteLocalEntity.LAX
            SAME_SITE_STRICT -> BrowserCookieSameSiteLocalEntity.STRICT
            else -> BrowserCookieSameSiteLocalEntity.UNSPECIFIED
        }

    private data class ChromeCookieRow(
        val hostKey: String,
        val name: String,
        val value: String,
        val encryptedValue: ByteArray,
        val path: String,
        val hasExpires: Boolean,
        val expiresUtc: Long,
        val isSecure: Boolean,
        val isHttpOnly: Boolean,
        val sameSite: Long,
    )
}
