package io.github.taetae98coding.diary.compose.web

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipeWebCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-b6eb4a10-bb07-4d9c-9ae2-4955a3aeb9d1 TC-365c5415-2b42-4722-83fe-adb910874e83 웹 카드를 우에서 좌로 밀면 삭제를 한 번 전달한다`() {
        var deleteCount = 0
        var clickCount = 0
        val web = web()
        setCard(web = web, onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 1
        clickCount shouldBe 0
    }

    @Test
    fun `TC-0c7fa192-3184-47ac-bdde-8ade25145c06 TC-f5dc7344-ec22-42a5-b056-828c59b6b225 웹 카드를 좌에서 우로 밀면 아무것도 전달하지 않는다`() {
        var deleteCount = 0
        var clickCount = 0
        val web = web()
        setCard(web = web, onClick = { clickCount += 1 }, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        clickCount shouldBe 0
        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
    }

    @Test
    fun `TC-ac066432-2064-4df7-93d8-0078db735347 TC-dbea7d0f-460e-41ec-b5fb-ed98566a7169 자리 표시 카드는 밀어도 삭제를 전달하지 않는다`() {
        var deleteCount = 0
        setCard(web = null, onDelete = { deleteCount += 1 })

        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        deleteCount shouldBe 0
        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).assertIsDisplayed()
    }

    private fun setCard(
        web: Web?,
        onClick: () -> Unit = {},
        onDelete: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeWebCard(
                    onEvent = { event ->
                        when (event) {
                            is WebListEvent.ClickWeb -> onClick()
                            is WebListEvent.SwipeDelete -> onDelete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    web = web,
                )
            }
        }
    }

    private fun web(): Web {
        val detail =
            fixtureMonkey
                .giveMeKotlinBuilder<WebDetail>()
                .setExp(WebDetail::title, "제목-${fixtureMonkey.giveMeOne<String>()}")
                .setExp(WebDetail::url, "https://example.com/${fixtureMonkey.giveMeOne<Int>()}")
                .sample()

        return fixtureMonkey
            .giveMeKotlinBuilder<Web>()
            .setExp(Web::detail, detail)
            .sample()
    }
}
