package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.exception.QrTitleBlankException
import io.github.taetae98coding.diary.domain.qr.exception.QrValueEmptyException
import io.github.taetae98coding.diary.domain.qr.usecase.AddQrUseCase
import io.github.taetae98coding.diary.feature.qr.ui.add.form.QrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.add.form.format
import io.github.taetae98coding.diary.feature.qr.ui.add.form.rememberQrAddFormState
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class QrAddRawTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-QR-ADD-FEATURE-049 처음 들어오면 정보 탭과 텍스트 포맷이 선택되어 있고 QR 값과 되돌릴 기록이 없다`() {
        setQrAddScreen()

        composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
        composeRule.qrCodeValue() shouldBe ""
        onUndo().assertIsNotEnabled()
        composeRule.selectTab(QR_TAB_INDEX)
        formatName() shouldBe TEXT_FORMAT
    }

    @Test
    fun `TC-QR-ADD-FEATURE-051 포맷을 바꿔도 QR 값은 그대로이고 그 포맷의 값이면 입력이 채워진다`() {
        val raw = "WIFI:T:WEP;S:home;P:pw;H:true;;"
        setQrAddScreen()
        composeRule.onQrTextInput().performTextInput(raw)

        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe raw
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe "home"
        composeRule.onQrFieldInput(PASSWORD_LABEL).editableText() shouldBe "pw"
        composeRule.onNode(hasText("WEP") and hasClickAction()).assertIsSelected()
        composeRule.onNode(hasText(HIDDEN_NETWORK_LABEL) and hasClickAction()).assertIsOn()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-052 그 포맷의 값이 아니면 포맷의 입력이 빈 입력으로 보인다`() {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val state = setQrAddScaffold()
        composeRule.onQrTextInput().performTextInput(HELLO)

        composeRule.write { state().contentState.selectFormat(QrFormat.WIFI) }
        composeRule.waitForIdle()
        composeRule.qrCodeValue() shouldBe HELLO
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe ""
        composeRule.onQrFieldInput(PASSWORD_LABEL).editableText() shouldBe ""
        composeRule.onNode(hasText("WPA/WPA2/WPA3") and hasClickAction()).assertIsSelected()
        composeRule.onNode(hasText(HIDDEN_NETWORK_LABEL) and hasClickAction()).assertIsOff()

        composeRule.write { state().contentState.selectFormat(QrFormat.EVENT) }
        composeRule.waitForIdle()
        composeRule.qrCodeValue() shouldBe HELLO
        composeRule.runOnIdle { state().contentState.eventPeriodState.value shouldBe DiaryDateTimeInputValue.AllDay(dateRange = today..today) }
        composeRule.onQrFieldInput("Event title").editableText() shouldBe ""

        composeRule.write { state().contentState.selectFormat(QrFormat.CONTACT) }
        composeRule.waitForIdle()
        composeRule.qrCodeValue() shouldBe HELLO
        listOf("Name", "Phone number", "Email", "Company", "Address", "Website").forEach { label ->
            composeRule.onQrFieldInput(label).editableText() shouldBe ""
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-053 그 포맷의 값에서 입력을 고치면 고친 부분만 바뀌고 나머지는 그대로 남는다`() {
        val state = setQrAddScaffold()
        composeRule.onQrTextInput().performTextInput("WIFI:T:WPA;S:home;E:PEAP;P:pw;;")
        composeRule.write { state().contentState.selectFormat(QrFormat.WIFI) }

        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextReplacement("office")
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe "WIFI:T:WPA;S:office;E:PEAP;P:pw;;"
        composeRule.runOnIdle { state().contentState.canUndo shouldBe false }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-054 다른 포맷의 값에서 입력을 고치면 새로 쓰고 되돌리기를 선택할 수 있게 된다`() {
        setQrAddScreen()
        composeRule.onQrTextInput().performTextInput(HELLO)
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)

        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe "WIFI:T:WPA;S:home;;"
        onUndo().assertIsEnabled()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-055 빈 QR 값에서 포맷 입력을 고치면 새로 쓰되 되돌릴 기록을 남기지 않는다`() {
        setQrAddScreen()
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = PHONE_FORMAT)

        composeRule.onQrFieldInput(PHONE_NUMBER_LABEL).performTextInput("010")
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe "tel:010"
        onUndo().assertIsNotEnabled()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-056 되돌리기를 선택하면 기록한 QR 값과 포맷으로 차례로 돌아간다`() {
        val detail = qrTestFixtureMonkey.qrDetail()
        setQrAddScreen()
        composeRule.onTitleInput().performTextInput(detail.title)
        composeRule.onDescriptionInput().performTextInput(detail.description)
        composeRule.onQrTextInput().performTextInput(HELLO)
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = PHONE_FORMAT)
        composeRule.onQrFieldInput(PHONE_NUMBER_LABEL).performTextInput("010")
        composeRule.selectTab(INFO_TAB_INDEX)
        composeRule.waitForIdle()

        onUndo().performClick()
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        formatName() shouldBe WIFI_FORMAT
        composeRule.qrCodeValue() shouldBe "WIFI:T:WPA;S:home;;"
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe "home"

        onUndo().performClick()
        composeRule.waitForIdle()

        formatName() shouldBe TEXT_FORMAT
        composeRule.qrTextInputText() shouldBe HELLO
        composeRule.qrCodeValue() shouldBe HELLO
        onUndo().assertIsNotEnabled()
        composeRule.titleInputText() shouldBe detail.title
        composeRule.descriptionInputText() shouldBe detail.description
    }

    @Test
    fun `TC-QR-ADD-FEATURE-057 QrScan 화면에서 QR을 읽어 돌아오면 QR 값을 바꾸고 알아본 포맷을 보여 준다`() {
        val detail = qrTestFixtureMonkey.qrDetail()
        val resultEventBus = ResultEventBus()
        setQrAddScreen(resultEventBus = resultEventBus)
        composeRule.onTitleInput().performTextInput(detail.title)
        composeRule.onDescriptionInput().performTextInput(detail.description)
        val caseList =
            listOf(
                Triple("WIFI:T:WPA;S:home;P:pw;;", WIFI_FORMAT, NETWORK_NAME_LABEL to "home"),
                Triple("https://example.com", "URL", "URL" to "https://example.com"),
                Triple(HELLO, TEXT_FORMAT, TEXT_LABEL to HELLO),
            )

        caseList.forEach { (scanned, expectedFormat, field) ->
            composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = TEXT_FORMAT)
            composeRule.onQrTextInput().performTextReplacement("abc")
            composeRule.selectTab(INFO_TAB_INDEX)
            composeRule.waitForIdle()

            composeRule.runOnIdle { resultEventBus.sendResult(result = QrScannedResult(value = scanned)) }
            composeRule.waitForIdle()

            withClue("읽은 값=$scanned") {
                composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
                formatName() shouldBe expectedFormat
                composeRule.qrCodeValue() shouldBe scanned
                composeRule.onQrFieldInput(field.first).editableText() shouldBe field.second
                onUndo().assertIsEnabled()
                composeRule.titleInputText() shouldBe detail.title
                composeRule.descriptionInputText() shouldBe detail.description
            }
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-057 QR 값이 비어 있을 때 스캔하면 되돌릴 기록을 남기지 않는다`() {
        val resultEventBus = ResultEventBus()
        setQrAddScreen(resultEventBus = resultEventBus)

        composeRule.runOnIdle { resultEventBus.sendResult(result = QrScannedResult(value = HELLO)) }
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        formatName() shouldBe TEXT_FORMAT
        composeRule.qrCodeValue() shouldBe HELLO
        onUndo().assertIsNotEnabled()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-058 추가에 성공하면 정보 탭으로 돌아가 QR 값과 되돌릴 기록을 비우고 선택한 포맷은 유지한다`() {
        val detail = qrTestFixtureMonkey.qrDetail()
        setQrAddScreen(viewModel = effectViewModel(effect = QrAddEffect.AddSucceeded))
        composeRule.onTitleInput().performTextInput(detail.title)
        composeRule.onDescriptionInput().performTextInput(detail.description)
        composeRule.onQrTextInput().performTextInput(HELLO)
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.waitForIdle()
        onUndo().assertIsEnabled()

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
        composeRule.onTitleInput().assertIsFocused()
        composeRule.titleInputText() shouldBe ""
        composeRule.descriptionInputText() shouldBe ""
        composeRule.qrCodeValue() shouldBe ""
        onUndo().assertIsNotEnabled()
        composeRule.selectTab(QR_TAB_INDEX)
        formatName() shouldBe WIFI_FORMAT
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe ""
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-059 QR 값이 비어 있으면 QR 탭으로 바꾸고 선택한 포맷의 첫 입력을 알린다`() {
        val caseList =
            listOf(
                Triple("텍스트", "내용을 입력해 주세요.", "내용"),
                Triple("URL", "URL을 입력해 주세요.", "URL"),
                Triple("연락처", "이름을 입력해 주세요.", "이름"),
                Triple(WIFI_FORMAT, "네트워크 이름을 입력해 주세요.", "네트워크 이름"),
                Triple("위치", COORDINATE_MESSAGE, "위도"),
                Triple("이메일", "받는 사람을 입력해 주세요.", "받는 사람"),
                Triple("전화", "전화번호를 입력해 주세요.", "전화번호"),
                Triple("SMS", "전화번호를 입력해 주세요.", "전화번호"),
                Triple("일정", "일정 제목을 입력해 주세요.", "일정 제목"),
            )
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(QrValueEmptyException())
        setQrAddScreen(viewModel = qrAddViewModel(addQrUseCase = useCase))
        composeRule.onTitleInput().performTextInput("회사")

        caseList.forEach { (format, message, focusedLabel) ->
            composeRule.selectFormat(formatLabel = KOREAN_FORMAT_LABEL, formatName = format)
            composeRule.selectTab(INFO_TAB_INDEX)
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(KOREAN_ADD_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            withClue("포맷=$format") {
                composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
                composeRule.onNodeWithText(message).assertExists()
                composeRule.onQrFieldInput(focusedLabel).assertIsFocused()
            }
        }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-060 포맷의 입력 일부만 채워도 QR 값이 있으면 추가를 요청한다`() {
        val viewModel = screenTestViewModel()
        setQrAddScreen(viewModel = viewModel)
        composeRule.onTitleInput().performTextInput("회사")
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = "Contact")
        composeRule.onQrFieldInput(PHONE_NUMBER_LABEL).performTextInput("010")
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val value = listOf("BEGIN:VCARD", "VERSION:3.0", "N:", "FN:", "TEL:010", "END:VCARD").joinToString(separator = "\r\n")
        verify(exactly = 1) { viewModel.add(detail = QrDetail(title = "회사", description = "", value = value)) }
    }

    @Test
    fun `TC-QR-ADD-FEATURE-061 포맷 입력으로 바로 앞에 쓴 값은 그 포맷의 값으로 보아 기록을 남기지 않는다`() {
        setQrAddScreen()
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = "URL")

        "example.com".forEach { char ->
            composeRule.onQrFieldInput("URL").performTextInput(char.toString())
            composeRule.waitForIdle()
        }

        composeRule.qrCodeValue() shouldBe "example.com"
        onUndo().assertIsNotEnabled()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-026 제목이 공백이면 QR 값과 포맷이 유지된다`() {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(QrTitleBlankException())
        setQrAddScreen(viewModel = qrAddViewModel(addQrUseCase = useCase))
        composeRule.onTitleInput().performTextInput(" ")
        composeRule.onDescriptionInput().performTextInput("출입용")
        composeRule.onQrTextInput().performTextInput("abc")

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInputText() shouldBe " "
        composeRule.descriptionInputText() shouldBe "출입용"
        composeRule.qrTextInputText() shouldBe "abc"
        formatName() shouldBe TEXT_FORMAT
    }

    @Test
    fun `TC-QR-ADD-FEATURE-026 QR 값이 비어 있으면 제목, 설명과 포맷이 유지된다`() {
        val useCase = mockk<AddQrUseCase>()
        coEvery { useCase(parameter = any()) } returns Result.failure(QrValueEmptyException())
        setQrAddScreen(viewModel = qrAddViewModel(addQrUseCase = useCase))
        composeRule.onTitleInput().performTextInput("회사")
        composeRule.onDescriptionInput().performTextInput("출입용")
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)

        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.titleInputText() shouldBe "회사"
        composeRule.descriptionInputText() shouldBe "출입용"
        composeRule.selectTab(QR_TAB_INDEX)
        formatName() shouldBe WIFI_FORMAT
        composeRule.qrCodeValue() shouldBe ""
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-032 되돌리기 기록은 알아본 포맷과 함께 최근 20개까지만 남는다`() {
        setQrAddScreen()
        composeRule.onQrTextInput().performTextInput("WIFI:S:home;;")
        val rawList =
            List(OVERWRITE_COUNT) { index ->
                if (index % 2 == 0) {
                    composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = PHONE_FORMAT)
                    composeRule.onQrFieldInput(PHONE_NUMBER_LABEL).performTextReplacement("$index")
                    composeRule.waitForIdle()
                    "tel:$index" to PHONE_FORMAT
                } else {
                    composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = "URL")
                    composeRule.onQrFieldInput("URL").performTextReplacement("http://a$index")
                    composeRule.waitForIdle()
                    "http://a$index" to "URL"
                }
            }

        rawList.dropLast(1).reversed().forEach { (raw, format) ->
            onUndo().performClick()
            composeRule.waitForIdle()

            withClue("QR 값=$raw") {
                composeRule.qrCodeValue() shouldBe raw
                formatName() shouldBe format
            }
        }

        onUndo().assertIsNotEnabled()
        composeRule.qrCodeValue() shouldBe "tel:0"
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-033 화면이 재생성되어도 QR 값, 포맷, 입력, 탭과 되돌리기 기록을 유지한다`() {
        val viewModel = screenTestViewModel()

        assertRawRetained { viewModel }
    }

    @Test
    fun `TC-QR-ADD-DOMAIN-033 시스템이 앱을 되살리며 화면을 다시 보여 줘도 QR 값과 되돌리기 기록을 복원한다`() {
        assertRawRetained { screenTestViewModel() }
    }

    private fun assertRawRetained(viewModelFactory: () -> QrAddViewModel) {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            QrAddScreenContent(viewModel = remember { viewModelFactory() }, resultEventBus = remember { ResultEventBus() })
        }
        composeRule.onQrTextInput().performTextInput(HELLO)
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.onNode(hasText("None") and hasClickAction()).performClick()
        composeRule.onNode(hasText(HIDDEN_NETWORK_LABEL) and hasClickAction()).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        formatName() shouldBe WIFI_FORMAT
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe "home"
        composeRule.onNode(hasText("None") and hasClickAction()).assertIsSelected()
        composeRule.onNode(hasText(HIDDEN_NETWORK_LABEL) and hasClickAction()).assertIsOn()
        composeRule.qrCodeValue() shouldBe "WIFI:T:nopass;S:home;H:true;;"

        onUndo().performClick()
        composeRule.waitForIdle()

        formatName() shouldBe TEXT_FORMAT
        composeRule.qrCodeValue() shouldBe HELLO
    }

    private fun onUndo() = composeRule.onNodeWithContentDescription(UNDO_DESCRIPTION)

    private fun formatName(): String {
        composeRule.selectTab(QR_TAB_INDEX)
        return composeRule.onNode(hasText(FORMAT_LABEL) and hasClickAction()).editableText()
    }

    private fun setQrAddScaffold(): () -> QrAddFormState {
        var state: QrAddFormState? = null
        composeRule.setContent {
            val formState = rememberQrAddFormState()
            state = formState

            DiaryTheme {
                QrAddScaffold(onEvent = {}, state = formState)
            }
        }
        composeRule.waitForIdle()
        return { checkNotNull(state) }
    }

    private fun setQrAddScreen(
        viewModel: QrAddViewModel = screenTestViewModel(),
        resultEventBus: ResultEventBus = ResultEventBus(),
    ) {
        composeRule.setContent { QrAddScreenContent(viewModel = viewModel, resultEventBus = resultEventBus) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun QrAddScreenContent(
        viewModel: QrAddViewModel,
        resultEventBus: ResultEventBus,
    ) {
        DiaryTheme {
            QrAddScreen(
                navigateUp = {},
                navigateToScan = {},
                permissionManager = rememberPermissionManager(),
                resultEventBus = resultEventBus,
                viewModel = viewModel,
            )
        }
    }

    private companion object {
        const val OVERWRITE_COUNT = 21
        const val WIFI_FORMAT = "Wi-Fi"
        const val PHONE_FORMAT = "Phone"
        const val TEXT_FORMAT = "Text"
        const val HELLO = "hello"
        const val FORMAT_LABEL = "Format"
        const val KOREAN_FORMAT_LABEL = "포맷"
        const val TEXT_LABEL = "Text"
        const val NETWORK_NAME_LABEL = "Network name"
        const val PASSWORD_LABEL = "Password"
        const val HIDDEN_NETWORK_LABEL = "Hidden network"
        const val PHONE_NUMBER_LABEL = "Phone number"
        const val UNDO_DESCRIPTION = "Undo"
        const val ADD_DESCRIPTION = "Add QR code"
        const val KOREAN_ADD_DESCRIPTION = "QR 추가"
        const val COORDINATE_MESSAGE = "지도에서 위치를 선택하거나, 위도는 -90에서 90, 경도는 -180에서 180 사이의 숫자로 입력해 주세요."
    }
}
