package io.github.taetae98coding.diary.core.browsercookie.impl

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// Chrome은 macOS에서 암호화한 쿠키 값 앞에 버전 표식 v10을 붙이고 공백 16바이트를 IV로 쓴다.
private const val VERSION_PREFIX = "v10"
private const val IV_LENGTH = 16
private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"
private const val KEY_ALGORITHM = "AES"
private const val HASH_ALGORITHM = "SHA-256"
private const val HASH_LENGTH = 32

internal object ChromeCookieDecryptor {
    private val iv = ByteArray(IV_LENGTH) { ' '.code.toByte() }

    fun decrypt(
        encryptedValue: ByteArray,
        key: ByteArray,
    ): ByteArray {
        val prefix = encryptedValue.copyOfRange(0, minOf(VERSION_PREFIX.length, encryptedValue.size)).decodeToString()
        require(prefix == VERSION_PREFIX) { "Unsupported Chrome cookie encryption version: $prefix" }

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, KEY_ALGORITHM), IvParameterSpec(iv))

        return cipher.doFinal(encryptedValue, VERSION_PREFIX.length, encryptedValue.size - VERSION_PREFIX.length)
    }

    // Chrome 130부터 복호화한 값 앞에 host_key의 SHA-256이 붙는다. 해시가 다르면 다른 도메인에서 옮겨 온 값이므로 버린다.
    fun stripDomainHash(
        decryptedValue: ByteArray,
        hostKey: String,
    ): ByteArray? {
        if (decryptedValue.size < HASH_LENGTH) return null

        val expected = MessageDigest.getInstance(HASH_ALGORITHM).digest(hostKey.encodeToByteArray())
        val actual = decryptedValue.copyOfRange(0, HASH_LENGTH)

        return if (expected.contentEquals(actual)) decryptedValue.copyOfRange(HASH_LENGTH, decryptedValue.size) else null
    }
}
