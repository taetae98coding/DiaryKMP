package io.github.taetae98coding.diary.feature.web.ui.detail.page

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.LocalDiaryWebSession
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScaffold
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoTab
import io.github.taetae98coding.diary.feature.web.ui.detail.rememberWebDetailScaffoldState
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.testContentUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.testWebDetail
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class WebDetailPageExternalChangeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-007 URL 방식에서 다른 경로에서 URL과 요청 헤더가 바뀌면 웹 표시 수단이 새 URL을 연다`() {
        val initial = testContentUiState(detail = testWebDetail(url = INITIAL_URL))
        var uiState: WebDetailUiState by mutableStateOf(initial)
        composeRule.setContent {
            DiaryTheme {
                val state = rememberWebDetailScaffoldState(initialTab = WebDetailTab.PAGE, initialViewMode = WebDetailViewMode.URL)

                WebDetailScaffold(
                    onEvent = {},
                    state = state,
                    formState = rememberWebDetailFormState(initialDetail = initial.detail),
                    uiStateProvider = { uiState },
                    pageUiStateProvider = { WebDetailPageUiState.Loading },
                    onFormEvent = {},
                    onTagPickerEvent = {},
                ) {
                    WebDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
                }
            }
        }
        composeRule.waitForIdle()
        shadowOf(findWebView()).lastLoadedUrl shouldBe INITIAL_URL

        composeRule.runOnIdle {
            uiState = initial.copy(detail = initial.detail.copy(url = CHANGED_URL, headerList = initial.detail.headerList + WebHeader(name = "header-${fixtureMonkey.giveMeOne<String>()}", value = fixtureMonkey.giveMeOne<String>())))
        }
        composeRule.waitForIdle()

        shadowOf(findWebView()).lastLoadedUrl shouldBe CHANGED_URL
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-055 수정으로 URL이 바뀌어도 로그인 정보를 다시 가져오지 않고 새 주소를 연다`() {
        val initial = testContentUiState(detail = testWebDetail(url = INITIAL_URL))
        var uiState: WebDetailUiState by mutableStateOf(initial)
        composeRule.setContent {
            CompositionLocalProvider(LocalDiaryWebSession provides DiaryWebSession(importCount = IMPORTED_COUNT)) {
                DiaryTheme {
                    val state = rememberWebDetailScaffoldState(initialTab = WebDetailTab.PAGE, initialViewMode = WebDetailViewMode.URL)

                    WebDetailScaffold(
                        onEvent = {},
                        state = state,
                        formState = rememberWebDetailFormState(initialDetail = initial.detail),
                        uiStateProvider = { uiState },
                        pageUiStateProvider = { WebDetailPageUiState.Loading },
                        onFormEvent = {},
                        onTagPickerEvent = {},
                    ) {
                        WebDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
        composeRule.waitForIdle()
        shadowOf(findWebView()).lastLoadedUrl shouldBe INITIAL_URL

        composeRule.runOnIdle { uiState = initial.copy(detail = initial.detail.copy(url = CHANGED_URL)) }
        composeRule.waitForIdle()

        shadowOf(findWebView()).lastLoadedUrl shouldBe CHANGED_URL
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    private fun findWebView(): WebView =
        composeRule.activity.window.decorView
            .findWebView()
            .shouldNotBeNull()

    private fun View.findWebView(): WebView? =
        when (this) {
            is WebView -> this
            is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { index -> getChildAt(index).findWebView() }
            else -> null
        }

    private companion object {
        const val INITIAL_URL = "https://developer.android.com/"
        const val CHANGED_URL = "https://kotlinlang.org/"
        const val IMPORTED_COUNT = 1
    }
}
