package io.github.taetae98coding.diary.compose.web

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import kotlin.time.Instant
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-HOME-FEATURE-001 웹 카드에 제목과 URL을 표시한다`() {
        val web = web(title = title(), description = description(), url = url())

        setWebCard(web)

        composeRule.onNodeWithText(web.detail.title).assertIsDisplayed()
        composeRule.onNodeWithText(web.detail.url).assertIsDisplayed()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-009 웹 카드는 설명을 표시하지 않는다`() {
        val web = web(title = title(), description = description(), url = url())

        setWebCard(web)

        composeRule.onNodeWithText(web.detail.description).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-017 웹 카드를 누르면 선택이 전달된다`() {
        var clickCount = 0
        val web = web(title = title(), description = description(), url = url())

        setWebCard(web = web, onClick = { clickCount++ })

        composeRule.onNodeWithText(web.detail.title).performClick()

        clickCount shouldBe 1
    }

    @Test
    fun `자리 표시 카드는 제목과 URL을 비워 표시한다`() {
        val web = web(title = title(), description = description(), url = url())

        setWebCard(web = null)

        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).assertExists()
        composeRule.onNodeWithText(web.detail.title).assertDoesNotExist()
        composeRule.onNodeWithText(web.detail.url).assertDoesNotExist()
    }

    @Test
    fun `TC-WEB-HOME-FEATURE-007 TC-SEARCH-HOME-FEATURE-013 TC-TAG-DETAIL-WEB-FEATURE-003 자리 표시 카드는 선택할 수 없다`() {
        var clickCount = 0

        setWebCard(web = null, onClick = { clickCount++ })

        composeRule.onNodeWithTag(WEB_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    private fun setWebCard(
        web: Web?,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                WebCard(
                    onClick = onClick,
                    web = web,
                )
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        // FixtureMonkey는 빈 문자열도 생성하므로 표시 여부 검증에 쓰는 값은 비어 있지 않게 접두사를 붙인다.
        private fun title(): String = "제목-${fixtureMonkey.giveMeOne<String>()}"

        private fun description(): String = "설명-${fixtureMonkey.giveMeOne<String>()}"

        private fun url(): String = "https://example.com/${fixtureMonkey.giveMeOne<Int>()}"

        // FixtureMonkey가 Instant를 생성하지 못하므로 웹 항목은 직접 만든다.
        private fun web(
            title: String,
            description: String,
            url: String,
        ): Web {
            val detail =
                fixtureMonkey
                    .giveMeKotlinBuilder<WebDetail>()
                    .setExp(WebDetail::title, title)
                    .setExp(WebDetail::description, description)
                    .setExp(WebDetail::url, url)
                    .sample()

            return Web(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
        }
    }
}
