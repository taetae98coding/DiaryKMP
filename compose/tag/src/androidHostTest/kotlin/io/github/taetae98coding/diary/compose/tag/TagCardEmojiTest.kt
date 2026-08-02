package io.github.taetae98coding.diary.compose.tag

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagCardEmojiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-FEATURE-001 카드에 태그 이모지와 제목을 함께 표시한다`() {
        setTagCard(tag(title = TAG_TITLE, emoji = TAG_EMOJI))

        composeRule.onNodeWithText("$TAG_EMOJI $TAG_TITLE").assertExists()
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-012 이모지가 비어 있으면 카드에 제목만 표시한다`() {
        setTagCard(tag(title = TAG_TITLE))

        composeRule.onNodeWithText(TAG_TITLE).assertExists()
        composeRule.onNodeWithText(" $TAG_TITLE").assertDoesNotExist()
    }

    private fun setTagCard(tag: Tag) {
        composeRule.setContent {
            DiaryTheme {
                TagCard(
                    tag = tag,
                    onClick = {},
                )
            }
        }
    }

    public companion object {
        private const val TAG_TITLE = "TagCardEmojiTitle"
        private const val TAG_EMOJI = "🏃"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(
            title: String,
            emoji: String = "",
        ): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = emoji, title = title))
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
