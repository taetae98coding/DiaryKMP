package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.feature.memo.ui.TEST_TAG_ADD_REQUEST_KEY
import io.github.taetae98coding.diary.feature.memo.ui.gemini.screenTestGeminiViewModel
import io.github.taetae98coding.diary.feature.memo.ui.place.screenTestPlaceMapViewModel
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_PRIMARY_TAG_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.DEFAULT_TAG_SELECT_LABEL
import io.github.taetae98coding.diary.feature.memo.ui.tag.testTag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoAddScreenBackgroundTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `TC-MEMO-ADD-FEATURE-068 다른 앱에 다녀와도 입력 내용과 선택이 그대로다`() {
        val title = "title-${fixtureMonkey.giveMeOne<String>()}"
        val description = "description-${fixtureMonkey.giveMeOne<String>()}"
        val tagTitle = "BackgroundTag${fixtureMonkey.giveMeOne<Int>()}"
        val tag = testTag(title = tagTitle)
        val viewModels = screenTestRealViewModel(initialPrimaryTagId = tag.id, tagList = listOf(tag))
        composeRule.setContent {
            MemoAddScreenTestTheme {
                BackgroundTestMemoAddScreen(viewModels = viewModels)
            }
        }
        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(title)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextInput(description)
        composeRule.waitForIdle()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assert(hasText(title))
        composeRule.onAllNodes(hasSetTextAction())[1].assert(hasText(description))
        composeRule.onNodeWithText(DEFAULT_TAG_SELECT_LABEL).performScrollTo()
        composeRule.onNodeWithText(tagTitle).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PRIMARY_TAG_DESCRIPTION).assertExists()
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

@Composable
private fun BackgroundTestMemoAddScreen(viewModels: MemoAddScreenViewModels) {
    MemoAddScreen(
        tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
        addViewModel = viewModels.viewModel,
        tagViewModel = viewModels.tagViewModel,
        webViewModel = viewModels.webViewModel,
        contactViewModel = viewModels.contactViewModel,
        placeViewModel = viewModels.placeViewModel,
        placeMapViewModel = screenTestPlaceMapViewModel(),
        geminiViewModel = screenTestGeminiViewModel(),
        navigateUp = {},
        navigateToTagAdd = {},
        navigateToTagDetail = {},
        navigateToWebAdd = {},
        navigateToWebDetail = {},
        navigateToContactAdd = {},
        navigateToContactDetail = {},
        navigateToPlaceAdd = {},
        navigateToPlaceDetail = {},
        initialDateRange = null,
        componentVisibleProvider = { MemoAddScaffoldComponentVisible() },
        isStandalone = true,
    )
}
