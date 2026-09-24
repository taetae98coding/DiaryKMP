package io.github.taetae98coding.diary.core.browsercookie.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ChromeCookieDecryptorTest :
    FunSpec({
        test("TC-CHROME-SESSION-IMPORT-DATA-001 v10으로 암호화한 값을 원래 값으로 되돌린다") {
            val key = randomAesKey()
            val value = "value-${fixtureMonkey.giveMeOne<String>()}"
            val encrypted = encryptChromeCookieValue(value = value, hostKey = "example.com", key = key, withDomainHash = false)

            ChromeCookieDecryptor.decrypt(encryptedValue = encrypted, key = key).decodeToString() shouldBe value
        }

        test("알 수 없는 버전 표식이면 실패한다") {
            val key = randomAesKey()
            val encrypted = "v20".encodeToByteArray() + randomAesKey()

            shouldThrow<IllegalArgumentException> {
                ChromeCookieDecryptor.decrypt(encryptedValue = encrypted, key = key)
            }
        }

        test("host_key 해시가 앞에 붙은 값에서 해시를 떼어 낸다") {
            val key = randomAesKey()
            val hostKey = ".example.com"
            val value = "value-${fixtureMonkey.giveMeOne<String>()}"
            val decrypted = ChromeCookieDecryptor.decrypt(encryptedValue = encryptChromeCookieValue(value = value, hostKey = hostKey, key = key), key = key)

            ChromeCookieDecryptor.stripDomainHash(decryptedValue = decrypted, hostKey = hostKey)?.decodeToString() shouldBe value
        }

        test("host_key 해시가 다르거나 짧으면 값을 버린다") {
            val key = randomAesKey()
            val decrypted = ChromeCookieDecryptor.decrypt(encryptedValue = encryptChromeCookieValue(value = "value", hostKey = "a.example.com", key = key), key = key)

            ChromeCookieDecryptor.stripDomainHash(decryptedValue = decrypted, hostKey = "b.example.com").shouldBeNull()
            ChromeCookieDecryptor.stripDomainHash(decryptedValue = ByteArray(8), hostKey = "a.example.com").shouldBeNull()
        }
    })
