package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SwipeTagCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-HOME-FEATURE-039 미완료 태그 목록의 태그 카드를 좌에서 우로 밀면 그 태그의 완료를 한 번 전달한다`() {
        val tag = fixtureMonkey.tag(title = TAG_TITLE)
        val eventList = mutableListOf<TagListEvent>()
        setSwipeTagCard(tag = tag, onEvent = eventList::add)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagListEvent.SwipeFinish(id = tag.id))
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-021 완료된 태그 목록의 태그 카드를 좌에서 우로 밀면 그 태그의 다시 시작을 한 번 전달한다`() {
        val tag = fixtureMonkey.tag(title = TAG_TITLE)
        val eventList = mutableListOf<TagListEvent>()
        setSwipeTagCard(tag = tag, onEvent = eventList::add, finishAction = SwipeFinishAction.RESTART)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagListEvent.SwipeRestart(id = tag.id))
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-040 미완료 태그 목록의 태그 카드를 우에서 좌로 밀면 그 태그의 삭제를 한 번 전달한다`() {
        assertDeleteSent(finishAction = SwipeFinishAction.FINISH)
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-022 완료된 태그 목록의 태그 카드를 우에서 좌로 밀면 그 태그의 삭제를 한 번 전달한다`() {
        assertDeleteSent(finishAction = SwipeFinishAction.RESTART)
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-043 미완료 태그 목록의 자리 표시 카드는 밀어도 완료와 삭제를 전달하지 않는다`() {
        assertPlaceholderSendsNothing(finishAction = SwipeFinishAction.FINISH)
    }

    @Test
    fun `TC-TAG-FINISHED-LIST-FEATURE-025 완료된 태그 목록의 자리 표시 카드는 밀어도 다시 시작과 삭제를 전달하지 않는다`() {
        assertPlaceholderSendsNothing(finishAction = SwipeFinishAction.RESTART)
    }

    @Test
    fun `TC-TAG-HOME-FEATURE-036 TC-TAG-FINISHED-LIST-FEATURE-011 태그 카드를 누르면 그 태그의 선택을 한 번 전달한다`() {
        val tag = fixtureMonkey.tag(title = TAG_TITLE)
        val eventList = mutableListOf<TagListEvent>()
        setSwipeTagCard(tag = tag, onEvent = eventList::add)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagListEvent.ClickTag(id = tag.id))
    }

    @Test
    fun `기본 환경에서 미완료 태그 목록의 좌에서 우 스와이프는 태그 완료 아이콘을 표시한다`() {
        assertStartIconShownAfterHalfSwipe(finishAction = SwipeFinishAction.FINISH, shownDescription = DEFAULT_FINISH_DESCRIPTION)
    }

    @Test
    fun `기본 환경에서 완료된 태그 목록의 좌에서 우 스와이프는 태그 다시 시작 아이콘을 표시한다`() {
        assertStartIconShownAfterHalfSwipe(finishAction = SwipeFinishAction.RESTART, shownDescription = DEFAULT_RESTART_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 미완료 태그 목록의 좌에서 우 스와이프는 태그 완료 아이콘을 표시한다`() {
        assertStartIconShownAfterHalfSwipe(finishAction = SwipeFinishAction.FINISH, shownDescription = KOREAN_FINISH_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 완료된 태그 목록의 좌에서 우 스와이프는 태그 다시 시작 아이콘을 표시한다`() {
        assertStartIconShownAfterHalfSwipe(finishAction = SwipeFinishAction.RESTART, shownDescription = KOREAN_RESTART_DESCRIPTION)
    }

    @Test
    fun `기본 환경에서 우에서 좌로 절반을 넘기면 태그 삭제 아이콘을 표시한다`() {
        assertIconShownAfterHalfSwipe(direction = -1f, shownDescription = DEFAULT_DELETE_DESCRIPTION)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 우에서 좌로 절반을 넘기면 태그 삭제 아이콘을 표시한다`() {
        assertIconShownAfterHalfSwipe(direction = -1f, shownDescription = KOREAN_DELETE_DESCRIPTION)
    }

    private fun assertDeleteSent(finishAction: SwipeFinishAction) {
        val tag = fixtureMonkey.tag(title = TAG_TITLE)
        val eventList = mutableListOf<TagListEvent>()
        setSwipeTagCard(tag = tag, onEvent = eventList::add, finishAction = finishAction)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagListEvent.SwipeDelete(id = tag.id))
    }

    private fun assertPlaceholderSendsNothing(finishAction: SwipeFinishAction) {
        val eventList = mutableListOf<TagListEvent>()
        setSwipeTagCard(tag = null, onEvent = eventList::add, finishAction = finishAction)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.shouldBeEmpty()
        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).assertExists()
    }

    private fun assertStartIconShownAfterHalfSwipe(
        finishAction: SwipeFinishAction,
        shownDescription: String,
    ) {
        assertIconShownAfterHalfSwipe(direction = 1f, shownDescription = shownDescription, finishAction = finishAction)
    }

    private fun assertIconShownAfterHalfSwipe(
        direction: Float,
        shownDescription: String,
        finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
    ) {
        setSwipeTagCard(tag = fixtureMonkey.tag(title = TAG_TITLE), onEvent = {}, finishAction = finishAction)

        composeRule.onNodeWithTag(TAG_CARD_TEST_TAG).performTouchInput {
            down(center)
            moveBy(delta = Offset(direction * PRE_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
            moveBy(delta = Offset(direction * PAST_HALF_DRAG_OFFSET, 0f), delayMillis = DRAG_DELAY_MILLIS)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(shownDescription).assertExists()
    }

    private fun setSwipeTagCard(
        tag: Tag?,
        onEvent: (TagListEvent) -> Unit,
        finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
    ) {
        composeRule.setContent {
            DiaryTheme {
                SwipeTagCard(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                    tag = tag,
                    finishAction = finishAction,
                )
            }
        }
    }

    private companion object {
        private const val PRE_HALF_DRAG_OFFSET = 40f
        private const val PAST_HALF_DRAG_OFFSET = 160f
        private const val DRAG_DELAY_MILLIS = 300L
        private const val DEFAULT_FINISH_DESCRIPTION = "Finish tag"
        private const val DEFAULT_RESTART_DESCRIPTION = "Restart tag"
        private const val DEFAULT_DELETE_DESCRIPTION = "Delete tag"
        private const val KOREAN_FINISH_DESCRIPTION = "태그 완료"
        private const val KOREAN_RESTART_DESCRIPTION = "태그 다시 시작"
        private const val KOREAN_DELETE_DESCRIPTION = "태그 삭제"
        private const val TAG_TITLE = "SwipeTagCardTitle"
    }
}
