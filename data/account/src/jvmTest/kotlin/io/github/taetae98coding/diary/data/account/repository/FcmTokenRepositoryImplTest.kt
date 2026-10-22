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

        test("TC-10ca5481-2fad-4307-afc7-cde4a831f225 TC-ed327973-684c-4131-af2e-3c9a8d007034 기기에 설치 ID가 없으면 등록과 해제를 요청하지 않는다") {
            val remote = mockk<FcmTokenRemoteDataSource>()
            val repository = repository(installationId = null, remote = remote)

            repository.upsert()
            repository.delete()

            coVerify(exactly = 0) { remote.upsert(fcmToken = any()) }
        }

        test("TC-151a7384-b1d2-4272-a502-e16304ba0087 등록 요청에는 현재 설치 ID, 시간대, 언어만 담는다") {
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = repository(installationId = "fid-a", remote = remote)

            repository.upsert()

            coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = "fid-a", timeZone = "Asia/Seoul", language = "ko-KR")) }
        }

        test("TC-4ad45434-a094-433f-a10c-75c8436f933d 같은 정보라도 요청마다 다시 등록한다") {
            val installationId = fixtureMonkey.nonBlankString()
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = repository(installationId = installationId, remote = remote)

            repository.upsert()
            repository.upsert()

            coVerify(exactly = 2) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = installationId, timeZone = "Asia/Seoul", language = "ko-KR")) }
        }

        context("TC-8882d07c-9e92-4fa8-9541-c16408248857 시간대나 언어가 바뀐 뒤의 등록에는 새 값이 담긴다") {
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

        test("TC-9dd94831-e04b-4153-a301-f3b3b594f394 등록에 실패하면 요청은 한 번만 보내고 실패를 전달하며, 다음 요청에서 다시 등록한다") {
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

        test("TC-3822e4ff-6e13-4ed0-8859-8ee236fd553d 해제 요청에는 현재 설치 ID만 담는다") {
            val remote = mockk<FcmTokenRemoteDataSource>()
            coEvery { remote.upsert(fcmToken = any()) } returns Unit
            val repository = repository(installationId = "fid-a", remote = remote)

            repository.delete()

            coVerify(exactly = 1) { remote.upsert(fcmToken = FcmTokenRemoteEntity(installationId = "fid-a", timeZone = null, language = null)) }
        }

        test("TC-2f536477-2e13-417c-8893-61f94d263f71 해제에 실패하면 요청은 한 번만 보내고 실패를 전달하며, 다음 요청에서 다시 해제한다") {
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

        test("TC-8df0e0c3-4978-4aa1-b355-df1ed8d79653 설치 ID를 받아 오지 못하면 제출하지 않고 실패를 전달한다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val provider = mockk<FcmTokenProvider>()
            coEvery { provider.getInstallationId() } throws failure
            val remote = mockk<FcmTokenRemoteDataSource>()
            val repository = FcmTokenRepositoryImpl(fcmTokenProvider = provider, fcmTokenRemoteDataSource = remote)

            shouldThrowExactly<IllegalStateException> { repository.upsert() } shouldBeSameInstanceAs failure
            shouldThrowExactly<IllegalStateException> { repository.delete() } shouldBeSameInstanceAs failure

            coVerify(exactly = 0) { remote.upsert(fcmToken = any()) }
        }

        test("TC-bf3183c4-151d-459d-a768-f0fc6596c1f4 새로 발급된 설치 ID는 다음 등록에 담긴다") {
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
