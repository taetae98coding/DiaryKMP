package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.feature.qr.ui.resetAndroidUiDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class QrHomeListRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-QR-HOME-DOMAIN-012 화면이 재생성되어도 보던 목록 위치를 유지한다`() {
        val qrList = restorationQrList()
        val pagingDataFlow = MutableStateFlow(qrPagingDataOf(qrList))
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                QrHomeList(
                    onEvent = {},
                    qrPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                )
            }
        }
        composeRule.onNodeWithTag(QR_HOME_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(qrList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(qrList.first().detail.title).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(qrList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(qrList.first().detail.title).assertDoesNotExist()
    }

    @Test
    fun `TC-QR-HOME-DOMAIN-013 앱이 백그라운드에 갔다가 돌아와도 보던 목록 위치를 유지한다`() {
        val qrList = restorationQrList()
        val pagingDataFlow = MutableStateFlow(qrPagingDataOf(qrList))
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    QrHomeList(
                        onEvent = {},
                        qrPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    )
                }
            }
        }
        composeRule.onNodeWithTag(QR_HOME_LIST_TEST_TAG).performScrollToIndex(RESTORATION_SCROLL_INDEX)
        composeRule.onNodeWithText(qrList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(qrList[RESTORATION_SCROLL_INDEX].detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(qrList.first().detail.title).assertDoesNotExist()
    }

    private fun restorationQrList(): List<Qr> =
        List(RESTORATION_QR_COUNT) { index ->
            testQr(titlePrefix = "${index.toString().padStart(length = 2, padChar = '0')}-")
        }

    private companion object {
        private const val RESTORATION_QR_COUNT = 40
        private const val RESTORATION_SCROLL_INDEX = 30
    }
}
