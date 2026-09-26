package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.result.ResultEventBus
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddScreenAddTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-ADD-FEATURE-019 처음 들어오면 제목과 설명이 비어 있고 제목 입력에 초점이 있다`() {
        setQrAddScreen(viewModel = screenTestViewModel())

        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.titleInputText() shouldBe ""
        composeRule.descriptionInputText() shouldBe ""
        composeRule.onTitleInput().assertIsFocused()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-021 추가에 성공하면 다음 QR을 작성할 수 있는 상태로 초기화한다`() {
        setQrAddScreen(viewModel = effectViewModel(effect = QrAddEffect.AddSucceeded))
        val detail = qrTestFixtureMonkey.qrDetail()
        fillInput(title = detail.title, description = detail.description, value = detail.value)
        composeRule.onQrValueInput().performClick()
        composeRule.waitForIdle()
        composeRule.onQrValueInput().assertIsFocused()

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
        composeRule.titleInputText() shouldBe ""
        composeRule.descriptionInputText() shouldBe ""
        composeRule.qrValueInputText() shouldBe ""
        composeRule.qrCodeValue() shouldBe ""
        composeRule.onTitleInput().assertIsFocused()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-022 제목, 설명, 값이 모두 있으면 추가에 성공하고 성공 피드백을 제공한다`() {
        assertAddSucceededMessage(description = "description-${qrTestFixtureMonkey.giveMeOne<String>()}")
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-022 설명이 비어 있어도 추가에 성공하고 성공 피드백을 제공한다`() {
        assertAddSucceededMessage(description = "")
    }

    @Test
    fun `TC-QR-ADD-FEATURE-022 기본 환경에서 추가 성공 피드백을 제공한다`() {
        setQrAddScreen(viewModel = effectViewModel(effect = QrAddEffect.AddSucceeded))

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertExists()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-024 저장에 실패하면 안내 없이 작성 내용을 유지하고 진행 상태만 해제한다`() {
        val useCase = mockk<AddQrUseCase>()
        val detail = qrTestFixtureMonkey.qrDetail()
        coEvery { useCase(parameter = any()) } returns Result.failure(IllegalStateException(qrTestFixtureMonkey.giveMeOne<String>()))
        setQrAddScreen(viewModel = QrAddViewModel(addQrUseCase = useCase))
        fillInput(title = detail.title, description = detail.description, value = detail.value)

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_VALUE_EMPTY_MESSAGE).assertDoesNotExist()
        composeRule.titleInputText() shouldBe detail.title
        composeRule.descriptionInputText() shouldBe detail.description
        composeRule.qrValueInputText() shouldBe detail.value
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true).assertDoesNotExist()

        clickAdd()

        coVerify(exactly = 2) { useCase(parameter = detail) }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-025 제목이 비어 있고 값은 있으면 제목 입력이 필요함을 알리고 제목 입력으로 초점을 옮긴다`() {
        assertInvalidInput(title = "", value = qrTestFixtureMonkey.qrDetail().value, exception = QrTitleBlankException(), message = KOREAN_TITLE_BLANK_MESSAGE, focusedIndex = TITLE_INPUT_INDEX)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-025 제목이 공백 문자뿐이고 값은 있으면 제목 입력이 필요함을 알리고 제목 입력으로 초점을 옮긴다`() {
        assertInvalidInput(title = "   ", value = qrTestFixtureMonkey.qrDetail().value, exception = QrTitleBlankException(), message = KOREAN_TITLE_BLANK_MESSAGE, focusedIndex = TITLE_INPUT_INDEX)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-025 제목은 있고 값이 비어 있으면 QR 값 입력이 필요함을 알리고 QR 값 입력으로 초점을 옮긴다`() {
        assertInvalidInput(title = qrTestFixtureMonkey.qrDetail().title, value = "", exception = QrValueEmptyException(), message = KOREAN_VALUE_EMPTY_MESSAGE, focusedIndex = VALUE_INPUT_INDEX)
    }

    @Test
    fun `TC-QR-ADD-FEATURE-025 기본 환경에서 제목 입력이 필요함을 알린다`() {
        setQrAddScreen(viewModel = effectViewModel(effect = QrAddEffect.TitleBlank))

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_TITLE_BLANK_MESSAGE).assertExists()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-025 기본 환경에서 QR 값 입력이 필요함을 알린다`() {
        setQrAddScreen(viewModel = effectViewModel(effect = QrAddEffect.ValueEmpty))

        clickAdd()

        composeRule.onNodeWithText(DEFAULT_VALUE_EMPTY_MESSAGE).assertExists()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-026 제목이 공백이면 작성 내용은 유지된다`() {
        assertInputRetained(title = " ", description = qrTestFixtureMonkey.qrDetail().description, value = qrTestFixtureMonkey.qrDetail().value, exception = QrTitleBlankException())
    }

    @Test
    fun `TC-QR-ADD-FEATURE-026 값이 비어 있으면 작성 내용은 유지된다`() {
        assertInputRetained(title = qrTestFixtureMonkey.qrDetail().title, description = qrTestFixtureMonkey.qrDetail().description, value = "", exception = QrValueEmptyException())
    }

    @Test
    fun `추가를 실행하면 입력한 제목, 설명과 값을 그대로 전달한다`() {
        val viewModel = screenTestViewModel()
        setQrAddScreen(viewModel = viewModel)
        val detail =
            QrDetail(
                title = "  ${qrTestFixtureMonkey.qrDetail().title}  ",
                description = " ${qrTestFixtureMonkey.qrDetail().description}\n",
                value = " ${qrTestFixtureMonkey.qrDetail().value}\n${qrTestFixtureMonkey.qrDetail().value} ",
            )
        fillInput(title = detail.title, description = detail.description, value = detail.value)

        clickAdd()

        verify(exactly = 1) { viewModel.add(detail = detail) }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-DOMAIN-016 화면이 재생성되면 표시 중이던 추가 성공 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotShownAfterRestore(result = Result.success(qrTestFixtureMonkey.giveMeOne<Uuid>()), message = KOREAN_ADD_SUCCEEDED_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-DOMAIN-016 화면이 재생성되면 표시 중이던 제목 입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotShownAfterRestore(result = Result.failure(QrTitleBlankException()), message = KOREAN_TITLE_BLANK_MESSAGE)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-DOMAIN-016 화면이 재생성되면 표시 중이던 QR 값 입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotShownAfterRestore(result = Result.failure(QrValueEmptyException()), message = KOREAN_VALUE_EMPTY_MESSAGE)
    }

    private fun assertFeedbackNotShownAfterRestore(
        result: Result<Uuid>,
        message: String,
    ) {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns result
        val viewModel = QrAddViewModel(addQrUseCase = useCase)
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { QrAddScreenContent(viewModel = viewModel) }
        composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(message).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertDoesNotExist()
    }

    private fun assertAddSucceededMessage(description: String) {
        val useCase = mockk<AddQrUseCase>()
        val detail = qrTestFixtureMonkey.qrDetail(description = description)
        coEvery { useCase(parameter = any()) } returns Result.success(qrTestFixtureMonkey.giveMeOne<Uuid>())
        setQrAddScreen(viewModel = QrAddViewModel(addQrUseCase = useCase))
        fillInput(title = detail.title, description = detail.description, value = detail.value)

        composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(KOREAN_ADD_SUCCEEDED_MESSAGE).assertExists()
        coVerify(exactly = 1) { useCase(parameter = detail) }
    }

    private fun assertInvalidInput(
        title: String,
        value: String,
        exception: Throwable,
        message: String,
        focusedIndex: Int,
    ) {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(exception)
        setQrAddScreen(viewModel = QrAddViewModel(addQrUseCase = useCase))
        fillInput(title = title, description = "", value = value)
        composeRule.onDescriptionInput().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertExists()
        composeRule.onNodeWithText(KOREAN_ADD_SUCCEEDED_MESSAGE).assertDoesNotExist()
        if (focusedIndex == TITLE_INPUT_INDEX) {
            composeRule.onTitleInput().assertIsFocused()
        } else {
            composeRule.onQrValueInput().assertIsFocused()
        }
    }

    private fun assertInputRetained(
        title: String,
        description: String,
        value: String,
        exception: Throwable,
    ) {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(exception)
        setQrAddScreen(viewModel = QrAddViewModel(addQrUseCase = useCase))
        fillInput(title = title, description = description, value = value)

        clickAdd()

        composeRule.titleInputText() shouldBe title
        composeRule.descriptionInputText() shouldBe description
        composeRule.qrValueInputText() shouldBe value
        composeRule.qrCodeValue() shouldBe value
    }

    private fun fillInput(
        title: String,
        description: String,
        value: String,
    ) {
        if (title.isNotEmpty()) composeRule.onTitleInput().performTextInput(title)
        if (description.isNotEmpty()) composeRule.onDescriptionInput().performTextInput(description)
        if (value.isNotEmpty()) composeRule.onQrValueInput().performTextInput(value)
        composeRule.waitForIdle()
    }

    private fun clickAdd() {
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setQrAddScreen(viewModel: QrAddViewModel) {
        composeRule.setContent { QrAddScreenContent(viewModel = viewModel) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun QrAddScreenContent(viewModel: QrAddViewModel) {
        DiaryTheme {
            QrAddScreen(
                navigateUp = {},
                navigateToScan = {},
                permissionManager = rememberPermissionManager(),
                resultEventBus = remember { ResultEventBus() },
                viewModel = viewModel,
            )
        }
    }

    private companion object {
        const val DEFAULT_TITLE = "Add QR code"
        const val DEFAULT_ADD_DESCRIPTION = "Add QR code"
        const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        const val DEFAULT_ADD_SUCCEEDED_MESSAGE = "QR code added."
        const val KOREAN_ADD_SUCCEEDED_MESSAGE = "QR이 추가되었습니다."
        const val DEFAULT_TITLE_BLANK_MESSAGE = "Please enter a title."
        const val KOREAN_TITLE_BLANK_MESSAGE = "제목을 입력해 주세요."
        const val DEFAULT_VALUE_EMPTY_MESSAGE = "Please enter a QR value."
        const val KOREAN_VALUE_EMPTY_MESSAGE = "QR 값을 입력해 주세요."
    }
}
