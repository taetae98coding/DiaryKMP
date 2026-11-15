package io.github.taetae98coding.diary.feature.qr.ui.detail

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import androidx.core.content.FileProvider
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.test.core.app.ApplicationProvider
import io.github.taetae98coding.diary.compose.core.share.ContentSharer
import io.github.taetae98coding.diary.compose.core.share.ContentSharerFactory
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.compose.qr.qrCodePngOf
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.qr.ui.add.QR_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.actionNameList
import io.github.taetae98coding.diary.feature.qr.ui.add.editableText
import io.github.taetae98coding.diary.feature.qr.ui.add.isTabSelected
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrFieldInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrTextInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onTitleInput
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTextInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.selectFormat
import io.github.taetae98coding.diary.feature.qr.ui.add.titleInputText
import io.github.taetae98coding.diary.feature.qr.ui.detail.action.AndroidQrActionLauncherFactory
import io.github.taetae98coding.diary.feature.qr.ui.detailTagScreenTestViewModel
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.qr.ui.testContentSharerFactory
import io.github.taetae98coding.diary.feature.qr.ui.testQrCodePngEncoder
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w480dp-h1200dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QrDetailShareTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Before
    fun clearFileProviderPathCache() {
        val cache =
            FileProvider::class.java
                .getDeclaredField("sCache")
                .apply { isAccessible = true }
                .get(null) as MutableMap<*, *>
        synchronized(cache) { cache.clear() }
    }

    @Test
    fun `TC-486b1dde-2f56-4d7f-8bb0-f9e1d29c8a1c 조회 중에는 공유를 실행할 수 없다`() {
        val fake = FakeViewModel(detail = QrDetail(title = COMPANY, description = "", value = EXAMPLE_URL))
        fake.uiState.value = QrDetailUiState.Loading
        composeRule.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.waitForIdle()

        composeRule.onNode(shareButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-61509433-21d2-463c-a7c6-c0c46f9b6b90 PNG 이미지를 공유할 수 없으면 공유 버튼을 두지 않는다`() {
        val sharer =
            mockk<ContentSharer> {
                every { isTextSupported } returns true
                every { isFileSupported(type = any()) } returns false
            }
        val sharerFactory = sharerFactoryOf(sharer = sharer)

        setScreen(detail = QrDetail(title = COMPANY, description = "", value = EXAMPLE_URL), sharerFactory = sharerFactory)

        composeRule.actionNameList() shouldNotContain SHARE
        composeRule.actionNameList() shouldContainAll listOf(OPEN_URL, UNDO, SCAN, DELETE)
    }

    @Test
    fun `TC-f7fa8e21-1d20-4d56-ae07-5727b1a511e2 입력 중인 QR 값의 이미지와 제목으로 공유한다`() {
        val caseList =
            listOf(
                COMPANY_WIFI to COMPANY_WIFI,
                "" to COMPANY,
                "   " to COMPANY,
            )

        val detail = QrDetail(title = COMPANY, description = "", value = HELLO)
        val fake = setScreen(detail = detail)

        caseList.forEach { (titleInput, expectedTitle) ->
            withClue(titleInput) {
                clearFileProviderPathCache()
                fake.reload(detail = detail)
                composeRule.onTitleInput().performTextReplacement(titleInput)
                composeRule.onQrTextInput().performTextReplacement(WIFI_VALUE)
                composeRule.waitForIdle()

                composeRule.onNode(shareButton()).performClick()
                composeRule.waitForIdle()

                val send = nextSendIntent()
                val uri = send.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java).shouldNotBeNull()
                send.type shouldBe PNG_MIME_TYPE
                uri.lastPathSegment shouldBe "$expectedTitle.png"
                send.getStringExtra(Intent.EXTRA_TEXT) shouldBe expectedTitle
                send.getStringExtra(Intent.EXTRA_SUBJECT) shouldBe expectedTitle
                application.contentResolver
                    .openInputStream(uri)
                    .shouldNotBeNull()
                    .use { stream -> stream.readBytes() } shouldBe qrCodePngOf(value = WIFI_VALUE)
                shadowOf(application).nextStartedActivity.shouldBeNull()
                composeRule.onNodeWithText(SHARE_FAILED).assertDoesNotExist()
                verify(exactly = 0) { fake.viewModel.update(detail = any()) }
            }
        }
    }

    @Test
    fun `TC-8b78fc5f-54ea-4306-97fa-82f9b2ac0cf7 QR 값이 비어 있으면 공유하지 않고 첫 입력을 알린다`() {
        val fake = setScreen(detail = QrDetail(title = COMPANY, description = "", value = EXAMPLE_URL))
        composeRule.onQrFieldInput(URL_LABEL).performTextClearance()
        composeRule.onTitleInput().performTextReplacement(EDITED_TITLE)
        composeRule.waitForIdle()

        composeRule.onNode(shareButton()).performClick()
        composeRule.waitForIdle()

        shadowOf(application).nextStartedActivity.shouldBeNull()
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.onNodeWithText(URL_EMPTY).assertExists()
        composeRule.onQrFieldInput(URL_LABEL).assertIsFocused()
        composeRule.selectedFormat() shouldBe URL_LABEL
        composeRule.titleInputText() shouldBe EDITED_TITLE
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-872621df-d4ad-4e4b-8a2d-0ea201aff827 공유 시트를 열지 못하면 공유하지 못했다고 알린다`() {
        shadowOf(application).checkActivities(true)
        setScreen(detail = QrDetail(title = COMPANY, description = "", value = HELLO))
        editToUrl()

        composeRule.onNode(shareButton()).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_FAILED).assertExists()
        assertUrlInputKept()
        assertUndoReturnsToHello()
    }

    @Test
    fun `TC-05561d7f-2c0c-4c28-a409-b4c1defd8fae 공유는 입력과 저장된 QR을 바꾸지 않는다`() {
        val fake = setScreen(detail = QrDetail(title = COMPANY, description = "", value = HELLO))
        editToUrl()

        composeRule.onNode(shareButton()).performClick()
        composeRule.waitForIdle()

        nextSendIntent()
        assertUrlInputKept()
        composeRule.actionNameList() shouldContainAll listOf(UPDATE)
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }
        assertUndoReturnsToHello()
    }

    @Test
    fun `TC-b165b8b3-2ee5-4f05-ade4-1019ade1cf7b 수정이나 삭제를 처리하는 동안에도 공유할 수 있다`() {
        val fake = setScreen(detail = QrDetail(title = COMPANY, description = "", value = EXAMPLE_URL))
        val progressList: List<(QrDetailUiState.Content) -> QrDetailUiState.Content> =
            listOf({ content -> content.copy(isUpdateInProgress = true) }, { content -> content.copy(isDeleteInProgress = true) })

        progressList.forEach { inProgress ->
            clearFileProviderPathCache()
            composeRule.runOnIdle { fake.uiState.value = inProgress(fake.uiState.value as QrDetailUiState.Content) }
            composeRule.waitForIdle()

            composeRule.onNode(shareButton()).performClick()
            composeRule.waitForIdle()

            nextSendIntent().type shouldBe PNG_MIME_TYPE
            composeRule.runOnIdle { fake.uiState.value = (fake.uiState.value as QrDetailUiState.Content).copy(isUpdateInProgress = false, isDeleteInProgress = false) }
        }
    }

    @Test
    fun `TC-2f603fc3-b9e7-4353-ae6d-fb31d329defd 공유하지 못했다는 안내는 재생성 후 다시 표시하지 않는다`() {
        shadowOf(application).checkActivities(true)
        val fake = FakeViewModel(detail = QrDetail(title = COMPANY, description = "", value = EXAMPLE_URL))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.onNode(shareButton()).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(SHARE_FAILED).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SHARE_FAILED).assertDoesNotExist()
    }

    @Test
    fun `TC-cb8ac985-9c74-4322-835c-6b2fc431d9b6 재생성되어도 공유를 다시 시작하지 않는다`() {
        val sharer =
            mockk<ContentSharer> {
                every { isTextSupported } returns true
                every { isFileSupported(type = any()) } returns true
                coEvery { share(content = any()) } coAnswers { awaitCancellation() }
            }
        val sharerFactory = sharerFactoryOf(sharer = sharer)
        val fake = FakeViewModel(detail = Arb.qrDetail().next())
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel, sharerFactory = sharerFactory) }
        composeRule.waitForIdle()
        composeRule.onNode(shareButton()).performClick()
        composeRule.waitForIdle()
        coVerify(exactly = 1) { sharer.share(content = any()) }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { sharer.share(content = any()) }
        composeRule.onNode(shareButton()).assertIsEnabled()
    }

    private fun editToUrl() {
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = URL_LABEL)
        composeRule.onQrFieldInput(URL_LABEL).performTextReplacement(EXAMPLE_URL)
        composeRule.waitForIdle()
    }

    private fun assertUrlInputKept() {
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.selectedFormat() shouldBe URL_LABEL
        composeRule.onQrFieldInput(URL_LABEL).editableText() shouldBe EXAMPLE_URL
    }

    private fun assertUndoReturnsToHello() {
        composeRule.onNode(hasClickAction() and hasContentDescription(UNDO)).performClick()
        composeRule.waitForIdle()

        composeRule.selectedFormat() shouldBe TEXT_FORMAT
        composeRule.qrTextInputText() shouldBe HELLO
    }

    private fun nextSendIntent(): Intent {
        val chooser = shadowOf(application).nextStartedActivity.shouldNotBeNull()
        chooser.action shouldBe Intent.ACTION_CHOOSER

        return chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java).shouldNotBeNull()
    }

    private fun SemanticsNodeInteractionsProvider.selectedFormat(): String = onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.DropdownList)).editableText()

    private fun shareButton(): SemanticsMatcher = hasClickAction() and hasContentDescription(SHARE)

    private fun setScreen(
        detail: QrDetail,
        sharerFactory: ContentSharerFactory = testContentSharerFactory(),
    ): FakeViewModel {
        val fake = FakeViewModel(detail = detail)
        composeRule.setContent { ScreenContent(viewModel = fake.viewModel, sharerFactory = sharerFactory) }
        composeRule.waitForIdle()

        return fake
    }

    // 다른 QR을 조회한 것처럼 식별자를 바꿔 입력을 저장된 내용으로 다시 채운다.
    private fun FakeViewModel.reload(detail: QrDetail) {
        composeRule.runOnIdle { uiState.value = QrDetailUiState.Content(id = Arb.kotlinUuid().next(), detail = detail) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun ScreenContent(
        viewModel: QrDetailViewModel,
        sharerFactory: ContentSharerFactory = remember { testContentSharerFactory() },
    ) {
        DiaryTheme {
            QrDetailScreen(
                navigateUp = {},
                navigateToScan = {},
                navigateToTagAdd = {},
                navigateToTagDetail = {},
                tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                permissionManager = rememberPermissionManager(),
                qrActionLauncherFactory = remember { AndroidQrActionLauncherFactory() },
                contentSharerFactory = sharerFactory,
                qrCodePngEncoder = remember { testQrCodePngEncoder() },
                resultEventBus = remember { ResultEventBus() },
                componentVisibleProvider = { QrDetailScaffoldComponentVisible() },
                qrViewModel = viewModel,
                tagViewModel = detailTagScreenTestViewModel(),
            )
        }
    }

    private class FakeViewModel(
        detail: QrDetail,
    ) {
        val uiState = MutableStateFlow<QrDetailUiState>(QrDetailUiState.Content(id = Arb.kotlinUuid().next(), detail = detail))
        val effect = Channel<QrDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel: QrDetailViewModel =
            mockk<QrDetailViewModel>(relaxed = true).apply {
                every { uiState } returns this@FakeViewModel.uiState
                every { effect } returns this@FakeViewModel.effect.receiveAsFlow()
            }
    }

    private companion object {
        const val COMPANY = "회사"
        const val COMPANY_WIFI = "회사 와이파이"
        const val EDITED_TITLE = "새 제목"
        const val HELLO = "hello"
        const val EXAMPLE_URL = "https://example.com"
        const val WIFI_VALUE = "WIFI:T:WPA;S:office;P:secret;;"
        const val PNG_MIME_TYPE = "image/png"

        const val TEXT_FORMAT = "텍스트"
        const val URL_LABEL = "URL"
        const val FORMAT_LABEL = "포맷"

        const val SHARE = "공유"
        const val OPEN_URL = "브라우저에서 열기"
        const val UNDO = "되돌리기"
        const val SCAN = "QR 스캔"
        const val DELETE = "QR 삭제"
        const val UPDATE = "QR 수정"
        const val SHARE_FAILED = "공유하지 못했습니다."
        const val URL_EMPTY = "URL을 입력해 주세요."
    }
}

private fun sharerFactoryOf(sharer: ContentSharer): ContentSharerFactory =
    object : ContentSharerFactory {
        @Composable
        override fun rememberContentSharer(): ContentSharer = sharer
    }
