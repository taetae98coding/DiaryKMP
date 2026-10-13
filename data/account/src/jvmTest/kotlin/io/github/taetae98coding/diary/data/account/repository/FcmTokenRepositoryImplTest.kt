package io.github.taetae98coding.diary.data.account.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.fcm.api.FcmTokenProvider
import io.github.taetae98coding.diary.core.network.api.fcm.datasource.FcmTokenRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.fcm.entity.FcmTokenRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.Locale
import java.util.TimeZone

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FcmTokenRepositoryImplTest :
    FunSpec({
        // 시간대와 언어는 기기 설정에서 읽으므로 JVM 기본값을 바꿔 제어하고, 테스트가 끝나면 되돌린다.
        val originalTimeZone = TimeZone.getDefault()
        val originalLocale = Locale.getDefault()

        beforeTest { setDevice(timeZone = "Asia/Seoul", language = "ko-KR") }
        afterTest {
            TimeZone.setDefault(originalTimeZone)
            Locale.setDefault(originalLocale)
        }

        test("TC-FCM-TOKEN-DOMAIN-001 TC-FCM-TOKEN-DOMAIN-022 기기에 설치 ID가 없으면 등록과 해제를 요청하지 않는다") {
            val remote = mockk<FcmTokenRemoteDataSource>()
            val repository = repository(installationId = null, remote = remote)

            repository.upsert()
            repository.delete()

            coVerify(exactly = 0) { remote.upsert(fcmToken = any()) }
        }

        test("TC-FCM-TOKEN-DATA-001 등록 요청에는 현재 설치 ID, 시간대, 언어만 담는다") {
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = repository(installationId = "fid-a", remote = remote)

            repository.upsert()

            coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = "fid-a", timeZone = "Asia/Seoul", language = "ko-KR")) }
        }

        test("TC-FCM-TOKEN-DOMAIN-019 같은 정보라도 요청마다 다시 등록한다") {
            val installationId = fixtureMonkey.nonBlankString()
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = repository(installationId = installationId, remote = remote)

            repository.upsert()
            repository.upsert()

            coVerify(exactly = 2) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = installationId, timeZone = "Asia/Seoul", language = "ko-KR")) }
        }

        context("TC-FCM-TOKEN-DOMAIN-007 시간대나 언어가 바뀐 뒤의 등록에는 새 값이 담긴다") {
            listOf(
                "시간대" to ("America/New_York" to "ko-KR"),
                "언어" to ("Asia/Seoul" to "en-US"),
            ).forEach { (name, device) ->
                test(name) {
                    val (timeZone, language) = device
                    val installationId = fixtureMonkey.nonBlankString()
                    val remote = mockk<FcmTokenRemoteDataSource>()
                    coEvery { remote.upsert(fcmToken = any()) } returns Unit
                    val repository = repository(installationId = installationId, remote = remote)

                    repository.upsert()
                    setDevice(timeZone = timeZone, language = language)
                    repository.upsert()

                    coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = installationId, timeZone = timeZone, language = language)) }
                }
            }
        }

        test("TC-FCM-TOKEN-DOMAIN-010 등록에 실패하면 요청은 한 번만 보내고 실패를 전달하며, 다음 요청에서 다시 등록한다") {
            val installationId = fixtureMonkey.nonBlankString()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } throws failure
            val repository = repository(installationId = installationId, remote = remote)

            shouldThrowExactly<IllegalStateException> { repository.upsert() } shouldBeSameInstanceAs failure
            coVerify(exactly = 1) { remote.upsert(fcmToken = any()) }

            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            repository.upsert()

            coVerify(exactly = 2) { remote.upsert(fcmToken = any()) }
        }

        test("TC-FCM-TOKEN-DATA-002 해제 요청에는 현재 설치 ID만 담는다") {
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = repository(installationId = "fid-a", remote = remote)

            repository.delete()

            coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = "fid-a", timeZone = null, language = null)) }
        }

        test("TC-FCM-TOKEN-DOMAIN-021 해제에 실패하면 요청은 한 번만 보내고 실패를 전달하며, 다음 요청에서 다시 해제한다") {
            val installationId = fixtureMonkey.nonBlankString()
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } throws failure
            val repository = repository(installationId = installationId, remote = remote)

            shouldThrowExactly<IllegalStateException> { repository.delete() } shouldBeSameInstanceAs failure
            coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = installationId)) }

            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            repository.delete()

            coVerify(exactly = 2) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = installationId)) }
        }

        test("TC-FCM-TOKEN-DOMAIN-024 설치 ID를 받아 오지 못하면 제출하지 않고 실패를 전달한다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val provider = mockk<FcmTokenProvider>()
            coEvery { provider.getInstallationId() } throws failure
            val remote = mockk<FcmTokenRemoteDataSource>()
            val repository = FcmTokenRepositoryImpl(fcmTokenProvider = provider, fcmTokenRemoteDataSource = remote)

            shouldThrowExactly<IllegalStateException> { repository.upsert() } shouldBeSameInstanceAs failure
            shouldThrowExactly<IllegalStateException> { repository.delete() } shouldBeSameInstanceAs failure

            coVerify(exactly = 0) { remote.upsert(fcmToken = any()) }
        }

        test("TC-FCM-TOKEN-DOMAIN-023 새로 발급된 설치 ID는 다음 등록에 담긴다") {
            val firstInstallationId = fixtureMonkey.nonBlankString()
            val newInstallationId = fixtureMonkey.nonBlankString()
            val provider = mockk<FcmTokenProvider>()
            coEvery { provider.getInstallationId() } returnsMany listOf(firstInstallationId, newInstallationId)
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = FcmTokenRepositoryImpl(fcmTokenProvider = provider, fcmTokenRemoteDataSource = remote)

            repository.upsert()
            repository.upsert()

            coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = newInstallationId, timeZone = "Asia/Seoul", language = "ko-KR")) }
        }
    })

private fun setDevice(
    timeZone: String,
    language: String,
) {
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone))
    Locale.setDefault(Locale.forLanguageTag(language))
}

private fun repository(
    installationId: String?,
    remote: FcmTokenRemoteDataSource,
): FcmTokenRepositoryImpl =
    FcmTokenRepositoryImpl(
        fcmTokenProvider = mockk<FcmTokenProvider>().also { coEvery { it.getInstallationId() } returns installationId },
        fcmTokenRemoteDataSource = remote,
    )
