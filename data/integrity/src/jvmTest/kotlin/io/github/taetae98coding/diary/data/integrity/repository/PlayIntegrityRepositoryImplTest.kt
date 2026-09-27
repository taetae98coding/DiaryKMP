package io.github.taetae98coding.diary.data.integrity.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityToken
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityTokenProvider
import io.github.taetae98coding.diary.core.network.api.integrity.datasource.PlayIntegrityRemoteDataSource
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlayIntegrityRepositoryImplTest :
    FunSpec({
        test("TC-PLAY-INTEGRITY-LOGGING-DATA-002 확인마다 새 무작위 값을 요청 해시로 담는다") {
            val token = fixtureMonkey.giveMeOne<PlayIntegrityToken>()
            val requestHashList = mutableListOf<String>()
            val tokenProvider = mockk<PlayIntegrityTokenProvider>()
            coEvery { tokenProvider.getToken(requestHash = capture(requestHashList)) } returns token
            val remoteDataSource = mockk<PlayIntegrityRemoteDataSource>()
            coEvery { remoteDataSource.decode(token = any(), packageName = any()) } returns verdict()
            val repository = PlayIntegrityRepositoryImpl(playIntegrityTokenProvider = tokenProvider, playIntegrityRemoteDataSource = remoteDataSource)

            repository.fetch()
            repository.fetch()

            requestHashList shouldHaveSize 2
            requestHashList[0] shouldNotBe requestHashList[1]
        }

        test("TC-PLAY-INTEGRITY-LOGGING-DATA-001 발급받은 토큰과 패키지 이름으로 요청해 받은 판정 결과를 그대로 돌려준다") {
            val token = fixtureMonkey.giveMeOne<PlayIntegrityToken>()
            val verdict = verdict()
            val tokenProvider = mockk<PlayIntegrityTokenProvider>()
            coEvery { tokenProvider.getToken(requestHash = any()) } returns token
            val remoteDataSource = mockk<PlayIntegrityRemoteDataSource>()
            coEvery { remoteDataSource.decode(token = token.token, packageName = token.packageName) } returns verdict
            val repository = PlayIntegrityRepositoryImpl(playIntegrityTokenProvider = tokenProvider, playIntegrityRemoteDataSource = remoteDataSource)

            repository.fetch() shouldBe verdict
        }

        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-007 Play Integrity를 제공하지 않는 플랫폼에서는 서버에 요청하지 않는다") {
            val tokenProvider = mockk<PlayIntegrityTokenProvider>()
            coEvery { tokenProvider.getToken(requestHash = any()) } returns null
            val remoteDataSource = mockk<PlayIntegrityRemoteDataSource>()
            val repository = PlayIntegrityRepositoryImpl(playIntegrityTokenProvider = tokenProvider, playIntegrityRemoteDataSource = remoteDataSource)

            repository.fetch().shouldBeNull()
            coVerify(exactly = 0) { remoteDataSource.decode(token = any(), packageName = any()) }
        }

        test("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-008 토큰 발급에 실패하면 서버에 요청하지 않고 실패를 전달한다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val tokenProvider = mockk<PlayIntegrityTokenProvider>()
            coEvery { tokenProvider.getToken(requestHash = any()) } throws failure
            val remoteDataSource = mockk<PlayIntegrityRemoteDataSource>()
            val repository = PlayIntegrityRepositoryImpl(playIntegrityTokenProvider = tokenProvider, playIntegrityRemoteDataSource = remoteDataSource)

            shouldThrowExactly<IllegalStateException> { repository.fetch() } shouldBe failure
            coVerify(exactly = 0) { remoteDataSource.decode(token = any(), packageName = any()) }
        }
    }) {
    private companion object {
        fun verdict(): JsonObject = JsonObject(mapOf("appIntegrity" to JsonObject(mapOf("appRecognitionVerdict" to JsonPrimitive(fixtureMonkey.giveMeOne<String>())))))
    }
}
