package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.activity.ComponentDialog
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsException
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManager
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsUserCancelException
import io.github.taetae98coding.diary.compose.credential.apple.AppleSignInResult
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.authentication.appleAuthorization
import io.github.taetae98coding.diary.core.testing.authentication.appleCredential
import io.github.taetae98coding.diary.domain.account.usecase.CheckAppleReauthenticationRequiredUseCase
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.DeleteAccountUseCase
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProfileScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-9f7b2656-4323-4665-97f8-2b3338e46146 두 동작을 순서대로 표시한다`() {
        setScreen(Fixture())

        val changeTop =
            composeRule
                .onNodeWithText(CHANGE_LABEL)
                .fetchSemanticsNode()
                .boundsInRoot.top
        val deleteTop =
            composeRule
                .onNodeWithText(DELETE_LABEL)
                .fetchSemanticsNode()
                .boundsInRoot.top

        (changeTop < deleteTop) shouldBe true
        composeRule.onNodeWithText(CHANGE_LABEL).assertIsEnabled()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsEnabled()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목과 동작 문구를 표시한다`() {
        setScreen(Fixture())

        composeRule.onNodeWithText("프로필").assertExists()
        composeRule.onNodeWithText("프로필 이미지 변경").assertExists()
        composeRule.onNodeWithText("탈퇴하기").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("탈퇴할까요?").assertExists()
        composeRule.onNodeWithText("탈퇴").assertExists()
        composeRule.onNodeWithText("취소").assertExists()
    }

    @Test
    fun `TC-34878f0f-1805-494b-8498-6d8e3ef35e3a 프로필 이미지 변경을 선택하면 ProfileImageEdit 화면으로 이동한다`() {
        val fixture = Fixture()
        setScreen(fixture)

        composeRule.onNodeWithText(CHANGE_LABEL).performClick()
        composeRule.waitForIdle()

        fixture.navigateToEditCount shouldBe 1
    }

    @Test
    fun `TC-7f3778a6-b045-4310-af80-c458ac781bcd 탈퇴가 진행 중이면 두 동작을 실행할 수 없다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        setScreen(fixture)
        confirmDelete()

        composeRule.onNodeWithText(CHANGE_LABEL).assertIsNotEnabled()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsNotEnabled()
        composeRule.onNodeWithText(CHANGE_LABEL).performClick()
        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()

        fixture.navigateToEditCount shouldBe 0
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = any()) }
    }

    @Test
    fun `TC-f6c0e0be-4d94-4ca3-91e1-fcce03c712c0 뒤로가면 이전 화면으로 돌아간다`() {
        val fixture = Fixture()
        setScreen(fixture)

        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        fixture.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-135005f9-705b-4f5f-9663-76b1c2abb1da 계정이 게스트가 되면 화면을 닫는다`() {
        val fixture = Fixture()
        setScreen(fixture)
        composeRule.waitForIdle()

        composeRule.runOnIdle { fixture.accountFlow.value = Result.success(Account.Guest) }
        composeRule.waitForIdle()

        fixture.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-39cad1b0-fe53-4dc0-b394-b2d34e99ad42 계정 확인에 실패해도 화면을 닫지 않는다`() {
        val fixture = Fixture()
        setScreen(fixture)
        composeRule.waitForIdle()

        composeRule.runOnIdle { fixture.accountFlow.value = Result.failure(IllegalStateException("account")) }
        composeRule.waitForIdle()

        fixture.navigateUpCount shouldBe 0
        composeRule.onNodeWithText(DELETE_LABEL).assertExists()
    }

    @Test
    fun `TC-39cad1b0-fe53-4dc0-b394-b2d34e99ad42 세션 갱신 여부만 바뀌어도 화면을 닫지 않는다`() {
        val fixture = Fixture()
        val user = Arb.accountUser().next().copy(isSessionValid = true)
        fixture.accountFlow.value = Result.success(user)
        setScreen(fixture)
        composeRule.waitForIdle()

        composeRule.runOnIdle { fixture.accountFlow.value = Result.success(user.copy(isSessionValid = false)) }
        composeRule.waitForIdle()

        fixture.navigateUpCount shouldBe 0
        composeRule.onNodeWithText(CHANGE_LABEL).assertIsEnabled()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-265aa834-9a55-4d5b-83db-a892be445edc 탈퇴하기를 선택하면 확인 다이얼로그가 열린다`() {
        val fixture = Fixture()
        setScreen(fixture)

        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertExists()
        composeRule.onNodeWithText(CONFIRM_MESSAGE).assertExists()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
    }

    @Test
    fun `TC-a82417c6-2e09-4a8a-a181-c3490c24f6d7 취소를 고르면 아무것도 바뀌지 않는다`() {
        val fixture = Fixture()
        setScreen(fixture)
        composeRule.onNodeWithText(DELETE_LABEL).performClick()

        composeRule.onNodeWithText(CANCEL_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        coVerify(exactly = 0) { fixture.appleCredentialsManager.signIn() }
    }

    @Test
    fun `TC-a82417c6-2e09-4a8a-a181-c3490c24f6d7 뒤로가기로 닫으면 아무것도 바뀌지 않는다`() {
        val fixture = Fixture()
        setScreen(fixture)
        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()

        val dialog = ShadowDialog.getLatestDialog() as ComponentDialog
        composeRule.runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        coVerify(exactly = 0) { fixture.appleCredentialsManager.signIn() }
    }

    @Test
    fun `TC-ed0014d8-5724-43b8-9211-311afb02c36a 재인증이 필요 없으면 바로 탈퇴를 요청하고 다이얼로그에 진행 중을 표시한다`() {
        val fixture = Fixture(isAppleReauthenticationRequired = false)
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        setScreen(fixture)

        confirmDelete()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertExists()
        composeRule.onNodeWithText(CANCEL_LABEL).assertIsNotEnabled()
        coVerify(exactly = 0) { fixture.appleCredentialsManager.signIn() }
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = null) }
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-394d73c1-14ac-40cc-8355-db7b3bc510ec 재인증이 필요하면 Apple 흐름에서 받은 인가 코드로 탈퇴를 요청한다`() {
        val authorization = Arb.appleAuthorization().next()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } returns signInResult(authorization = authorization)
        setScreen(fixture)

        confirmDelete()

        coVerify(exactly = 1) { fixture.appleCredentialsManager.signIn() }
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = authorization) }
    }

    @Test
    fun `TC-b28f7750-5460-48cc-927e-e6e0ab50a3a9 Apple 재인증을 취소하면 탈퇴하지 않고 안내하지 않는다`() {
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } throws AppleCredentialsUserCancelException()
        setScreen(fixture)

        confirmDelete()

        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsEnabled()
        composeRule.onNodeWithText(CHANGE_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-364abafa-b866-4902-b128-c8a3799b563f Apple 재인증 취소는 오류 보고를 남기지 않는다`() {
        val reportList = mutableListOf<CrashlyticsLog>()
        val delegate = mockk<DiaryLoggerDelegate>()
        every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
        DiaryLogger.add(delegate = delegate)
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } throws AppleCredentialsUserCancelException()
        setScreen(fixture)

        confirmDelete()

        reportList shouldBe emptyList()
    }

    @Test
    fun `TC-30f87464-4507-40d0-aafe-b02ec5314b62 TC-364abafa-b866-4902-b128-c8a3799b563f 오류 없이 인가 코드 없이 끝나면 탈퇴 실패를 안내하고 오류 보고는 남기지 않는다`() {
        val reportList = recordCrashlyticsLog()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } returns signInResult(authorization = null)
        setScreen(fixture)

        confirmDelete()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()
        reportList shouldBe emptyList()
    }

    @Test
    fun `TC-30f87464-4507-40d0-aafe-b02ec5314b62 TC-364abafa-b866-4902-b128-c8a3799b563f Apple 로그인 흐름이 오류로 끝나면 탈퇴 실패를 안내하고 오류 보고를 남긴다`() {
        val reportList = recordCrashlyticsLog()
        val failure = AppleCredentialsException(message = "apple failure")
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } throws failure
        setScreen(fixture)

        confirmDelete()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()
        reportList.single().throwable shouldBe failure
    }

    @Test
    fun `TC-ed9f47a1-176a-4d60-a181-06e62faa6583 서버가 실패하면 실패를 안내하고 다시 시도할 수 있다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } returns Result.failure(IllegalStateException("delete"))
        setScreen(fixture)

        confirmDelete()

        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        fixture.navigateUpCount shouldBe 0
        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONFIRM_TITLE).assertExists()
    }

    @Test
    fun `TC-13a4dded-2539-40aa-8637-ea6f4de0517b 탈퇴에 성공하면 안내 없이 게스트가 되어 화면을 닫는다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers {
            fixture.accountFlow.value = Result.success(Account.Guest)
            Result.success(Unit)
        }
        setScreen(fixture)

        confirmDelete()

        fixture.navigateUpCount shouldBe 1
        composeRule.onNodeWithText(FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-ff12655f-9d05-45d5-941d-052f02a957c2 Apple 재인증 중 다이얼로그에서 탈퇴를 다시 고르면 새 흐름만 연다`() {
        val firstAuthorization = Arb.appleAuthorization().next()
        val secondAuthorization = Arb.appleAuthorization().next()
        val first = CompletableDeferred<AppleSignInResult>()
        val second = CompletableDeferred<AppleSignInResult>()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } coAnswers { first.await() } coAndThen { second.await() }
        setScreen(fixture)

        confirmDelete()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(CONFIRM_LABEL).assertIsEnabled()

        composeRule.onNodeWithText(CONFIRM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            first.complete(signInResult(authorization = firstAuthorization))
            second.complete(signInResult(authorization = secondAuthorization))
        }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { fixture.appleCredentialsManager.signIn() }
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = firstAuthorization) }
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = secondAuthorization) }
    }

    @Test
    fun `TC-ff12655f-9d05-45d5-941d-052f02a957c2 탈퇴를 다시 고른 뒤 끝난 첫 흐름의 인가 코드는 받지 않는다`() {
        val firstAuthorization = Arb.appleAuthorization().next()
        val secondAuthorization = Arb.appleAuthorization().next()
        val first = CompletableDeferred<AppleSignInResult>()
        val second = CompletableDeferred<AppleSignInResult>()
        val secondCheck = CompletableDeferred<Result<Boolean>>()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.checkUseCase(parameter = Unit) } returns Result.success(true) coAndThen { secondCheck.await() }
        coEvery { fixture.appleCredentialsManager.signIn() } coAnswers { first.await() } coAndThen { second.await() }
        setScreen(fixture)

        confirmDelete()
        composeRule.onNodeWithText(CONFIRM_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle { first.complete(signInResult(authorization = firstAuthorization)) }
        composeRule.waitForIdle()

        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        composeRule.onNodeWithText(CONFIRM_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()

        composeRule.runOnIdle { secondCheck.complete(Result.success(true)) }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertDoesNotExist()
        composeRule.runOnIdle { second.complete(signInResult(authorization = secondAuthorization)) }
        composeRule.waitForIdle()

        coVerify(exactly = 2) { fixture.appleCredentialsManager.signIn() }
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = firstAuthorization) }
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = secondAuthorization) }
    }

    @Test
    fun `TC-fdf51fd0-26c2-4877-a583-b9a9166aeecf Apple 재인증 중 취소로 닫으면 흐름을 끝내고 탈퇴하지 않는다`() {
        assertDismissDuringReauthenticationEndsFlow { composeRule.onNodeWithText(CANCEL_LABEL).performClick() }
    }

    @Test
    fun `TC-fdf51fd0-26c2-4877-a583-b9a9166aeecf Apple 재인증 중 뒤로가기로 닫으면 흐름을 끝내고 탈퇴하지 않는다`() {
        assertDismissDuringReauthenticationEndsFlow {
            val dialog = ShadowDialog.getLatestDialog() as ComponentDialog
            composeRule.runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
        }
    }

    private fun assertDismissDuringReauthenticationEndsFlow(dismiss: () -> Unit) {
        val pending = CompletableDeferred<AppleSignInResult>()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } coAnswers { pending.await() }
        setScreen(fixture)

        confirmDelete()
        dismiss()
        composeRule.waitForIdle()
        composeRule.runOnIdle { pending.complete(signInResult(authorization = Arb.appleAuthorization().next())) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
    }

    @Test
    fun `TC-2d841dee-ec2d-4eab-8783-63ab01d80be1 탈퇴 진행 중에는 뒤로가기로 다이얼로그를 닫을 수 없다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        setScreen(fixture)
        confirmDelete()

        val dialog = ShadowDialog.getLatestDialog() as ComponentDialog
        composeRule.runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertExists()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = any()) }
    }

    @Test
    fun `TC-c94e0258-ad3d-4541-af01-4070488bca25 확인 다이얼로그는 화면이 재생성되어도 열림과 닫힘을 유지한다`() {
        val fixture = Fixture()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ProfileContent(fixture) }
        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertExists()

        composeRule.onNodeWithText(CANCEL_LABEL).performClick()
        composeRule.waitForIdle()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
    }

    @Test
    fun `TC-fe336a0c-9832-488a-901e-eade609d22e5 Apple 재인증 중 화면이 재생성되면 흐름을 중단하고 탈퇴 대기로 돌아간다`() {
        val pending = CompletableDeferred<AppleSignInResult>()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.appleCredentialsManager.signIn() } coAnswers { pending.await() }
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ProfileContent(fixture) }

        confirmDelete()
        coVerify(exactly = 1) { fixture.appleCredentialsManager.signIn() }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.runOnIdle { pending.complete(signInResult(authorization = Arb.appleAuthorization().next())) }
        composeRule.waitForIdle()

        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        composeRule.onNodeWithText(CONFIRM_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-c6f65655-042d-482c-8e05-bc07a3289ec6 판단 중 재생성되어도 재인증이 필요하면 재인증을 시작한다`() {
        val checkResult = CompletableDeferred<Result<Boolean>>()
        val fixture = Fixture(isAppleReauthenticationRequired = true)
        coEvery { fixture.checkUseCase(parameter = Unit) } coAnswers { checkResult.await() }
        coEvery { fixture.appleCredentialsManager.signIn() } coAnswers { CompletableDeferred<AppleSignInResult>().await() }
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ProfileContent(fixture) }

        confirmDelete()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()

        composeRule.runOnIdle { checkResult.complete(Result.success(true)) }
        composeRule.waitForIdle()

        coVerify(exactly = 1) { fixture.appleCredentialsManager.signIn() }
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-c6f65655-042d-482c-8e05-bc07a3289ec6 판단 중 재생성되어도 재인증이 필요 없으면 진행 중을 유지하고 한 번만 요청한다`() {
        val checkResult = CompletableDeferred<Result<Boolean>>()
        val fixture = Fixture()
        coEvery { fixture.checkUseCase(parameter = Unit) } coAnswers { checkResult.await() }
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ProfileContent(fixture) }

        confirmDelete()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsNotEnabled()
        composeRule.onNodeWithText(CHANGE_LABEL).assertIsNotEnabled()

        composeRule.runOnIdle { checkResult.complete(Result.success(false)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsNotEnabled()
        composeRule.onNodeWithText(CHANGE_LABEL).assertIsNotEnabled()
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = null) }
    }

    @Test
    fun `TC-01c0991e-d5bf-4e4a-9d1b-5713a21c8fe6 탈퇴 처리 중 화면이 재생성되면 진행 중 표시가 이어진다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ProfileContent(fixture) }

        confirmDelete()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()
        coVerify(exactly = 1) { fixture.deleteAccountUseCase(parameter = any()) }
    }

    @Test
    fun `TC-99f22181-eeaf-4512-a2be-4958af49c4d9 실패한 뒤 떠났다 다시 들어오면 안내 없이 대기로 시작한다`() {
        val failed = Fixture()
        coEvery { failed.deleteAccountUseCase(parameter = any()) } returns Result.failure(IllegalStateException("delete"))
        val current = mutableStateOf(failed)
        composeRule.setContent { key(current.value) { ProfileContent(current.value) } }
        confirmDelete()
        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()

        composeRule.runOnIdle { current.value = Fixture() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DELETE_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-0ad36304-87ed-4c7b-bf3c-34c2ff1b125e 재인증 필요 여부를 판단하지 못하면 탈퇴하지 않고 실패를 안내한다`() {
        val fixture = Fixture(isAppleReauthenticationCheckFailed = true)
        setScreen(fixture)

        confirmDelete()

        coVerify(exactly = 0) { fixture.appleCredentialsManager.signIn() }
        coVerify(exactly = 0) { fixture.deleteAccountUseCase(parameter = any()) }
        composeRule.onNodeWithText(FAILED_MESSAGE).assertExists()
    }

    @Test
    fun `탈퇴 진행 표시는 취소 버튼과 세로 가운데가 같다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        setScreen(fixture)

        confirmDelete()

        val progress = composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).fetchSemanticsNode().boundsInRoot
        val cancel = composeRule.onNodeWithText(CANCEL_LABEL).fetchSemanticsNode().boundsInRoot
        (abs(progress.center.y - cancel.center.y) <= 1F) shouldBe true
    }

    @Test
    fun `탈퇴 진행 중에도 안내 문구와 버튼 자리를 그대로 둔다`() {
        val fixture = Fixture()
        coEvery { fixture.deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
        setScreen(fixture)
        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()
        val confirmBefore = composeRule.onNodeWithText(CONFIRM_LABEL).fetchSemanticsNode().boundsInRoot
        val cancelBefore = composeRule.onNodeWithText(CANCEL_LABEL).fetchSemanticsNode().boundsInRoot

        composeRule.onNodeWithText(CONFIRM_LABEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(PROGRESS_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(CONFIRM_MESSAGE).assertExists()
        composeRule.onNodeWithText(CONFIRM_LABEL).fetchSemanticsNode().boundsInRoot shouldBe confirmBefore
        composeRule.onNodeWithText(CANCEL_LABEL).fetchSemanticsNode().boundsInRoot shouldBe cancelBefore
    }

    private fun confirmDelete() {
        composeRule.onNodeWithText(DELETE_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONFIRM_LABEL).performClick()
        composeRule.waitForIdle()
    }

    private fun setScreen(fixture: Fixture) {
        composeRule.setContent { ProfileContent(fixture) }
    }

    private companion object {
        const val CHANGE_LABEL = "Change profile photo"
        const val DELETE_LABEL = "Delete account"
        const val CONFIRM_TITLE = "Delete account?"
        const val CONFIRM_MESSAGE =
            "Your account and all data stored on the server will be deleted. This can't be undone. " +
                "Data for this account on this device, including changes not yet sent to the server, will also be deleted."
        const val CONFIRM_LABEL = "Delete"
        const val CANCEL_LABEL = "Cancel"
        const val FAILED_MESSAGE = "Couldn't delete your account. Please try again."
        const val PROGRESS_DESCRIPTION = "Deleting account"
        const val NAVIGATE_UP_DESCRIPTION = "Navigate up"
    }
}

@androidx.compose.runtime.Composable
private fun ProfileContent(fixture: Fixture) {
    DiaryTheme {
        ProfileScreen(
            navigateUp = { fixture.navigateUpCount += 1 },
            navigateToProfileImageEdit = { fixture.navigateToEditCount += 1 },
            appleCredentialsManager = fixture.appleCredentialsManager,
            accountViewModel = fixture.accountViewModel,
            deleteViewModel = fixture.deleteViewModel,
        )
    }
}

private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
    val reportList = mutableListOf<CrashlyticsLog>()
    val delegate = mockk<DiaryLoggerDelegate>()
    every { delegate.log(log = any()) } answers { (firstArg<DiaryLog>() as? CrashlyticsLog)?.let(reportList::add) }
    DiaryLogger.add(delegate = delegate)

    return reportList
}

private fun signInResult(authorization: AppleAuthorization?): AppleSignInResult = AppleSignInResult(credential = Arb.appleCredential().next(), authorization = authorization)

private class Fixture(
    isAppleReauthenticationRequired: Boolean = false,
    isAppleReauthenticationCheckFailed: Boolean = false,
) {
    var navigateUpCount = 0
    var navigateToEditCount = 0
    val accountFlow = MutableStateFlow(Result.success<Account>(Arb.accountUser().next()))
    val appleCredentialsManager: AppleCredentialsManager = mockk()
    val deleteAccountUseCase: DeleteAccountUseCase = mockk()
    private val getAccountUseCase: GetAccountUseCase = mockk()
    val checkUseCase: CheckAppleReauthenticationRequiredUseCase = mockk()

    init {
        every { getAccountUseCase(parameter = Unit) } returns accountFlow
        coEvery { checkUseCase(parameter = Unit) } returns
            if (isAppleReauthenticationCheckFailed) {
                Result.failure(IllegalStateException("check"))
            } else {
                Result.success(isAppleReauthenticationRequired)
            }
        coEvery { deleteAccountUseCase(parameter = any()) } returns Result.success(Unit)
    }

    val accountViewModel: ProfileAccountViewModel = ProfileAccountViewModel(getAccountUseCase = getAccountUseCase)
    val deleteViewModel: ProfileDeleteViewModel =
        ProfileDeleteViewModel(
            savedStateHandle = SavedStateHandle(),
            checkAppleReauthenticationRequiredUseCase = checkUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
        )
}
