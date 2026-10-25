package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko")
class SettingFileScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `한국어 환경에서 제목은 파일 설정이다`() {
        setSettingFileScaffold(uiState = SettingFileUiState.Loading)

        composeRule.onNodeWithText("파일 설정").assertExists()
    }

    @Test
    fun `TC-283ec755-2b37-46d3-b133-b22eb704af9a 저장된 설정을 확인하기 전이거나 읽지 못하면 위치와 동작을 표시하지 않는다`() {
        setSettingFileScaffold(uiState = SettingFileUiState.Loading)

        composeRule.onNodeWithText(DOWNLOAD_LOCATION_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_LOCATION_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(RESET_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-2e3176b1-a1f4-4217-9cb9-e7141780387f 고른 폴더가 없으면 기본 위치를 쓴다고 표시하고 되돌리기를 제공하지 않는다`() {
        setSettingFileScaffold(uiState = SettingFileUiState.Content(directory = null))

        composeRule.onNodeWithText(DEFAULT_LOCATION_LABEL).assert(hasClickLabel(CHOOSE_LABEL))
        composeRule.onNodeWithText("기기의 Download 폴더").assertExists()
        composeRule.onNodeWithText(RESET_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-0d5074c9-38b9-433a-aa49-7b74fe270e88 고른 폴더가 있으면 그 폴더와 되돌리기를 표시한다`() {
        val directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>().copy(name = "Documents")
        setSettingFileScaffold(uiState = SettingFileUiState.Content(directory = directory))

        composeRule.onNodeWithText("Documents").assert(hasClickLabel(CHOOSE_LABEL))
        composeRule.onNodeWithText(CHOSEN_DIRECTORY_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_LOCATION_LABEL, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(RESET_LABEL).assert(hasClickAction())
    }

    @Test
    fun `TC-b06cf4e1-aa8a-40f1-b29b-bd81ba582ae5 이름이 빈 폴더를 골라 두면 위치 자리에 고른 폴더를 표시한다`() {
        val directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>().copy(uri = fixtureMonkey.nonBlankString(), name = "")
        setSettingFileScaffold(uiState = SettingFileUiState.Content(directory = directory))

        composeRule.onAllNodesWithText(CHOSEN_DIRECTORY_LABEL, useUnmergedTree = true).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_LOCATION_LABEL, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(directory.uri, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `현재 위치와 따로 폴더 고르기 항목을 두지 않는다`() {
        setSettingFileScaffold(uiState = SettingFileUiState.Content(directory = null))

        composeRule.onNodeWithText(CHOOSE_LABEL, useUnmergedTree = true).assertDoesNotExist()
    }

    private fun setSettingFileScaffold(uiState: SettingFileUiState) {
        composeRule.setContent {
            DiaryTheme {
                SettingFileScaffold(
                    onEvent = {},
                    uiStateProvider = { uiState },
                )
            }
        }
    }
}

private fun hasClickLabel(label: String): SemanticsMatcher =
    SemanticsMatcher("click label is $label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

private const val DOWNLOAD_LOCATION_LABEL = "다운로드 위치"
private const val RESET_LABEL = "기본 위치로 되돌리기"
private const val DEFAULT_LOCATION_LABEL = "기본 위치"
private const val CHOOSE_LABEL = "폴더 고르기"
private const val CHOSEN_DIRECTORY_LABEL = "고른 폴더"
