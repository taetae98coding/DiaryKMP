package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
internal const val DEFAULT_OPEN_IN_NEW_DESCRIPTION = "Open externally"
internal const val DEFAULT_DELETE_DESCRIPTION = "Delete web"
internal const val DEFAULT_UPDATE_DESCRIPTION = "Update web"
internal const val DEFAULT_FORM_TAB_DESCRIPTION = "Edit web information"
internal const val DEFAULT_PAGE_TAB_DESCRIPTION = "View web page"
internal const val DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION = "Add header"
internal const val DEFAULT_PAGE_DESCRIPTION = "Web page"
internal const val DEFAULT_RETRY_BUTTON = "Retry"
internal const val DEFAULT_VIEW_MODE_DESCRIPTION = "Web page view mode"
internal const val DEFAULT_VIEW_MODE_TITLE = "Web page view mode"
internal const val DEFAULT_URL_VIEW_MODE_LABEL = "URL mode"
internal const val DEFAULT_RESPONSE_VIEW_MODE_LABEL = "Response body mode"
internal const val DEFAULT_URL_VIEW_MODE_DESCRIPTION = "Saved request headers are not applied."

private const val TITLE_INPUT_INDEX = 0
private const val DESCRIPTION_INPUT_INDEX = 1
private const val URL_INPUT_INDEX = 2
private const val HEADER_NAME_INPUT_INDEX = 3
private const val HEADER_VALUE_INPUT_INDEX = 4
internal const val INPUT_COUNT_WITHOUT_HEADER = 3
internal const val INPUT_COUNT_PER_HEADER = 2

// FixtureMonkey는 빈 문자열도 생성하므로 표시 여부 검증에 쓰는 값은 비어 있지 않게 접두사를 붙인다.
internal fun testWebDetail(
    title: String = "제목-${fixtureMonkey.giveMeOne<String>()}",
    description: String = "설명-${fixtureMonkey.giveMeOne<String>()}",
    url: String = "https://example.com/${fixtureMonkey.giveMeOne<Int>()}",
    headerList: List<WebHeader> = emptyList(),
): WebDetail =
    WebDetail(
        title = title,
        description = description,
        url = url,
        headerList = headerList,
    )

internal fun testContentUiState(detail: WebDetail = testWebDetail()): WebDetailUiState.Content = WebDetailUiState.Content(id = Uuid.random(), detail = detail)

internal fun testWebPage(): WebPage = fixtureMonkey.giveMeOne<WebPage>()

internal fun ComposeContentTestRule.selectFormTab() {
    onNodeWithContentDescription(DEFAULT_FORM_TAB_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.openViewModeSheet() {
    onNodeWithContentDescription(DEFAULT_VIEW_MODE_DESCRIPTION).performClick()
    waitForIdle()
}

// 표시 방식 줄과 선택 목록이 같은 라벨을 그리므로 목록 안의 줄을 마지막 노드로 집는다.
internal fun ComposeContentTestRule.selectViewMode(label: String) {
    openViewModeSheet()
    onAllNodesWithText(label).onLast().performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.selectPageTab() {
    onNodeWithContentDescription(DEFAULT_PAGE_TAB_DESCRIPTION).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.titleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun ComposeContentTestRule.descriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun ComposeContentTestRule.urlInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[URL_INPUT_INDEX]

internal fun ComposeContentTestRule.headerNameInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEADER_NAME_INPUT_INDEX + row * INPUT_COUNT_PER_HEADER]

internal fun ComposeContentTestRule.headerValueInput(row: Int = 0): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[HEADER_VALUE_INPUT_INDEX + row * INPUT_COUNT_PER_HEADER]

internal fun ComposeContentTestRule.inputCount(): Int = onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size

internal fun ComposeContentTestRule.addHeaderRow() {
    onNodeWithContentDescription(DEFAULT_HEADER_ADD_BUTTON_DESCRIPTION).performClick()
    waitForIdle()
}

/**
 * WebDetail 화면은 결과 이벤트 버스를 [LocalResultEventBus]에서 읽으므로, 화면을 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun WebDetailScreenTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}
