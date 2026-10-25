package io.github.taetae98coding.diary.feature.qr.ui.detail

import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.ui.add.INFO_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.QR_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.actionNameList
import io.github.taetae98coding.diary.feature.qr.ui.add.editableText
import io.github.taetae98coding.diary.feature.qr.ui.add.isTabSelected
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrFieldInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrTextInput
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTestFixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTextInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.selectFormat
import io.github.taetae98coding.diary.feature.qr.ui.add.selectTab
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w480dp-h1200dp")
class QrDetailActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-d70ff3ff-85de-43ee-a754-a310072b65c7 실행할 내용이 있으면 그 동작의 QR 실행 버튼을 둔다`() {
        val caseList =
            listOf(
                EXAMPLE_URL to OPEN_URL,
                "tel:010-1234-5678" to CALL,
                "mailto:a@example.com" to SEND_EMAIL,
                "SMSTO:010:hi" to SEND_SMS,
                "geo:37.5665,126.978" to SHOW_LOCATION,
                lines("BEGIN:VCARD", "FN:홍길동", "END:VCARD") to ADD_CONTACT,
                lines("BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT") to ADD_EVENT,
                "WIFI:T:WPA;S:home;P:password;;" to JOIN_WIFI,
            )
        val fake = setScreen()

        caseList.forEach { (value, name) ->
            withClue(value) {
                fake.reload(detail = qrTestFixtureMonkey.qrDetail(value = value))

                listOf(INFO_TAB_INDEX, QR_TAB_INDEX).forEach { tabIndex ->
                    composeRule.selectTab(tabIndex)
                    composeRule.waitForIdle()

                    composeRule.actionButtonNameList() shouldBe listOf(name)
                }
            }
        }
    }

    @Test
    fun `TC-fe7abe31-5d79-4800-a153-d0017469d1c7 실행할 내용이 없으면 QR 실행 버튼을 두지 않는다`() {
        val fake = setScreen()

        listOf(HELLO, "WIFI:T:WEP;S:home;P:12345;;").forEach { value ->
            withClue(value) {
                fake.reload(detail = qrTestFixtureMonkey.qrDetail(value = value))

                composeRule.actionButtonNameList() shouldBe emptyList()
                composeRule.actionNameList() shouldContainAll listOf(UNDO, SCAN, DELETE)
            }
        }
    }

    @Test
    fun `TC-738c3946-6994-4045-a33f-07b836d9c799 QR 값을 고치면 반영하지 않아도 QR 실행 버튼이 바로 바뀐다`() {
        val fake = setScreen(detail = qrTestFixtureMonkey.qrDetail(value = HELLO))
        val caseList = listOf(EXAMPLE_URL to listOf(OPEN_URL), "tel:010" to listOf(CALL), "memo" to emptyList())

        caseList.forEach { (value, nameList) ->
            composeRule.onQrTextInput().performTextReplacement(value)
            composeRule.waitForIdle()

            withClue(value) {
                composeRule.actionButtonNameList() shouldBe nameList
                composeRule.selectedFormat() shouldBe TEXT_FORMAT
            }
        }
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-078e34e2-00e0-4297-a78d-2ed40be8f4d2 QR 실행을 선택하면 입력 중인 내용을 그 동작으로 앱 밖에 넘긴다`() {
        val caseList = ACTION_INTENT_CASE_LIST
        val fake = setScreen()

        caseList.forEach { (value, name, assertIntent) ->
            withClue(value) {
                fake.reload(detail = qrTestFixtureMonkey.qrDetail(value = value))

                composeRule.onNode(actionButton(name)).performClick()
                composeRule.waitForIdle()

                assertIntent(shadowApplication().nextStartedActivity.shouldNotBeNull())
                shadowApplication().nextStartedActivity.shouldBeNull()
                composeRule.onNodeWithText(ACTION_FAILED).assertDoesNotExist()
            }
        }
    }

    @Test
    fun `TC-eb92e9a9-4c62-4721-a118-a4fc49fc9432 앱 밖에 넘기지 못하면 화면에 머무르고 실행 실패를 알린다`() {
        shadowApplication().checkActivities(true)
        val detail = qrTestFixtureMonkey.qrDetail(value = HELLO)
        var navigateUpCount = 0
        setScreen(detail = detail, navigateUp = { navigateUpCount++ })
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = URL_LABEL)
        composeRule.onQrFieldInput(URL_LABEL).performTextReplacement(EXAMPLE_URL)
        composeRule.waitForIdle()

        composeRule.onNode(actionButton(OPEN_URL)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ACTION_FAILED).assertExists()
        composeRule.onNode(hasText(detail.title) and !hasSetTextAction(), useUnmergedTree = true).assertExists()
        navigateUpCount shouldBe 0
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.selectedFormat() shouldBe URL_LABEL
        composeRule.onQrFieldInput(URL_LABEL).editableText() shouldBe EXAMPLE_URL

        composeRule.onNode(hasClickAction() and hasContentDescription(UNDO)).performClick()
        composeRule.waitForIdle()

        composeRule.selectedFormat() shouldBe TEXT_FORMAT
        composeRule.qrTextInputText() shouldBe HELLO
    }

    @Test
    fun `TC-4d13cccf-c95e-416e-a95b-10ec459309ba 기기가 받지 않는 Wi-Fi 비밀번호면 실행 실패를 알린다`() {
        setScreen(detail = qrTestFixtureMonkey.qrDetail(value = "WIFI:T:WPA;S:home;P:비밀번호12345;;"))

        composeRule.onNode(actionButton(JOIN_WIFI)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ACTION_FAILED).assertExists()
        shadowApplication().nextStartedActivity.shouldBeNull()
    }

    @Test
    fun `TC-783a079d-91b3-4e7b-a710-9d178982ed2d QR 실행은 입력과 저장된 QR을 바꾸지 않는다`() {
        val fake = setScreen(detail = qrTestFixtureMonkey.qrDetail(value = HELLO))
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = URL_LABEL)
        composeRule.onQrFieldInput(URL_LABEL).performTextReplacement(EXAMPLE_URL)
        composeRule.waitForIdle()

        composeRule.onNode(actionButton(OPEN_URL)).performClick()
        composeRule.waitForIdle()

        shadowApplication().nextStartedActivity.shouldNotBeNull().dataString shouldBe EXAMPLE_URL
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.selectedFormat() shouldBe URL_LABEL
        composeRule.onQrFieldInput(URL_LABEL).editableText() shouldBe EXAMPLE_URL
        composeRule.actionNameList() shouldContainAll listOf(UPDATE)
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }

        composeRule.onNode(hasClickAction() and hasContentDescription(UNDO)).performClick()
        composeRule.waitForIdle()

        composeRule.selectedFormat() shouldBe TEXT_FORMAT
        composeRule.qrTextInputText() shouldBe HELLO
    }

    @Test
    fun `TC-0612ab5b-49a4-4139-a108-c973a1a2cfe7 QR 실행 실패 안내는 화면이 재생성되면 다시 표시하지 않는다`() {
        shadowApplication().checkActivities(true)
        val fake = FakeViewModel(detail = qrTestFixtureMonkey.qrDetail(value = EXAMPLE_URL))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.onNode(actionButton(OPEN_URL)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ACTION_FAILED).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ACTION_FAILED).assertDoesNotExist()
    }

    @Test
    fun `TC-bb36484b-5854-4b63-a357-340177a91c25 수정이나 삭제를 처리하는 동안에도 QR 실행을 선택할 수 있다`() {
        val fake = setScreen(detail = qrTestFixtureMonkey.qrDetail(value = EXAMPLE_URL))
        val progressList: List<(QrDetailUiState.Content) -> QrDetailUiState.Content> =
            listOf({ content -> content.copy(isUpdateInProgress = true) }, { content -> content.copy(isDeleteInProgress = true) })

        progressList.forEach { inProgress ->
            composeRule.runOnIdle { fake.uiState.value = inProgress(fake.uiState.value as QrDetailUiState.Content) }
            composeRule.waitForIdle()

            composeRule.onNode(actionButton(OPEN_URL)).performClick()
            composeRule.waitForIdle()

            shadowApplication().nextStartedActivity.shouldNotBeNull().dataString shouldBe EXAMPLE_URL
            composeRule.runOnIdle { fake.uiState.value = (fake.uiState.value as QrDetailUiState.Content).copy(isUpdateInProgress = false, isDeleteInProgress = false) }
        }
    }

    @Test
    fun `조회 중에는 QR 실행 버튼을 두지 않는다`() {
        val fake = FakeViewModel(detail = qrTestFixtureMonkey.qrDetail(value = EXAMPLE_URL))
        fake.uiState.value = QrDetailUiState.Loading
        composeRule.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.waitForIdle()

        composeRule.actionNameList() shouldNotContainAnyOf ACTION_NAME_LIST
    }

    private fun SemanticsNodeInteractionsProvider.actionButtonNameList(): List<String> = actionNameList().filter { name -> name in ACTION_NAME_LIST }

    private fun SemanticsNodeInteractionsProvider.selectedFormat(): String = onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.DropdownList)).editableText()

    private fun setScreen(
        detail: QrDetail = qrTestFixtureMonkey.qrDetail(),
        navigateUp: () -> Unit = {},
    ): FakeViewModel {
        val fake = FakeViewModel(detail = detail)
        composeRule.setContent { ScreenContent(viewModel = fake.viewModel, navigateUp = navigateUp) }
        composeRule.waitForIdle()

        return fake
    }

    // 다른 QR을 조회한 것처럼 식별자를 바꿔 입력을 저장된 내용으로 다시 채운다.
    private fun FakeViewModel.reload(detail: QrDetail) {
        composeRule.runOnIdle { uiState.value = QrDetailUiState.Content(id = qrTestFixtureMonkey.giveMeOne<Uuid>(), detail = detail) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun ScreenContent(
        viewModel: QrDetailViewModel,
        navigateUp: () -> Unit = {},
    ) {
        DiaryTheme {
            QrDetailScreen(
                navigateUp = navigateUp,
                navigateToScan = {},
                permissionManager = rememberPermissionManager(),
                resultEventBus = remember { ResultEventBus() },
                componentVisibleProvider = { QrDetailScaffoldComponentVisible() },
                viewModel = viewModel,
            )
        }
    }

    private class FakeViewModel(
        detail: QrDetail,
    ) {
        val uiState = MutableStateFlow<QrDetailUiState>(QrDetailUiState.Content(id = qrTestFixtureMonkey.giveMeOne<Uuid>(), detail = detail))
        val effect = Channel<QrDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel: QrDetailViewModel =
            mockk<QrDetailViewModel>(relaxed = true).apply {
                every { uiState } returns this@FakeViewModel.uiState
                every { effect } returns this@FakeViewModel.effect.receiveAsFlow()
            }
    }

    private companion object {
        const val HELLO = "hello"
        const val EXAMPLE_URL = "https://example.com"
        const val TEXT_FORMAT = "텍스트"
        const val URL_LABEL = "URL"
        const val FORMAT_LABEL = "포맷"

        const val UNDO = "되돌리기"
        const val SCAN = "QR 스캔"
        const val DELETE = "QR 삭제"
        const val UPDATE = "QR 수정"

        const val OPEN_URL = "브라우저에서 열기"
        const val CALL = "전화 걸기"
        const val SEND_EMAIL = "메일 쓰기"
        const val SEND_SMS = "문자 보내기"
        const val SHOW_LOCATION = "지도에서 보기"
        const val ADD_CONTACT = "연락처에 추가"
        const val ADD_EVENT = "캘린더에 추가"
        const val JOIN_WIFI = "Wi-Fi 연결"
        const val ACTION_FAILED = "QR을 실행하지 못했습니다."

        val ACTION_NAME_LIST: List<String> = listOf(OPEN_URL, CALL, SEND_EMAIL, SEND_SMS, SHOW_LOCATION, ADD_CONTACT, ADD_EVENT, JOIN_WIFI)

        val ACTION_INTENT_CASE_LIST: List<Triple<String, String, (Intent) -> Unit>> =
            listOf(
                Triple("https://example.com/a?b=1", OPEN_URL) { intent ->
                    intent.action shouldBe Intent.ACTION_VIEW
                    intent.dataString shouldBe "https://example.com/a?b=1"
                },
                Triple("tel:010-1234-5678", CALL) { intent ->
                    intent.action shouldBe Intent.ACTION_DIAL
                    intent.data?.scheme shouldBe "tel"
                    intent.data?.schemeSpecificPart shouldBe "010-1234-5678"
                },
                Triple("mailto:a@example.com?subject=Hi&body=Hello%20there", SEND_EMAIL) { intent ->
                    intent.action shouldBe Intent.ACTION_SENDTO
                    intent.data?.scheme shouldBe "mailto"
                    intent.getStringArrayExtra(Intent.EXTRA_EMAIL)?.toList() shouldBe listOf("a@example.com")
                    intent.getStringExtra(Intent.EXTRA_SUBJECT) shouldBe "Hi"
                    intent.getStringExtra(Intent.EXTRA_TEXT) shouldBe "Hello there"
                },
                Triple("SMSTO:010:hi", SEND_SMS) { intent ->
                    intent.action shouldBe Intent.ACTION_SENDTO
                    intent.data?.scheme shouldBe "smsto"
                    intent.data?.schemeSpecificPart shouldBe "010"
                    intent.getStringExtra("sms_body") shouldBe "hi"
                },
                Triple("geo:37.5665,126.978", SHOW_LOCATION) { intent ->
                    intent.action shouldBe Intent.ACTION_VIEW
                    intent.dataString shouldBe "geo:37.566500,126.978000?q=37.566500,126.978000"
                },
                Triple(
                    lines(
                        "BEGIN:VCARD",
                        "FN:홍길동",
                        "TEL:010",
                        "EMAIL:a@example.com",
                        "ORG:회사",
                        "ADR:;;세종대로;서울;;;",
                        "URL:https://example.com",
                        "END:VCARD",
                    ),
                    ADD_CONTACT,
                ) { intent ->
                    intent.action shouldBe ContactsContract.Intents.Insert.ACTION
                    intent.type shouldBe ContactsContract.RawContacts.CONTENT_TYPE
                    intent.getStringExtra(ContactsContract.Intents.Insert.NAME) shouldBe "홍길동"
                    intent.getStringExtra(ContactsContract.Intents.Insert.PHONE) shouldBe "010"
                    intent.getStringExtra(ContactsContract.Intents.Insert.EMAIL) shouldBe "a@example.com"
                    intent.getStringExtra(ContactsContract.Intents.Insert.COMPANY) shouldBe "회사"
                    intent.getStringExtra(ContactsContract.Intents.Insert.POSTAL) shouldBe "세종대로 서울"
                    val website =
                        intent
                            .getParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, android.content.ContentValues::class.java)
                            .shouldNotBeNull()
                            .single()
                    website.getAsString(ContactsContract.Data.MIMETYPE) shouldBe ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                    website.getAsString(ContactsContract.CommonDataKinds.Website.URL) shouldBe "https://example.com"
                },
                Triple(
                    lines(
                        "BEGIN:VEVENT",
                        "SUMMARY:회의",
                        "DTSTART;VALUE=DATE:20261014",
                        "DTEND;VALUE=DATE:20261016",
                        "LOCATION:회의실",
                        "DESCRIPTION:안건",
                        "END:VEVENT",
                    ),
                    ADD_EVENT,
                ) { intent ->
                    intent.action shouldBe Intent.ACTION_INSERT
                    intent.data shouldBe CalendarContract.Events.CONTENT_URI
                    intent.getStringExtra(CalendarContract.Events.TITLE) shouldBe "회의"
                    intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION) shouldBe "회의실"
                    intent.getStringExtra(CalendarContract.Events.DESCRIPTION) shouldBe "안건"
                    intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false) shouldBe true
                    intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0) shouldBe utcMidnightMillis(LocalDate(2026, 10, 14))
                    intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0) shouldBe utcMidnightMillis(LocalDate(2026, 10, 16))
                },
                Triple("WIFI:T:WPA;S:home;P:password;H:true;;", JOIN_WIFI) { intent ->
                    intent.action shouldBe Settings.ACTION_WIFI_ADD_NETWORKS
                    val suggestion = intent.wifiSuggestion()
                    suggestion.ssid?.unquoted() shouldBe "home"
                    suggestion.passphrase?.unquoted() shouldBe "password"
                    suggestion.isHiddenSsid shouldBe true
                },
                Triple("WIFI:S:cafe;;", JOIN_WIFI) { intent ->
                    intent.action shouldBe Settings.ACTION_WIFI_ADD_NETWORKS
                    val suggestion = intent.wifiSuggestion()
                    suggestion.ssid?.unquoted() shouldBe "cafe"
                    suggestion.passphrase.shouldBeNull()
                    suggestion.isHiddenSsid shouldBe false
                },
            )

        fun actionButton(name: String): SemanticsMatcher = hasClickAction() and hasContentDescription(name)

        fun shadowApplication() = shadowOf(RuntimeEnvironment.getApplication())

        fun lines(vararg lineList: String): String = lineList.joinToString(separator = "\r\n")

        fun utcMidnightMillis(date: LocalDate): Long = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

        fun Intent.wifiSuggestion(): WifiNetworkSuggestion =
            getParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, WifiNetworkSuggestion::class.java)
                .shouldNotBeNull()
                .single()

        fun String.unquoted(): String = removeSurrounding("\"")
    }
}
