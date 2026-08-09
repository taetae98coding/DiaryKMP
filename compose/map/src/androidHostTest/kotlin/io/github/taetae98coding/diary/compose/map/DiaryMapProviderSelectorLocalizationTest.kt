package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.DEFAULT_GOOGLE_LABEL
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.DEFAULT_NAVER_LABEL
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.DEFAULT_PROVIDER_CONTENT_DESCRIPTION
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.KOREAN_GOOGLE_LABEL
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.KOREAN_NAVER_LABEL
import io.github.taetae98coding.diary.compose.map.DiaryMapProviderTestFixture.KOREAN_PROVIDER_CONTENT_DESCRIPTION
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapProviderSelectorLocalizationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `TC-DIARY-MAP-FEATURE-020 겹쳐 표시하는 배치의 한국어 환경 전환 컨트롤 문구를 표시한다`() {
        setDiaryMapProviderSelector()

        composeRule.onNodeWithText(KOREAN_NAVER_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_GOOGLE_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_PROVIDER_CONTENT_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-DIARY-MAP-FEATURE-020 겹쳐 표시하는 배치의 기본 환경 전환 컨트롤 문구를 표시한다`() {
        setDiaryMapProviderSelector()

        composeRule.onNodeWithText(DEFAULT_NAVER_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_GOOGLE_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PROVIDER_CONTENT_DESCRIPTION).assertExists()
    }

    private fun setDiaryMapProviderSelector() {
        composeRule.setContent {
            DiaryTheme {
                DiaryMapProviderSelector(state = rememberDiaryMapState())
            }
        }
    }
}
