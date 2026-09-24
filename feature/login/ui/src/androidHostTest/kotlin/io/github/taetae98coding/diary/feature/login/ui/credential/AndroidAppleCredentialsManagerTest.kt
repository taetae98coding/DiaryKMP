@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.login.ui.credential

import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.seconds

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidAppleCredentialsManagerTest {
    private val returnUri = "io.github.taetae98coding.diary.dev://$APPLE_SIGN_IN_REDIRECT_HOST"

    @Test
    fun `TC-LOGIN-FEATURE-022 응답 없이 브라우저에서 앱으로 돌아오면 취소로 처리한다`() =
        runTest {
            val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED, UnconfinedTestDispatcher(testScheduler))
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(lifecycleOwner.lifecycle, launchedUrlList::add)

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            leaveAndReturn(lifecycleOwner)
            advanceTimeBy(AndroidAppleCredentialsManager.REDIRECT_GRACE_AFTER_RETURN + 1.seconds)

            launchedUrlList.size shouldBe 1
            shouldThrowExactly<AppleCredentialsUserCancelException> { signIn.await().getOrThrow() }
        }

    @Test
    fun `TC-LOGIN-FEATURE-023 앱으로 돌아오기 전에 도착한 인증 결과로 로그인한다`() =
        runTest {
            val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED, UnconfinedTestDispatcher(testScheduler))
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(lifecycleOwner.lifecycle, launchedUrlList::add)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { manager.signIn() }
            lifecycleOwner.currentState = Lifecycle.State.STARTED
            runCurrent()
            AppleSignInRedirectRelay.deliver(redirectUri(launchedUrlList.single(), idToken))
            lifecycleOwner.currentState = Lifecycle.State.RESUMED
            runCurrent()

            signIn.await().idToken shouldBe idToken
        }

    @Test
    fun `TC-LOGIN-FEATURE-023 앱으로 돌아온 직후에 도착한 인증 결과도 취소로 처리하지 않는다`() =
        runTest {
            val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED, UnconfinedTestDispatcher(testScheduler))
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(lifecycleOwner.lifecycle, launchedUrlList::add)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { manager.signIn() }
            leaveAndReturn(lifecycleOwner)
            AppleSignInRedirectRelay.deliver(redirectUri(launchedUrlList.single(), idToken))
            runCurrent()

            signIn.await().idToken shouldBe idToken
        }

    @Test
    fun `Apple이 취소를 알린 응답은 사용자 취소로 처리한다`() =
        runTest {
            val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED, UnconfinedTestDispatcher(testScheduler))
            val launchedUrlList = mutableListOf<String>()
            val manager = manager(lifecycleOwner.lifecycle, launchedUrlList::add)

            val signIn = async(start = CoroutineStart.UNDISPATCHED) { runCatching { manager.signIn() } }
            val state = launchedUrlList.single().toUri().getQueryParameter(AppleWebSignInResponse.STATE_PARAMETER)
            AppleSignInRedirectRelay.deliver("$returnUri?error=user_cancelled_authorize&state=$state".toUri())
            runCurrent()

            shouldThrowExactly<AppleCredentialsUserCancelException> { signIn.await().getOrThrow() }
        }

    private fun manager(
        lifecycle: Lifecycle,
        launchBrowser: (String) -> Unit,
    ): AndroidAppleCredentialsManager =
        AndroidAppleCredentialsManager(
            lifecycle = lifecycle,
            requestFactory =
                AppleWebSignInRequestFactory(
                    AppleCredentialsConfig(
                        clientId = "io.github.taetae98coding.diary.dev.web-${fixtureMonkey.giveMeOne<String>()}",
                        callbackUrl = "https://${fixtureMonkey.giveMeOne<Long>().toULong()}.supabase.co/functions/v1/v1-session-apple-callback",
                    ),
                ),
            returnUri = returnUri,
            launchBrowser = launchBrowser,
        )

    private fun redirectUri(
        authorizationUrl: String,
        idToken: String,
    ) = returnUri
        .toUri()
        .buildUpon()
        .appendQueryParameter(AppleWebSignInResponse.ID_TOKEN_PARAMETER, idToken)
        .appendQueryParameter(AppleWebSignInResponse.STATE_PARAMETER, authorizationUrl.toUri().getQueryParameter(AppleWebSignInResponse.STATE_PARAMETER))
        .build()

    private fun TestScope.leaveAndReturn(lifecycleOwner: TestLifecycleOwner) {
        lifecycleOwner.currentState = Lifecycle.State.STARTED
        runCurrent()
        lifecycleOwner.currentState = Lifecycle.State.RESUMED
        runCurrent()
    }
}
