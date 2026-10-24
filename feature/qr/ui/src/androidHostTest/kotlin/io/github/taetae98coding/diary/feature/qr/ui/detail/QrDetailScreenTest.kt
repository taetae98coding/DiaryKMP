package io.github.taetae98coding.diary.feature.qr.ui.detail

import android.Manifest
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.ui.add.INFO_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.QR_TAB_INDEX
import io.github.taetae98coding.diary.feature.qr.ui.add.descriptionInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.editableText
import io.github.taetae98coding.diary.feature.qr.ui.add.isTabSelected
import io.github.taetae98coding.diary.feature.qr.ui.add.onDescriptionInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrFieldInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onQrTextInput
import io.github.taetae98coding.diary.feature.qr.ui.add.onTitleInput
import io.github.taetae98coding.diary.feature.qr.ui.add.qrCodeValue
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTestFixtureMonkey
import io.github.taetae98coding.diary.feature.qr.ui.add.qrTextInputText
import io.github.taetae98coding.diary.feature.qr.ui.add.selectFormat
import io.github.taetae98coding.diary.feature.qr.ui.add.selectTab
import io.github.taetae98coding.diary.feature.qr.ui.add.titleInputText
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeValueKey
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
class QrDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-001 조회에 성공하면 저장된 내용으로 화면을 채운다`() {
        val caseList =
            listOf(
                FillCase(value = HELLO, format = TEXT_FORMAT, fieldList = emptyList()),
                FillCase(value = EXAMPLE_URL, format = URL_LABEL, fieldList = listOf(URL_LABEL to EXAMPLE_URL)),
                FillCase(value = "WIFI:T:WPA;S:home;P:pw;;", format = WIFI_FORMAT, fieldList = listOf(NETWORK_NAME_LABEL to "home", "비밀번호" to "pw")),
                FillCase(value = "tel:010", format = "전화", fieldList = listOf("전화번호" to "010")),
                FillCase(value = "geo:37.5665,126.978", format = "위치", fieldList = listOf("위도" to "37.5665", "경도" to "126.978")),
            )
        val fake = setScreen()

        caseList.forEach { fillCase ->
            val detail = qrTestFixtureMonkey.qrDetail(value = fillCase.value)
            fake.reload(detail = detail)

            composeRule.onNode(screenTitle(detail.title), useUnmergedTree = true).assertExists()
            composeRule.isTabSelected(INFO_TAB_INDEX) shouldBe true
            composeRule.titleInputText() shouldBe detail.title
            composeRule.descriptionInputText() shouldBe detail.description
            composeRule.qrCodeValue() shouldBe detail.value
            composeRule.onNodeWithContentDescription(UNDO).assertIsNotEnabled()
            composeRule.selectTab(QR_TAB_INDEX)
            composeRule.waitForIdle()
            composeRule.selectedFormat() shouldBe fillCase.format
            if (fillCase.fieldList.isEmpty()) composeRule.qrTextInputText() shouldBe detail.value
            fillCase.fieldList.forEach { (label, text) -> composeRule.onQrFieldInput(label).editableText() shouldBe text }
            if (fillCase.format == WIFI_FORMAT) composeRule.onNode(hasText("WPA/WPA2/WPA3") and hasClickAction()).assertIsSelected()
        }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-002 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        setScreen()

        composeRule.onAllNodes(isFocused() and hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-003 조회 중에는 뒤로가기 외의 입력과 동작을 실행할 수 없다`() {
        val fake = FakeViewModel()
        fake.uiState.value = QrDetailUiState.Loading
        composeRule.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(LOADING).assertExists()
        composeRule.buttonNameList() shouldBe listOf(NAVIGATE_UP)
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-005 화면에는 정해진 탭, 입력과 동작만 둔다`() {
        setScreen(detail = qrTestFixtureMonkey.qrDetail(value = HELLO))
        composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(2)
        composeRule.buttonNameList() shouldContainExactlyInAnyOrder listOf(NAVIGATE_UP, UNDO, SCAN, DELETE, UPDATE, INFO_TAB, QR_TAB) + INFO_INPUT_CONTROL_LIST
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(FORMAT_LABEL, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-006 입력이 저장된 내용과 다를 때만 수정 동작을 제공한다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val storedDescription = qrTestFixtureMonkey.nonBlankString()
        val detail = QrDetail(title = storedTitle, description = storedDescription, value = HELLO)
        val caseList: List<Pair<() -> Unit, Boolean>> =
            listOf(
                {} to false,
                { composeRule.onTitleInput().performTextReplacement("${storedTitle}2") } to true,
                { composeRule.onDescriptionInput().performTextReplacement("$storedDescription ") } to true,
                { composeRule.onQrTextInput().performTextReplacement("hello!") } to true,
                {
                    composeRule.onTitleInput().performTextReplacement("${storedTitle}2")
                    composeRule.onTitleInput().performTextReplacement(storedTitle)
                } to false,
                { composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = URL_LABEL) } to false,
                { composeRule.onQrTextInput().performTextClearance() } to true,
            )
        val fake = setScreen(detail = detail)

        caseList.forEach { (action, isProvided) ->
            fake.reload(detail = detail)

            action()
            composeRule.waitForIdle()

            composeRule.onAllNodes(updateButton()).fetchSemanticsNodes().size shouldBe if (isProvided) 1 else 0
        }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-007 수정에 성공하면 화면과 입력을 유지하고 성공 피드백을 제공한다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val editedTitle = editedText()
        val fake = setScreen(detail = QrDetail(title = storedTitle, description = "", value = HELLO))
        every { fake.viewModel.update(detail = any()) } answers {
            fake.uiState.value = fake.content().copy(detail = firstArg())
            fake.effect.trySend(QrDetailEffect.UpdateSucceeded)
        }
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.waitForIdle()

        composeRule.onNode(updateButton()).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertExists()
        composeRule.onNode(screenTitle(editedTitle), useUnmergedTree = true).assertExists()
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe "home"
        composeRule.onNodeWithContentDescription(UNDO).performClick()
        composeRule.waitForIdle()
        composeRule.qrTextInputText() shouldBe HELLO
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-008 수정을 처리하는 동안 진행 상태를 표시하고 다른 동작은 그대로 실행할 수 있다`() {
        val fake = setScreen()
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = URL_LABEL)
        composeRule.onQrFieldInput(URL_LABEL).performTextInput(EXAMPLE_URL)
        composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(isUpdateInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onAllNodes(progressIndicator(), useUnmergedTree = true).assertCountEquals(1)
        composeRule.onNodeWithContentDescription(DELETE).performClick()
        composeRule.onNodeWithContentDescription(SCAN).assertIsEnabled()
        composeRule.onNodeWithContentDescription(UNDO).assertIsEnabled()
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.waitForIdle()
        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.onTitleInput().performTextInput("3")
        composeRule.waitForIdle()

        verify(exactly = 1) { fake.viewModel.delete() }
        composeRule.titleInputText().endsWith("23") shouldBe true
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-009 수정에 실패하면 안내 없이 입력을 유지하고 다시 시도할 수 있다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val editedTitle = editedText()
        setScreen(detail = QrDetail(title = storedTitle, description = "", value = HELLO)).let { fake ->
            composeRule.onTitleInput().performTextReplacement(editedTitle)
            composeRule.waitForIdle()

            composeRule.onNode(updateButton()).performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertDoesNotExist()
            composeRule.titleInputText() shouldBe editedTitle
            composeRule.onNode(screenTitle(storedTitle), useUnmergedTree = true).assertExists()
            composeRule.onNode(updateButton()).performClick()
            composeRule.waitForIdle()
            verify(exactly = 2) { fake.viewModel.update(detail = QrDetail(title = editedTitle, description = "", value = HELLO)) }
        }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-010 제목을 비운 채 수정하면 기존 제목이 유지되고 수정 동작이 계속 제공된다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val storedDescription = qrTestFixtureMonkey.nonBlankString()
        val editedDescription = editedText()
        val detail = QrDetail(title = storedTitle, description = storedDescription, value = HELLO)
        val fake = setScreen(detail = detail)
        every { fake.viewModel.update(detail = any()) } answers {
            fake.uiState.value = fake.content().copy(detail = firstArg<QrDetail>().copy(title = storedTitle))
            fake.effect.trySend(QrDetailEffect.UpdateSucceeded)
        }

        listOf("", "  ").forEach { title ->
            fake.reload(detail = detail)
            composeRule.onTitleInput().performTextReplacement(title)
            composeRule.onDescriptionInput().performTextReplacement(editedDescription)
            composeRule.waitForIdle()

            composeRule.onNode(updateButton()).performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertExists()
            composeRule.onNode(screenTitle(storedTitle), useUnmergedTree = true).assertExists()
            composeRule.titleInputText() shouldBe title
            composeRule.onNode(updateButton()).assertExists()
        }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-011 QR 값이 비어 있으면 QR 탭에서 선택한 포맷의 첫 입력을 알린다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val editedTitle = editedText()
        val fake = setScreen(detail = QrDetail(title = storedTitle, description = "", value = EXAMPLE_URL))
        every { fake.viewModel.update(detail = any()) } answers { fake.effect.trySend(QrDetailEffect.ValueEmpty) }
        composeRule.onQrFieldInput(URL_LABEL).performTextClearance()
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.waitForIdle()

        composeRule.onNode(updateButton()).performClick()
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.onNodeWithText(URL_EMPTY).assertExists()
        composeRule.onQrFieldInput(URL_LABEL).assertIsFocused()
        composeRule.onNode(screenTitle(storedTitle), useUnmergedTree = true).assertExists()
        composeRule.titleInputText() shouldBe editedTitle
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-013 카메라 권한을 쓸 수 있으면 QrScan 화면으로 이동한다`() {
        val editedTitle = editedText()
        var navigateToScanCount = 0
        setScreen(registry = respondingRegistry(isGranted = true), navigateToScan = { navigateToScanCount += 1 })
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 1
        composeRule.onNodeWithText(PERMISSION_DENIED).assertDoesNotExist()
        composeRule.titleInputText() shouldBe editedTitle
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-013 카메라 권한이 이미 허용되어 있으면 QrScan 화면으로 이동한다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        var navigateToScanCount = 0
        val editedTitle = editedText()
        setScreen(registry = respondingRegistry(isGranted = true), navigateToScan = { navigateToScanCount += 1 })
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 1
        composeRule.titleInputText() shouldBe editedTitle
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-013 카메라 권한이 허용되지 않으면 이동하지 않고 안내를 표시한다`() {
        val editedTitle = editedText()
        var navigateToScanCount = 0
        setScreen(registry = respondingRegistry(isGranted = false), navigateToScan = { navigateToScanCount += 1 })
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 0
        composeRule.onNodeWithText(PERMISSION_DENIED).assertExists()
        composeRule.titleInputText() shouldBe editedTitle
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-012 QrScan 화면에서 읽은 값을 받으면 QR 값을 바꾸고 수정 동작을 제공한다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val resultEventBus = ResultEventBus()
        val fake = setScreen(detail = QrDetail(title = storedTitle, description = "", value = HELLO), resultEventBus = resultEventBus)

        composeRule.runOnIdle { resultEventBus.sendResult(result = QrScannedResult(value = EXAMPLE_URL)) }
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.selectedFormat() shouldBe URL_LABEL
        composeRule.qrCodeValue() shouldBe EXAMPLE_URL
        composeRule.onNodeWithContentDescription(UNDO).assertIsEnabled()
        composeRule.onNode(updateButton()).assertExists()
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-014 되돌리기는 저장된 QR을 바꾸지 않고 입력만 되돌린다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val fake = setScreen(detail = QrDetail(title = storedTitle, description = "", value = HELLO))
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.waitForIdle()
        composeRule.onNode(updateButton()).assertExists()

        composeRule.onNodeWithContentDescription(UNDO).performClick()
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.selectedFormat() shouldBe TEXT_FORMAT
        composeRule.qrCodeValue() shouldBe HELLO
        composeRule.onAllNodes(updateButton()).assertCountEquals(0)
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-015 삭제에 성공하면 확인을 묻지 않고 QrHome 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val detail = qrTestFixtureMonkey.qrDetail()
        val fake = setScreen(detail = detail, navigateUp = { navigateUpCount += 1 })
        every { fake.viewModel.delete() } answers { fake.effect.trySend(QrDetailEffect.DeleteSucceeded) }

        listOf(INFO_TAB_INDEX, QR_TAB_INDEX).forEachIndexed { index, tab ->
            fake.reload(detail = detail)
            composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
            composeRule.selectTab(tab)
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DELETE).performClick()
            composeRule.waitForIdle()

            composeRule.onAllNodes(isDialog()).assertCountEquals(0)
            verify(exactly = index + 1) { fake.viewModel.delete() }
            navigateUpCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-016 삭제를 처리하는 동안 진행 상태를 표시하고 입력과 수정을 그대로 실행할 수 있다`() {
        val fake = setScreen()
        composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(isDeleteInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onAllNodes(progressIndicator(), useUnmergedTree = true).assertCountEquals(1)
        composeRule.onTitleInput().performTextInput("3")
        composeRule.onNode(updateButton()).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { fake.viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-018 저장된 제목이 다른 경로에서 바뀌면 화면 제목만 갱신한다`() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val editedTitle = editedText()
        val otherTitle = otherText()
        val fake = setScreen(detail = QrDetail(title = storedTitle, description = "", value = HELLO))
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.onQrTextInput().performTextReplacement("hi")
        composeRule.waitForIdle()

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(detail = QrDetail(title = otherTitle, description = "", value = "bye")) }
        composeRule.waitForIdle()

        composeRule.onNode(screenTitle(otherTitle), useUnmergedTree = true).assertExists()
        composeRule.qrTextInputText() shouldBe "hi"
        composeRule.qrCodeValue() shouldBe "hi"
        composeRule.titleInputText() shouldBe editedTitle
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-020 뒤로가면 수정하지 않은 채 돌아간다`() {
        var navigateUpCount = 0
        val detail = qrTestFixtureMonkey.qrDetail()
        val fake = setScreen(detail = detail, navigateUp = { navigateUpCount += 1 })

        listOf(INFO_TAB_INDEX, QR_TAB_INDEX).forEachIndexed { index, tab ->
            fake.reload(detail = detail)
            composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
            composeRule.selectTab(tab)
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(NAVIGATE_UP).performClick()
            composeRule.waitForIdle()

            navigateUpCount shouldBe index + 1
        }
        verify(exactly = 0) { fake.viewModel.update(detail = any()) }
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-022 수정 성공 피드백은 화면이 재생성되면 다시 표시하지 않는다`() {
        val fake = FakeViewModel()
        every { fake.viewModel.update(detail = any()) } answers { fake.effect.trySend(QrDetailEffect.UpdateSucceeded) }
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.onTitleInput().performTextInput(APPENDED_TEXT)
        composeRule.onNode(updateButton()).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-DETAIL-DOMAIN-007 화면이 재생성되어도 입력, 포맷, 탭과 되돌리기 기록을 유지한다`() {
        assertInputRetained()
    }

    @Test
    fun `TC-QR-DETAIL-DOMAIN-007 시스템이 앱을 되살리며 화면을 다시 보여 줘도 입력, 포맷, 탭과 되돌리기 기록을 복원한다`() {
        assertInputRetained()
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-018 저장된 일정 QR이 다른 경로에서 바뀌어도 입력 중이던 일정 기간을 덮어쓰지 않는다`() {
        val stored =
            listOf(
                "BEGIN:VEVENT",
                "SUMMARY:a",
                "DTSTART;VALUE=DATE:20260101",
                "DTEND;VALUE=DATE:20260102",
                "END:VEVENT",
            ).joinToString(separator = "\n")
        val fake = setScreen(detail = qrTestFixtureMonkey.qrDetail(value = stored))
        composeRule.selectTab(QR_TAB_INDEX)
        composeRule.onQrFieldInput(EVENT_TITLE_LABEL).performTextReplacement("b")
        composeRule.waitForIdle()
        val edited = composeRule.qrCodeValue()

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(detail = fake.content().detail.copy(value = stored.replace("20260101", "20270101"))) }
        composeRule.waitForIdle()

        composeRule.qrCodeValue() shouldBe edited
        composeRule.onQrFieldInput(EVENT_TITLE_LABEL).editableText() shouldBe "b"
    }

    @Test
    @Config(qualifiers = SCROLLABLE_QUALIFIERS)
    fun `TC-QR-DETAIL-FEATURE-023 수정을 처리하는 동안 보고 있던 화면 위치를 유지한다`() {
        val fake = setScreen()
        val scrolledTop = scrollToBottomInput()

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(isUpdateInProgress = true) }
        composeRule.waitForIdle()
        qrImageTop() shouldBe scrolledTop

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(isUpdateInProgress = false) }
        composeRule.waitForIdle()
        qrImageTop() shouldBe scrolledTop
    }

    @Test
    @Config(qualifiers = SCROLLABLE_QUALIFIERS)
    fun `TC-QR-DETAIL-FEATURE-023 삭제를 처리하는 동안과 삭제가 실패해 화면에 머무를 때 보고 있던 화면 위치를 유지한다`() {
        val fake = setScreen()
        val scrolledTop = scrollToBottomInput()

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(isDeleteInProgress = true) }
        composeRule.waitForIdle()
        qrImageTop() shouldBe scrolledTop

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(isDeleteInProgress = false) }
        composeRule.waitForIdle()
        qrImageTop() shouldBe scrolledTop
    }

    @Test
    @Config(qualifiers = SCROLLABLE_QUALIFIERS)
    fun `TC-QR-DETAIL-FEATURE-024 저장된 내용이 갱신되어도 보고 있던 화면 위치를 유지한다`() {
        val fake = setScreen()
        val scrolledTop = scrollToBottomInput()

        composeRule.runOnIdle { fake.uiState.value = fake.content().copy(detail = fake.content().detail.copy(title = otherText())) }
        composeRule.waitForIdle()

        qrImageTop() shouldBe scrolledTop
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-025 화면이 재생성되면 표시 중이던 권한 안내를 다시 표시하지 않는다`() {
        val fake = FakeViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel, registry = remember { respondingRegistry(isGranted = false) }) }
        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PERMISSION_DENIED).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(PERMISSION_DENIED).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-026 권한 요청에 응답하기 전에 화면이 재생성되면 허용 응답을 반영하지 않는다`() {
        assertResponseIgnoredAfterRestore(isGranted = true)
    }

    @Test
    fun `TC-QR-DETAIL-FEATURE-026 권한 요청에 응답하기 전에 화면이 재생성되면 거부 응답을 반영하지 않는다`() {
        assertResponseIgnoredAfterRestore(isGranted = false)
    }

    @Test
    fun `TC-CAMERA-PERMISSION-FEATURE-001 TC-CAMERA-PERMISSION-DOMAIN-001 QrDetail에서 권한이 없으면 QR 스캔 시작을 선택할 때마다 카메라 권한 요청을 시작한다`() {
        val input = slot<Any>()
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), capture(input), any()) } answers {
            registry.dispatchResult(firstArg<Int>(), mapOf(Manifest.permission.CAMERA to false))
        }
        setScreen(registry = registry)

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()
        verify(exactly = 1) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
        (input.captured as Array<*>).toList() shouldContainExactly listOf(Manifest.permission.CAMERA)

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()
        verify(exactly = 2) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    @Test
    fun `TC-CAMERA-PERMISSION-FEATURE-002 QrDetail에서 카메라 권한이 이미 허용되어 있으면 요청이 시작되지 않는다`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
        val registry = respondingRegistry(isGranted = true)
        setScreen(registry = registry)

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()

        verify(exactly = 0) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    @Test
    fun `TC-CAMERA-PERMISSION-DOMAIN-002 QrDetail 화면이 표시되는 것만으로는 요청하지 않는다`() {
        val registry = respondingRegistry(isGranted = false)
        setScreen(registry = registry)

        verify(exactly = 0) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    private fun assertInputRetained() {
        val storedTitle = qrTestFixtureMonkey.nonBlankString()
        val editedTitle = editedText()
        val fake = FakeViewModel(detail = QrDetail(title = storedTitle, description = "", value = HELLO))
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel) }
        composeRule.onTitleInput().performTextReplacement(editedTitle)
        composeRule.selectFormat(formatLabel = FORMAT_LABEL, formatName = WIFI_FORMAT)
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).performTextInput("home")
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.isTabSelected(QR_TAB_INDEX) shouldBe true
        composeRule.onQrFieldInput(NETWORK_NAME_LABEL).editableText() shouldBe "home"
        composeRule.onNode(updateButton()).assertExists()
        composeRule.titleInputText() shouldBe editedTitle
        composeRule.onNodeWithContentDescription(UNDO).performClick()
        composeRule.waitForIdle()
        composeRule.qrTextInputText() shouldBe HELLO
    }

    private fun SemanticsNodeInteractionsProvider.buttonNameList(): List<String> =
        onAllNodes(hasClickAction() and !hasSetTextAction())
            .fetchSemanticsNodes()
            .map { node ->
                node.config
                    .getOrNull(SemanticsProperties.ContentDescription)
                    ?.joinToString()
                    .orEmpty()
            }

    private fun assertResponseIgnoredAfterRestore(isGranted: Boolean) {
        val requestCode = slot<Int>()
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(capture(requestCode), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } just runs
        var navigateToScanCount = 0
        val fake = FakeViewModel()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { ScreenContent(viewModel = fake.viewModel, registry = registry, navigateToScan = { navigateToScanCount += 1 }) }
        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            if (isGranted) shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
            registry.dispatchResult(requestCode.captured, mapOf(Manifest.permission.CAMERA to isGranted))
        }
        composeRule.waitForIdle()

        navigateToScanCount shouldBe 0
        composeRule.onNodeWithText(PERMISSION_DENIED).assertDoesNotExist()

        composeRule.onNodeWithContentDescription(SCAN).performClick()
        composeRule.waitForIdle()
        if (isGranted) navigateToScanCount shouldBe 1 else verify(exactly = 2) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    private fun qrImageTop(): Float =
        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(QrCodeValueKey), useUnmergedTree = true)
            .fetchSemanticsNode()
            .positionInRoot.y

    // 본문을 끝까지 스크롤하고 실제로 화면 위치가 옮겨졌는지 확인한 뒤 옮겨진 위치를 돌려준다.
    private fun scrollToBottomInput(): Float {
        val top = qrImageTop()
        composeRule.onDescriptionInput().performScrollTo()
        composeRule.waitForIdle()

        return qrImageTop().also { scrolledTop -> scrolledTop shouldBeLessThan top }
    }

    private fun SemanticsNodeInteractionsProvider.selectedFormat(): String = onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.DropdownList)).editableText()

    private fun setScreen(
        detail: QrDetail = qrTestFixtureMonkey.qrDetail(),
        registry: ActivityResultRegistry = respondingRegistry(isGranted = false),
        navigateUp: () -> Unit = {},
        navigateToScan: () -> Unit = {},
        resultEventBus: ResultEventBus = ResultEventBus(),
    ): FakeViewModel {
        val fake = FakeViewModel(detail = detail)
        composeRule.setContent {
            ScreenContent(
                viewModel = fake.viewModel,
                registry = registry,
                navigateUp = navigateUp,
                navigateToScan = navigateToScan,
                resultEventBus = resultEventBus,
            )
        }
        composeRule.waitForIdle()

        return fake
    }

    // 다른 QR을 조회한 것처럼 식별자를 바꿔 입력을 저장된 내용으로 다시 채운다.
    private fun FakeViewModel.reload(detail: QrDetail) {
        composeRule.runOnIdle { uiState.value = QrDetailUiState.Content(id = Uuid.random(), detail = detail) }
        composeRule.waitForIdle()
    }

    @Composable
    private fun ScreenContent(
        viewModel: QrDetailViewModel,
        registry: ActivityResultRegistry = remember { respondingRegistry(isGranted = false) },
        navigateUp: () -> Unit = {},
        navigateToScan: () -> Unit = {},
        resultEventBus: ResultEventBus = remember { ResultEventBus() },
    ) {
        val registryOwner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

        DiaryTheme {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                QrDetailScreen(
                    navigateUp = navigateUp,
                    navigateToScan = navigateToScan,
                    permissionManager = rememberPermissionManager(),
                    resultEventBus = resultEventBus,
                    componentVisibleProvider = { QrDetailScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun respondingRegistry(isGranted: Boolean): ActivityResultRegistry {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } answers {
            if (isGranted) shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA)
            registry.dispatchResult(firstArg<Int>(), mapOf(Manifest.permission.CAMERA to isGranted))
        }

        return registry
    }

    private class FakeViewModel(
        detail: QrDetail = qrTestFixtureMonkey.qrDetail(),
    ) {
        val uiState = MutableStateFlow<QrDetailUiState>(QrDetailUiState.Content(id = Uuid.random(), detail = detail))
        val effect = Channel<QrDetailEffect>(capacity = Channel.BUFFERED)
        val viewModel: QrDetailViewModel =
            mockk<QrDetailViewModel>(relaxed = true).apply {
                every { uiState } returns this@FakeViewModel.uiState
                every { effect } returns this@FakeViewModel.effect.receiveAsFlow()
            }

        fun content(): QrDetailUiState.Content = uiState.value as QrDetailUiState.Content
    }

    private data class FillCase(
        val value: String,
        val format: String,
        val fieldList: List<Pair<String, String>>,
    )

    private companion object {
        const val HELLO = "hello"
        const val EXAMPLE_URL = "https://example.com"
        const val NETWORK_NAME_LABEL = "네트워크 이름"
        const val WIFI_FORMAT = "Wi-Fi"
        const val URL_LABEL = "URL"
        const val TEXT_FORMAT = "텍스트"
        const val EVENT_TITLE_LABEL = "일정 제목"
        const val APPENDED_TEXT = "2"

        const val NAVIGATE_UP = "뒤로가기"
        const val UNDO = "되돌리기"
        const val SCAN = "QR 스캔"
        const val DELETE = "QR 삭제"
        const val UPDATE = "QR 수정"
        const val INFO_TAB = "정보"
        const val QR_TAB = "QR"
        const val LOADING = "QR 불러오는 중"
        const val FORMAT_LABEL = "포맷"
        const val UPDATE_SUCCEEDED = "QR이 수정되었습니다."
        const val URL_EMPTY = "URL을 입력해 주세요."

        // 제목 입력과 설명 입력이 각자 갖는 지우기 버튼과 설명 입력의 입력·미리보기 전환이다.
        val INFO_INPUT_CONTROL_LIST: List<String> = listOf("지우기", "지우기", "입력", "미리보기")
        const val SCROLLABLE_QUALIFIERS = "ko-w480dp-h480dp"
        const val PERMISSION_DENIED = "카메라 권한을 허용해야 QR을 스캔할 수 있습니다."

        fun editedText(): String = "edited-" + qrTestFixtureMonkey.nonBlankString()

        fun otherText(): String = "other-" + qrTestFixtureMonkey.nonBlankString()

        fun updateButton(): SemanticsMatcher = hasClickAction() and hasContentDescription(UPDATE)

        fun screenTitle(title: String): SemanticsMatcher = hasText(title) and !hasSetTextAction()

        fun progressIndicator(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
    }
}
