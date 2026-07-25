package io.github.taetae98coding.diary.compose.core.theme

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 테마는 목록과 컴포넌트 간격을 제공한다`() {
        lateinit var dimens: DiaryDimens

        composeRule.setContent {
            DiaryTheme {
                dimens = DiaryTheme.dimens
            }
        }

        composeRule.runOnIdle {
            dimens.itemSpacing shouldBe 8.dp
            dimens.componentSpacing shouldBe 12.dp
        }
    }
}
