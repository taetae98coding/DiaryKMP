package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.domain.qr.content.QrContent
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import io.github.taetae98coding.diary.feature.qr.ui.code.fitsInQrCode
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
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
class QrAddFormatTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-031 한국어 환경에서 포맷 선택은 아홉 개 포맷을 정해진 순서로 보여 준다`() {
        assertFormatMenu(formatLabel = KOREAN_FORMAT_LABEL, currentFormatName = "텍스트", expected = KOREAN_FORMAT_NAME_LIST)
    }

    @Test
    fun `TC-QR-ADD-FEATURE-031 기본 환경에서 포맷 선택은 아홉 개 포맷을 정해진 순서로 보여 준다`() {
        assertFormatMenu(formatLabel = DEFAULT_FORMAT_LABEL, currentFormatName = "Text", expected = DEFAULT_FORMAT_NAME_LIST)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-032 포맷마다 정해진 입력을 보여 준다`() {
        val state = setQrAddScaffold()
        composeRule.selectTab(QR_TAB_INDEX)
        val expectedMap =
            mapOf(
                QrFormat.TEXT to listOf("내용"),
                QrFormat.URL to listOf("URL"),
                QrFormat.CONTACT to listOf("이름", KOREAN_PHONE_NUMBER_LABEL, "이메일", "회사", "주소", "웹사이트"),
                QrFormat.WIFI to listOf(KOREAN_NETWORK_NAME_LABEL, KOREAN_PASSWORD_LABEL),
                QrFormat.LOCATION to listOf("위도", "경도"),
                QrFormat.EMAIL to listOf("받는 사람", "메일 제목", "본문"),
                QrFormat.PHONE to listOf(KOREAN_PHONE_NUMBER_LABEL),
                QrFormat.SMS to listOf(KOREAN_PHONE_NUMBER_LABEL, "메시지"),
                QrFormat.EVENT to listOf("일정 제목", "장소", "일정 설명"),
            )

        expectedMap.forEach { (format, labelList) ->
            composeRule.write { state().contentState.selectFormat(format) }
            composeRule.waitForIdle()

            withClue("포맷=$format") {
                composeRule.qrFieldLabelList() shouldContainExactly labelList
            }
        }

        composeRule.write { state().contentState.selectFormat(QrFormat.WIFI) }
        composeRule.waitForIdle()
        listOf("WPA/WPA2/WPA3", "WEP", "없음", "숨겨진 네트워크").forEach { text ->
            composeRule.onNode(hasText(text) and hasClickAction()).assertExists()
        }
        composeRule.write { state().contentState.selectFormat(QrFormat.EVENT) }
        composeRule.waitForIdle()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ToggleableState) and hasText("날짜·시간")).assertExists()
    }

    @Test
    fun `TC-QR-ADD-FEATURE-033 탭을 오가도 두 탭의 입력과 QR 그림이 그대로 남는다`() {
        val detail = qrTestFixtureMonkey.qrDetail()
        val url = "https://${qrTestFixtureMonkey.qrDetail().value.filter(Char::isLetterOrDigit)}a.com"
        val state = setQrAddScaffold()
        composeRule.onTitleInput().performTextInput(detail.title)
        composeRule.onDescriptionInput().performTextInput(detail.description)
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.write { state().contentState.selectFormat(QrFormat.URL) }
        composeRule.onQrFieldInput(DEFAULT_URL_LABEL).performTextInput(url)
        composeRule.waitForIdle()

        repeat(2) {
            composeRule.selectTab(INFO_TAB_INDEX)
            composeRule.waitForIdle()
            composeRule.qrCodeValue() shouldBe url
            composeRule.titleInputText() shouldBe detail.title
            composeRule.descriptionInputText() shouldBe detail.description

            composeRule.selectTab(QR_TAB_INDEX)
            composeRule.waitForIdle()
            composeRule.qrCodeValue() shouldBe url
            composeRule.onQrFieldInput(DEFAULT_URL_LABEL).editableText() shouldBe url
            composeRule.runOnIdle { state().contentState.format shouldBe QrFormat.URL }
        }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-QR-ADD-FEATURE-036 보안 방식이 없음이면 비밀번호 입력을 숨기고 되돌리면 입력했던 비밀번호가 다시 보인다`() {
        val state = setQrAddScaffold()
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.write { state().contentState.selectFormat(QrFormat.WIFI) }
        composeRule.onQrFieldInput(KOREAN_NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.onQrFieldInput(KOREAN_PASSWORD_LABEL).performTextInput("pw")

        composeRule.onNode(hasText("없음") and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.qrFieldLabelList() shouldContainExactly listOf(KOREAN_NETWORK_NAME_LABEL)
        composeRule.qrCodeValue() shouldBe "WIFI:T:nopass;S:home;;"

        composeRule.onNode(hasText("WPA/WPA2/WPA3") and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onQrFieldInput(KOREAN_PASSWORD_LABEL).editableText() shouldBe "pw"
        composeRule.qrCodeValue() shouldBe "WIFI:T:WPA;S:home;P:pw;;"
    }

    @Test
    fun `TC-QR-ADD-FEATURE-037 포맷 입력이 QR 하나에 담을 수 없게 되면 반영하지 않는다`() {
        val longestUrl = "a".repeat(MAX_BYTE_LENGTH)
        val state = setQrAddScaffold()
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.write { state().contentState.selectFormat(QrFormat.URL) }
        composeRule.onQrFieldInput(DEFAULT_URL_LABEL).performTextInput(longestUrl)
        composeRule.waitForIdle()

        composeRule.onQrFieldInput(DEFAULT_URL_LABEL).performTextInput("a")
        composeRule.waitForIdle()

        composeRule.onQrFieldInput(DEFAULT_URL_LABEL).editableText() shouldBe longestUrl
        composeRule.qrCodeValue() shouldBe longestUrl
    }

    private fun assertFormatMenu(
        formatLabel: String,
        currentFormatName: String,
        expected: List<String>,
    ) {
        setQrAddScaffold()
        composeRule.selectTab(QR_TAB_INDEX)

        composeRule.onNode(hasText(formatLabel) and hasClickAction()).performClick()
        composeRule.waitForIdle()

        val menuItemList =
            composeRule
                .onAllNodes(hasClickAction() and hasAnyAncestor(isPopup()))
                .fetchSemanticsNodes()
                .map { node -> node.config[SemanticsProperties.Text].joinToString { text -> text.text } }
        menuItemList shouldContainExactly expected
        composeRule.onNode(hasText(formatLabel) and hasClickAction()).editableText() shouldBe currentFormatName
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

    private companion object {
        const val MAX_BYTE_LENGTH = 2953
        const val KOREAN_PHONE_NUMBER_LABEL = "전화번호"
        const val KOREAN_NETWORK_NAME_LABEL = "네트워크 이름"
        const val KOREAN_PASSWORD_LABEL = "비밀번호"
        const val KOREAN_FORMAT_LABEL = "포맷"
        const val DEFAULT_FORMAT_LABEL = "Format"
        const val DEFAULT_URL_LABEL = "URL"
        const val DEFAULT_PHONE_NUMBER_LABEL = "Phone number"
        const val DEFAULT_ADDRESS_LABEL = "Address"
        val KOREAN_FORMAT_NAME_LIST = listOf("텍스트", "URL", "연락처", "Wi-Fi", "위치", "이메일", "전화", "SMS", "일정")
        val DEFAULT_FORMAT_NAME_LIST = listOf("Text", "URL", "Contact", "Wi-Fi", "Location", "Email", "Phone", "SMS", "Event")
    }
}
