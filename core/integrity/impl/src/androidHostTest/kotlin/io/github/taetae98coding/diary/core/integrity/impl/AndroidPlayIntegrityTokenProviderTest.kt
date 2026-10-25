package io.github.taetae98coding.diary.core.integrity.impl

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.integrity.api.PlayIntegrityToken
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidPlayIntegrityTokenProviderTest {
    private val integrityManager = mockk<StandardIntegrityManager>()

    @Before
    fun setUp() {
        val firebaseOptions = mockk<FirebaseOptions>()
        every { firebaseOptions.gcmSenderId } returns fixtureMonkey.giveMeOne<Long>().toString()
        val firebaseApp = mockk<FirebaseApp>()
        every { firebaseApp.options } returns firebaseOptions

        mockkStatic(FirebaseApp::class, IntegrityManagerFactory::class)
        every { FirebaseApp.getInstance() } returns firebaseApp
        every { IntegrityManagerFactory.createStandard(any<Context>()) } returns integrityManager
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseApp::class, IntegrityManagerFactory::class)
    }

    @Test
    fun `TC-3e0082f8-6e96-4f1f-ae14-648babc4b671 준비가 무효가 되면 그 확인은 실패하고 다음 확인에서 다시 준비한 뒤 무결성 확인 정보를 요청한다`() {
        val token = fixtureMonkey.nonBlankString()
        val invalidProvider = tokenProvider(Tasks.forException(integrityException(StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID)))
        val preparedAgainProvider = tokenProvider(Tasks.forResult(integrityToken(token)))
        every { integrityManager.prepareIntegrityToken(any<PrepareIntegrityTokenRequest>()) } returnsMany
            listOf(Tasks.forResult(invalidProvider), Tasks.forResult(preparedAgainProvider))
        val context = RuntimeEnvironment.getApplication()
        val playIntegrityTokenProvider = AndroidPlayIntegrityTokenProvider(context = context)

        shouldThrow<StandardIntegrityException> { getToken(playIntegrityTokenProvider) }
        val result = getToken(playIntegrityTokenProvider)

        result shouldBe PlayIntegrityToken(token = token, packageName = context.packageName)
        verify(exactly = 2) { integrityManager.prepareIntegrityToken(any<PrepareIntegrityTokenRequest>()) }
        verify(exactly = 1) { preparedAgainProvider.request(any<StandardIntegrityTokenRequest>()) }
    }

    @Test
    fun `준비가 무효라는 실패가 아니면 준비한 제공자를 버리지 않고 다음 확인에 다시 쓴다`() {
        val token = fixtureMonkey.nonBlankString()
        val preparedProvider = mockk<StandardIntegrityTokenProvider>()
        every { preparedProvider.request(any<StandardIntegrityTokenRequest>()) } returnsMany
            listOf(
                Tasks.forException(integrityException(StandardIntegrityErrorCode.NETWORK_ERROR)),
                Tasks.forResult(integrityToken(token)),
            )
        every { integrityManager.prepareIntegrityToken(any<PrepareIntegrityTokenRequest>()) } returns Tasks.forResult(preparedProvider)
        val context = RuntimeEnvironment.getApplication()
        val playIntegrityTokenProvider = AndroidPlayIntegrityTokenProvider(context = context)

        shouldThrow<StandardIntegrityException> { getToken(playIntegrityTokenProvider) }
        val result = getToken(playIntegrityTokenProvider)

        result shouldBe PlayIntegrityToken(token = token, packageName = context.packageName)
        verify(exactly = 1) { integrityManager.prepareIntegrityToken(any<PrepareIntegrityTokenRequest>()) }
    }

    private fun getToken(playIntegrityTokenProvider: AndroidPlayIntegrityTokenProvider): PlayIntegrityToken = runBlocking { withTimeout(TIMEOUT_MILLIS) { playIntegrityTokenProvider.getToken(requestHash = fixtureMonkey.nonBlankString()) } }

    private companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        // StandardIntegrityException은 공개 생성자가 없어 Google Play가 돌려주는 오류를 MockK로 흉내 낸다.
        private fun integrityException(code: Int): StandardIntegrityException =
            mockk<StandardIntegrityException>(relaxed = true) {
                every { errorCode } returns code
            }

        private fun integrityToken(value: String): StandardIntegrityToken =
            mockk<StandardIntegrityToken> {
                every { token() } returns value
            }

        private fun tokenProvider(response: Task<StandardIntegrityToken>): StandardIntegrityTokenProvider =
            mockk<StandardIntegrityTokenProvider> {
                every { request(any<StandardIntegrityTokenRequest>()) } returns response
            }
    }
}
