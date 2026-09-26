package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebDetailScaffoldStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `웹 페이지 탭으로 시작하고 선택할 때마다 그 탭으로 바뀐다`() {
        val state = WebDetailScaffoldState(initialTab = WebDetailTab.PAGE, initialViewMode = WebDetailViewMode.URL)

        state.tab shouldBe WebDetailTab.PAGE

        state.select(tab = WebDetailTab.FORM)
        state.tab shouldBe WebDetailTab.FORM

        state.select(tab = WebDetailTab.PAGE)
        state.tab shouldBe WebDetailTab.PAGE
    }

    @Test
    fun `넓은 창의 시작 쪽 탭은 메모 탭을 골랐을 때만 메모이고 그 밖에는 수정 폼이다`() {
        val state = WebDetailScaffoldState(initialTab = WebDetailTab.PAGE, initialViewMode = WebDetailViewMode.URL)

        state.startTab shouldBe WebDetailTab.FORM

        state.select(tab = WebDetailTab.MEMO)
        state.startTab shouldBe WebDetailTab.MEMO

        state.select(tab = WebDetailTab.FORM)
        state.startTab shouldBe WebDetailTab.FORM
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-061 화면이 재생성되어도 메모 탭 선택을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        var state by mutableStateOf<WebDetailScaffoldState?>(null)

        restorationTester.setContent {
            state = rememberWebDetailScaffoldState()
        }

        composeRule.runOnIdle { checkNotNull(state).select(tab = WebDetailTab.MEMO) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { checkNotNull(state).tab shouldBe WebDetailTab.MEMO }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-037 URL 방식으로 시작하고 선택할 때마다 그 방식으로 바뀐다`() {
        val state = WebDetailScaffoldState(initialTab = WebDetailTab.PAGE, initialViewMode = WebDetailViewMode.URL)

        state.viewMode shouldBe WebDetailViewMode.URL

        state.select(viewMode = WebDetailViewMode.RESPONSE)
        state.viewMode shouldBe WebDetailViewMode.RESPONSE

        state.select(viewMode = WebDetailViewMode.URL)
        state.viewMode shouldBe WebDetailViewMode.URL
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-045 화면이 재생성되어도 고른 표시 방식을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        var state by mutableStateOf<WebDetailScaffoldState?>(null)

        restorationTester.setContent {
            state = rememberWebDetailScaffoldState()
        }

        composeRule.runOnIdle {
            checkNotNull(state).viewMode shouldBe WebDetailViewMode.URL
            checkNotNull(state).select(viewMode = WebDetailViewMode.RESPONSE)
        }
        composeRule.runOnIdle { checkNotNull(state).viewMode shouldBe WebDetailViewMode.RESPONSE }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { checkNotNull(state).viewMode shouldBe WebDetailViewMode.RESPONSE }
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-051 백그라운드에 다녀와도 선택한 탭과 고른 표시 방식을 유지한다`() {
        val lifecycleOwner = TestLifecycleOwner(initialState = Lifecycle.State.RESUMED)
        var state by mutableStateOf<WebDetailScaffoldState?>(null)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                state = rememberWebDetailScaffoldState()
            }
        }
        composeRule.runOnIdle {
            checkNotNull(state).select(tab = WebDetailTab.MEMO)
            checkNotNull(state).select(viewMode = WebDetailViewMode.RESPONSE)
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        composeRule.runOnIdle {
            checkNotNull(state).tab shouldBe WebDetailTab.MEMO
            checkNotNull(state).viewMode shouldBe WebDetailViewMode.RESPONSE
        }
    }

    @Test
    fun `TC-WEB-DETAIL-FEATURE-068 화면이 재생성되어도 선택한 웹 정보 수정 탭이나 웹 페이지 탭을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        var state by mutableStateOf<WebDetailScaffoldState?>(null)

        restorationTester.setContent {
            state = rememberWebDetailScaffoldState()
        }

        composeRule.runOnIdle {
            checkNotNull(state).tab shouldBe WebDetailTab.PAGE
            checkNotNull(state).select(tab = WebDetailTab.FORM)
        }
        composeRule.runOnIdle { checkNotNull(state).tab shouldBe WebDetailTab.FORM }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            checkNotNull(state).tab shouldBe WebDetailTab.FORM
            checkNotNull(state).select(tab = WebDetailTab.PAGE)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { checkNotNull(state).tab shouldBe WebDetailTab.PAGE }
    }
}
