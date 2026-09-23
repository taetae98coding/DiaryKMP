package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
    fun `화면이 재생성되어도 선택한 탭을 유지한다`() {
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

        composeRule.runOnIdle { checkNotNull(state).tab shouldBe WebDetailTab.FORM }
    }
}
