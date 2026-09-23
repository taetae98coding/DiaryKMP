package io.github.taetae98coding.diary.compose.map.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.DEFAULT_GOOGLE_LABEL
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.DEFAULT_NAVER_LABEL
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.label
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.kotest.matchers.comparables.shouldBeLessThan
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapProviderSelectorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-MAP-FEATURE-001 초기 제공자를 지정하지 않으면 네이버 지도가 선택된 상태로 표시된다`() {
        setDiaryMapProviderSelector()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-002 초기 제공자로 네이버 지도를 지정하면 네이버 지도가 선택된 상태로 표시된다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.NAVER)

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-002 초기 제공자로 Google 지도를 지정하면 Google 지도가 선택된 상태로 표시된다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.GOOGLE)

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-003 네이버 지도가 선택된 상태에서 Google 지도를 선택하면 선택 상태가 옮겨진다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.NAVER)

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-003 Google 지도가 선택된 상태에서 네이버 지도를 선택하면 선택 상태가 옮겨진다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.GOOGLE)

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).performClick()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-004 이미 선택된 네이버 지도를 다시 선택하면 선택 상태가 유지된다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.NAVER)

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).performClick()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-004 이미 선택된 Google 지도를 다시 선택하면 선택 상태가 유지된다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.GOOGLE)

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-DOMAIN-001 화면이 재생성되어도 선택한 제공자가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setDiaryMapProviderSelector(
            restorationTester = restorationTester,
            initialProvider = DiaryMapProvider.NAVER,
        )

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsNotSelected()
    }

    @Test
    fun `TC-DIARY-MAP-DOMAIN-002 표시를 새로 시작하면 초기 제공자로 되돌아간다`() {
        var isVisible by mutableStateOf(true)

        composeRule.setContent {
            DiaryTheme {
                if (isVisible) {
                    DiaryMapProviderSelector(state = rememberDiaryMapState(initialProvider = DiaryMapProvider.NAVER))
                }
            }
        }

        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).performClick()
        composeRule.runOnIdle { isVisible = false }
        composeRule.runOnIdle { isVisible = true }

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertIsSelected()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertIsNotSelected()
    }

    @Test
    fun `네이버 지도 항목을 Google 지도 항목보다 왼쪽에 표시한다`() {
        setDiaryMapProviderSelector()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).getUnclippedBoundsInRoot().left shouldBeLessThan
            composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).getUnclippedBoundsInRoot().left
    }

    @Test
    fun `선택한 제공자를 반복해서 선택해도 선택 상태가 유지된다`() {
        setDiaryMapProviderSelector(initialProvider = DiaryMapProvider.NAVER)

        DiaryMapProvider.entries.forEach { provider ->
            repeat(REPEAT_COUNT) {
                composeRule.onNodeWithText(label(provider)).performClick()
            }

            composeRule.onNodeWithText(label(provider)).assertIsSelected()
        }
    }

    private fun setDiaryMapProviderSelector(
        restorationTester: StateRestorationTester? = null,
        initialProvider: DiaryMapProvider? = null,
    ) {
        val content = @Composable {
            DiaryTheme {
                DiaryMapProviderSelector(
                    state =
                        if (initialProvider == null) {
                            rememberDiaryMapState()
                        } else {
                            rememberDiaryMapState(initialProvider = initialProvider)
                        },
                )
            }
        }

        if (restorationTester == null) {
            composeRule.setContent(content)
        } else {
            restorationTester.setContent(content)
        }
    }

    private companion object {
        const val REPEAT_COUNT = 3
    }
}
