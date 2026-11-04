package io.github.taetae98coding.diary.compose.credential.apple

import android.net.Uri
import androidx.browser.auth.AuthTabIntent
import androidx.core.net.toUri
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.credential.AuthorizationResponseParameter
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidAppleCredentialsManagerTest {
    private val redirectScheme = "io.github.taetae98coding.diary.dev"
    private val resultReceiver = AuthTabResultReceiver()
    private val returnUri = "$redirectScheme://${AndroidAppleCredentialsManager.APPLE_SIGN_IN_REDIRECT_HOST}"

    @Test
    fun `TC-86dee46f-aeb7-420e-9d67-28230edc8116 응답 없이 인증용 브라우저 탭을 닫으면 취소로 처리한다`() =
        runTest {
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(launchedUrlList::add)

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            resultReceiver.deliver(resultCode = AuthTabIntent.RESULT_CANCELED, resultUri = null)
            runCurrent()

            launchedUrlList.size shouldBe 1
            shouldThrowExactly<AppleCredentialsUserCancelException> { signIn.await().getOrThrow() }
        }

    @Test
    fun `TC-9d7843cf-9a4c-40b6-86fb-1c2408dfa172 인증용 브라우저 탭이 넘긴 인증 결과로 로그인한다`() =
        runTest {
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(launchedUrlList::add)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { manager.signIn() }
            resultReceiver.deliver(resultCode = AuthTabIntent.RESULT_OK, resultUri = redirectUri(launchedUrlList.single(), idToken))
            runCurrent()

            signIn.await().credential.idToken shouldBe idToken
        }

    @Test
    fun `인증용 브라우저 탭을 앱 복귀 주소의 scheme으로 연다`() =
        runTest {
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(launchedUrlList::add)

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            val state =
                launchedUrlList
                    .single()
                    .toUri()
                    .getQueryParameter(AuthorizationResponseParameter.STATE)
                    .orEmpty()
            resultReceiver.deliver(resultCode = AuthTabIntent.RESULT_CANCELED, resultUri = null)
            runCurrent()
            signIn.await()

            AppleWebSignInState.decodeReturnUri(state) shouldBe returnUri
        }

    @Test
    fun `Apple이 취소를 알린 응답은 사용자 취소로 처리한다`() =
        runTest {
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(launchedUrlList::add)

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            val state = launchedUrlList.single().toUri().getQueryParameter(AuthorizationResponseParameter.STATE)
            resultReceiver.deliver(resultCode = AuthTabIntent.RESULT_OK, resultUri = "$returnUri?error=user_cancelled_authorize&state=$state".toUri())
            runCurrent()

            shouldThrowExactly<AppleCredentialsUserCancelException> { signIn.await().getOrThrow() }
        }

    @Test
    fun `인증용 브라우저 탭이 취소가 아닌 실패를 알리면 인증 결과 획득 실패로 처리한다`() =
        runTest {
            val manager = manager {}

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            resultReceiver.deliver(resultCode = AuthTabIntent.RESULT_UNKNOWN_CODE, resultUri = null)
            runCurrent()

            shouldThrowExactly<AppleCredentialsException> { signIn.await().getOrThrow() }
        }

    @Test
    fun `새 로그인을 시작하면 이전 로그인은 결과를 받지 않고 끝난다`() =
        runTest {
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(launchedUrlList::add)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val first = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            val second = async(start = CoroutineStart.UNDISPATCHED) { manager.signIn() }
            resultReceiver.deliver(resultCode = AuthTabIntent.RESULT_OK, resultUri = redirectUri(launchedUrlList.last(), idToken))
            runCurrent()

            (first.await().exceptionOrNull() is CancellationException) shouldBe true
            second.await().credential.idToken shouldBe idToken
        }

    private fun manager(launchAuthTab: (String) -> Unit): AndroidAppleCredentialsManager =
        AndroidAppleCredentialsManager(
            requestFactory = AppleWebSignInRequestFactory(config = fixtureMonkey.androidAppleCredentialsConfig()),
            redirectScheme = redirectScheme,
            resultReceiver = resultReceiver,
            launchAuthTab = launchAuthTab,
        )

    private fun FixtureMonkey.androidAppleCredentialsConfig(): AppleCredentialsConfig =
        AppleCredentialsConfig(
            clientId = "io.github.taetae98coding.diary.dev.web-${giveMeOne<String>()}",
            callbackUrl = "https://${giveMeOne<Long>().toULong()}.supabase.co/functions/v1/v1-session-apple-callback",
        )

    private fun redirectUri(
        authorizationUrl: String,
        idToken: String,
    ): Uri =
        returnUri
            .toUri()
            .buildUpon()
            .appendQueryParameter(AuthorizationResponseParameter.ID_TOKEN, idToken)
            .appendQueryParameter(AuthorizationResponseParameter.STATE, authorizationUrl.toUri().getQueryParameter(AuthorizationResponseParameter.STATE))
            .build()
}
